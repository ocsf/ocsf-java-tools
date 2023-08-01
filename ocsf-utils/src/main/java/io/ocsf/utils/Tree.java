
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

import java.util.Stack;

/**
 * A node in an expression tree.
 */
public final class Tree
{
  public static final Tree Empty = new Tree(Token.Eol);

  public final Token op;
  public final Tree  left, right;

  Tree(final Token o, final Tree l, final Tree r)
  {
    op    = o;
    left  = l;
    right = r;
  }

  Tree(final Token t, final Token f, final Tree r)
  {
    op    = t;
    left  = new Tree(f);
    right = r;
  }

  Tree(final Token t, final Token f, final Token v)
  {
    op    = t;
    left  = new Tree(f);
    right = new Tree(v);
  }

  // creates a leaf node
  Tree(final Token o)
  {
    op   = o;
    left = right = null;
  }

  public String asString()
  {
    if (this != Empty)
    {
      final StringBuilder sb    = new StringBuilder(256);
      final Stack<Tree>   stack = new Stack<>();

      stack.push(this);
      while (!stack.isEmpty())
      {
        final Tree t = stack.pop();
        if (t.op.isRelOp())
        {
          appendOpNode(sb, t.left).append(' ').append(t.op).append(' ');
          appendOpNode(sb, t.right).append(' ');
        }
        else
        {
          sb.append(t.op);
          // n.left may be null in case of unary operation such as Not
          if (t.left != null)
            stack.push(t.left);

          sb.append(' ');

          if (t.right != null)
            stack.push(t.right);
        }
      }
      return sb.toString();
    }

    return Strings.EMPTY;
  }

  @Override
  public String toString()
  {
    if (this != Tree.Empty)
    {
      final StringBuilder sb = new StringBuilder(1024);
      traverse(this, sb);
      return sb.toString();
    }

    return Boolean.TRUE.toString(); // an empty expression evaluates as 'true'
  }

  /*
   * Recursive traverse implementation that encloses logical expressions in brackets.
   */
  private static void traverse(final Tree n, final StringBuilder sb)
  {
    if (n == null) return;

    if (n.op.isRelOp())
    {
      // this is a leaf:
      appendOpNode(sb, n.left);
      sb.append(' ').append(n.op).append(' ');
      appendOpNode(sb, n.right);
    }
    else if (n.op == Token.Exec)
    {
      sb.append(n.left);
      sb.append(' ').append(n.op).append(' ');

      if (enclose(n, n.right))
      {
        sb.append('(');
        traverse(n.right, sb);
        sb.append(')');
      }
      else
        traverse(n.right, sb);
    }
    else
    {
      // n.left may be null in case of unary operation such as Not
      if (n.left != null)
      {
        if (enclose(n, n.left))
        {
          sb.append('(');
          traverse(n.left, sb);
          sb.append(')');
        }
        else
          traverse(n.left, sb);

        sb.append(' ');
      }

      sb.append(n.op);

      if (n.right != null)
      {
        sb.append(' ');

        if (enclose(n, n.right))
        {
          sb.append('(');
          traverse(n.right, sb);
          sb.append(')');
        }
        else
          traverse(n.right, sb);
      }
    }
  }

  private static StringBuilder appendOpNode(final StringBuilder sb, final Tree node)
  {
    if (node != null)
      sb.append(node.op);
    else
      sb.append("<nop>");

    return sb;
  }

  private static boolean enclose(final Tree parent, final Tree child)
  {
    // brackets always required after the not (!) operator
    if (parent.op == Token.Not)
      return true;

    // this is a leaf that is a whole expression, so the brackets are never needed
    if (child.op.isRelOp())
      return false;

    // let "(X && Y)" and "(X || Y)" always be in brackets for better clarity
    return child.op == Token.And || child.op == Token.Or;
  }

}
