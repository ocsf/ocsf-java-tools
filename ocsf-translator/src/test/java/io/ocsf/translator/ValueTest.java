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

package io.ocsf.translator;

import io.ocsf.utils.Maps;
import io.ocsf.utils.parsers.Json5Parser;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ValueTest
{
  private static final String CreateEventShortRule =
    "{" +
    "  \"description\": \"Create a simple OCSF event.\"," +
    "  \"references\": [" +
    "    \"https://schema.ocsf.io/1.0.0-rc.3/base_event\"" +
    "  ]," +
    "  \"rules\": [" +
    "    {" +
    "      \"_\": {" +
    "        \"category_uid\": 0," +
    "        \"class_uid\": 0," +
    "        \"activity_id\": 0," +
    "        \"message\": \"A simple OCSF event created by the OCSF Translator Tool\"," +
    "        \"metadata\": {" +
    "          \"product\": {" +
    "            \"lang\": \"en\"," +
    "            \"name\": \"OCSF Translator Tool\"," +
    "            \"uid\": \"521d0c88-2721-11ee-952e-0242ac110003\"," +
    "            \"vendor_name\": \"ocsf.io\"," +
    "            \"version\": \"1.0.0-dev\"" +
    "          }," +
    "          \"profiles\": []," +
    "          \"version\": \"1.0.0-rc.3\"" +
    "        }," +
    "        \"severity_id\": 1," +
    "        \"status_id\": 1," +
    "        \"time\": 1689873456010477," +
    "        \"timezone_offset\": 0" +
    "      }" +
    "    }" +
    "  ]" +
    "}";

  @Test
  public void createEventShort()
  {
    try
    {
      final Map<String, Object> translated = TranslatorBuilder
        .fromString(CreateEventShortRule)
        .apply(new HashMap<>());

      Assert.assertEquals(9, translated.size());
      Assert.assertEquals(0, Maps.getIn(translated, "category_uid"));
      Assert.assertEquals(
        "521d0c88-2721-11ee-952e-0242ac110003", Maps.getIn(translated, "metadata.product.uid"));
    }
    catch (final Exception e)
    {
      Assert.fail(e.getMessage());
    }
  }

  private static final String CreateEventLongRule =
    "{" +
    "  \"description\": \"Create a simple OCSF event.\"," +
    "  \"references\": [" +
    "    \"https://schema.ocsf.io/1.0.0-rc.3/base_event\"" +
    "  ]," +
    "  \"rules\": [" +
    "    {" +
    "      \"_\": {" +
    "        \"description\": \"An example demonstrating how to create a basic event using the " +
    "translation rules.\"," +
    "        \"@value\": {" +
    "          \"activity_id\": 0," +
    "          \"category_uid\": 0," +
    "          \"class_uid\": 0," +
    "          \"message\": \"A simple OCSF event created by the OCSF Translator Tool\"," +
    "          \"metadata\": {" +
    "            \"product\": {" +
    "              \"lang\": \"en\"," +
    "              \"name\": \"OCSF Translator Tool\"," +
    "              \"uid\": \"521d0c88-2721-11ee-952e-0242ac110003\"," +
    "              \"vendor_name\": \"ocsf.io\"," +
    "              \"version\": \"1.0.0-dev\"" +
    "            }," +
    "            \"profiles\": []," +
    "            \"version\": \"1.0.0-rc.3\"" +
    "          }," +
    "          \"severity_id\": 1," +
    "          \"status_id\": 1," +
    "          \"time\": 1689873456010477," +
    "          \"timezone_offset\": 0" +
    "        }" +
    "      }" +
    "    }" +
    "  ]" +
    "}";

  @Test
  public void createEventLong()
  {
    try
    {
      final Map<String, Object> translated = TranslatorBuilder
        .fromString(CreateEventLongRule)
        .apply(new HashMap<>());

      Assert.assertEquals(9, translated.size());
      Assert.assertEquals(0, Maps.getIn(translated, "category_uid"));
      Assert.assertEquals(
        "521d0c88-2721-11ee-952e-0242ac110003", Maps.getIn(translated, "metadata.product.uid"));
    }
    catch (final Exception e)
    {
      Assert.fail(e.getMessage());
    }
  }

  private static final String Data =
    "{" +
    "  \"severity\": 5," +
    "  \"product\": \"ASA\"," +
    "  \"code\": 111010," +
    "  \"level\": 5," +
    "  \"host\": \"10.160.0.10\"," +
    "  \"message\": \"User 'admin', running 'CLI' from IP 0.0.0.0, executed 'dir disk0:/dap" +
    ".xml'\"," +
    "  \"facility\": 20," +
    "  \"timestamp\": \"Oct 06 2021 15:02:30\"" +
    "}";

  private static final String Rule =
    "{" +
    "  \"description\": \"Add OCSF metadata to the event.\"," +
    "  \"references\": [" +
    "    \"https://schema.ocsf.io/1.0.0-rc.3/objects/metadata\"" +
    "  ]," +
    "  \"when\": \"code = 111010\"," +
    "  \"parser\": {" +
    "    \"name\": \"message\"," +
    "    \"pattern\": \"User '#{username}', running '#{application}' from IP #{ip_addr}, executed" +
    " '#{cmd}'\"," +
    "    \"output\": \"data\"" +
    "  }," +
    "  \"rules\": [" +
    "    {" +
    "      \"_\": {" +
    "        \"metadata\": {" +
    "          \"profiles\": []," +
    "          \"version\": \"1.0.0-rc.3\"" +
    "        }" +
    "      }" +
    "    }," +
    "    {" +
    "      \"host\": {" +
    "        \"@move\": \"origin.device.ip\"" +
    "      }" +
    "    }," +
    "    {" +
    "      \"data.username\": {" +
    "        \"@move\": \"user.name\"" +
    "      }" +
    "    }" +
    "  ]" +
    "}";

  @Test
  public void addMetadata()
  {
    try
    {
      final Map<String, Object> data = Json5Parser.to(Data);
      final Map<String, Object> translated = TranslatorBuilder
        .fromString(Rule)
        .apply(data);

      Assert.assertEquals(3, translated.size());
      Assert.assertEquals("10.160.0.10", Maps.getIn(translated, "origin.device.ip"));
      Assert.assertEquals("admin", Maps.getIn(translated, "user.name"));
      Assert.assertEquals("1.0.0-rc.3", Maps.getIn(translated, "metadata.version"));
    }
    catch (final Exception e)
    {
      Assert.fail(e.getMessage());
    }
  }
}