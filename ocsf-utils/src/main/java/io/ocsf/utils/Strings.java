/*
 * Copyright 2024 Splunk Inc.
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

package io.ocsf.utils;

import java.text.CollationElementIterator;
import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Language-sensitive text searching utility class.
 */
public final class Strings
{
  public static final String LineSplitter       = "\\R+";
  public static final String WhiteSpaceSplitter = "\\s+";

  public static final String EMPTY = "";

  private static final int mask = 0xFFFF0000; // Collator.PRIMARY

  private static final RuleBasedCollator collator = (RuleBasedCollator) Collator.getInstance();

  private Strings() {}

  public static boolean isNotEmpty(final String s)
  {
    if (s != null)
      for (final char ch : s.toCharArray())
        if (!Character.isWhitespace(ch))
          return true;

    return false;
  }

  public static boolean isEmpty(final String s) {return s == null || s.trim().isEmpty();}

  public static boolean startsWith(final String text, final String sub)
  {
    if (text.length() < sub.length())
      return false;

    final String s = text.substring(0, sub.length());

    return search(s, Strings.getCollationElementIterator(sub)) > -1;
  }

  public static boolean endsWith(final String text, final String sub)
  {
    if (text.length() < sub.length())
      return false;

    final String s = text.substring(text.length() - sub.length());

    return search(s, Strings.getCollationElementIterator(sub)) > -1;
  }

  public static int search(final String text, final String sub)
  {
    return search(text, Strings.getCollationElementIterator(sub));
  }

  public static List<Object> toArray(final Object value)
  {
    return toArray(value, LineSplitter);
  }

  public static List<Object> toArray(final Object value, final String splitter)
  {
    if (value instanceof String)
    {
      // Split the string and convert to list
      final String[]     split = ((String) value).split(splitter);
      final List<Object> list  = new ArrayList<>(split.length);
      for (final String s : split)
      {
        final String s1 = s.trim();
        if (!s1.isEmpty())
        {
          list.add(s1);
        }
      }
      return list;
    }
    else if (value instanceof List<?>)
    {
      return Maps.typecast(value);
    }

    // Everything else, including null, gets wrapped in a single-item list
    return Collections.singletonList(value);
  }

  private static int search(final String text, final CollationElementIterator patIter)
  {
    final CollationElementIterator it = collator.getCollationElementIterator(text);

    for (int i = 0; i < text.length(); ++i)
    {
      it.setOffset(i);
      patIter.reset();

      if (match(it, patIter)) return i;
    }

    return -1; // no match
  }

  private static CollationElementIterator getCollationElementIterator(final String source)
  {
    return collator.getCollationElementIterator(source);
  }

  private static boolean match(
    final CollationElementIterator text, final CollationElementIterator pattern)
  {
    do
    {
      final int i      = pattern.next() & Strings.mask;
      final int target = text.next() & Strings.mask;

      if (i == Strings.mask)
        return true; // end of pattern

      if (i != target)
        return false; // mismatch
    }
    while (true);
  }

  /**
   * Tests if the last element of a dotted path exists earlier in the path, indicating a loop.
   * @param path a dotted path of names, as used with JSON paths.
   * @return true if there is a loop
   */
  public static boolean isPathLooped(final String path)
  {
    if (path == null || path.isEmpty())
    {
      return false;
    }
    final String[] elements = path.split("\\.");
    if (elements.length <= 1)
    {
      return false;
    }
    final String last = elements[elements.length - 1];
    for (int i = elements.length - 2; i >= 0; --i)
    {
      if (last.equals(elements[i]))
      {
        return true;
      }
    }
    return false;
  }

  public static String quote(final String s) {
    if (s == null)
    {
      return "null";
    }
    return '"' + s.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
  }

  public static String quote(final Object o) {
    if (o == null)
    {
      return "null";
    }
    return quote(o.toString());
  }
}
