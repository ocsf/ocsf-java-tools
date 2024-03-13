/*
 * Copyright 2023 Splunk Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ocsf.schema.cli;

import io.ocsf.parsers.*;
import io.ocsf.schema.Dictionary;
import io.ocsf.schema.Schema;
import io.ocsf.schema.Utils;
import io.ocsf.schema.cli.CommandLineParser.Argument;
import io.ocsf.translator.Translator;
import io.ocsf.translator.TranslatorBuilder;
import io.ocsf.utils.Files;
import io.ocsf.utils.FuzzyHashMap;
import io.ocsf.utils.Json;
import io.ocsf.utils.parsers.Parser;
import io.ocsf.utils.parsers.ParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * OCSF event translator command line tool.
 */
public final class Main
{
  private static final Logger logger = LogManager.getLogger(Main.class);

  private Main() {}

  public static final String Name = "ocsf-cli";

  private static final CommandLineParser clp;

  private static String schemaUrl = SchemaServices.SCHEMA_URL;

  // the path to the schema file
  private static Path    schemaFile  = null;
  private static boolean schemaEnums = false;
  private static boolean observables = false;

  private static boolean verbose = false;

  private static final int OK = 0;

  private static final FuzzyHashMap<Parser> parsers = Parsers.parsers();

  private static final Consumer<Map<String, Object>> printer =
    data -> System.out.println(Json.format(data));

  private static final String ExampleParser = "-p " + WindowsMultilineParser.SourceType + " ";
  private static final String ExampleRule   = "-R rules -r rule-4103-m ";

  static
  {
    final String helpMsg =
      "\n" +
      "OCSF command line tool for parsing, translating, and validating events.\n\n" +
      "Where possible options are:\n";

    clp = new CommandLineParser(Name, helpMsg);

    clp.add("Parsing options:\n");
    clp.add('p', "parser", "parser", "parse one or more events using the 'parser'");

    clp.add("Translation options:\n");
    clp.add('R', "rules-dir", "path", "specify the rules folder");
    clp.add('r', "rule", "rule.json", "specify the rule name");

    clp.add("");
    clp.add('s', "Schema", "schema.json", "specify the schema file (adds type_uid)");
    clp.add('S', "schema", "schema.json", "specify the schema file (adds type_uid enum text)");
    clp.add('o', "observables", null, "generate the observables (requires schema file)");

    clp.add("Validation options:\n");
    clp.add('v', "validate", null, "validate one or more events");
    clp.add('u', "url", "url",
            "specify the OCSF schema server URL, default: " + SchemaServices.SCHEMA_URL);

    clp.add("Other options:\n");
    clp.add('P', "parsers", null, "print the available parsers");
    clp.add('V', "verbose", null, "enable verbose output");
    clp.addHelp();

    clp.add("Usage examples:");
    clp.add(
      formatExample(
        "Parse a single event",
        ExampleParser
        + "4103.event"
      ));
    clp.add(
      formatExample(
        "Parse multiple events",
        ExampleParser
        + "4103-1.event 4103-2.event"
      ));
    clp.add(
      formatExample(
        "Parse all events in the data folder",
        ExampleParser
        + "data\n"
      ));

    clp.add(
      formatExample(
        "Translate a single event",
        ExampleRule + "parsed-4103.json"
      ));
    clp.add(
      formatExample(
        "Translate multiple events",
        ExampleRule + "parsed-4103-1.json parsed-4103-2.json"
      ));
    clp.add(
      formatExample(
        "Translate all events in the data folder",
        ExampleRule + "data\n"));

    clp.add(
      formatExample(
        "Translate and enrich a single event",
        ExampleRule + "-s schema.json parsed-4103.json"
      ));
    clp.add(
      formatExample(
        "Translate and enrich a single event",
        ExampleRule + "-S schema.json parsed-4103.json\n"
      ));

    clp.add(
      formatExample(
        "Validate a single event",
        "-v translated-4103.json"
      ));
    clp.add(
      formatExample(
        "Validate multiple events",
        "-v translated-4103-1.json translated-4103-2.json"
      ));
    clp.add(
      formatExample(
        "Validate all events in the data folder",
        "-v data\n"
      ));

    clp.add(
      formatExample(
        "Parse and translate an event",
        ExampleParser
        + ExampleRule + "4103.event"
      ));
    clp.add(
      formatExample(
        "Parse, translate, and validate an event",
        ExampleParser
        + ExampleRule + "-v 4103.event"
      ));
  }

  public static void main(final String... args)
  {
    if (logger.isInfoEnabled())
      logger.info("Started {} with {}", Name, args);

    if (args.length == 0)
    {
      clp.help();
      System.exit(OK);
    }

    clp.parseCommandLine(args);

    verbose = clp.getArg('V').isSet();

    printHelp();

    final List<String> files = clp.extraArgs();
    if (files.isEmpty())
    {
      System.err.println("No files on the command line. Are you missing something?");
      clp.help();
      System.exit(1);
    }

    initSchema();

    parser()
      .ifPresentOrElse(
        p -> parse(p, files, parsed ->
          translator()
            .ifPresentOrElse(
              t -> translate(t, parsed, translated ->
                validator()
                  .ifPresentOrElse(
                    v -> validate(v, translated),
                    () -> printer.accept(translated))),
              () -> printer.accept(parsed))),
        () ->
          translator()
            .ifPresentOrElse(
              t -> translate(t, files, translated ->
                validator()
                  .ifPresentOrElse(
                    v -> validate(v, translated),
                    () -> printer.accept(translated))),
              () ->
                validator()
                  .ifPresentOrElse(
                    v -> validateFiles(v, files),
                    clp::help)));
  }

  private static void printHelp()
  {
    if (clp.getArg('h').isSet())
    {
      clp.help();
      if (verbose)
        printParsers();

      System.exit(OK);
    }

    if (clp.getArg('P').isSet())
    {
      printParsers();
      System.exit(OK);
    }
  }

  private static void printParsers()
  {
    System.out.println("Parser list:");
    parsers
      .values()
      .stream()
      .sorted(Comparator.comparing(Object::toString))
      .forEach(parser -> System.out.printf("\t%s%n", parser));

    System.out.println();
  }

  private static void initSchema()
  {
    Argument arg = clp.getArg('s');
    if (arg.isSet())
    {
      schemaFile = Paths.get(arg.value());
    }
    else
    {
      arg = clp.getArg('S');
      if (arg.isSet())
      {
        schemaFile  = Paths.get(arg.value());
        schemaEnums = true;
      }
    }

    arg = clp.getArg('u');
    if (arg.isSet())
    {
      schemaUrl = validateSchemaUrl(arg.value());
    }

    observables = schemaFile != null && clp.getArg('o').isSet();
  }

  private static Optional<Parser> parser()
  {
    final Argument arg = clp.getArg('p');

    if (arg.isSet())
    {
      final Parser p = parsers.get(arg.value());

      if (p != null)
        return Optional.of(p);

      System.err.println("Invalid parser name: " + arg.value());
      printParsers();
      System.exit(2);
    }

    return Optional.empty();
  }

  private static Optional<Translator> translator()
  {
    final Argument ruleDir  = clp.getArg('R');
    final Argument ruleFile = clp.getArg('r');

    return ruleDir.isSet() && ruleFile.isSet() ?
           Optional.ofNullable(translator(ruleDir.value(), ruleFile.value())) :
           Optional.empty();
  }

  private static Optional<SchemaServices> validator()
  {
    return clp.getArg('v').isSet() ?
           Optional.of(new SchemaServices(schemaUrl)) : Optional.empty();
  }

  private static void parse(
    final Parser parser, final List<String> files,
    final Consumer<Map<String, Object>> consumer)
  {
    for (final String arg : files)
    {
      visitAllDirsAndFiles(new File(arg), file ->
      {
        if (verbose)
          System.out.println("// parse file: " + file);

        try
        {
          final Map<String, Object> data = parser.parse(Files.readFile(file));
          if (data != null)
          {
            consumer.accept(data);
          }
          else
          {
            System.err.println("Unable to parse file: " + file);
          }
        }
        catch (final Exception e)
        {
          if (e instanceof IOException)
          {
            System.err.printf("Fatal: Unable to save parse result: %s%n", e.getMessage());
            System.exit(4);
          }
          System.err.printf("Unable to parse file: %s. %s%n", file, e.getMessage());
          e.printStackTrace(System.err);
        }
      });
    }
  }

  private static void translate(
    final Translator translator, final List<String> files,
    final Consumer<Map<String, Object>> consumer)
  {
    for (final String arg : files)
    {
      visitAllDirsAndFiles(new File(arg), file ->
      {
        if (verbose)
          System.out.println("// translate file: " + file);

        try
        {
          final Map<String, Object> data = readJson(file);
          if (!translate(translator, data, consumer))
          {
            System.err.printf("Unable to translate file: %s%n", file.getPath());
          }
        }
        catch (final Exception e)
        {
          System.err.printf("Unable to parse file: %s. %s%n", file, e.getMessage());
          e.printStackTrace(System.err);
        }
      });
    }
  }

  private static void validateFiles(final SchemaServices validator, final List<String> files)
  {
    final List<Map<String, Object>> data = new ArrayList<>();

    for (final String schemaFile : files)
    {
      visitAllDirsAndFiles(new File(schemaFile), file ->
      {
        if (verbose)
          System.out.println("// validate file: " + file);

        try
        {
          data.add(Files.readJson(file.toPath()));
        }
        catch (final IOException e)
        {
          System.err.printf("Validate: unable to read file %s: %s%n", file.getName(),
                            e.getMessage());
        }
      });
    }

    // if possible, send a batch of events
    switch (data.size())
    {
      case 0:
        System.err.println("No data files found");
        break;

      case 1:
        validate(validator, data.get(0));
        break;

      default:
        validate(validator, data);
        break;
    }
  }

  private static boolean translate(
    final Translator translator, final Map<String, Object> data,
    final Consumer<Map<String, Object>> consumer)
  {
    if (data == null)
    {
      return false;
    }

    final Map<String, Object> translated = translator.apply(data);

    if (data != translated)
    {
      if (!data.isEmpty())
      {
        translated.put(Dictionary.UNMAPPED, data);
      }

      consumer.accept(translated);
      return true;
    }

    return false;
  }

  private static Translator translator(final String home, final String rule)
  {
    try
    {
      final String file = resolveFile(home, rule);

      if (verbose)
      {
        System.out.println("// rules home: " + home);
        System.out.println("// rule  file: " + file);
      }

      final Translator translator = TranslatorBuilder.fromFile(
        Paths.get(home),
        Paths.get(file));

      if (schemaFile != null)
      {
        final Schema schema = new Schema(schemaFile, schemaEnums, observables);

        return data -> Utils.addTypeUid(Utils.addUuid(schema.enrich(translator.apply(data))));
      }

      return data -> Utils.addTypeUid(Utils.addUuid(translator.apply(data)));
    }
    catch (final ParserException e)
    {
      System.err.println();
      System.err.printf("Unable to parse the file: %s: %s%n", rule, e.getMessage());
    }
    catch (final IOException e)
    {
      System.err.println();
      System.err.printf("Unable to open the file: %s%n", e);
    }

    System.exit(3);
    return null;
  }

  private static String validateSchemaUrl(final String value)
  {
    try
    {
      new URL(value);
    }
    catch (final MalformedURLException e)
    {
      System.err.printf("Invalid OCSF schema server URL: %s. Error: %s%n", value, e.getMessage());
      System.exit(2);
    }

    return value;
  }

  private static void validate(final SchemaServices validator, final Map<String, Object> data)
  {
    final List<Map<String, Object>> dataList = new ArrayList<>();
    dataList.add(data);
    validate(validator, dataList);
  }

  private static void validate(final SchemaServices validator, final List<Map<String, Object>> list)
  {
    try
    {
      for (final Map<String, Object> item : list)
      {
        final Map<String, Object> data = validator.validate(Json.toString(item));
        Main.printer.accept(data);
      }
    }
    catch (final InterruptedException e)
    {
      System.err.printf("Validate: schema service has been interrupted: %s%n", e.getMessage());
      System.exit(4);
    }
    catch (final IOException e)
    {
      System.err.printf("Validate: unable to connect to the schema server: %s%n", e.getMessage());
      System.exit(4);
    }
  }

  private static Map<String, Object> readJson(final File filename)
  {
    try
    {
      return Files.readJson(filename.getPath());
    }
    catch (final ParserException e)
    {
      System.err.println();
      System.err.printf("Invalid data file: %s. Error: %s%n", filename, e);
    }
    catch (final IOException e)
    {
      System.err.println();
      System.err.printf("Unable to open file: %s. Error: %s%n", filename, e);
    }

    System.exit(2);
    return null;
  }

  private static void visitAllDirsAndFiles(final File file, final Consumer<File> action)
  {

    if (file.isFile())
    {
      action.accept(file);
    }
    else if (file.isDirectory())
    {
      final String[] children = file.list();

      if (children != null)
      {
        for (final String child : children)
          visitAllDirsAndFiles(new File(file, child), action);
      }
    }
    else
    {
      System.err.printf("Invalid path: %s%n", file.getPath());
    }
  }

  /**
   * Attempts to resolve the given name as a filename on the specified path.
   *
   * @param path - the path to the rules' directory
   * @param name - the full path to rule, relative path of rule, or base name of rule
   * @return - the relative path of rule
   */
  static String resolveFile(final String path, final String name)
    throws NotDirectoryException, FileNotFoundException
  {
    final File baseDir = new File(path);
    if (!baseDir.isDirectory())
    {
      throw new NotDirectoryException(String.format("rule-dir '%s' is not a directory", path));
    }

    // look for a file with an exact name
    File file = new File(name);
    if (file.isFile())
    {
      return name;
    }

    // look for a file relative to the specified path
    file = new File(baseDir, name);
    if (file.isFile())
    {
      return file.getPath();
    }

    // scan for a file path that matches the given name
    final List<String> matches = new ArrayList<>();
    visitAllDirsAndFiles(baseDir, f ->
    {
      final String p = f.getPath();
      if (p.contains(name) && p.endsWith(Files.JSON_FILE_EXT))
      {
        try
        {
          final Map<String, Object> data = Files.readJson(p);

          if (data.containsKey(TranslatorBuilder.RuleList))
            matches.add(p);
        }
        catch (final Exception e)
        {
          // ignore the file
        }
      }
    });

    if (matches.isEmpty())
    {
      // didn't find anything
      throw new FileNotFoundException(
        String.format("Cannot find a rule with rule-dir %s and ruleName %s", path, name));
    }

    if (matches.size() > 1)
    {
      System.err.printf("Found %d possible rules for '%s':%n", matches.size(), name);
      matches.forEach(f -> System.err.printf("\t%s%n", f));
    }

    // found it - return the file path
    return matches.get(0);
  }

  private static String formatExample(final String text, final String str)
  {
    final int n = 44 - text.length();

    return String.format("   %s%" + n + "s\u001B[1m%s %s\u001B[0m", text, ' ', Name, str);
  }

}

