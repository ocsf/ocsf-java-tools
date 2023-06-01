/*
 * Copyright 2023 Open Cybersecurity Schema Framework
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

package io.ocsf.schema.parsers;

import io.ocsf.schema.util.Json;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public class InfobloxDhcpSyslogTest
{

  @Test
  public void parse() throws Exception
  {
    final InfobloxDHCPParser parser = new InfobloxDHCPParser();

    Arrays.stream(InfobloxDhcpSyslogData.Data).map((Function<String, Map<String, Object>>) s -> {
      try
      {
        return parser.parse(s);
      }
      catch (final Exception e)
      {
        Assert.fail(e.getMessage());
        return Collections.emptyMap();
      }
    }).forEach(map -> {
      Assert.assertNotNull(map);
      Assert.assertEquals(Json.format(map), 6, map.size());
    });
  }
}