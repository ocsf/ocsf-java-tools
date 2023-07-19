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

import java.util.HashMap;
import java.util.Map;

/**
 * Name/Value parser using <code>':'</code> or <code>'='</code> as a separator. For example:
 * <code>name="Bob",age=22</code>.
 * <p>
 * NOTE: This class is intended for use in a single thread.
 *
 * @author Roumen Roupski
 */
public final class NameValueParser
{
  private final char[] buf;

  private final int len;

  // current char position
  private int pos;

  private final StringBuilder sb = new StringBuilder(32);

  /**
   * Parse the given text containing a single line with name/value pairs.
   *
   * @param text the text to parse
   * @return the parsed data
   * @throws ParserException If syntax error.
   */
  public static Map<String, Object> parse(final String text) throws ParserException
  {
    if (text == null || text.isEmpty()) return null;

    return new NameValueParser(text.toCharArray()).parse();
  }

  public NameValueParser(final char[] buf)
  {
    this(buf, 0, buf.length);
  }

  public NameValueParser(final char[] buf, final int pos, final int len)
  {
    this.buf = buf;
    this.pos = pos;
    this.len = len;
  }

  public Map<String, Object> parse() throws ParserException
  {
    final Map<String, Object> map = new HashMap<>();

    try
    {
      while (pos < len)
      {
        pos = skip(buf, pos, len);
        field(map, name(pos));
        pos = separator(buf, pos, len);
      }
    }
    catch (final ArrayIndexOutOfBoundsException e)
    {
      throw syntaxError("Invalid input data");
    }

    return map;
  }

  private String value() throws ParserException
  {
    pos = skip(buf, pos, len);

    // look ahead to check the next char
    final int ch = buf[pos];

    return ch == '"' || ch == '\'' ? string() : text();
  }

  /**
   * Returns the characters up to the next close quote character. Backslash processing is done.
   *
   * @return A string.
   * @throws ParserException Unterminated string.
   */
  private String string() throws ParserException
  {
    // get the string quote char
    final int quote = next();

    sb.setLength(0);
    try
    {
      int ch;
      while ((ch = next()) != quote)
      {
        if (ch == '\\')
        {
          sb.append(getEscapeChar());
        }
        else
        {
          sb.append((char) ch);
        }
      }

      return sb.toString();
    }
    catch (final ArrayIndexOutOfBoundsException e)
    {
      throw syntaxError("Unterminated string");
    }
  }

  /**
   * Handles unquoted text. Backslash processing is done.
   */
  private String text() throws ParserException
  {
    sb.setLength(0);

    // the index of the last space char in the input buffer (sort of)
    int i = 0;

    int ch;
    while ((ch = next()) != '=')
    {
      if (ch == '\\')
      {
        sb.append(getEscapeChar());
      }
      else
      {
        if (ch == ' ')
        {
          // save the position of the next char after the space
          i = pos;
        }

        sb.append((char) ch);
      }

      // check for the end of buffer
      if (pos >= len)
      {
        return sb.toString();
      }
    }

    // back off to the first space (the start of the next name)
    pos = i;

    final int end = sb.lastIndexOf(" ");
    return sb.substring(0, end);
  }

  private void field(final Map<String, Object> map, final String name) throws ParserException
  {
    map.put(name, value());
  }

  private String name(final int at)
  {
    int i = pos;
    while (buf[i] != '=' && buf[i] != ':')
    {
      ++i;
    }

    pos = i + 1;

    return new String(buf, at, i - at).trim().intern();
  }

  /**
   * Returns the next char from the string.
   */
  private int next()
  {
    return buf[pos++];
  }

  /**
   * Skip all white-spaces, returns the position of first non-white-space char or 'len' if end of
   * the buffer is reached.
   */
  private static int skip(final char[] buf, final int pos, final int len)
  {
    for (int i = pos; i < len; ++i)
    {
      if (!Character.isWhitespace(buf[i]))
      {
        return i;
      }
    }

    return len;
  }

  private static int separator(final char[] buf, final int pos, final int len)
  {
    for (int i = pos; i < len; ++i)
    {
      // skip an optional field separator
      final int ch = buf[i];

      if (ch == ',' || ch == ';')
      {
        continue;
      }

      if (!Character.isWhitespace(buf[i]))
      {
        return i;
      }
    }

    return len;
  }


  private char getEscapeChar() throws ParserException
  {
    final int ch = next();
    switch (ch)
    {
      case 'b':
        return ('\b');
      case 't':
        return ('\t');
      case 'n':
        return ('\n');
      case 'f':
        return ('\f');
      case 'r':
        return ('\r');
      case 'u':
        return unicode();
      case '|':
      case '=':
      case '"':
      case '\'':
      case '\\':
      case '/':
        return (char) ch;
      default:
        throw syntaxError(ch, pos);
    }
  }

  private char unicode()
  {
    int value = 0;
    for (int i = 0; i < 4; ++i)
    {
      final int c = next();
      switch (c)
      {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          value = (value << 4) + c - '0';
          break;
        case 'a':
        case 'b':
        case 'c':
        case 'd':
        case 'e':
        case 'f':
          value = (value << 4) + c - 'a' + 10;
          break;
        case 'A':
        case 'B':
        case 'C':
        case 'D':
        case 'E':
        case 'F':
          value = (value << 4) + c - 'A' + 10;
          break;
        default:
          break;
      }
    }
    return (char) value;
  }

  private ParserException syntaxError(final String message)
  {
    if (pos < buf.length)
    {
      return new ParserException(
        message + " at " + pos + ", found: '" + buf[pos] + '\'' + " " +
        Integer.toHexString(buf[pos] & 0x0ffff));
    }

    return new ParserException(message + " at the end");
  }

  private static ParserException syntaxError(final int ch, final int pos)
  {
    return new ParserException(
      "Illegal escape char at " + pos + ": 0x" + Integer.toHexString(ch & 0x0ffff));
  }
}
