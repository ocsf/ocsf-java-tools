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

import java.util.Map;

public class WinEventLogParser implements Parser
{
  public static final String SourceType = "WinEventLog";

  @Override
  public Map<String, Object> parse(final String text) throws Exception
  {
    return new WinMultiLineParser(text).parse();
  }

  @Override
  public String toString() { return SourceType; }

}
