
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

/**
 * An illegal or inappropriate character in a query expression.
 *
 * @author Roumen Roupski
 */
public class IllegalCharacterException extends InvalidExpressionException
{
  private static final long   serialVersionUID = 1L;
  private static final String Message          = "Illegal character (%s) at %d";

  /**
   * Constructs a new exception with the specified char and position.
   *
   * @param ch  the illegal character that caused the issue
   * @param pos the position of the illegal character
   */
  public IllegalCharacterException(final int ch, final int pos)
  {
    super(String.format(Message, ch > 0 ? Character.toString((char) ch) : "EOS", pos));
  }
}
