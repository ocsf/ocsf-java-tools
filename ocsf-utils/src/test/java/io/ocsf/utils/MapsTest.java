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

import io.ocsf.utils.parsers.Json5Parser;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class MapsTest
{
  private static final String OBJ =
    "{\"Process Information\": {\n" +
    "    \"Process ID\": \"0x0\",\n" +
    "    \"Process Name\": \"-\"\n" +
    "  }}";


  @Test
  public void getIn()
  {
    final Map<String, Object> data = Json5Parser.to(OBJ);
    Assert.assertEquals("0x0", Maps.getIn(data, "Process Information.Process ID"));
  }

  @Test
  public void moveIn()
  {
    final Map<String, Object> data = Json5Parser.to(OBJ);
    final Map<String, Object> dest = new HashMap<>();

    Maps.moveIn(data, "Process Information.Process ID", dest, "process.id");

    Assert.assertEquals(1, data.size());
    Assert.assertEquals(1, dest.size());

    Assert.assertNull(Maps.getIn(data, "Process Information.Process ID"));
    Assert.assertEquals("0x0", Maps.getIn(dest, "process.id"));
  }

  @Test
  public void cleanup()
  {
    final Map<String, Object> data = FMap.<String, Object>b().p("empty", FMap.b());

    Maps.cleanup(data);
    Assert.assertEquals(0, data.size());
  }

  @Test
  public void downcase()
  {
    final Map<String, Object> data = FMap.<String, Object>b()
                                         .p("Foo", 1)
                                         .p("BOO", FMap.<String, Object>b().p("Greeting", "hello"));

    final Map<String, Object> map = Maps.downcase(data);

    Assert.assertEquals(1, map.get("foo"));
    Assert.assertEquals("hello", Maps.getIn(map, "boo.greeting"));
  }
}