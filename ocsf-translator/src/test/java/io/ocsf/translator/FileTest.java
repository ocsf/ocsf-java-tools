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

package io.ocsf.translator;

import io.ocsf.utils.Files;
import io.ocsf.utils.Json;
import io.ocsf.utils.parsers.ParserException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

public final class FileTest
{
  private FileTest() {}

  public static void main(final String... args) throws IOException
  {
    if (args.length > 1)
    {
      final String home = args[0];
      final String ruleFile = args[1];

      final Translator
        translator = TranslatorBuilder.fromFile(Paths.get(home), Paths.get(ruleFile));

      System.out.println("Using rule file: " + ruleFile);

      for (int i = 1; i < args.length; ++i)
      {
        final File file = new File(args[i]);

        visitAllDirsAndFiles(translator, file);
      }
    }
    else
    {
      System.err.println("Usage FileTest <rules-dir> <rule.json> <data.json>");
    }
  }

  private static void visitAllDirsAndFiles(
    final Translator translator, final File file) throws IOException
  {
    System.out.println("Processing " + file);

    final BasicFileAttributes basicFileAttributes =
      java.nio.file.Files.readAttributes(file.toPath(), BasicFileAttributes.class);

    if (basicFileAttributes.isRegularFile())
    {
      transform(translator, file.getPath());
    }
    else if (basicFileAttributes.isDirectory())
    {
      final String[] children = file.list();

      if (children != null)
        for (final String child : children)
          visitAllDirsAndFiles(translator, new File(file, child));
    }
  }

  private static void transform(
    final Translator translator, final String filename) throws IOException
  {
    try
    {
      final Map<String, Object> data = Files.readJson(filename);

      final Map<String, Object> translated = translator.apply(data);
      if (translated.containsKey("class_id"))
      {
        System.out.println(Json.format(translated));
        if (!data.isEmpty())
        {
          System.out.println(Json.format(data));
        }
      }
    }
    catch (final ParserException e)
    {
      System.err.println("bad json file: " + e.getMessage());
      e.printStackTrace(System.err);
    }
  }
}
