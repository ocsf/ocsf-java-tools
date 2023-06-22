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

package io.ocsf.schema;

import io.ocsf.schema.util.FMap;
import io.ocsf.schema.util.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SchemaTest
{
  private Schema schema;
  private static final int TEST_CLASS_ID = 1001; // File System Activity
  private static final int TEST_ACTIVITY_ID = 3; // Update
  private static final int TEST_DISPOSITION_ID = 1; // Blocked

  @Before
  public void setUp()
  {
    schema = new Schema(Paths.get("build/schema.json"), true);
  }

  @Test
  public void enrichInvalidClassId()
  {
    final FMap<String, Object> data = FMap.<String, Object>b()
        .p(Dictionary.CLASS_ID, 10)
        .p(Dictionary.ACTIVITY_ID, 22);

    final Map<String, Object> enriched = schema.enrich(data);

    Assert.assertEquals(data.size(), enriched.size());
  }

  @Test
  public void enrichActivity()
  {
    final FMap<String, Object> data = FMap.<String, Object>b()
        .p(Dictionary.CLASS_ID, TEST_CLASS_ID)
        .p(Dictionary.ACTIVITY_ID, TEST_ACTIVITY_ID);

    final Map<String, Object> enriched = schema.enrich(data);

    Assert.assertEquals(data.size() + 3, enriched.size());
    Assert.assertNotNull(enriched.get(Dictionary.CLASS_NAME));
    Assert.assertNotNull(enriched.get(Dictionary.ACTIVITY_NAME));
    Assert.assertNotNull(enriched.get(Dictionary.TYPE_NAME));
    Assert.assertEquals(Schema.makeEventUid(TEST_CLASS_ID, TEST_ACTIVITY_ID), enriched.get(Dictionary.TYPE_UID));
  }

  @Test
  public void enrichActivityWithDisposition()
  {
    final FMap<String, Object> data = FMap.<String, Object>b()
        .p(Dictionary.CLASS_ID, TEST_CLASS_ID)
        .p(Dictionary.ACTIVITY_ID, TEST_ACTIVITY_ID)
        .p(Dictionary.DISPOSITION_ID, TEST_DISPOSITION_ID);

    final Map<String, Object> enriched = schema.enrich(data);

    Assert.assertEquals(data.size() + 4, enriched.size());
    Assert.assertNotNull(enriched.get(Dictionary.CLASS_NAME));
    Assert.assertNotNull(enriched.get(Dictionary.ACTIVITY_NAME));
    Assert.assertNotNull(enriched.get(Dictionary.DISPOSITION));
    Assert.assertNotNull(enriched.get(Dictionary.TYPE_UID));
    Assert.assertEquals(Schema.makeEventUid(TEST_CLASS_ID, TEST_ACTIVITY_ID), enriched.get(Dictionary.TYPE_UID));
  }

  @Test
  public void enrichEnumInvalid()
  {
    final FMap<String, Object> data = FMap.<String, Object>b()
      .p(Dictionary.CLASS_ID, TEST_CLASS_ID)
      .p(Dictionary.ACTIVITY_ID, 22);

    final Map<String, Object> enriched = schema.enrich(data);

    Assert.assertEquals(data.size() + 1, enriched.size());
    Assert.assertNotNull(enriched.get(Dictionary.CLASS_NAME));
    Assert.assertNotNull(enriched.get(Dictionary.TYPE_UID));
    Assert.assertNull(enriched.get(Dictionary.ACTIVITY_NAME));
  }

  @Test
  public void enrichEnumOther()
  {
    final String activityName = "Something Else";

    final FMap<String, Object> data = FMap.<String, Object>b()
      .p(Dictionary.CLASS_ID, TEST_CLASS_ID)
      .p(Dictionary.ACTIVITY_ID, 99)
      .p(Dictionary.ACTIVITY_NAME, activityName);

    final Map<String, Object> enriched = schema.enrich(data);

    Assert.assertEquals(data.size() + 2, enriched.size());
    Assert.assertNotNull(enriched.get(Dictionary.CLASS_NAME));
    Assert.assertNotNull(enriched.get(Dictionary.TYPE_UID));
    Assert.assertEquals(activityName, enriched.get(Dictionary.ACTIVITY_NAME));
  }

  @Test
  public void enrichEmbeddedObject()
  {
    final FMap<String, Object> data = FMap.<String, Object>b()
        .p(Dictionary.CLASS_ID, TEST_CLASS_ID)
        .p(Dictionary.ACTIVITY_ID, 1)
        .p("device", FMap.<String, Object>b()
            .p("hostname", "Laptop.local")
            .p("type_id", 3) // Laptop
        );

    final Map<String, Object> enriched = schema.enrich(data);

    Assert.assertEquals(data.size() + 4, enriched.size());
    Assert.assertNotNull(enriched.get(Dictionary.CLASS_NAME));
    Assert.assertNotNull(enriched.get(Dictionary.ACTIVITY_NAME));
    Assert.assertNotNull(enriched.get(Dictionary.TYPE_UID));
    Assert.assertNotNull(enriched.get(Dictionary.TYPE_NAME));

    Assert.assertEquals("Laptop", Maps.getIn(enriched, "device.type"));
  }

  @Test
  public void enrichEmbeddedObjects()
  {
    final FMap<String, Object> data = FMap.<String, Object>b()
      .p(Dictionary.CLASS_ID, TEST_CLASS_ID)
      .p(Dictionary.ACTIVITY_ID, 1)
      .p("device", FMap.<String, Object>b()
        .p("hostname", FMap.<String, Object>b()
          .p("name", "Laptop")
          .p("type_id", -1))
        .p("type_id", 3) // Laptop
      );

    final Map<String, Object> enriched = schema.enrich(data);

    Assert.assertEquals(data.size() + 4, enriched.size());
    Assert.assertNotNull(enriched.get(Dictionary.CLASS_NAME));
    Assert.assertNotNull(enriched.get(Dictionary.ACTIVITY_NAME));
    Assert.assertNotNull(enriched.get(Dictionary.TYPE_UID));
    Assert.assertNotNull(enriched.get(Dictionary.TYPE_NAME));

    Assert.assertEquals("Laptop", Maps.getIn(enriched, "device.type"));
  }

  @Test
  public void enrichEmbeddedObject0()
  {
    final FMap<String, Object> data = FMap.<String, Object>b()
      .p(Dictionary.CLASS_ID, TEST_CLASS_ID)
      .p(Dictionary.ACTIVITY_ID, 1)
      .p("device", FMap.<String, Object>b()
        .p("image", FMap.<String, Object>b()
          .p("name", FMap.<String, Object>b()
            .p("name", "Laptop")
            .p("type_id", -1))
        .p("type_id", -1))
      .p("type_id", 3) // Laptop
      );

    final Map<String, Object> enriched = schema.enrich(data);

    Assert.assertEquals(data.size() + 4, enriched.size());
    Assert.assertNotNull(enriched.get(Dictionary.CLASS_NAME));
    Assert.assertNotNull(enriched.get(Dictionary.ACTIVITY_NAME));
    Assert.assertNotNull(enriched.get(Dictionary.TYPE_UID));
    Assert.assertNotNull(enriched.get(Dictionary.TYPE_NAME));

    Assert.assertEquals("Laptop", Maps.getIn(enriched, "device.type"));
  }

  @Test
  public void invalidObjectAttribute()
  {
    final FMap<String, Object> data = FMap.<String, Object>b()
        .p(Dictionary.CLASS_ID, TEST_CLASS_ID)
        .p(Dictionary.ACTIVITY_ID, 1)
        .p("app_name", FMap.<String, Object>b()
            .p("opcode", 1)
            .p("hostname", "example.com"));

    final Map<String, Object> enriched = schema.enrich(data);

    Assert.assertEquals(data.size() + 3, enriched.size());
    Assert.assertNotNull(enriched.get(Dictionary.CLASS_NAME));
    Assert.assertNotNull(enriched.get(Dictionary.ACTIVITY_NAME));
    Assert.assertNotNull(enriched.get(Dictionary.TYPE_UID));
    Assert.assertNotNull(enriched.get(Dictionary.TYPE_NAME));
  }

  @Test
  public void enrichArray()
  {
    final FMap<String, Object> data = FMap.<String, Object>b()
        .p(Dictionary.CLASS_ID, TEST_CLASS_ID)
        .p(Dictionary.ACTIVITY_ID, 1)
        .p("device", FMap.<String, Object>b()
            .p("hostname", "example.com")
            .p("network_interfaces",
               Collections.singletonList(
                   FMap.<String, Object>b()
                       .p("type_id", 3)  // Mobile
      )));

    final Map<String, Object> enriched = schema.enrich(data);

    Assert.assertEquals(data.size() + 4, enriched.size());
    Assert.assertNotNull(enriched.get(Dictionary.CLASS_NAME));
    Assert.assertNotNull(enriched.get(Dictionary.ACTIVITY_NAME));
    Assert.assertNotNull(enriched.get(Dictionary.TYPE_UID));
    Assert.assertNotNull(enriched.get(Dictionary.TYPE_NAME));

    final List<Map<String, Object>> array = Maps.typecast(Maps.getIn(enriched, "device.network_interfaces"));
    Assert.assertNotNull(array);
    Assert.assertEquals(1, array.size());

    final Map<String, Object> ni = array.get(0);
    Assert.assertEquals("Mobile", ni.get("type"));
  }
}