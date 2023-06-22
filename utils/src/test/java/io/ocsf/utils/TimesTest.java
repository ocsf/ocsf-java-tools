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

import org.junit.Assert;
import org.junit.Test;

public class TimesTest
{
  @Test
  public void parseTime()
  {
    final long t = Times.parseTime("12/03/2021 10:54:37 PM");
    Assert.assertTrue(t > 0);
  }

  @Test
  public void parse()
  {
    final long t = Times.parse("+1h");
    Assert.assertTrue(t > 0);
  }
}
