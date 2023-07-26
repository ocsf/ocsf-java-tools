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

package io.ocsf.parsers;

import io.ocsf.utils.Json;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public final class CiscoSyslogFileParser
{
  private CiscoSyslogFileParser() {}

  public static void parse(final Stream<String> events)
  {
    final CiscoSyslogParser parser = new CiscoSyslogParser();

    events.map((Function<String, Map<String, Object>>) s -> {
      try
      {
        return parser.parse(s);
      }
      catch (final Exception e)
      {
        return Collections.emptyMap();
      }
    }).forEach(map -> {
      final String data = Json.format(map);

      System.out.println(data);
    });
  }

  public static void main(final String... files) throws Exception
  {
    if (files.length > 0)
    {
      for (final String file : files)
      {
        parse(Files.readAllLines(Paths.get(file)).stream());
      }
    }
    else
    {
      System.out.println("Usage: CiscoSyslogFileParser <filename>");
    }
  }
}