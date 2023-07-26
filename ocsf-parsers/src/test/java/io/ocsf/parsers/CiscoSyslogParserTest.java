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
import io.ocsf.utils.parsers.Syslog;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public class CiscoSyslogParserTest
{

  @Test
  public void parse()
  {
    final CiscoSyslogParser parser = new CiscoSyslogParser();

    Arrays.stream(CiscoSyslogData.Data).map((Function<String, Map<String, Object>>) s -> {
      try
      {
        return parser.parse(s);
      }
      catch (final Exception e)
      {
        Assert.fail(e.getMessage());
        return Collections.emptyMap();
      }
    }).forEach(map -> {
      Assert.assertNotNull(map);
      Assert.assertEquals(Json.format(map), 8, map.size());
    });
  }

  @Test
  public void parseMissingHost() throws Exception
  {
    final String text =
      "<165>Oct 06 15:02:30: %ASA-5-111008: User 'admin' executed the 'dir disk0:/dap.xml' " +
      "command.";

    final CiscoSyslogParser   parser = new CiscoSyslogParser();
    final Map<String, Object> parsed = parser.parse(text);
    Assert.assertNull(parsed);

  }

  @Test
  public void timeLength()
  {
    Arrays.stream(CiscoSyslogData.Data)
          .map(s -> Syslog.timeLength(s, s.indexOf('>') + 1))
          .forEach(n -> Assert.assertTrue(n > 0));
  }

}