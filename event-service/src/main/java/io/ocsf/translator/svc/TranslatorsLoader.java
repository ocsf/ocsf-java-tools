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

import io.ocsf.utils.FuzzyHashMap;
import io.ocsf.utils.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * TranslatorsLoader loads the translation rules found in the given <code>path</code>.
 */
public final class TranslatorsLoader
{
  private static final Logger logger = LogManager.getLogger(TranslatorsLoader.class);

  private static final String RULE_TYPE_FILE = ".metadata";

  private TranslatorsLoader() {}

  /**
   * Scans the given <code>path</code>, compiles, and loads the translation rules found in the
   * path.
   *
   * @param path the path to the rules folder
   * @return a map of translators
   * @throws IOException if the given <code>path</code> is invalid
   */
  public static FuzzyHashMap<TranslatorsManager> load(final String path) throws IOException
  {
    return load(Paths.get(path));
  }

  /**
   * Scans the given <code>path</code>, compiles, and loads the translation rules found in the
   * path.
   *
   * @param path the path to the rules folder
   * @return a map of translators
   * @throws IOException if the given <code>path</code> is invalid
   */
  public static FuzzyHashMap<TranslatorsManager> load(final Path path) throws IOException
  {
    final Path home = validate(path);

    logger.info("Scanning {} for translation rules", home);

    final FuzzyHashMap<TranslatorsManager> acc = new FuzzyHashMap<>(getPathName(home));

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
    final FuzzyHashMap<TranslatorsManager> acc) throws IOException
  {
    try (final DirectoryStream<Path> stream = Files.newDirectoryStream(path))
    {
      for (final Path p : stream)
      {
        if (Files.isDirectory(p))
        {
          logger.info("Loading rules from: {}", p);

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
    final FuzzyHashMap<TranslatorsManager> acc) throws IOException
  {
    TranslatorsManager translators = acc.get(type);

    if (translators == null)
    {
      translators = new TranslatorsManager(home);
      acc.put(type, translators);
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
    return name.endsWith(io.ocsf.utils.Files.JSON_FILE_EXT);
  }

  private static String readSourceType(final Path path, final String type) throws IOException
  {
    final Path file = path.resolve(RULE_TYPE_FILE);

    if (Files.isRegularFile(file))
    {
      final Map<String, String> data = io.ocsf.utils.Files.readJson(file);

      return data.get("type");
    }

    return type;
  }
}
