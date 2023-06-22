
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

import java.text.CollationElementIterator;
import java.text.Collator;
import java.text.RuleBasedCollator;

/**
 * Language-sensitive text searching utility class.
 */
public final class Strings
{
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

  public static final boolean startsWith(final String text, final String sub)
  {
    if (text.length() < sub.length())
      return false;

    final String s = text.substring(0, sub.length());

    return search(s, Strings.getCollationElementIterator(sub)) > -1;
  }

  public static final boolean endsWith(final String text, final String sub)
  {
    if (text.length() < sub.length())
      return false;

    final String s = text.substring(text.length() - sub.length());

    return search(s, Strings.getCollationElementIterator(sub)) > -1;
  }

  public static final int search(final String text, final String sub)
  {
    return search(text, Strings.getCollationElementIterator(sub));
  }

  private static final int search(final String text, final CollationElementIterator patIter)
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

  private static final CollationElementIterator getCollationElementIterator(final String source)
  {
    return collator.getCollationElementIterator(source);
  }

  private static final boolean match(final CollationElementIterator text, final CollationElementIterator pattern)
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

}
