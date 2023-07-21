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
import io.ocsf.utils.parsers.ParserException;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class PerfTest
{
  private PerfTest() {}

  public static void main(final String... args) throws IOException
  {
    if (args.length > 2)
    {
      final String home = args[0];
      final String rule = args[1];

      System.out.println("Using rule home: " + home);
      System.out.println("Using rule file: " + rule);

      final List<Map<String, Object>> data = new ArrayList<>(args.length - 1);

      for (int i = 2; i < args.length; ++i)
      {
        final String dataFile = args[i];
        System.out.println("Using data file: " + dataFile);

        try
        {
          data.add(Files.readJson(dataFile));
        }
        catch (final ParserException e)
        {
          System.err.println("bad json file: " + e.getMessage());
          e.printStackTrace(System.err);
        }
      }

      final Translator
        translator = TranslatorBuilder.fromFile(Paths.get(home), Paths.get(rule));

      final long start = System.currentTimeMillis();
      for (int i = 0; i < 100_000; ++i)
           data.forEach(e -> {
             if (translator.apply(e) == null)
               System.err.append("should not happened");
           });

      System.out.printf("Elapsed: %,d ms%n", System.currentTimeMillis() - start);

    }
    else
    {
      System.err.println("Usage: PerfTest <rules-dir> <rule.jason> <data.json>");
    }
  }
}
