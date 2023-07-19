/*
 * Copyright (c) 2023 Splunk Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.ocsf.translator.svc;

import io.ocsf.schema.Dictionary;
import io.ocsf.schema.Utils;
import io.ocsf.translator.Translator;
import io.ocsf.utils.Files;
import io.ocsf.utils.Maps;
import io.ocsf.utils.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This class manages a group of related event translators.
 * <p>
 * NOTE: This class is intended for use in a single thread.
 */
public final class Translators
{
  private static final Logger logger = LoggerFactory.getLogger(Translators.class);

  private final Path home;

  private final Map<String, Translator.I> translators = new LinkedHashMap<>();

  // This translator is used by the 'translate(data)' function when none of the named translators
  // translates the given data. It is set to the translator that does not have a 'when' clause.
  private Translator.I translator;

  /**
   * Creates a new collections of translators.
   *
   * @param path the home folder of the translator's rules
   */
  public Translators(final String path)
  {
    this(Paths.get(path));
  }

  public Translators(final Path path)
  {
    Objects.requireNonNull(path, "path is a required parameter");

    this.home = path;
  }

  public int size()
  {
    return translators.size();
  }

  /**
   * Adds a new translator from a JSON file.
   *
   * @param name the name of the translator
   * @param path the path to a file with JSON encoded translation rules
   * @throws ParserException invalid json file
   * @throws IOException     unable to read the file
   */
  public void addFile(final String name, final Path path) throws IOException
  {
    add(name, path, Files::readJson);
  }

  /**
   * Adds a new translator from Java resources.
   *
   * @param name the name of the translator
   * @param path the path to a file with JSON encoded translation rules
   * @throws ParserException invalid json file
   * @throws IOException     unable to read the file
   */
  public void addResource(final String name, final Path path) throws IOException
  {
    add(name, path, Files::readJsonFromResource);
  }

  /**
   * Adds a new translator.
   *
   * @param name   the name of the translator
   * @param path   the path to the JSON encoded translation rules
   * @param reader the JSON reader
   * @throws ParserException invalid json
   * @throws IOException     unable to read the resource
   */
  public void add(
    final String name, final Path path, final Translator.JsonReader reader) throws IOException
  {
    put(name, Translator.build(home, reader, Maps.typecast(reader.read(validate(path)))));
  }

  public void addRule(
    final String name, final Translator.JsonReader reader, final Map<String, Object> rule)
    throws IOException
  {
    put(name, Translator.build(home, reader, rule));
  }

  public void put(final String name, final Translator.I translator)
  {
    if (translator.isDefault())
    {
      if (translators.put(name, translator) != null)
        logger.warn("Translator {} has been overwritten", name);
    }
    else
    {
      if (this.translator != null)
        logger.warn("The default translator {} has been overwritten", name);

      this.translator = translator;
    }
  }

  /**
   * Resolves and validates that the given path is a sub-folder in the home path.
   *
   * @param home the home path
   * @param path the path to be validated
   * @return the resolved path
   * @throws IOException if the path is outside the home path
   */
  public static Path validate(final Path home, final Path path) throws IOException
  {
    final Path p = home.resolve(path).normalize();
    if (!p.startsWith(home))
      throw new IOException("Using a path outside the rules home is not allowed: " + p);

    return p;
  }

  /**
   * Translates a single event using the given translator.
   * <p>
   * The event data will be translated if the rules' <code>when</code> condition evaluates as
   * <code>true</code>.
   *
   * @param name the field to translate to data.
   * @param data the data to be translated
   * @return the translated data or null if the event was not translated
   */
  public Map<String, Object> translate(final String name, final Map<String, Object> data)
  {
    final Translator.I t = translators.get(name);

    return t != null ? translate(t, data) : null;
  }

  /**
   * Translates a single event by trying all translators in the order they were added.
   * <p>
   * The event data will be translated by the first translator which evaluates the rules'
   * <code>when</code> condition as
   * <code>true</code>.
   *
   * @param data the data to be translated
   * @return the translated data or null if the event was not translated
   */
  public Map<String, Object> translate(final Map<String, Object> data)
  {
    if (data != null)
    {
      for (final Translator.I t : translators.values())
      {
        final Map<String, Object> translated = translate(t, data);
        if (translated != null)
        {
          return translated;
        }
      }

      // use the default translator
      if (translator != null)
      {
        return translate(translator, data);
      }
    }

    return null;
  }

  @Override
  public String toString()
  {
    return home.getFileName().toString();
  }

  private static Map<String, Object> translate(
    final Translator.I translator, final Map<String, Object> data)
  {
    final Map<String, Object> translated = translator.apply(data);
    if (data == translated)
    {
      return null;
    }

    if (!data.isEmpty())
    {
      translated.put(Dictionary.UNMAPPED, data);
    }

    return Utils.addUuid(translated);
  }

  private Path validate(final Path path) throws IOException
  {
    final Path p = home.resolve(path).normalize();
    if (!p.startsWith(home))
      throw new IOException("Using a path outside the rules home is not allowed: " + p);

    return validate(home, path);
  }
}
