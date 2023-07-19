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

package io.ocsf.utils.parsers;

import java.util.Map;

/**
 * The event parser interface.
 */
@FunctionalInterface
public interface Parser
{
  /**
   * Parses the given text.
   *
   * @param text the event to be parsed
   * @return the parsed event or {@code null} if the input is not recognized as an event
   * @throws Exception when unable to parse the text
   */
  Map<String, Object> parse(final String text) throws Exception;
}
