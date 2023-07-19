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

package io.ocsf.utils;

import io.ocsf.utils.parsers.Syslog;
import org.junit.Assert;
import org.junit.Test;

public class SyslogTest
{

  @Test
  public void decodePriority()
  {
    final FMap<String, Object> data = FMap.b();

    Syslog.decodePriority(165, data);

    Assert.assertEquals(2, data.size());
    Assert.assertEquals(165, severity(data.get(Syslog.FACILITY), data.get(Syslog.SEVERITY)));
  }

  @Test
  public void decodeIntPriority()
  {
    final int[] pair = Syslog.decodePriority(165);

    Assert.assertNotNull(pair);
    Assert.assertEquals(2, pair.length);
    Assert.assertEquals(165, severity(pair[0], pair[1]));
  }

  private static int severity(final int facility, final int severity)
  {
    return facility * 8 + severity;
  }

  private static int severity(final Object facility, final Object severity)
  {
    Assert.assertNotNull(facility);
    Assert.assertNotNull(severity);

    return severity(((Integer) facility).intValue(), ((Integer) severity).intValue());
  }

}