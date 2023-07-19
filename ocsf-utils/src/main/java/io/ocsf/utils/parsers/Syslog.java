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

import java.util.Map;

/**
 * Syslog helper functions.
 */
public final class Syslog
{
  private Syslog() {}

  public static final String FACILITY = "facility";
  public static final String SEVERITY = "severity";

  public static final String RFC3164_TIME1 = "MMM dd HH:mm:ss";
  public static final String RFC3164_TIME2 = "MMM dd yyyy HH:mm:ss";

  private static final int RFC3164_TIME_POS = 9;

  private static final int RFC3164_TIME1_LEN = RFC3164_TIME1.length();
  private static final int RFC3164_TIME2_LEN = RFC3164_TIME2.length();

  public static Map<String, Object> decodePriority(
    final Integer priority, final Map<String, Object> data)
  {
    if (priority != null)
    {
      data.put(FACILITY, facility(priority));
      data.put(SEVERITY, severity(priority));
    }

    return data;
  }

  public static int[] decodePriority(final Integer priority)
  {
    return priority != null ? new int[]{facility(priority), severity(priority)} : null;
  }

  /**
   * Finds the length of the syslog time.
   *
   * <pre>
   * <strong>RFC3164</strong>
   * TIMESTAMP field is the local time, in the format of: Mmm dd hh:mm:ss
   *  Where:
   *    Mmm is the English language abbreviation for the month, one of:
   *       Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec
   *    dd is the day of the month
   *    hh:mm:ss is the local time. The hour (hh) is represented in a
   *       24-hour format.  Valid entries are between 00 and 23,
   *       inclusive.  The minute (mm) and second (ss) entries are between
   *       00 and 59 inclusive.
   *
   * <strong>RFC5424</strong>
   * TIMESTAMP field is derived from RFC3339 standard. Examples:
   *  1985-04-12T23:20:50.52Z
   *  1985-04-12T19:20:50.52-04:00
   *  2003-10-11T22:14:15.003Z
   * </pre>
   *
   * @param text the syslog line
   * @param pos  the time position in the text
   * @return the length of the time string, or a negative number if no valid time is found
   */
  public static int timeLength(final String text, final int pos)
  {
    if (pos < text.length())
    {
      final int first = text.charAt(pos);
      if (first > '0' && first <= '9')
      {
        // assumes RFC5424 time, find the next space to determine the length
        return text.indexOf(' ', pos) - pos;
      }
      else
      {
        final int t = text.indexOf(':', pos);

        // RFC3164: Mmm dd hh:mm:ss
        if (t - pos == RFC3164_TIME_POS)
        {
          return RFC3164_TIME1_LEN;
        }

        // RFC3164: Mmm dd yyyy hh:mm:ss
        return RFC3164_TIME2_LEN;
      }
    }

    return -1;
  }


  private static int facility(final int priority) {return priority >> 3;}

  private static int severity(final int priority) {return priority & 0x07;}
}
