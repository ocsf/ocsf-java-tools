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
package io.ocsf.translator.svc;

/**
 * This exception indicates that the OCSF translator did not translate the raw event.
 */
public class TranslatorException extends Exception
{
  public enum Reason
  {
    MissingSourceType("Missing sourceType field"),
    MissingRawData("Missing rawEvent field"),
    NoParser("No event parser"),
    NoTranslator("No event translator"),
    ParserError("unable to parse the event"),
    TranslatorError("unable to translate the event"),
    UnsupportedEvent("Unsupported event");

    private final String desc;

    Reason(final String desc)
    {
      this.desc = desc;
    }

    @Override
    public String toString()
    {
      return "Reason: " + desc;
    }
  }

  /**
   * Returns the reason to throw this exception.
   *
   * @return the reason to throw this exception
   */
  public Reason getReason() {return reason;}

  private final Reason reason;

  /**
   * Constructs a new exception with the specified detail message and a reason.
   *
   * @param reason the reason to throw this exception
   */
  public TranslatorException(final Reason reason)
  {
    this.reason = reason;
  }

  /**
   * Constructs a new exception with the specified detail message, reason, and cause.
   *
   * @param reason the reason to throw this exception
   * @param cause  the cause (which is saved for later retrieval by the {@link #getCause()} method).
   *               (A {@code null} value is permitted, and indicates that the cause is nonexistent
   *               or unknown.)
   */
  public TranslatorException(final Reason reason, final Throwable cause)
  {
    super(cause);
    this.reason = reason;
  }

  /**
   * To minimize the overhead of creating exception.
   *
   * @return a reference to this {@code Throwable} instance.
   * @see Throwable#printStackTrace()
   */
  @Override
  public Throwable fillInStackTrace() {return this;}

  @Override
  public String getMessage()
  {
    return reason.toString();
  }
}
