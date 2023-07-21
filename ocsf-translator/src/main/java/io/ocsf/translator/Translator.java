package io.ocsf.translator;

import java.util.Map;

/**
 * Translator interface.
 */
@FunctionalInterface
public interface Translator
{
  /**
   * Applies the translator's rules to the given data.
   *
   * @param data the event data
   * @return the transformed event data
   */
  Map<String, Object> apply(final Map<String, Object> data);

  default Map<String, Object> apply(
    final Map<String, Object> data,
    final Map<String, Object> translated)
  {
    return translated;
  }

  default boolean isDefault() {return false;}
}
