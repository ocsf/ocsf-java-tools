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

import io.ocsf.utils.parsers.Json5Parser;
import io.ocsf.utils.parsers.Parser;

import java.util.Map;

/**
 * VMWare Carbon Black event parser.
 */
public class CarbonBlackParser implements Parser
{
  public static final String SourceType = "vmware:carbon-black";

  @Override
  public Map<String, Object> parse(final String text) throws Exception
  {
    return Json5Parser.to(text);
  }

  @Override
  public String toString() {return SourceType;}
}
