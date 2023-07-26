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

/**
 * Microsoft System Monitor (Sysmon) event parser (XML).
 * See <a href="https://learn.microsoft.com/en-us/sysinternals/">Sysinternals</a>
 */
public class WindowsSysmonParser extends WindowsXmlParser
{
  public static final String SourceType = "microsoft:windows:sysmon";

  @Override
  public String toString() {return SourceType;}
}
