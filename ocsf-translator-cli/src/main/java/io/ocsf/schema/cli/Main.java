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

import io.ocsf.parser.parsers.*;
import io.ocsf.parser.Parser;
import io.ocsf.schema.Dictionary;
import io.ocsf.schema.Utils;
import io.ocsf.schema.Schema;
import io.ocsf.schema.cli.CommandLineParser.Argument;
import io.ocsf.translators.Translator;
import io.ocsf.utils.Files;
import io.ocsf.utils.Json;
import io.ocsf.utils.ParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * OCSF event translator command line tool.
 */
public final class Main
{
  private Main() {}

  public static final String Name = "ocsf-translator-cli";

  private static final CommandLineParser clp;

  private static String schemaUrl = SchemaServices.SCHEMA_URL;

  // the path to the schema file
  private static Path schemaFile = null;
  private static boolean schemaEnums = false;
  private static boolean observables = false;
  private static String writeResultDir = null;

  private static boolean verbose = false;

  private static final Parsers parsers = new Parsers();

  private static final class Item
  {
    final File source;
    final Map<String, Object> data;
    final String task;  // used for creating file name.

    private boolean printed;

    Item()
    {
      this.source = null;
      this.data = null;
      this.task = null;
      this.printed = true;
    }

    Item(final File source, final Map<String, Object> data, final String task)
    {
      this.source = source;
      this.data = data;
      this.task = task;
      this.printed = false;
    }

    boolean isPrinted()
    {
      return printed;
    }

    void setPrinted()
    {
      printed = true;
    }
  }

  private static final Item EOS = new Item();

  static
  {
    parsers.register(new CarbonBlackParser());
    parsers.register(new ProofpointEmailParser());
    parsers.register(new Office365Parser());
    parsers.register(new XmlWinEventLogParser());
    parsers.register(new XmlWinEventSecurityLogParser());
    parsers.register(new XmlWinSysmonEventLogParser());
    parsers.register(new WinEventLogParser());
    parsers.register(new WinEventSecurityLogParser());
    parsers.register(new CiscoSyslogParser());
    parsers.register(new InfobloxDHCPParser());
    parsers.register(new BoxParser());
  }

  private static final Consumer<Item> printer = item -> {
    if (!item.isPrinted())
    {
      item.setPrinted();

      final String formatted = Json.format(item.data);
      if (writeResultDir == null)
      {
        System.out.println(formatted);
      }
      else
      {
        final String[] parts = item.source.getName().split("\\.(?=[^\\.]+$)");
        final Path path = Path.of(
          writeResultDir,
          String.format("%s-%s.json", item.task, parts[0]));

        try (final PrintStream writer = new PrintStream(path.toString()))
        {
          writer.println(formatted);
        }
        catch (FileNotFoundException e)
        {
          System.err.printf("Failed to write result for %s: %s",
            path, e.getMessage());
        }
      }
    }
  };

  // Declaring ANSI_RESET so that we can reset the color
  public static final String ANSI_RESET = "\u001B[0m";

  // Declaring the color
  // Custom declaration
  public static final String ANSI_CODE = "\u001B[1m";

  static
  {
    final String helpMsg = "\n" +
      "OCSF command line tool for parsing, translating, and validating events.\n\n" +
      "Where possible options include:\n";

    clp = new CommandLineParser(Name, helpMsg);

    clp.add("Event parsing options:\n");
    clp.add('p', "parser", "parser", "parse one or more events using the 'parser'");
    clp.add('P', "parsers", null, "print the available parsers");

    clp.add("Event translating options:\n");
    clp.add('r', "rule", "rule.json", "specify the rule name");
    clp.add('R', "rules-dir", "path", "specify the rules folder");

    clp.add("");
    clp.add('s', "Schema", "schema.json", "specify the schema file (adds type_uid)");
    clp.add('S', "schema", "schema.json", "specify the schema file (adds type_uid enum text)");
    clp.add('o', "observables", null, "generate the observables");

    clp.add("Event validating options:\n");
    clp.add('v', "validate", null, "validate one or more events");
    clp.add('u', "url", "url",
      "specify the OCSF schema server URL, default: " + SchemaServices.SCHEMA_URL);

    clp.add("Other options:\n");
    clp.add('W', "write", "dir", "write result as {process}-filename.json to directory 'dir'");
    clp.add('V', "verbose", null, "enable verbose output");
    clp.addHelp();

    clp.add("Usage examples:");
    clp.add("   Parse a single event                        " + ANSI_CODE + "schema-cli -p " +
      "WinEventLog 4103.event" + ANSI_RESET);
    clp.add("   Parse multiple events                       " + ANSI_CODE + "schema-cli -p " +
      "WinEventLog 4103-1.event 4103-2.event" + ANSI_RESET);
    clp.add("   Parse all events in a folder                " + ANSI_CODE + "schema-cli -p " +
      "WinEventLog data" + ANSI_RESET + "\n");

    clp.add("   Translate a single event                    " + ANSI_CODE + "schema-cli -R rules " +
      "-r rule-4103-m parsed-4103.json" + ANSI_RESET);
    clp.add("   Translate multiple events                   " + ANSI_CODE + "schema-cli -R rules " +
      "-r rule-4103-m parsed-4103-1.json parsed-4103-2.json" + ANSI_RESET);
    clp.add("   Translate all events in a folder            " + ANSI_CODE + "schema-cli -R rules " +
      "-r rule-4103-m parsed" + ANSI_RESET);
    clp.add("   Translate a single event, add type_uid      " + ANSI_CODE + "schema-cli -R rules " +
      "-r rule-4103-m -s schema.json parsed-4103.json" + ANSI_RESET + "\n");
    clp.add("   Translate a single event, add enum text     " + ANSI_CODE + "schema-cli -R rules " +
      "-r rule-4103-m -S schema.json parsed-4103.json" + ANSI_RESET + "\n");

    clp.add("   Validate a single event                     " + ANSI_CODE + "schema-cli -v " +
      "translated-4103.json" + ANSI_RESET);
    clp.add("   Validate multiple events                    " + ANSI_CODE + "schema-cli -v " +
      "translated-4103-1.json translated-4103-2.json" + ANSI_RESET);
    clp.add("   Validate all events in a folder             " + ANSI_CODE + "schema-cli -v " +
      "translated" + ANSI_RESET + "\n");

    clp.add("   Parse and translate a single event          " + ANSI_CODE + "schema-cli -p " +
      "WinEventLog -R rules -r rule-4103-m 4103.event" + ANSI_RESET);
    clp.add("   Parse, translate, and validate an event     " + ANSI_CODE + "schema-cli -p " +
      "WinEventLog -R rules -r rule-4103-m -v 4103.event" + ANSI_RESET);
    clp.add("   Parse, translate, add enums, and validate   " + ANSI_CODE + "schema-cli -p " +
      "WinEventLog -R rules -r rule-4103-m -S schema.json -v 4103.event" + ANSI_RESET + "\n");
  }

  public static void main(final String... args)
  {
    clp.parseCommandLine(args);

    verbose = clp.getArg('V').isSet();
    writeResultDir = clp.getArg('W').value();

    printHelp();

    initSchema();

    final List<String> files = clp.extraArgs();

    if (files.isEmpty())
    {
      System.err.println("No files on the command line. Are you missing something?");
      clp.help();
      System.exit(1);
    }

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

      System.exit(0);
    }

    if (clp.getArg('P').isSet())
    {
      printParsers();
      System.exit(0);
    }
  }

  private static void printParsers()
  {
    System.out.println("Available Parsers:");
    parsers.values().forEach(parser -> System.out.printf("\t%s%n", parser));
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
        schemaFile = Paths.get(arg.value());
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
      final Parser p = parsers.parser(arg.value());

      if (p != null)
        return Optional.of(p);

      System.err.println("Invalid parser name: " + arg.value());
      printParsers();
      System.exit(2);
    }

    return Optional.empty();
  }

  private static Optional<Translator.I> translator()
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

  private static void parse(final Parser parser, final List<String> files,
    final Consumer<Item> consumer)
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
            final Item parseItem = new Item(file, data, "parse");
            if (writeResultDir != null) printer.accept(parseItem);
            consumer.accept(parseItem);
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

    consumer.accept(EOS);
  }

  private static void translate(
    final Translator.I translator, final List<String> files,
    final Consumer<Item> consumer)
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
          final Item                item = new Item(file, data, "source");
          if (!translate(translator, item, consumer))
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

    consumer.accept(EOS);
  }

  private static void validateFiles(final SchemaServices validator, final List<String> files)
  {
    final List<Item> data = new ArrayList<>();

    for (final String schemaFile : files)
    {
      visitAllDirsAndFiles(new File(schemaFile), file ->
      {
        if (verbose)
          System.out.println("// validate file: " + file);

        try
        {
          data.add(new Item(file, Files.readJson(file.toPath()), "validate"));
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
    final Translator.I translator, final Item source, final Consumer<Item> consumer)
  {
    if (source.data == null)
    {
      return false;
    }

    final Map<String, Object> translated = translator.apply(source.data);

    if (source.data != translated)
    {
      if (!source.data.isEmpty())
      {
        translated.put(Dictionary.UNMAPPED, source.data);
      }

      final Item transItem = new Item(source.source, translated, "translate");
      if (writeResultDir != null) printer.accept(transItem);
      consumer.accept(transItem);
      return true;
    }

    return false;
  }

  private static Translator.I translator(final String home, final String rule)
  {
    try
    {
      final String ruleFile = resolveRuleFile(home, rule);

      if (verbose)
      {
        System.out.println("// rules home: " + home);
        System.out.println("// rule  file: " + ruleFile);
      }

      final Translator.I translator = Translator.fromFile(Paths.get(home),
        Paths.get(ruleFile));

      if (schemaFile != null)
      {
        final Schema schema = new Schema(schemaFile, schemaEnums, observables);

        return data -> Utils.addUuid(schema.enrich(translator.apply(data)));
      }

      return data -> Utils.addUuid(translator.apply(data));
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

  private static void validate(final SchemaServices validator, final Item data)
  {
    final List<Item> dataList = new ArrayList<>();
    dataList.add(data);
    validate(validator, dataList);
  }

  private static void validate(final SchemaServices validator, final List<Item> list)
  {
    try
    {
      for (final Item item : list)
      {
        final Map<String, Object> data = validator.validate(Json.toString(item.data));
        Main.printer.accept(new Item(item.source, data, "validate"));
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
  }

  /**
   * Attempts to resolve ruleName on rulePath using three techniques:
   * <pre>
   *  1. ruleName is relative to rulePath: rulePath / ruleName, If exists, stop
   *  2. Remove as prefix "rulePath" from ruleName, go to step 1
   *  3. Scan for rule file using glob pattern (expressed as a find cmd):
   *      find $rulePath -name "${ruleName}.json" -o "${ruleName}-*.json" -o "*-${ruleName}.json"
   * </pre>
   *
   * @param rulePath - base directory of rules
   * @param ruleName - full path to rule, relative path of rule, or base name of rule
   * @return - the relative path of rule
   */
  private static String resolveRuleFile(final String rulePath, final String ruleName) throws NotDirectoryException, FileNotFoundException
  {
    final File baseDir = new File(rulePath);
    if (!baseDir.isDirectory())
    {
      throw new NotDirectoryException(String.format("rule-dir '%s' is not a directory", rulePath));
    }

    // 1. look for
    File ruleFile = new File(baseDir, ruleName);
    if (ruleFile.isFile())
    {
      return ruleName; // ruleName was already relative to baseDir.
    }

    // 2. See if ruleFile can be removed from ruleName.  In bash: ${ruleName#$ruleFile}
    if (ruleName.startsWith(rulePath))
    {
      final String relativeRuleName = ruleName.substring(rulePath.length());
      ruleFile = new File(baseDir, relativeRuleName);
      if (ruleFile.isFile())
      {
        return relativeRuleName;
      }
    }

    // 3. the hard part.  Scan for rule name - assuming it's a file basename identifier.
    final List<String> matches = new ArrayList<>();
    final String matchRe = String.format("^(.*-%s|%s|%s-.*).json$", ruleName, ruleName,
      ruleName);

    visitAllDirsAndFiles(baseDir, file ->
    {
      final String baseName = file.getName();
      if (baseName.matches(matchRe))
      {
        matches.add(file.getPath());
      }
    });

    if (matches.size() == 0)
    {
      // came up short - didn't find anything.
      throw new FileNotFoundException(String.format("Cannot find a rule with rule-dir %s and " +
        "ruleName %s", rulePath, ruleName));
    }

    if (matches.size() > 1)
    {
      System.err.printf("Found %d possible rules for '%s':%n", matches.size(), ruleName);
      matches.forEach(file -> System.err.printf("\t%s%n", file));
    }

    // found it - return the path relative to basedir.
    final String name = matches.get(0).substring(baseDir.getPath().length());
    return name.charAt(0) == '/' ? name.substring(1) : name;
  }

  private static void runPerformanceTest(final String home, final String[] args)
  {
    System.out.println("Using rules home : " + home);

    try
    {
      final Schema schema = new Schema(schemaFile, schemaEnums, observables);

      final LoadRunner runner = new LoadRunner(home, schema);
      for (final String file : args)
      {
        if (file.endsWith(".json"))
        {
          System.out.println("Using data file  : " + file);
          runner.run(file);
        }
      }

      runner.printResults();
    }
    catch (final IOException ex)
    {
      System.err.printf("Performance test failed: %s%n", ex);
    }
  }
}
