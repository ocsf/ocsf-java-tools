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

import io.ocsf.utils.FuzzyHashMap;
import io.ocsf.utils.parsers.Parser;

/**
 * Initializes all parsers.
 */
public final class Parsers
{
  private static final FuzzyHashMap<Parser> parsers = new FuzzyHashMap<>("Parsers");

  static
  {
    parsers.put(new CarbonBlackParser());
    parsers.put(new ProofpointEmailParser());
    parsers.put(new Windows365Parser());
    parsers.put(new WindowsXmlParser());
    parsers.put(new WindowsSysmonParser());
    parsers.put(new WindowsMultilineParser());
    parsers.put(new CiscoSyslogParser());
    parsers.put(new InfobloxSyslogParser());
    parsers.put(new BoxParser());
  }

  private Parsers() {}

  /**
   * Returns the instantiated parsers.
   *
   * @return a map of parsers
   */
  public static FuzzyHashMap<Parser> parsers()
  {
    return parsers;
  }

}
