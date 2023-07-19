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

package io.ocsf.translator.svc.config;

import io.ocsf.translator.svc.concurrent.MutableProcessorList;
import io.ocsf.translator.svc.concurrent.ProcessorList;
import io.ocsf.translator.svc.Translators;
import io.ocsf.utils.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * TransformersConfig loads all translation rules in the given <code>home</code> folder.
 */
public final class ConfigTranslators
{
  private static final Logger logger = LoggerFactory.getLogger(ConfigTranslators.class);

  private static final String SOURCE_TYPE_FILE = ".metadata";

  private ConfigTranslators() {}

  /**
   * Scans the given <code>path</code>, compiles, and loads the translation rules found in the path.
   *
   * @param path the path to the rules folder
   * @return a list of translators
   * @throws IOException if the given <code>path</code> is invalid
   */
  public static ProcessorList<Translators> load(final String path) throws IOException
  {
    return load(Paths.get(path));
  }

  /**
   * Scans the given <code>path</code>, compiles, and loads the translation rules found in the path.
   *
   * @param path the path to the rules folder
   * @return a list of translators
   * @throws IOException if the given <code>path</code> is invalid
   */
  public static ProcessorList<Translators> load(final Path path) throws IOException
  {
    final Path home = validate(path);

    logger.info("Init rules: {}", home);

    final MutableProcessorList<Translators> acc = new MutableProcessorList<>(getPathName(home));
    loadTransformers(home, home, null, acc);

    return acc;
  }

  private static Path validate(final Path path) throws IOException
  {
    final Path p = path.normalize().toAbsolutePath();

    if (Files.notExists(p))
    {
      throw new IOException("Path " + path + " not found");
    }

    return p;
  }

  private static void loadTransformers(
      final Path home, final Path path, final String type,
      final MutableProcessorList<Translators> acc) throws IOException
  {
    try (final DirectoryStream<Path> stream = Files.newDirectoryStream(path))
    {
      for (final Path p : stream)
      {
        if (Files.isDirectory(p))
        {
          logger.info("Load rules: {}", p);
          loadTransformers(home, p, readSourceType(p, type), acc);
        }
        else if (type != null && Files.isRegularFile(p) && isJsonFile(p))
        {
          logger.info("{}: add rule: {}", type, p.getFileName());
          addTranslationRule(home, p, type, acc);
        }
      }
    }
  }

  private static void addTranslationRule(
      final Path home, final Path path, final String type,
      final MutableProcessorList<Translators> acc) throws IOException
  {
    Translators translators = acc.get(type);

    if (translators == null)
    {
      translators = new Translators(home);
      acc.register(type, translators);
    }

    final Path   rulePath = home.resolveSibling(path);
    final Object rule     = io.ocsf.utils.Files.readJson(rulePath);

    // the rule file contains a single JSON object
    if (rule instanceof Map<?, ?>)
    {
      translators.addRule(getRuleName(path), io.ocsf.utils.Files::readJson, Maps.typecast(rule));
    }
  }

  private static String getPathName(final Path path)
  {
    return path.getFileName().toString();
  }

  private static String getRuleName(final Path path)
  {
    final String name = getPathName(path);
    return name.substring(0, name.lastIndexOf('.'));
  }

  private static boolean isJsonFile(final Path path)
  {
    final String name = getPathName(path);
    return name.endsWith(".json");
  }

  private static String readSourceType(final Path path, final String type) throws IOException
  {
    final Path file = path.resolve(SOURCE_TYPE_FILE);
    if (Files.isRegularFile(file))
    {
      final Map<String, String> data = io.ocsf.utils.Files.readJson(file);

      return data.get("type");
    }

    return type;
  }
}
