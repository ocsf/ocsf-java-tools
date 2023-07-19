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

public class JsonTest
{
  @Test
  public void encodeDouble()
  {
    // Check that double encoding does not use scientific notation
    final double v = 1681419369.997d;
    final String s = Json.toString(v);
    Assert.assertEquals("1681419369.997", s);
  }

  @Test
  public void encodeDoubleSmall()
  {
    // Check that double encoding does not use scientific notation
    final double v = 0.00000021d;
    final String s = Json.toString(v);
    Assert.assertEquals("0.00000021", s);
  }

  @Test
  public void encodeDoubleNoFraction()
  {
    // Check that double encoding does not use scientific notation
    final double v = 1681419280.0d;
    final String s = Json.toString(v);
    Assert.assertEquals("1681419280.0", s);
  }

  @Test
  public void encodeFloat()
  {
    // Check that double encoding does not use scientific notation
    final float  v = 16814.1f;
    final String s = Json.toString(v);
    Assert.assertEquals("16814.1", s);
  }

  @Test
  public void encodeFloatNoFraction()
  {
    // Check that double encoding does not use scientific notation
    final float  v = 1681410.0f;
    final String s = Json.toString(v);
    Assert.assertEquals("1681410.0", s);
  }

  @Test
  public void encodeFloatSmall()
  {
    // Check that double encoding does not use scientific notation
    final float  v = 0.00000021f;
    final String s = Json.toString(v);
    Assert.assertEquals("2.1E-7", s);
  }

  @Test
  public void encodeFloatTrailingZeros()
  {
    // Check that double encoding does not use scientific notation
    final float  v = 16_800_000.0f;
    final String s = Json.toString(v);
    Assert.assertEquals("1.68E7", s);
  }
}
