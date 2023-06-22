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

package io.ocsf.schema.parsers;

import io.ocsf.schema.util.Parser;
import io.ocsf.schema.util.PatternParser;
import io.ocsf.schema.util.Syslog;

import java.util.Map;

/**
 * Infoblox DHCP syslog parser. It parses and extracts the syslog header fields and the message text.
 * <p>
 * Sample events:
 * <pre>
 *   &lt;30&gt;Sep 28 10:15:46 192.168.1.2 dhcpd[13613]: DHCPACK on 192.168.1.120 to 00:50:56:13:60:56 (C8703420628) via eth1 relay eth1 lease-duration 600 (RENEW) uid 01:00:50:56:13:60:56
 *   &lt;30&gt;Sep 28 04:45:33 192.168.1.2 dhcpd[27785]: DHCPEXPIRE on 192.168.1.120 to 00:50:56:13:60:56
 *   &lt;30&gt;Sep 24 13:53:26 192.168.1.2 dhcpd[6453]: DHCPRELEASE of 192.168.1.120 from 00:50:56:13:60:56 (C8703420628) via eth1 (found) TransID 43e49f96 uid 01:00:50:56:13:60:56
 * </pre>
 */
public class InfobloxDHCPParser implements Parser
{
  private static final String SourceType = "infoblox:dhcp";

  protected static final String Priority = "priority";

  private static final String Pattern =
      "<#{priority: integer}>#{timestamp: string(syslog-time)} #{server_ip} dhcpd[#{pid}]: #{message}";

  private final Parser parser;

  public InfobloxDHCPParser()
  {
    this(Pattern);
  }

  public InfobloxDHCPParser(final String pattern)
  {
    this.parser = PatternParser.create(pattern);
  }

  @Override
  public Map<String, Object> parse(final String text) throws Exception
  {
    return decodePriority(parser.parse(text));
  }

  private static Map<String, Object> decodePriority(final Map<String, Object> data)
  {
    return data != null ? Syslog.decodePriority((Integer) data.remove(Priority), data) : null;
  }

  @Override
  public String toString() {return SourceType;}
}
