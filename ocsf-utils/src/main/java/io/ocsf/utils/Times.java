
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.concurrent.TimeUnit;

/**
 * Time related helper functions.
 */
public final class Times
{
  private static final String            ISO8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
  private static final DateTimeFormatter formatter           =
    DateTimeFormatter.ofPattern(ISO8601_DATE_FORMAT);

  private static final int Iso8601Marker = 10; // position of the time marker 'T'

  private static final String            ZONED_FORMAT = "MM/dd/yy HH:mm:ss z";
  private static final DateTimeFormatter zoned_df     = DateTimeFormatter.ofPattern(ZONED_FORMAT);

  private static final String            LOCAL_FORMAT   = "MM/dd/yy HH:mm:ss";
  private static final DateTimeFormatter local_df       = DateTimeFormatter.ofPattern(LOCAL_FORMAT);
  private static final int               local_date_len = LOCAL_FORMAT.length();

  private static final String            LOCAL_12H_FORMAT   = "MM/dd/yyyy hh:mm:ss a";
  private static final DateTimeFormatter local_12h_df       =
    DateTimeFormatter.ofPattern(LOCAL_12H_FORMAT);
  private static final int               local_12h_date_len = LOCAL_12H_FORMAT.length() + 1;

  private Times() {}

  public static String currentIso8601Time()
  {
    return toIso8601String(OffsetDateTime.now());
  }

  public static String toIso8601String(final long time)
  {
    return toIso8601String(accessor(time));
  }

  public static String toIso8601String(final TemporalAccessor date)
  {
    return formatter.format(date);
  }

  public static long parseTime(final String value)
  {
    if (value.length() > Iso8601Marker && value.charAt(Iso8601Marker) == 'T')
      return Instant.from(parseBest(value)).toEpochMilli();

    return parseLocalTime(value);
  }

  public static long parse(final String arg)
  {
    if (arg.isEmpty()) return System.currentTimeMillis();

    switch (arg.charAt(0))
    {
      case '+':
        return System.currentTimeMillis() + parseTimeUnits(arg.substring(1));
      case '-':
        return System.currentTimeMillis() - parseTimeUnits(arg.substring(1));
      default:
        return Times.parseTime(arg);
    }
  }

  private static TemporalAccessor parseBest(final String text)
  {
    return DateTimeFormatter.ISO_DATE_TIME.parseBest(
      text, ZonedDateTime::from, LocalDateTime::from);
  }

  private static long parseTimeUnits(final String arg)
  {
    switch (arg.charAt(arg.length() - 1))
    {
      case 'M':
      case 'm':
        return TimeUnit.MINUTES.toMillis(parseLong(arg));
      case 'H':
      case 'h':
        return TimeUnit.HOURS.toMillis(parseLong(arg));
      case 'D':
      case 'd':
        return TimeUnit.DAYS.toMillis(parseLong(arg));
      default:
        return Long.parseLong(arg); // ms
    }
  }

  private static long parseLong(final String arg)
  {
    return Long.parseLong(arg.substring(0, arg.length() - 1));
  }

  private static long parseLocalTime(final String value)
  {
    final int len = value.length();
    if (len <= local_date_len)
    {
      final int pos = value.indexOf('.');
      if (pos > 0)
      {
        final String time = value.substring(0, pos);
        final String fraq = value.substring(pos + 1);

        return Long.parseLong(time) * 1000L + Integer.parseInt(fraq);
      }

      if (len < local_date_len)
        return Long.parseLong(value);
    }

    if (len == local_date_len)
      return Instant.from(local_df.parse(value, LocalDateTime::from).atZone(ZoneId.systemDefault()))
                    .toEpochMilli();

    if (len == local_12h_date_len)
      return Instant
        .from(local_12h_df.parse(value, LocalDateTime::from).atZone(ZoneId.systemDefault()))
        .toEpochMilli();

    return Instant.from(zoned_df.parse(value, ZonedDateTime::from)).toEpochMilli();
  }

  private static TemporalAccessor accessor(final long time)
  {
    return ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
  }

}
