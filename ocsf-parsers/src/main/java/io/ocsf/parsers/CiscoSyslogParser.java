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

import io.ocsf.utils.parsers.Parser;
import io.ocsf.utils.parsers.PatternParser;
import io.ocsf.utils.parsers.Syslog;

import java.util.Map;

/**
 * General purpose Cisco syslog parser. It parses and extracts the syslog header fields and the
 * message text.
 * <p>
 * The default format is:
 * <pre>
 *   &lt;Facility&gt;timestamp host: %Product-Level-Code: Message
 * </pre>
 *
 * <p>
 * Sample events:
 * <pre>
 *   &lt;165&gt;Oct 06 2021 15:02:30 10.160.0.10 : %ASA-5-111010: User 'admin', running 'CLI' from IP 0.0.0.0, executed 'dir disk0:/dap.xml
 *   &lt;111&gt;Mar 12 07:18:12 10.11.12.13 : %ASA-6-713228: Group = dummy_group, Username = dummy_user, IP = 10.0.0.1 Assigned private IP address 10.0.0.2 to remote user
 *   &lt;111&gt;Mar 12 07:18:12 10.11.12.13 : %ASA-7-716014: Group my_group User joe_user View file readme.txt .
 * </pre>
 */
public class CiscoSyslogParser implements Parser
{
  private static final String SourceType = "cisco:syslog";

  protected static final String Priority = "priority";

  private static final String Pattern =
    "<#{priority: integer}>#{timestamp: string(syslog-time)} #{host}: %#{product}-#{level: " +
    "integer}-#{code: integer}: #{message}";

  private final Parser parser;

  public CiscoSyslogParser()
  {
    this(Pattern);
  }

  public CiscoSyslogParser(final String pattern)
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
