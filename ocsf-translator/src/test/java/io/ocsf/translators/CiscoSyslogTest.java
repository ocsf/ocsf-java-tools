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

package io.ocsf.translators;

import io.ocsf.parsers.CiscoSyslogParser;
import io.ocsf.utils.Maps;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class CiscoSyslogTest
{
  private static final String Data = "<165>Oct 06 2021 15:02:30 10.160.0.10 : %ASA-5-111010: User 'admin', running 'CLI' from IP 0.0.0.0, executed 'dir disk0:/dap.xml'";
  private static final String Rule = "{" +
      "  \"when\": \"code = 111010\"," +
      "  \"parser\": {" +
      "    \"name\": \"message\"," +
      "    \"pattern\": \"User '#{username}', running '#{application}' from IP #{ip_addr}, executed '#{cmd}'\"," +
      "    \"output\": \"data\"" +
      "  }," +
      "  \"rules\": [" +
      "    {" +
      "      \"host\": {" +
      "        \"@move\": \"origin.device.ip\"" +
      "      }" +
      "    }," +
      "    {" +
      "      \"data.username\": {" +
      "        \"@move\": \"user.name\"" +
      "      }" +
      "    }" +
      "  ]" +
      "}";

  @Test
  public void parse()
  {
    final CiscoSyslogParser parser = new CiscoSyslogParser();

    try
    {
      final Map<String, Object> parsed = parser.parse(Data);
      Assert.assertNotNull(parsed);

      final Map<String, Object> translated = Translator
          .fromString(Rule)
          .apply(parsed);

      Assert.assertEquals(2, translated.size());
      Assert.assertEquals("10.160.0.10", Maps.getIn(translated, "origin.device.ip"));
      Assert.assertEquals("admin", Maps.getIn(translated, "user.name"));
    }
    catch (final Exception e)
    {
      Assert.fail(e.getMessage());
    }
  }

}