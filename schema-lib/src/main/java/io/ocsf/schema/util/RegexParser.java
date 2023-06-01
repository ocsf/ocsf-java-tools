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

package io.ocsf.schema.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RegexParser
{
  private final Pattern pattern;

  /**
   * Creates a new pattern parser using the given regex pattern.
   *
   * @param pattern the regex pattern
   * @return a new parser
   */
  public static Parser create(final String pattern) throws ParserException
  {
    return line -> new RegexParser(pattern).parse(line);
  }

  private RegexParser(final String pattern)
  {
    this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
  }

  private Map<String, Object> parse(final String text)
  {
    final Matcher matcher = pattern.matcher(text);
    if (matcher.matches())
    {
      final Map<String, Object> data   = new HashMap<>();
      final List<String>        groups = Utils.getNamedGroups(pattern);

      groups.forEach(name -> data.put(name, matcher.group(name)));

      return data;
    }

    return null; // Match not found
  }

}
