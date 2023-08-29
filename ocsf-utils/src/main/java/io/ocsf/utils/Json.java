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

package io.ocsf.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * A helper class to translate a Java object to a string. It supports two formats: compact and
 * formatted/beautified.
 *
 * <p>Warning: This method assumes that the data is acyclical.
 */
public final class Json
{
  private Json() {}

  // ISO 8601 data/time format used for Date fields
  private static final String            ISO8601        = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(ISO8601);

  // Avoid scientific notation when encoding doubles. See:
  // https://stackoverflow.com/questions/16098046/how-do-i-print-a-double-value-without-scientific-notation-using-java
  // Scientific notation isn't parsed by Splunk, despite being legal JSON (at least for the
  // indexed time field).
  private static final DecimalFormat DECIMAL_FORMAT;

  static
  {
    // Using English locale to get dot (.) as decimal separator, which is required by JSON
    DECIMAL_FORMAT = new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    // 340 = DecimalFormat.DOUBLE_FRACTION_DIGITS (which is package-private)
    DECIMAL_FORMAT.setMaximumFractionDigits(340);
  }

  private static final String NULL  = null;
  private static final char[] COMMA = ", " .toCharArray();
  private static final char[] EMPTY_ARRAY = "[]" .toCharArray();

  private static final String EMPTY_OBJECT_STR = "{}";
  private static final char[] EMPTY_OBJECT     = EMPTY_OBJECT_STR.toCharArray();

  private static final int INDENT           = 2;
  private static final int INIT_BUFFER_SIZE = 1024;

  /**
   * Makes a JSON text of an Object value. If the value is Boolean, String, Number, Collection, Map,
   * or null then it will be encoded as the corresponding JSON types otherwise, the value's toString
   * method will be called, and the result will be quoted.
   *
   * <p>For compactness, no whitespaces are added.
   *
   * @param value a value to be serialized
   * @return a string representation of the object
   */
  public static String toString(final Object value)
  {
    return objectToString(value, new StringBuilder(INIT_BUFFER_SIZE)).toString();
  }

  public static String toString(final Map<String, Object> map)
  {
    if (map != null)
      return mapToString(map, new StringBuilder(capacity(map.size()))).toString();

    return Strings.EMPTY;
  }

  /**
   * Makes a JSON text of an Object value. If the value is Boolean, String, Number, Collection, Map,
   * or null then it will be encoded as the corresponding JSON types otherwise, the value's toString
   * method will be called, and the result will be quoted.
   *
   * @param value a value to be serialized
   * @return a string representation of the object
   */
  public static String format(final Object value)
  {
    return formatObject(value, new StringBuilder(INIT_BUFFER_SIZE)).toString();
  }

  public static String format(final Map<String, Object> map)
  {
    if (map != null)
      return mapToString(0, map, new StringBuilder(capacity(map.size()))).toString();

    return Strings.EMPTY;
  }

  private static StringBuilder objectToString(final Object value, final StringBuilder buf)
  {
    if (value instanceof String)
    {
      return quote((String) value, buf);
    }

    if (value instanceof Number)
    {
      return buf.append(validateNumber(value));
    }

    if (value instanceof Boolean)
    {
      return buf.append(value);
    }

    if (value instanceof Map<?, ?>)
    {
      return mapToString(Maps.typecast(value), buf);
    }

    if (value instanceof Collection<?>)
    {
      return collToString(Maps.typecast(value), buf);
    }

    if (value instanceof Character)
    {
      return appendChar((Character) value, buf);
    }

    if (value instanceof Date)
    {
      return buf.append('"').append(toIso8601String((Date) value)).append('"');
    }

    if (value == null)
    {
      return buf.append(NULL);
    }

    if (value.getClass().isArray())
    {
      if (value instanceof String[])
      {
        arrayToString((String[]) value, buf);
      }
      else if (value instanceof int[])
      {
        buf.append(Arrays.toString((int[]) value));
      }
      else if (value instanceof long[])
      {
        buf.append(Arrays.toString((long[]) value));
      }
      else if (value instanceof byte[])
      {
        buf.append(Arrays.toString((byte[]) value));
      }
      else if (value instanceof char[])
      {
        quote(new String((char[]) value), buf);
      }
      else if (value instanceof short[])
      {
        buf.append(Arrays.toString((short[]) value));
      }
      else if (value instanceof boolean[])
      {
        buf.append(Arrays.toString((boolean[]) value));
      }
      else if (value instanceof double[])
      {
        buf.append(Arrays.toString((double[]) value));
      }
      else if (value instanceof Map[])
      {
        mapArrayToString(Maps.typecast(value), buf);
      }
      else if (value instanceof Collection[])
      {
        collArrayToString(Maps.typecast(value), buf);
      }
      else if (value instanceof Object[])
      {
        deepToString((Object[]) value, buf);
      }
      else
      {
        throw new UnsupportedOperationException(
          "JSON conversion is not supported for " + value.getClass());
      }

      return buf;
    }

    return quote(value.toString(), buf);
  }

  private static StringBuilder formatObject(final Object value, final StringBuilder buf)
  {
    return formatObject(0, value, buf);
  }

  private static StringBuilder formatObject(
    final int indent, final Object value, final StringBuilder buf)
  {

    if (value instanceof String)
    {
      return quote((String) value, buf);
    }

    if (value instanceof Number)
    {
      return buf.append(validateNumber(value));
    }

    if (value instanceof Boolean)
    {
      return buf.append(value);
    }

    if (value instanceof Map<?, ?>)
    {
      return mapToString(indent, Maps.typecast(value), buf);
    }

    if (value instanceof Collection<?>)
    {
      return collToString(indent, Maps.typecast(value), buf);
    }

    if (value instanceof Character)
    {
      return appendChar((Character) value, buf);
    }

    if (value instanceof Date)
    {
      return buf.append('"').append(toIso8601String((Date) value)).append('"');
    }

    if (value == null)
    {
      return buf.append(NULL);
    }

    if (value.getClass().isArray())
    {
      if (value instanceof String[])
      {
        arrayToString((String[]) value, buf);
      }
      else if (value instanceof int[])
      {
        buf.append(Arrays.toString((int[]) value));
      }
      else if (value instanceof long[])
      {
        buf.append(Arrays.toString((long[]) value));
      }
      else if (value instanceof byte[])
      {
        buf.append(Arrays.toString((byte[]) value));
      }
      else if (value instanceof char[])
      {
        quote(new String((char[]) value), buf);
      }
      else if (value instanceof short[])
      {
        buf.append(Arrays.toString((short[]) value));
      }
      else if (value instanceof boolean[])
      {
        buf.append(Arrays.toString((boolean[]) value));
      }
      else if (value instanceof double[])
      {
        buf.append(Arrays.toString((double[]) value));
      }
      else if (value instanceof Map[])
      {
        mapArrayToString(indent, Maps.typecast(value), buf);
      }
      else if (value instanceof Collection[])
      {
        collArrayToString(indent, Maps.typecast(value), buf);
      }
      else if (value instanceof Object[])
      {
        deepToString((Object[]) value, buf);
      }
      else
      {
        throw new UnsupportedOperationException(
          "JSON conversion is not supported for " + value.getClass());
      }

      return buf;
    }

    return quote(value.toString(), buf);
  }

  private static String toIso8601String(final Date time)
  {
    return toIso8601String(time.getTime());
  }

  private static String toIso8601String(final long time)
  {
    return toIso8601String(accessor(time));
  }

  private static String toIso8601String(final TemporalAccessor date)
  {
    return TIME_FORMATTER.format(date);
  }

  private static TemporalAccessor accessor(final long time)
  {
    return ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
  }

  private static StringBuilder mapToString(final Map<String, Object> map, final StringBuilder sb)
  {
    if (map.isEmpty())
    {
      return sb.append(EMPTY_OBJECT);
    }

    sb.append('{');
    for (final Map.Entry<String, Object> e : map.entrySet())
    {
      quote(e.getKey(), sb).append(':');
      objectToString(e.getValue(), sb).append(',');
    }
    trim(sb).append('}');

    return sb;
  }

  private static StringBuilder mapToString(
    final int indent, final Map<String, Object> map,
    final StringBuilder sb)
  {
    if (map.isEmpty())
    {
      return sb.append(EMPTY_OBJECT);
    }

    final char[] spaces = spaces(indent + INDENT);

    sb.append("{\n");
    for (final Map.Entry<String, Object> e : map.entrySet())
    {
      sb.append(spaces);
      quote(e.getKey(), sb).append(": ");
      formatObject(spaces.length, e.getValue(), sb).append(",\n");
    }

    trim(sb, 2).append('\n');
    if (spaces.length > INDENT)
    {
      sb.append(spaces, 0, spaces.length - INDENT);
    }
    sb.append('}');

    return sb;
  }

  private static void arrayToString(final String[] a, final StringBuilder sb)
  {
    if (a.length == 0)
    {
      sb.append(EMPTY_ARRAY);
    }
    else
    {
      sb.append('[');
      for (final String elem : a)
      {
        quote(elem, sb).append(',');
      }
      trim(sb).append(']');
    }
  }

  // based on java.utils.Arrays.deepToString() implementation
  private static void deepToString(final Object[] a, final StringBuilder buf)
  {
    if (a.length == 0)
    {
      buf.append(EMPTY_ARRAY);
      return;
    }

    buf.append('[');
    final int max = a.length - 1;
    for (int i = 0; ; i++)
    {
      final Object element = a[i];
      if (element == null)
      {
        buf.append(NULL);
      }
      else
      {
        final Class<?> eClass = element.getClass();
        if (eClass.isArray())
        {
          if (eClass == byte[].class)
          {
            buf.append(Arrays.toString((byte[]) element));
          }
          else if (eClass == short[].class)
          {
            buf.append(Arrays.toString((short[]) element));
          }
          else if (eClass == int[].class)
          {
            buf.append(Arrays.toString((int[]) element));
          }
          else if (eClass == long[].class)
          {
            buf.append(Arrays.toString((long[]) element));
          }
          else if (eClass == char[].class)
          {
            buf.append(Arrays.toString((char[]) element));
          }
          else if (eClass == float[].class)
          {
            buf.append(Arrays.toString((float[]) element));
          }
          else if (eClass == double[].class)
          {
            buf.append(Arrays.toString((double[]) element));
          }
          else if (eClass == boolean[].class)
          {
            buf.append(Arrays.toString((boolean[]) element));
          }
          else
          {
            // element is an array of object references
            deepToString((Object[]) element, buf);
          }
        }
        else
        {
          // element is non-null and not an array
          objectToString(element, buf);
        }
      }

      if (i == max)
      {
        break;
      }

      buf.append(COMMA);
    }

    buf.append(']');
  }

  private static void collArrayToString(final Collection<Object>[] a, final StringBuilder sb)
  {
    if (a.length == 0)
    {
      sb.append(EMPTY_ARRAY);
    }
    else
    {
      sb.append('[');
      for (final Collection<Object> col : a)
      {
        collToString(col, sb).append(',');
      }
      trim(sb).append(']');
    }
  }

  private static void collArrayToString(
    final int indent, final Collection<Object>[] a,
    final StringBuilder sb)
  {
    if (a.length == 0)
    {
      sb.append(EMPTY_ARRAY);
    }

    final char[] spaces = spaces(indent + INDENT);

    sb.append("[\n");
    for (final Collection<Object> col : a)
    {
      sb.append(spaces);
      collToString(spaces.length, col, sb).append(",\n");
    }
    trim(sb, 2).append('\n');
    if (spaces.length > INDENT)
    {
      sb.append(spaces, 0, spaces.length - INDENT);
    }
    sb.append(']');

  }

  private static int capacity(final int size)
  {
    final int len = 100 * size;
    if (len <= 0)
      return 64 * 1_000;

    return len;
  }

  private static String validateNumber(final Object num)
  {
    if (num instanceof Double)
    {
      if (((Double) num).isInfinite() || ((Double) num).isNaN())
      {
        throw new IllegalArgumentException("JSON does not allow non-finite numbers.");
      }

      return DECIMAL_FORMAT.format(num);
    }

    if (num instanceof Float)
    {
      if (((Float) num).isInfinite() || ((Float) num).isNaN())
      {
        throw new IllegalArgumentException("JSON does not allow non-finite numbers.");
      }

      return Float.toString((float) num);
    }

    return num.toString();
  }

  private static StringBuilder quote(final String s, final StringBuilder sb)
  {
    if (s == null)
    {
      return sb.append(NULL);
    }
    if (s.isEmpty())
    {
      return sb.append("\"\"");
    }

    sb.append('"');
    for (final char ch : s.toCharArray())
    {
      appendChar(ch, sb);
    }
    sb.append('"');

    return sb;
  }

  private static StringBuilder appendChar(final int ch, final StringBuilder sb)
  {
    if (ch < ' ' || (ch >= '\u0080' && ch < '\u00a0') || (ch >= '\u2000' && ch < '\u2100'))
    {
      switch (ch)
      {
        case '\b':
          sb.append("\\b");
          break;
        case '\t':
          sb.append("\\t");
          break;
        case '\n':
          sb.append("\\n");
          break;
        case '\f':
          sb.append("\\f");
          break;
        case '\r':
          sb.append("\\r");
          break;
        default:
        {
          final String t = "000" + Integer.toHexString(ch);
          sb.append("\\u").append(t.substring(t.length() - 4));
          break;
        }
      }
    }
    else
    {
      if (ch == '\\' || ch == '"')
      {
        sb.append('\\');
      }

      sb.append((char) ch);
    }

    return sb;
  }

  private static void mapArrayToString(final Map<String, Object>[] a, final StringBuilder sb)
  {
    if (a.length == 0)
    {
      sb.append(EMPTY_ARRAY);
    }
    else
    {
      sb.append('[');
      for (final Map<String, Object> m : a)
      {
        mapToString(m, sb).append(',');
      }
      trim(sb).append(']');
    }
  }

  private static void mapArrayToString(
    final int indent, final Map<String, Object>[] a,
    final StringBuilder sb)
  {
    if (a.length == 0)
    {
      sb.append(EMPTY_ARRAY);
    }

    final char[] spaces = spaces(indent + INDENT);

    sb.append("[\n");
    for (final Map<String, Object> m : a)
    {
      sb.append(spaces);
      mapToString(spaces.length, m, sb).append(",\n");
    }
    trim(sb, 2).append('\n');
    if (spaces.length > INDENT)
    {
      sb.append(spaces, 0, spaces.length - INDENT);
    }
    sb.append(']');

  }

  private static StringBuilder collToString(final Collection<?> col, final StringBuilder sb)
  {
    if (col.isEmpty())
    {
      return sb.append(EMPTY_ARRAY);
    }

    sb.append('[');
    for (final Object elem : col)
    {
      objectToString(elem, sb).append(COMMA);
    }
    trim(sb, COMMA.length).append(']');

    return sb;
  }

  private static StringBuilder collToString(
    final int indent, final Collection<Object> col,
    final StringBuilder sb)
  {
    if (col.isEmpty())
    {
      return sb.append(EMPTY_ARRAY);
    }

    sb.append('[');
    for (final Object elem : col)
    {
      formatObject(indent, elem, sb).append(COMMA);
    }
    trim(sb, COMMA.length).append(']');

    return sb;
  }

  /*
   * Removes the last separator from a given StringBuilder.
   */
  private static StringBuilder trim(final StringBuilder sb, final int sepLen)
  {
    final int bufLen = sb.length();
    if (bufLen > sepLen)
    {
      sb.setLength(bufLen - sepLen);
    }

    return sb;
  }

  /*
   * Removes the last separator from a given StringBuilder.
   */
  private static StringBuilder trim(final StringBuilder sb)
  {
    return trim(sb, 1);
  }

  private static char[] spaces(final int n)
  {
    final char[] chars = new char[n];
    Arrays.fill(chars, ' ');
    return chars;
  }
}
