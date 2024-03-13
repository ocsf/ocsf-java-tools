package io.ocsf.schema.cli;

import io.ocsf.parsers.Parsers;
import io.ocsf.parsers.WindowsXmlParser;
import io.ocsf.translator.Translator;
import io.ocsf.translator.TranslatorBuilder;
import io.ocsf.utils.FuzzyHashMap;
import io.ocsf.utils.Json;
import io.ocsf.utils.parsers.Parser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Example
{
  private static final Logger logger = LogManager.getLogger(Example.class);
  private static final Path RULE_FILE_NAME = Path.of("rule.json");
  private static final FuzzyHashMap<Parser> parsers = Parsers.parsers();

  public static void main(final String[] args)
  {
    final Path rulesHome = Path.of("src/main/dist/examples");
    final Path sourceTypeRules = Path.of("src/main/dist/examples/microsoft/windows/xml");
    final Path eventFile = Path.of("src/main/dist/examples/microsoft/windows/xml/4103/raw.xml");
    final Parser parser = parsers.get(WindowsXmlParser.SourceType);

    try
    {
      final TranslatorGroup translatorGroup = loadTranslators(rulesHome, sourceTypeRules);

      final String event = Files.readString(eventFile, StandardCharsets.UTF_8);
      logger.info("Raw event:\n{}", event);

      final Map<String, Object> parsedEvent = parse(parser, event);
      final Map<String, Object> translatedEvent = translatorGroup.translate(parsedEvent);

      logger.info("Translated event:\n{}", Json.format(translatedEvent));
    }
    catch (final IOException e)
    {
      logger.error("Failed to load raw event: {}", eventFile, e);
    }
    catch (final RuntimeException e)
    {
      logger.error("Failed to translate", e);
    }
  }

  private static TranslatorGroup loadTranslators(final Path home, final Path sourceTypeRules)
  {
    logger.info("Loading rules: {}", sourceTypeRules);
    try
    {
      final List<Translator> translators = new ArrayList<>();
      loadTranslators(home, sourceTypeRules, translators);

      Translator defaultTranslator = null;
      final List<Translator> conditionalTranslators = new ArrayList<>();
      for (final Translator translator : translators)
      {
        if (translator.isDefault())
        {
          if (defaultTranslator != null)
          {
            logger.warn("Duplicate default translator found: {}", sourceTypeRules);
          }
          defaultTranslator = translator;
          continue;
        }
        conditionalTranslators.add(translator);
      }
      return new TranslatorGroup(defaultTranslator, conditionalTranslators);
    }
    catch (final IOException e)
    {
      throw new RuntimeException("Failed to load translation rules", e);
    }
  }

  private static void loadTranslators(
      final Path home,
      final Path path,
      final List<Translator> translators
  ) throws IOException
  {
    try (final DirectoryStream<Path> stream = Files.newDirectoryStream(path))
    {
      for (final Path p : stream)
      {
        if (Files.isDirectory(p))
        {
          loadTranslators(home, p, translators);
        }
        else if (Files.isRegularFile(p) && p.getFileName().equals(RULE_FILE_NAME))
        {
          logger.info("Adding rule: {}", p);
          translators.add(TranslatorBuilder.fromFile(home, p));
        }
      }
    }
  }

  private static Map<String, Object> parse(final Parser parser, final String event)
  {
    try
    {
      return parser.parse(event);
    }
    catch (Exception e)
    {
      throw new RuntimeException("Failed to parse raw event", e);
    }
  }
}
