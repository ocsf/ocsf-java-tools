
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

import java.util.Set;

@SuppressWarnings("WeakerAccess")
public class Token
{
  // keyword tokens
  public static final int NULL = 0xff;

  // relational operator mask
  private static final int ROP_MASK = 0x0f;

  // operator tokens: order is significant! Do not change the values. They are
  // used as index into an array. See Evaluator.Operator class.
  public static final int NE = 0x00;
  public static final int EQ = 0x01;
  public static final int LIKE = 0x02;
  public static final int NOT_LIKE = 0x03;
  public static final int GE = 0x04;
  public static final int GT = 0x05;
  public static final int LE = 0x06;
  public static final int LT = 0x07;
  public static final int MATCH = 0x08;
  public static final int NOT_MATCH = 0x09;
  public static final int STARTS_WITH = 0x0A;
  public static final int ENDS_WITH = 0x0B;
  public static final int IN = 0x0C;
  public static final int NOT_IN = 0x0D;
  public static final int IS_NULL = 0x0E;
  public static final int IS_NOT_NULL = 0x0F;

  public static final int AND = 0x10;
  public static final int OR = 0x11;
  public static final int NOT = 0x12;
  public static final int CONTAINS = 0x13;

  // type tokens
  public static final int NUM = 0x20;
  public static final int STRING = 0x21;
  public static final int DATE = 0x22;
  public static final int BOOL = 0x23;
  public static final int SET = 0x24; // a set of number, string, boolean, date
  public static final int IPMASK = 0x25; // for example, 192.168.1.0/24
  private static final int FIELD = 0x26;

  // other tokens
  private static final int EOL = 0x40;
  private static final int LBRACKET = 0x41;
  private static final int RBRACKET = 0x42;

  private static final class Field extends Token
  {
    private final String name;

    private Field(final String name)
    {
      super(name);
      this.name = name;
    }

    @Override
    public final String name() {return name;}
  }

  public static Token field(final String name) {return new Field(name);}

  public static final Token Null = new Token(Token.NULL, null);

  public static final Token And = new Token(Token.AND, "and");
  public static final Token Or = new Token(Token.OR, "or");
  public static final Token Not = new Token(Token.NOT, "!");

  public static final Token LBracket = new Token(Token.LBRACKET, "(");
  public static final Token RBracket = new Token(Token.RBRACKET, ")");

  public static final Token Eq = new Token(Token.EQ, "=");
  public static final Token Ne = new Token(Token.NE, "!=");
  public static final Token Like = new Token(Token.LIKE, "like");
  public static final Token NotLike = new Token(Token.NOT_LIKE, "not_like");

  public static final Token EndsWith = new Token(Token.ENDS_WITH, "ends_with");

  public static final Token StartsWith = new Token(Token.STARTS_WITH, "starts_with");

  public static final Token Ge = new Token(Token.GE, ">=");
  public static final Token Gt = new Token(Token.GT, ">");
  public static final Token Le = new Token(Token.LE, "<=");
  public static final Token Lt = new Token(Token.LT, "<");
  public static final Token Match = new Token(Token.MATCH, "match");
  public static final Token NotMatch = new Token(Token.NOT_MATCH, "not_match");
  public static final Token In = new Token(Token.IN, "in");
  public static final Token NotIn = new Token(Token.NOT_IN, "not_in");

  public static final Token Contains = new Token(Token.CONTAINS, "contains");

  public static final Token IsNull = new Token(Token.IS_NULL, "is");
  public static final Token IsNotNull = new Token(Token.IS_NOT_NULL, "is_not");

  public static final Token Eol = new Token(Token.EOL, "<eol>");


  public final int token;
  public final Object value;

  private Token(final String name)
  {
    this.token = FIELD;
    this.value = name;
  }

  public Token(final int token, final Object value)
  {
    this.token = token;
    this.value = value;
  }

  public final boolean isRelOp()
  {
    return (token & ~ROP_MASK) == 0;
  }

  public final boolean isValue()
  {
    return token == NULL || (token >= NUM && token < FIELD);
  }

  public final boolean isField()
  {
    return token == FIELD;
  }

  @SuppressWarnings("unchecked")
  public final <T> T value()
  {
    return (T) value;
  }

  @Override
  public final String toString()
  {
    switch (token)
    {
      case STRING:
        return quote(value.toString());
      case SET:
        return toString((Set<?>) value);
      default:
        return String.valueOf(value);
    }
  }

  public String name()
  {
    throw new Error("token " + this + " is not a field name");
  }

  private static final String toString(final Set<?> set)
  {
    final StringBuilder buf = new StringBuilder(16 * set.size());

    buf.append('[');

    for (final Object item : set)
      buf.append(toString(item)).append(',');

    buf.setCharAt(buf.length() - 1, ']');

    return buf.toString();
  }

  private static final String toString(final Object value)
  {
    return value instanceof String ? quote(value.toString()) : String.valueOf(value);
  }

  private static final String quote(final String s) {return Json.toString(s);}
}
