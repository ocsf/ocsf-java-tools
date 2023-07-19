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

package io.ocsf.utils;

import io.ocsf.utils.parsers.Parser;
import io.ocsf.utils.parsers.RegexParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class RegexParserTest
{
  private static final String R =
    "(?<evcls>DHCPACK)\\s+on\\s+(?<ip>\\S+)\\s+to\\s+(?<mac>\\S+)(?:\\s+\\((?<host>.+?)\\))" +
    "?\\s+via\\s+(?<interface>.*)\\s+relay\\s+(?<relay>\\S+)\\s+lease-duration\\s+" +
    "(?<duration>\\d+).*?(?:uid\\s+(?<uid>.+))?";

  @Test
  public void parse() throws Exception
  {
    final String text =
      "DHCPACK on 10.127.16.36 to 00:e0:4c:08:00:7e (PF345215) via eth2 relay 10.127.16.1 " +
      "lease-duration 43200 (RENEW) uid 01:00:e0:4c:08:00:7e";

    final Parser              parser = RegexParser.create(R);
    final Map<String, Object> data   = parser.parse(text);

    Assert.assertNotNull(data);
    Assert.assertEquals(Json.format(data), 8, data.size());
    Assert.assertEquals("evcls", "DHCPACK", data.get("evcls"));
    Assert.assertEquals("ip", "10.127.16.36", data.get("ip"));
    Assert.assertEquals("mac", "00:e0:4c:08:00:7e", data.get("mac"));
    Assert.assertEquals("host", "PF345215", data.get("host"));
    Assert.assertEquals("interface", "eth2", data.get("interface"));
    Assert.assertEquals("relay", "10.127.16.1", data.get("relay"));
    Assert.assertEquals("duration", "43200", data.get("duration"));
    Assert.assertEquals("uid", "01:00:e0:4c:08:00:7e", data.get("uid"));
  }

}
