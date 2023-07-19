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

import io.ocsf.utils.Maps;
import io.ocsf.utils.Utils;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Single line event parser and data extractor.
 * <p>
 * The parser extracts structured data out of a single text line using a pattern. The pattern is
 * defined by the parts of the string that will be discarded.
 * <p>
 * A successful match requires all keys in a pattern to have a value. If any of the #{key} defined
 * in the pattern do not have a value, then an exception is thrown and may be handled by the
 * on_failure directive. An empty key %{} or a key starting with '_' can be used to match and
 * exclude values from the final data. All matched values are represented as string data types.
 * <p>
 * The pattern options:
 * <pre>
 *  &lt;pattern&gt; :=
 *      #{_} | #{_name}       |   // the value is unused/ignored
 *      #{name}               |   // the value is a string
 *      #{name: &lt;type&gt;}       |   // the value type is defined
 *
 *  &lt;type&gt; :=
 *      string[(length)]      |   // the value is a string with optional length
 *      integer               |   // the value is a 32-bit integer
 *      long                  |   // the value is a 64-bit integer
 *      number                |   // the value is a number, supports integer,
 *                                // long, double, and hex numbers
 *      json                  |   // the value is a JSON object
 *      &lt;datetime&gt;                // the value is a formatted time
 *
 *  &lt;datetime&gt; := datetime(&lt;format&gt;)
 *  &lt;format&gt;   := "Java DateTimeFormatter pattern"
 * </pre>
 *
 * <p>
 * For example:
 * <pre>
 * [#{event_time: datetime(dd/mmm/yyyy:HH:mm:ss Z)}] "#{host}" #{host_ip1} #{host_ip2} #{port: integer} #{http.status: integer} #{message} "#{http.verb} #{http.url} #{http.version}" "#{server}" "#{risk}" "#{content_type}" #{bytes_in: integer} #{bytes_out: integer} "#{agent}" "#{ref_url}" #{_rest}
 * </pre>
 * NOTE: This class is intended for use in a single thread.
 */
public final class PatternParser
{
  private final char[] buf;

  private static final int MaxStrLen = 1024;

  private final int len;

  // current char position
  private int pos;

  /**
   * The line parser rule interface.
   */
  private interface Rule
  {
    boolean apply(final Recognizer recognizer, final Map<String, Object> data);
  }


  /**
   * Creates a new pattern parser using the given string pattern.
   *
   * @param pattern the string pattern
   * @return a new pattern parser
   */
  public static Parser create(final String pattern) throws ParserException
  {
    final List<Rule> rules = new PatternParser(pattern).compile();

    return line -> {
      final Recognizer recognizer = new Recognizer(line);

      final Map<String, Object> data = new HashMap<>();
      for (final Rule rule : rules)
        if (!rule.apply(recognizer, data))
          return null; // no match

      return data;
    };
  }

  private PatternParser(final String pattern)
  {
    this(pattern.toCharArray());
  }

  private PatternParser(final char[] buf)
  {
    this(buf, 0, buf.length);
  }

  private PatternParser(final char[] buf, final int pos, final int len)
  {
    this.buf = buf;
    this.pos = pos;
    this.len = len;
  }

  private List<Rule> compile()
  {
    final StringBuilder sb    = new StringBuilder(64);
    final List<Rule>    rules = new ArrayList<>();

    boolean inField = false;
    Field   field   = null;

    while (pos < len)
    {
      final int ch = next();

      if (ch == '#' && buf[pos] == '{')
      {
        if (sb.length() > 0)
        {
          final String s = sb.toString();

          if (field != null)
          {
            if (field.ignore())
              rules.add((recognizer, data) -> recognizer.ignore(s));
            else
            {
              final Field f = field;
              rules.add((recognizer, data) -> recognizer.value(f, s, data));
            }

            field = null;
          }
          else
          {
            rules.add((recognizer, data) -> recognizer.skip(s));
          }

          sb.setLength(0);
        }

        ++pos; // skip '{'
        inField = true;
      }
      else if (ch == '}' && inField)
      {
        field   = field(sb.toString());
        inField = false;
        sb.setLength(0);
      }
      else
      {
        sb.append((char) ch);
      }
    }

    if (field != null)
    {
      if (sb.length() > 0)
      {
        final String s = sb.toString();

        if (field.ignore())
          rules.add((recognizer, data) -> recognizer.ignore(s));
        else
        {
          final Field f = field;
          rules.add((recognizer, data) -> recognizer.value(f, s, data));
        }
      }
      else
      {
        if (field.ignore())
          rules.add((recognizer, data) -> Recognizer.ignore());
        else
        {
          final Field f = field;
          rules.add((recognizer, data) -> recognizer.value(f, data));
        }
      }
    }
    else if (sb.length() > 0)
    {
      final String s = sb.toString();
      rules.add((recognizer, data) -> recognizer.skip(s));
    }

    return rules;
  }

  /**
   * Returns the next char from the string and advances the current read position.
   */
  private int next()
  {
    return buf[pos++];
  }


  /*
   * Field data extractor
   */
  private static Field field(final String field) throws ParserException
  {
    final int pos = field.indexOf(':');
    if (pos > 0)
    {
      // split the string to get the type definition
      final String name = field.substring(0, pos).trim();
      final String type = field.substring(pos + 1).trim();

      switch (type)
      {
        case "integer":
          return new IntegerField(name);

        case "long":
          return new LongIntField(name);

        case "number":
          return new NumberField(name);

        case "string":
          return new Field(name);

        case "json":
          return new JsonField(name);

        case "cef":
          return new CEFField(name);

        default:
        {
          final int i = type.indexOf('(');

          if (i > 0)
          {
            final String text = type.substring(i + 1, type.length() - 1);

            if (type.startsWith("datetime"))
            {
              return new DateTimeField(name, text);
            }

            if (type.startsWith("string"))
            {
              if (SyslogTimeStringField.Type.equalsIgnoreCase(text))
              {
                return new SyslogTimeStringField(name);
              }

              return new StringField(name, text);
            }

            throw new ParserException("Invalid type: " + type);
          }

          throw new ParserException("Missing type argument: " + type);
        }
      }
    }

    return new Field(field.trim());
  }

  private static class Field
  {
    final String name;

    Field(final String name)            {this.name = name;}

    boolean ignore()                    {return name.charAt(0) == '_';}

    int length()                        {return 0;}

    Object typecast(final String value) {return value;}
  }

  private static class StringField extends Field
  {
    final int len;

    StringField(final String name, final String text)
    {
      super(name);

      try
      {
        this.len = Integer.parseInt(text);

        if (len <= 0 || len > MaxStrLen)
          throw new ParserException(
            "Invalid string length: " + text + ". Valid range is: 1-" + MaxStrLen);
      }
      catch (final NumberFormatException ex)
      {
        throw new ParserException("String length in not a valid number: " + text);
      }
    }

    @Override
    int length() {return len;}
  }

  private static class JsonField extends Field
  {
    JsonField(final String name)
    {
      super(name);
    }

    @Override
    Object typecast(final String value)
    {
      try
      {
        return Json5Parser.parse(value);
      }
      catch (final ParserException ignore)
      {
        // ignore the error and keep the original data
      }

      return value;
    }
  }

  private static class CEFField extends Field
  {
    CEFField(final String name)
    {
      super(name);
    }

    @Override
    Object typecast(final String value)
    {
      try
      {
        return CEFParser.parse(value);
      }
      catch (final ParserException ignore)
      {
        // ignore the error and keep the original data
      }

      return value;
    }
  }

  private static class IntegerField extends Field
  {

    IntegerField(final String name) {super(name);}

    @Override
    Object typecast(final String value)
    {
      try
      {
        return Integer.parseInt(value);
      }
      catch (final NumberFormatException ignore)
      {
        // ignore the error and keep the original data
      }

      return value;
    }
  }

  private static class LongIntField extends Field
  {

    LongIntField(final String name) {super(name);}

    @Override
    Object typecast(final String value)
    {
      try
      {
        return Long.parseLong(value);
      }
      catch (final NumberFormatException ignore)
      {
        // ignore the error and keep the original data
      }

      return value;
    }
  }

  private static class NumberField extends Field
  {

    NumberField(final String name) {super(name);}

    @Override
    Object typecast(final String value)
    {
      try
      {
        return Utils.number(value);
      }
      catch (final NumberFormatException ignore)
      {
        // ignore the error and keep the original data
      }

      return value;
    }
  }

  private static class DateTimeField extends Field
  {
    private final DateTimeFormatter df;

    DateTimeField(final String name, final String format)
    {
      super(name);

      try
      {
        this.df = DateTimeFormatter.ofPattern(format);
      }
      catch (final IllegalArgumentException ex)
      {
        throw new ParserException("Invalid datetime format: " + format);
      }
    }

    @Override
    Object typecast(final String value)
    {
      try
      {
        return Instant.from(df.parse(value, ZonedDateTime::from)).toEpochMilli();
      }
      catch (final DateTimeParseException ignore)
      {
        // ignore the error and keep the original data
      }

      return value;
    }
  }

  private static class SyslogTimeStringField extends Field
  {
    static final String Type = "syslog-time";
    static final int    ID   = -1;

    SyslogTimeStringField(final String name)
    {
      super(name);
    }

    static int length(final String text, final int pos)
    {
      return Syslog.timeLength(text, pos);
    }

    @Override
    int length() {return ID;}
  }

  /*
   * Stateful pattern recognizer.
   */
  private static class Recognizer
  {
    private final String line;
    private final int    len;
    private       int    pos;

    Recognizer(final String line)
    {
      this.line = line;
      this.len  = line.length();
      this.pos  = 0;
    }

    boolean skip(final String str)
    {
      final int end = line.indexOf(str, pos);
      if (end == pos)
      {
        pos = end + str.length();
        return true;
      }

      return false;
    }

    boolean ignore(final String str)
    {
      final int end = line.indexOf(str, pos);
      if (end > 0)
      {
        pos = end + str.length();
        return true;
      }

      return false;
    }

    static boolean ignore() {return true;}

    boolean value(final Field field, final Map<String, Object> data)
    {
      Maps.putIn(data, field.name, field.typecast(line.substring(pos, len)));
      return true;
    }

    boolean value(final Field field, final String str, final Map<String, Object> data)
    {
      final int len = field.length();
      final int end;

      if (len == 0)
      {
        end = line.indexOf(str, pos);
      }
      else if (len > 0)
      {
        end = pos + field.length();
      }
      else if (len == SyslogTimeStringField.ID)
      {
        final int n = SyslogTimeStringField.length(line, pos);
        end = n > 0 ? pos + n : -1;
      }
      else
      {
        end = -1;
      }

      if (end > -1)
      {
        final Object value = field.typecast(line.substring(pos, end).trim());
        Maps.putIn(data, field.name, value);
        pos = end + str.length();
        return true;
      }

      return false;
    }
  }
}
