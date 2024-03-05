package io.ocsf.schema.cli;

import io.ocsf.translator.Translator;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TranslatorGroup
{
  private final Translator defaultTranslator;
  private final List<Translator> conditionalTranslators;

  public TranslatorGroup(
      final Translator defaultTranslator,
      final List<Translator> conditionalTranslators
  )
  {
    this.defaultTranslator = defaultTranslator;
    this.conditionalTranslators
        = Objects.requireNonNull(conditionalTranslators, "conditionalTranslators");
  }

  public Map<String, Object> translate(final Map<String, Object> parsedEvent)
  {
    for (final Translator translator : conditionalTranslators)
    {
      final Map<String, Object> translated = translator.apply(parsedEvent);
      if (translated != null)
      {
        return translated;
      }
    }

    if (defaultTranslator != null)
    {
      return defaultTranslator.apply(parsedEvent);
    }
    return null;
  }
}
