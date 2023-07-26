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

package io.ocsf.parsers;

import io.ocsf.utils.Json;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public class InfobloxSyslogParserTest
{

  @Test
  public void parse()
  {
    final InfobloxSyslogParser parser = new InfobloxSyslogParser();

    Arrays.stream(InfobloxSyslogData.Data).map((Function<String, Map<String, Object>>) s -> {
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
      Assert.assertEquals(Json.format(map), 7, map.size());
    });
  }
}