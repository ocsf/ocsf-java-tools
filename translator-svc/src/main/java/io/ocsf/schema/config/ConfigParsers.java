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

package io.ocsf.schema.config;

import io.ocsf.schema.concurrent.MutableProcessorList;
import io.ocsf.schema.concurrent.ProcessorList;
import io.ocsf.schema.util.Parser;
import io.ocsf.schema.parsers.*;

/**
 * Initializes all parsers.
 */
public final class ConfigParsers
{
  private static final MutableProcessorList<Parser> parsers = new MutableProcessorList<>("Parsers");

  static
  {
    parsers.register(new CarbonBlackParser());
    parsers.register(new ProofpointEmailParser());
    parsers.register(new Office365Parser());
    parsers.register(new XmlWinEventLogParser());
    parsers.register(new XmlWinEventSecurityLogParser());
    parsers.register(new XmlWinSysmonEventLogParser());
    parsers.register(new WinEventLogParser());
    parsers.register(new WinEventSecurityLogParser());
    parsers.register(new CiscoSyslogParser());
    parsers.register(new InfobloxDHCPParser());
    parsers.register(new BoxParser());
  }

  private ConfigParsers() {}

  /**
   * Returns the instantiated parsers.
   *
   * @return a list of parsers
   */
  public static ProcessorList<Parser> parsers()
  {
    return parsers;
  }

}
