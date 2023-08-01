
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

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Expression evaluator.
 */
public final class BooleanEvaluator
{
  private BooleanEvaluator() {}

  @SuppressWarnings("ConstantConditions")
  public static boolean evaluate(final Tree node, final Maps.Supplier<?> data)
  {
    // An empty expression evaluates to true
    if (node.op == Token.Eol)
      return true;

    if (node.op.isRelOp())
    {
      if (node.right != null)
        return evaluate(
          node.op.token, data.get(node.left.op.name()), node.right.op);
    }

    switch (node.op.token)
    {
      case Token.AND:
        return evaluate(node.left, data) && evaluate(node.right, data);

      case Token.OR:
        return evaluate(node.left, data) || evaluate(node.right, data);

      case Token.NOT:
        return !evaluate(node.right, data);

      case Token.EXEC:
        return exec(node.right, data.get(node.left.op.name()));
    }

    return false;
  }

  private static boolean evaluate(final int op, final Object field, final Token value)
  {
    return operations[op].evaluate(field, value);
  }

  @FunctionalInterface
  private interface Operator
  {
    boolean evaluate(final Object field, final Token value);
  }

  private static final Operator[] operations = {
    // NE
    (field, value) -> compare(field, value) != 0,
    // EQ
    (field, value) -> compare(field, value) == 0,
    // Like
    (field, value) -> {
      if (field == null)
        return value.token == Token.NULL;

      if (value.token == Token.STRING)
        return ((Token.Value) value).like(field.toString());

      return value.token != Token.NULL &&
             Strings.search(field.toString(), value.value().toString()) > -1;
    },
    // Contains
    (field, value) -> {
      if (field == null)
        return value.token == Token.NULL;

      return value.token != Token.NULL &&
             Strings.search(field.toString(), value.value().toString()) > -1;
    },
    // GE
    (field, value) -> compare(field, value) >= 0,
    // GT
    (field, value) -> compare(field, value) > 0,
    // LE
    (field, value) -> compare(field, value) <= 0,
    // LT
    (field, value) -> compare(field, value) < 0,
    // MATCH
    (field, value) ->
    {
      if (field == null)
        return value.token == Token.NULL;

      if (value.token == Token.STRING)
        return ((Token.Value) value).matches(field.toString());

      return compare(field, value) == 0;
    },
    // Starts With
    (field, value) -> {
      if (field == null)
        return value.token == Token.NULL;

      return value.token != Token.NULL &&
             Strings.startsWith(field.toString(), value.value().toString());
    },
    // Ends With
    (field, value) -> {
      if (field == null)
        return value.token == Token.NULL;

      return value.token != Token.NULL &&
             Strings.endsWith(field.toString(), value.value().toString());
    },
    // In
    (field, value) ->
    {
      switch (value.token)
      {
        case Token.SET:
        {
          final Set<Object> set = value.value();
          return set.contains(field);
        }

        case Token.IPMASK:
        {
          final Network mask = value.value();
          if (field instanceof String)
            return mask.hasMember((String) field);

          if (field instanceof Integer)
            return mask.hasMember((Integer) field);

          if (field instanceof Long)
            return mask.hasMember((Long) field);
        }
      }
      return compare(field, value) == 0;
    },
    // NotIn
    (field, value) ->
    {
      switch (value.token)
      {
        case Token.SET:
        {
          final Set<Object> set = value.value();
          return !set.contains(field);
        }

        case Token.IPMASK:
        {
          final Network mask = value.value();
          if (field instanceof String)
            return !mask.hasMember((String) field);

          if (field instanceof Integer)
            return !mask.hasMember((Integer) field);

          if (field instanceof Long)
            return !mask.hasMember((Long) field);
        }
      }

      return compare(field, value) != 0;
    },
    // IS_NULL
    (field, value) -> field == null,
    // IS_NOT_NULL
    (field, value) -> field != null
  };

  @SuppressWarnings("unchecked")
  static boolean exec(final Tree node, final Object data)
  {
    if (node.op.isValue())
    {
      if (data instanceof Collection<?>)
      {
        for (final Object datum : ((Collection<Object>) data))
          if (evaluate(Token.EQ, datum, node.op))
            return true;
      }
      else if (data == null)
      {
        return node.op.token == Token.NULL;
      }

      return Strings.search(data.toString(), String.valueOf(node.op.value)) > -1;
    }
    else if (data instanceof Collection<?>)
    {
      for (final Object datum : ((Collection<Object>) data))
        if (datum instanceof Map<?, ?> &&
            evaluate(node, key -> Maps.getIn((Map<String, Object>) datum, key)))
          return true;
    }
    else if (data instanceof Map<?, ?>)
    {
      return evaluate(node, key -> Maps.getIn((Map<String, Object>) data, key));
    }

    return false;
  }

  @SuppressWarnings("unchecked")
  private static int compare(final Object field, final Token value)
  {
    if (field == null)
      return value.token == Token.NULL ? 0 : -1;

    switch (value.token)
    {
      case Token.STRING:
        return field.toString().compareToIgnoreCase(value.value());

      case Token.NUM:
      {
        final Number num = value.value();
        if (field instanceof Integer)
          return Integer.compare(((Integer) field), num.intValue());

        if (field instanceof Long)
          return Long.compare(((Long) field), num.longValue());

        if (field instanceof Double)
          return Double.compare(((Double) field), num.doubleValue());

        if (field instanceof Date)
        {
          if (num instanceof Long)
            return Long.compare(((Date) field).getTime(), num.longValue());
        }

        if (field instanceof String)
        {
          try
          {
            if (num instanceof Integer)
              return Integer.compare(Integer.parseInt((String) field), (Integer) num);

            if (num instanceof Long)
              return Long.compare(Long.parseLong((String) field), (Long) num);

            if (num instanceof Double)
              return Double.compare(Double.parseDouble((String) field), (Double) num);
          }
          catch (final NumberFormatException e)
          {
            // invalid number
          }
        }
        break;
      }

      case Token.DATE:
      {
        final Long time = value.value();

        if (field instanceof String)
        {
          try
          {
            return Long.compare(Times.parseTime((String) field), time);
          }
          catch (final Exception e)
          {
            return -1;  // invalid date
          }
        }

        if (field instanceof Long)
          return Long.compare(((Long) field), time);

        if (field instanceof Date)
          return Long.compare(((Date) field).getTime(), time);

        break;
      }

      case Token.BOOL:
      {
        final Boolean b = value.value();
        if (field instanceof Boolean)
          return ((Boolean) field).compareTo(b);

        return Boolean.valueOf(field.toString()).compareTo(b);
      }

      case Token.IPMASK:
        break;

      case Token.NULL:
        return 1;
    }

    if (field.getClass() == value.getClass() && field instanceof Comparable<?>)
      return ((Comparable<Object>) field).compareTo(value);

    return field.toString().compareToIgnoreCase(value.toString());
  }

}
