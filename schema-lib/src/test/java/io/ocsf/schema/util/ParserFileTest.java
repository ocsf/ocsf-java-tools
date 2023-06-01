/*
 * Copyright 2023 Open Cybersecurity Schema Framework
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

package io.ocsf.schema.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class ParserFileTest
{
  private ParserFileTest() {}

  public static void main(final String... args) throws IOException
  {
    if (args.length > 0)
    {
      for (final String name : args)
      {
        System.out.println("Reading file: " + name);

        Files.readAllLines(Paths.get(name)).forEach(
            line -> {
              try
              {
                System.out.printf("'%s' ...", line);
                ExpressionParser.parse(line);
                System.out.println(" ok");
              }
              catch (final InvalidExpressionException e)
              {
                System.out.println(" ERROR: " + e.getMessage());
              }
            }
        );

        System.out.println("All done");
      }
    }
    else
    {
      System.err.println("Usage ParserTest <file>");
    }
  }
}
