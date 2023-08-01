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

package io.ocsf.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class ParserFileTest
{
  private ParserFileTest() {}

  private static final String TEST_FILE_NAME = "ocsf-utils/src/test/data/parser-tests.txt";

  public static void main(final String... args) throws IOException
  {
    if (args.length > 0)
    {
      for (final String filename : args)
      {
        System.out.println("Reading file: " + filename);

        runTests(filename);

        System.out.println("All done");
      }
    }
    else
    {
      System.out.println("Using the default test file: " + TEST_FILE_NAME);
      runTests(TEST_FILE_NAME);
    }
  }

  private static void runTests(final String filename) throws IOException
  {
    Files.readAllLines(Paths.get(filename)).forEach(
      line -> {
        try
        {
          System.out.printf("'%s' ...", line);
          BooleanExpression.parse(line);
          System.out.println(" ok");
        }
        catch (final InvalidExpressionException e)
        {
          System.out.println(" ERROR: " + e.getMessage());
        }
      }
    );

  }
}
