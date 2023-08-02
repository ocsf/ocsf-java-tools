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

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Set;

/**
 * The boolean expression grammar:
 *
 * <pre>
 *  &lt;expression&gt;  := &lt;factor&gt; { &lt;or&gt; &lt;expression&gt; }
 *  &lt;factor&gt;      := &lt;term&gt; { &lt;and&gt; &lt;factor&gt; }
 *  &lt;and&gt;         := "and"
 *  &lt;or&gt;          := "or"
 *  &lt;term&gt;        := "("&lt;expression&gt;")" | &lt;not&gt; "("&lt;expression&gt;")" |
 *                   &lt;field&gt; &lt;operator&gt; &lt;value&gt;
 *  &lt;not&gt;         := "not" | "!"
 *  &lt;operator&gt;    := "=" | "==" | "!=" |
 *                   "&lt;" | "&lt;=" | "&gt;" | "&gt;=" |
 *                   "is" | "is_not" |
 *                   "in" | "not_in" |
 *                   "contains" | "like" | "match" |
 *                   "starts_with" | "ends_with" |
 *                   "exec"
 * </pre>
 * NOTE: This class is intended for use in a single thread.
 */
public final class BooleanExpression
{
  private static final String ERR_MSG = "Expected %s, but found '%s'";

  private final char[] buf;
  private       int    pos = -1;

  // last successfully parsed token
  private Token token;

  private final Deque<Tree> stack = new LinkedList<>();

  // temp string buffer
  private final StringBuilder sb = new StringBuilder(64);

  public static Tree parse(final String exp) throws InvalidExpressionException
  {
    return new BooleanExpression(exp).parse();
  }

  public BooleanExpression(final String exp)
  {
    if (exp == null || exp.isEmpty())
      buf = null;
    else
      buf = exp.toCharArray();
  }

  public Tree parse() throws InvalidExpressionException
  {
    if (buf == null) return Tree.Empty;

    try
    {
      // read ahead the first token
      nextToken();

      expression();

      // the expression must end with end-of-line
      if (token != Token.Eol)
        throw new InvalidExpressionException(String.format(ERR_MSG, Token.Eol, token));

      // check for an empty expression, such as '()'
      if (stack.isEmpty())
        return Tree.Empty;

      if (stack.size() != 1)
        throw new InvalidExpressionException("Invalid query: " + new String(buf));

      // the last Node in the stack should be the root of the expression tree
      // and the stack should be empty
      return stack.pop();
    }
    catch (final ArrayIndexOutOfBoundsException e)
    {
      throw new InvalidExpressionException("Expression incomplete", e);
    }
    catch (final StackOverflowError e)
    {
      throw new InvalidExpressionException("Expression too complex", e);
    }
  }

  private void expression() throws InvalidExpressionException
  {
    factor();

    if (token == Token.Or)
    {
      final Token o = token;
      final Tree  l = stack.pop();

      nextToken();
      expression();

      stack.push(new Tree(o, l, stack.pop()));
    }
  }

  private void factor() throws InvalidExpressionException
  {
    term();

    if (token == Token.And)
    {
      final Token o = token;
      final Tree  l = stack.pop();

      nextToken();
      factor();

      stack.push(new Tree(o, l, stack.pop()));
    }
  }

  private void term() throws InvalidExpressionException
  {
    if (token == Token.Not)
    {
      nextToken();

      if (token == Token.LBracket)
      {
        term();
        final Tree next = stack.pop();
        stack.push(new Tree(Token.Not, (Tree) null, next));
        return;
      }

      throw new InvalidExpressionException(String.format(ERR_MSG, Token.LBracket, token));
    }

    if (token == Token.LBracket)
    {
      nextToken();

      expression();

      if (token == Token.RBracket)
      {
        nextToken();
        return;
      }
      throw new InvalidExpressionException(String.format(ERR_MSG, Token.RBracket, token));
    }

    if (token.isField())
    {
      // save the last token
      final Token t = token;

      nextToken();

      if (token.isRelOp())
      {
        // save the operand token
        final Token op = token;

        nextToken();

        final Token value = token;

        if (value.isValue())
        {
          stack.push(new Tree(op, t, value));
          nextToken();
          return;
        }

        throw new InvalidExpressionException(String.format(ERR_MSG, "value", value));
      }

      if (token == Token.Exec)
      {
        // save the operand token
        final Token op = token;

        nextToken();

        expression();

        if (stack.isEmpty())
        {
          if (token == Token.Eol)
            throw new InvalidExpressionException(
              String.format(ERR_MSG, "expression or value", token));

          stack.push(new Tree(op, t, token));
          nextToken();
        }
        else
        {
          stack.push(new Tree(op, t, stack.pop()));
        }

        return;
      }

      throw new InvalidExpressionException(String.format(ERR_MSG, "operator", token));
    }
  }

  private void nextToken() throws InvalidExpressionException
  {
    token = readToken();
  }

  private Token readToken() throws InvalidExpressionException
  {
    while (++pos < buf.length)
    {
      final int ch = buf[pos];
      switch (ch)
      {
        case '<': // '<='
          return isEqualSign() ? Token.Le : Token.Lt;

        case '>': // '>='
          return isEqualSign() ? Token.Ge : Token.Gt;

        case '=': // '=='
        {
          isEqualSign();
          return Token.Eq;
        }

        case '!':
        {
          // Token.Not
          if (buf[++pos] == '=')
          {
            return Token.Ne;
          }

          while (buf[pos] != '(')
          {
            if (!Character.isWhitespace(buf[pos]))
              throw new IllegalCharacterException(buf[pos], pos);
            ++pos;
          }

          --pos; // backup one char
          return Token.Not;
        }

        case '(':
          return Token.LBracket;

        case ')':
          return Token.RBracket;

        case '\'': // single-quoted string
        case '"':  // double-quoted string
        {
          final String s = string();

          if (isField(token))
            return Token.field(s);

          if (token == Token.In || token == Token.NotIn)
            return new Token(Token.IPMASK, new Network(s));

          return Token.value(s);
        }

        case '`': // time value
          return readTimeValue();

        case '[': // set value
          return readSet();

        default:
          if (Character.isWhitespace(ch))
            break;

          return isDigit(ch) ? new Token(Token.NUM, number()) : token();
      }
    }

    return Token.Eol;
  }

  private static boolean isField(final Token token)
  {
    return token == null
           || token == Token.And
           || token == Token.Or
           || token == Token.Exec
           || token == Token.LBracket;
  }

  private boolean isEqualSign()
  {
    if (buf[pos + 1] == '=')
    {
      ++pos;
      return true;
    }
    return false;
  }

  private Token readSet() throws InvalidExpressionException
  {
    final Set<Object> values = new HashSet<>();

    while (++pos < buf.length)
    {
      pos = skip(buf, pos, buf.length - 1);
      switch (buf[pos])
      {
        case ',':
          break;

        case ']':
          return new Token(Token.SET, values);

        default:
          values.add(value());
          break;
      }
    }

    throw new InvalidExpressionException(String.format(ERR_MSG, "',' or ']'", Token.Eol));
  }

  private Object value() throws InvalidExpressionException
  {
    final int ch = buf[pos];
    switch (ch)
    {
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

  private static boolean isExtendedDigit(final int ch)
  {
    return isDigit(ch) || ch == 'E' || ch == 'e' || ch == 'x' || ch == 'X';
  }

  private Number number() throws InvalidExpressionException
  {
    int end = pos;
    while (end < buf.length && isExtendedDigit(buf[end])) ++end;

    try
    {
      return getNumber(buf, pos, end - pos);
    }
    catch (final NumberFormatException e)
    {
      throw syntaxError("Invalid number");
    }
    finally
    {
      pos = end - 1;
    }
  }

  private static Number getNumber(final char[] buf, final int offset, final int len)
  {
    if (buf[offset] == '0' && len > 2)
    {
      final int ch = buf[offset + 1];
      if (ch == 'x' || ch == 'X')
        return Integer.valueOf(new String(buf, offset + 2, len - 2), 16);
    }

    final String s = new String(buf, offset, len);
    if (s.indexOf('.') > -1 || s.indexOf('e') > -1 || s.indexOf('E') > -1)
      return Double.valueOf(s);

    return getNumber(s);
  }

  private static Number getNumber(final String s)
  {
    final long n = Long.parseLong(s, 10);
    final int  i = (int) n;

    // Don't use: return n == i ? Integer.valueOf(i):Long.valueOf(n);
    //noinspection RedundantIfStatement
    if (n == i)
      return i;

    return n;
  }


  /*
   * Skip all white-spaces, returns the position of first non-white-space char or 'len' if end of
   * the buffer is reached.
   */
  private static int skip(final char[] buf, final int pos, final int len)
  {
    for (int i = pos; i < len; ++i)
      if (!Character.isWhitespace(buf[i]))
        return i;

    return len;
  }

  private char getEscapeChar() throws InvalidExpressionException
  {
    final int ch = buf[++pos];
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
      case '"':
      case '\'':
      case '\\':
      case '/':
        return (char) ch;
      case 'u':
        return ((char) Integer.parseInt(getUnicode(), 16));

      default:
        throw syntaxError("Illegal escape");
    }
  }

  /*
   * Get the next 4 characters as string, containing a unicode number.
   *
   * @return A string of n characters.
   * @throws InvalidExpressionException if there are not n characters remaining in the source
   *                                    string.
   */
  private String getUnicode() throws InvalidExpressionException
  {
    final int offset = pos + 1; // skip the 'u'

    pos += 4;
    if (pos < buf.length) return new String(buf, offset, 4);

    throw syntaxError("Underflow error");
  }

  /*
   * Returns a symbol size, in number of chars, starting from the current position, or 0 if end of
   * the buffer is reached.
   */
  private static int getSymbolSize(final char[] buf, final int pos, final int len)
  {
    int size = 0;
    for (int i = pos; i < len && isSymbolChar(buf[i]); ++i)
         ++size;

    return size;
  }

  private static final char[] validChars = {
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, '#', '$', 0, 0, 0, 0,
    0, 0, 0, 0, '-', '.', 0, '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', 0, 0,
    0, 0, '?', '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
    'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 0, 0, 0, 0, '_', 0,
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
    'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
  };

  private static boolean isSymbolChar(final int ch)
  {
    return ch > 0 && ch < validChars.length && validChars[ch] != 0;
  }

  /*
   * Handle unquoted text. This could be the values true, false, or null
   */
  private Object symbol() throws InvalidExpressionException
  {
    switch (buf[pos])
    {
      case 'T':
      case 't':

      case 'N':
      case 'n':
        return symbol(4);

      case 'F':
      case 'f':
        return symbol(5);

      default:
        throw syntaxError("Unexpected symbol: " + buf[pos]);
    }
  }

  private Object symbol(final int len) throws InvalidExpressionException
  {
    final String s = new String(buf, pos, len);

    pos += len - 1;
    switch (s.toLowerCase(Locale.US))
    {
      case "true":
        return Boolean.TRUE;

      case "false":
        return Boolean.FALSE;

      case "null":
        return null;

      default:
        throw syntaxError("Unexpected symbol: " + s);
    }
  }

  /*
   * Handle unquoted text. This could be the values true, false, null, or field name
   */
  private Token token() throws InvalidExpressionException
  {
    final int size = getSymbolSize(buf, pos, buf.length);

    if (size == 0) throw syntaxError("Unexpected char");

    try
    {
      return stringToToken(new String(buf, pos, size));
    }
    finally
    {
      pos += size - 1;
    }
  }

  static Token stringToToken(final String s)
  {
    switch (s.toLowerCase(Locale.US))
    {
      case "true":
        return new Token(Token.BOOL, Boolean.TRUE);

      case "false":
        return new Token(Token.BOOL, Boolean.FALSE);

      case "null":
        return Token.Null;

      case "not":
        return Token.Not;

      case "or":
        return Token.Or;

      case "and":
        return Token.And;

      case "in":
        return Token.In;

      case "not_in":
        return Token.NotIn;

      case "like":
        return Token.Like;

      case "contains":
        return Token.Contains;

      case "match":
        return Token.Match;

      case "starts_with":
        return Token.StartsWith;

      case "ends_with":
        return Token.EndsWith;

      case "exec":
        return Token.Exec;

      case "is":
        return Token.IsNull;

      case "is_not":
        return Token.IsNotNull;

      default: // field name
        return Token.field(s);
    }
  }

  /*
   * Returns the characters up to the next close quote character. Backslash processing is done. The
   * formal JSON format does not allow strings in single quotes, but an implementation is allowed to
   * accept them.
   *
   * @return a string.
   * @throws InvalidExpressionException Unterminated string.
   */
  private String string() throws InvalidExpressionException
  {
    sb.setLength(0);
    try
    {
      int       ch;
      final int quote = buf[pos];

      while ((ch = (buf[++pos])) != quote)
      {
        if (ch == '\\')
          sb.append(getEscapeChar());
        else
          sb.append((char) ch);
      }

      return sb.toString();
    }
    catch (final ArrayIndexOutOfBoundsException e)
    {
      throw syntaxError("Unterminated string");
    }
  }

  private Token readTimeValue() throws InvalidExpressionException
  {
    final int start = pos + 1; // skip the quote
    int       ch;

    // read characters until we find end quote or EOL/EOF
    while ((ch = buf[++pos]) > 0)
    {
      if (ch == '`')
      {
        final int count = pos - start;
        if (count > 0)
        {
          final String text = new String(buf, start, count);
          try
          {
            return new Token(Token.DATE, Times.parse(text));
          }
          catch (final Exception e)
          {
            throw new InvalidExpressionException("Invalid date format: " + text);
          }
        }
        break;
      }
    }

    throw new IllegalCharacterException(ch, pos);
  }

  /*
   * Make a InvalidExpressionException to signal a syntax error.
   *
   * @param message The error message.
   * @return A InvalidExpressionException object, suitable for throwing
   */
  private InvalidExpressionException syntaxError(final String message)
  {
    return pos < buf.length ?
           new InvalidExpressionException(message + " at " + pos + ": '" + buf[pos] + '\'') :
           new InvalidExpressionException(message + " at the end");
  }
}
