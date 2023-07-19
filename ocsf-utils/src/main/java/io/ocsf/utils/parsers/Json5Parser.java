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

package io.ocsf.utils.parsers;

import io.ocsf.utils.Strings;
import io.ocsf.utils.Utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * JSON5 parser, see <a href="https://json5.org">JSON5 - JSON for Humans</a>.
 * <p>
 * NOTE: This class is intended for use in a single thread.
 *
 * @author Roumen Roupski
 */
public final class Json5Parser
{
  private final char[] buf;

  private final int len;

  // current char position
  private int pos;

  private final StringBuilder sb = new StringBuilder(32);

  /**
   * Return the next JSON value. The value can be a Boolean, Double, Integer, Long, String, List,
   * Map, or null.
   *
   * <p>This method can be used to parse multiple JSON values in the buffer.
   *
   * @param <T> the type of the parsed JSON data
   * @return A JSON value, <code>null</code> indicates the end of the buffer
   * @throws ParserException If syntax error.
   */
  @SuppressWarnings("unchecked")
  public <T> T to() throws ParserException
  {
    pos = skip(buf, pos, len);
    return pos < buf.length ? (T) value() : null;
  }

  @SuppressWarnings("unchecked")
  public static <T> T to(final String text) throws ParserException
  {
    if (text == null || text.isEmpty())
    {
      return null;
    }

    return (T) new Json5Parser(text.toCharArray()).parse();
  }

  @SuppressWarnings("unchecked")
  public static <T> T to(final byte[] bytes, final int offset, final int length)
    throws ParserException
  {
    if (bytes == null || bytes.length == 0) return null;

    // String constructor will validate offset and length against bytes
    return (T) new Json5Parser(
      new String(bytes, offset, length, StandardCharsets.UTF_8).toCharArray()).parse();
  }

  /**
   * A helper method to parse a given string containing JSON text.
   *
   * @param text the text to parse
   * @return an object
   * @throws ParserException If syntax error.
   */
  public static Object parse(final String text) throws ParserException
  {
    if (text == null) return null;

    if (text.isEmpty()) return text;

    return new Json5Parser(text.toCharArray()).parse();
  }

  public static Object parse(final byte[] bytes) throws ParserException
  {
    if (bytes == null) return null;

    if (bytes.length == 0) return Strings.EMPTY;

    return new Json5Parser(
      new String(bytes, StandardCharsets.UTF_8).toCharArray()).parse();
  }

  /**
   * Parses the string and returns a JSON value. The value can be a Boolean, Double, Integer, Long,
   * String, List, Map, or null.
   *
   * @return A JSON value.
   * @throws ParserException If syntax error.
   */
  public Object parse() throws ParserException
  {
    return value();
  }

  private Json5Parser(final char[] buf)
  {
    this(buf, 0, buf.length);
  }

  private Json5Parser(final char[] buf, final int pos, final int len)
  {
    this.buf = buf;
    this.pos = pos;
    this.len = len;
  }

  /**
   * Get the next value. The value can be a Boolean, Double, Integer, List, Map, Long, String, or
   * null.
   *
   * @return An object.
   * @throws ParserException If syntax error.
   */
  private Object value() throws ParserException
  {
    pos = skip(buf, pos, len);

    // look ahead to check the next char
    final int ch = buf[pos];
    switch (ch)
    {
      case '{':
        return object();

      case '[':
        return array(']');
      case '(':
        return array(')');

      case '"':
      case '\'':
        return string();

      default:
        return isDigit(ch) ? number() : symbol();
    }
  }

  private static boolean isDigit(final int ch)
  {
    return (ch >= '0' && ch <= '9') || ch == '.' || ch == '-' || ch == '+';
  }

  /**
   * Returns the characters up to the next close quote character. Backslash processing is done. The
   * formal JSON format does not allow strings in single quotes, but an implementation is allowed to
   * accept them.
   *
   * @return A String.
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
          final char c = getEscapeChar();
          if (c != '\r')
            sb.append(c);
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
   * Parse an object value.
   *
   * @return A <tt>Map</tt> of name/value pairs.
   * @throws ParserException If syntax error.
   */
  private Map<String, Object> object() throws ParserException
  {
    final Map<String, Object> map = new HashMap<>();

    ++pos; // skip '{'

    try
    {
      while (pos < len)
      {
        // get the object's next field name, if any
        pos = skip(buf, pos, len);
        switch (next())
        {
          case '}':
            return map;

          case '"':
            field(map, name(pos, '"'));
            break;

          case '\'':
            field(map, name(pos, '\''));
            break;

          // optional field separator
          case ',':
          case ';':
            break;

          default:
            field(map, name(pos - 1));
            break;
        }
      }
    }
    catch (final ArrayIndexOutOfBoundsException e)
    {
      throw syntaxError("Invalid JSON");
    }

    throw syntaxError("Unexpected end of string");
  }

  private void field(final Map<String, Object> map, final String name) throws ParserException
  {
    map.put(name, value());
  }

  /**
   * Parse an array value.
   *
   * @return An array (list) of objects.
   * @throws ParserException If syntax error.
   */
  private List<?> array(final int q) throws ParserException
  {
    final List<Object> values = new ArrayList<>();

    ++pos; // skip '[' or '(
    while (pos < len)
    {
      pos = skip(buf, pos, len);
      if (buf[pos] == q)
      {
        ++pos;
        return values;
      }

      values.add(value());

      pos = skip(buf, pos, len);

      switch (next())
      {
        case ';':
        case ',':
        {
          break;
        }

        default:
        {
          if (q == buf[pos - 1])
          {
            return values;
          }

          throw syntaxError("Expected a ',' or ']'");
        }
      }
    }

    throw syntaxError("Unexpected end of string");
  }

  /**
   * Handle unquoted text. This could be the values true, false, or null
   */
  private Object symbol() throws ParserException
  {
    final int size = getSymbolSize(buf, pos, len);

    if (size == 0)
    {
      throw syntaxError("Missing value");
    }

    try
    {
      return symbol(new String(buf, pos, size));
    }
    finally
    {
      pos += size;
    }
  }

  static Object symbol(final String s) throws ParserException
  {
    switch (s.toLowerCase(Locale.US))
    {
      case "true":
        return Boolean.TRUE;

      case "false":
        return Boolean.FALSE;

      case "null":
        return null;

      default:
        throw new ParserException("Unexpected symbol: " + s);
    }
  }

  private String name(final int at)
  {
    int i = pos;
    while (buf[i] != ':' && buf[i] != '=')
    {
      ++i;
    }

    pos = i + 1;
    return new String(buf, at, i - at).trim().intern();
  }

  private String name(final int at, final int ch)
  {
    int i = pos;
    while (buf[i] != ch)
    {
      ++i;
    }

    // the name is optionally followed by ':' or '='
    pos = skip(buf, i + 1, len);
    if (buf[pos] == ':' || buf[pos] == '=')
    {
      ++pos;
    }

    return new String(buf, at, i - at).intern();
  }

  /*
   * Parse a number. The method supports non-standard Hex 0x-convention.
   *
   * @throws NumberFormatException if the string does not contain a number
   */
  private Object number()
  {
    final int size = getSymbolSize(buf, pos, len);

    try
    {
      return Utils.number(buf, pos, size);
    }
    finally
    {
      pos += size;
    }
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
      int ch = buf[i];

      // skip comments
      if (ch == '/')
      {
        ch = buf[++i]; // get next char
        if (ch == '/')
        {
          i = skipLine(buf, i + 1, len);
        }
        else if (ch == '*')
        {
          i = skipLines(buf, i + 1, len);
        }
      }
      else if (!Character.isWhitespace(ch))
      {
        return i;
      }
    }

    return len;
  }

  private static int skipLine(final char[] buf, final int pos, final int len)
  {
    for (int i = pos; i < len; ++i)
    {
      if (buf[i] == '\n')
      {
        return i;
      }
    }
    return len;
  }

  private static int skipLines(final char[] buf, final int pos, final int len)
  {
    for (int i = pos; i < len; ++i)
    {
      if (buf[i] == '*' && buf[i + 1] == '/')
      {
        return i + 1;
      }
    }
    return len;
  }

  /**
   * Returns a symbol size in a number of chars, starting from the current position, or 0 if end of
   * the buffer is reached.
   */
  private static int getSymbolSize(final char[] buf, final int pos, final int len)
  {
    int size = 0;
    for (int i = pos; i < len && isSymbolChar(buf[i]); ++i)
    {
      ++size;
    }

    return size;
  }

  private static boolean isSymbolChar(final int ch)
  {
    return !Character.isWhitespace(ch) && ",:]}/\\\"[{;=#" .indexOf(ch) < 0;
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

  /**
   * Make a JsonParseException to signal a syntax error.
   *
   * @param message The error message.
   * @return A JsonParseException object, suitable for throwing
   */
  private ParserException syntaxError(final String message)
  {
    if (pos < buf.length)
    {
      return
        new ParserException(
          message + " at " + pos + ", found: '" + buf[pos] + '\'' + " " + Integer
            .toHexString(buf[pos] & 0x0ffff));
    }

    return new ParserException(message + " at the end");
  }

  private static ParserException syntaxError(final int ch, final int pos)
  {
    return new ParserException(
      "Illegal escape at " + pos + ": 0x" + Integer.toHexString(ch & 0x0ffff));
  }
}
