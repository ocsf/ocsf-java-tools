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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SchemaAccessTest
{
  private static final int TEST_CLASS_ID = 1001; // File System Activity

  private Schema schema;

  @Before
  public void setUp()
  {
    schema = new Schema(Paths.get("build/schema.json"), true);
  }

  @Test
  public void testGetClass()
  {
    final Optional<Map<String, Object>> type = schema.getClass(TEST_CLASS_ID);

    Assert.assertTrue(type.isPresent());
    Assert.assertEquals(TEST_CLASS_ID, type.get().get(Dictionary.UID));
  }

  @Test
  public void getClassObservables()
  {
    final Optional<List<Map<String, Object>>> observables = schema.getObservables(TEST_CLASS_ID);
    Assert.assertTrue(observables.isPresent());
    Assert.assertFalse(observables.get().isEmpty());
  }

  @Test
  public void testGetHostnameObservables()
  {
    final Optional<Map<String, Map<String, Object>>> observables =
      schema.getObservables(TEST_CLASS_ID, Observables.TypeID.Hostname);

    Assert.assertTrue(observables.isPresent());
    Assert.assertFalse(observables.get().isEmpty());
  }

  @Test
  public void testGetUserObservables()
  {
    final Optional<Map<String, Map<String, Object>>> observables =
      schema.getObservables(TEST_CLASS_ID, Observables.TypeID.Process);

    Assert.assertTrue(observables.isPresent());
    Assert.assertFalse(observables.get().isEmpty());
  }

  @Test
  public void getAssociations()
  {
    final Optional<Associations> associations = schema.getAssociations(TEST_CLASS_ID);
    Assert.assertTrue(associations.isPresent());
  }

  @Test
  public void getObject()
  {
    final Optional<Map<String, Object>> process = schema.getObject("process");

    Assert.assertTrue(process.isPresent());
    Assert.assertFalse(process.get().isEmpty());
  }
}