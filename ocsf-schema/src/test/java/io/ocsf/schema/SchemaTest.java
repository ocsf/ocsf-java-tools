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

import io.ocsf.utils.FMap;
import io.ocsf.utils.Maps;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SchemaTest
{
  private static Schema schema = null;

  public static final String DISPOSITION    = "disposition";
  public static final String DISPOSITION_ID = "disposition_id";

  private static final int TEST_DISPOSITION_ID = 1; // Blocked

  @BeforeClass
  public static void setUp()
  {
    schema = new Schema(Paths.get("build/schema.json"), true, true);
  }

  @Test
  public void testGetClass()
  {
    final Optional<Map<String, Object>> type = schema.getClass(Data.TEST_CLASS_ID);

    Assert.assertTrue(type.isPresent());
    Assert.assertEquals(Data.TEST_CLASS_ID, type.get().get(Dictionary.UID));
  }

  @Test
  public void getObject()
  {
    final Optional<Map<String, Object>> process = schema.getObject("process");

    Assert.assertTrue(process.isPresent());
    Assert.assertFalse(process.get().isEmpty());
  }

  @Test
  public void testEnrichClass()
  {
    final FMap<String, Object> data = FMap.<String, Object>b()
                                          .p(Dictionary.CLASS_UID, Data.TEST_CLASS_ID)
                                          .p(Dictionary.ACTIVITY_ID, Data.TEST_ACTIVITY_ID);

    final Map<String, Object> enriched = schema.enrich(data, false, false);

    // should add type_uid to both data and enriched, but not activity_name and class_name
    Assert.assertEquals(data.size(), enriched.size());
  }

  @Test
  public void enrichInvalidClassId()
  {
    final FMap<String, Object> data = FMap.<String, Object>b()
                                          .p(Dictionary.CLASS_UID, 666)
                                          .p(Dictionary.ACTIVITY_ID, 22);

    final Map<String, Object> enriched = schema.enrich(data);

    Assert.assertEquals(data.size(), enriched.size());
  }

  @Test
  public void enrichActivity()
  {
    final FMap<String, Object> data = FMap.<String, Object>b()
                                          .p(Dictionary.CLASS_UID, Data.TEST_CLASS_ID)
                                          .p(Dictionary.ACTIVITY_ID, Data.TEST_ACTIVITY_ID);

    final Map<String, Object> enriched = schema.enrich(data);

    Assert.assertEquals(data.size() + 3, enriched.size());
    Assert.assertNotNull(enriched.get(Dictionary.CLASS_NAME));
    Assert.assertNotNull(enriched.get(Dictionary.ACTIVITY_NAME));
    Assert.assertNotNull(enriched.get(Dictionary.TYPE_NAME));
    Assert.assertEquals(
      Utils.typeUid(Data.TEST_CLASS_ID, Data.TEST_ACTIVITY_ID),
      enriched.get(Dictionary.TYPE_UID));
  }

  @Test
  public void enrichActivityWithDisposition()
  {
    final FMap<String, Object> data = FMap.<String, Object>b()
                                          .p(Dictionary.CLASS_UID, Data.TEST_CLASS_ID)
                                          .p(Dictionary.ACTIVITY_ID, Data.TEST_ACTIVITY_ID)
                                          .p(DISPOSITION_ID, TEST_DISPOSITION_ID);

    final Map<String, Object> enriched = schema.enrich(data);

    Assert.assertEquals(data.size() + 4, enriched.size());
    Assert.assertNotNull(enriched.get(Dictionary.CLASS_NAME));
    Assert.assertNotNull(enriched.get(Dictionary.ACTIVITY_NAME));
    Assert.assertNotNull(enriched.get(Dictionary.TYPE_UID));
    Assert.assertNotNull(enriched.get(DISPOSITION));
    Assert.assertEquals(
      Utils.typeUid(Data.TEST_CLASS_ID, Data.TEST_ACTIVITY_ID),
      enriched.get(Dictionary.TYPE_UID));
  }

  @Test
  public void enrichEnumInvalid()
  {
    final FMap<String, Object> data = FMap.<String, Object>b()
                                          .p(Dictionary.CLASS_UID, Data.TEST_CLASS_ID)
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
                                          .p(Dictionary.CLASS_UID, Data.TEST_CLASS_ID)
                                          .p(Dictionary.ACTIVITY_ID, 99)
                                          .p(Dictionary.ACTIVITY_NAME, activityName);

    final Map<String, Object> enriched = schema.enrich(data);

    Assert.assertEquals(data.size() + 2, enriched.size());
    Assert.assertNotNull(enriched.get(Dictionary.CLASS_NAME));
    Assert.assertNotNull(enriched.get(Dictionary.TYPE_UID));
    Assert.assertEquals(activityName, enriched.get(Dictionary.ACTIVITY_NAME));
  }

  @Test
  public void enrichEmbeddedObject1()
  {
    final FMap<String, Object> data = FMap.<String, Object>b()
                                          .p(Dictionary.CLASS_UID, Data.TEST_CLASS_ID)
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
  public void enrichEmbeddedObject2()
  {
    final FMap<String, Object> data = FMap.<String, Object>b()
                                          .p(Dictionary.CLASS_UID, Data.TEST_CLASS_ID)
                                          .p(Dictionary.ACTIVITY_ID, 1)
                                          .p("device", FMap.<String, Object>b()
                                                           .p("os", FMap.<String, Object>b()
                                                                        .p("name", "Manjaro")
                                                                        .p("type_id", 200)) // Linux
                                                           .p("type_id", 3) // Laptop
                                          );

    final Map<String, Object> enriched = schema.enrich(data);

    Assert.assertEquals(data.size() + 4, enriched.size());
    Assert.assertNotNull(enriched.get(Dictionary.CLASS_NAME));
    Assert.assertNotNull(enriched.get(Dictionary.ACTIVITY_NAME));
    Assert.assertNotNull(enriched.get(Dictionary.TYPE_UID));
    Assert.assertNotNull(enriched.get(Dictionary.TYPE_NAME));

    Assert.assertEquals("Laptop", Maps.getIn(enriched, "device.type"));
    Assert.assertEquals("Linux", Maps.getIn(enriched, "device.os.type"));
  }

  @Test
  public void enrichEmbeddedBadObject()
  {
    final FMap<String, Object> data = FMap.<String, Object>b()
                                          .p(Dictionary.CLASS_UID, Data.TEST_CLASS_ID)
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
  public void invalidObjectAttribute()
  {
    final FMap<String, Object> data = FMap.<String, Object>b()
                                          .p(Dictionary.CLASS_UID, Data.TEST_CLASS_ID)
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
                                          .p(Dictionary.CLASS_UID, Data.TEST_CLASS_ID)
                                          .p(Dictionary.ACTIVITY_ID, 1)
                                          .p("device", FMap.<String, Object>b()
                                                           .p("hostname", "example.com")
                                                           .p(
                                                             "network_interfaces",
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

    final List<Map<String, Object>> array = Maps.typecast(Maps.getIn(enriched, "device" +
                                                                               ".network_interfaces"));
    Assert.assertNotNull(array);
    Assert.assertEquals(1, array.size());

    final Map<String, Object> ni = array.get(0);
    Assert.assertEquals("Mobile", ni.get("type"));
  }
}