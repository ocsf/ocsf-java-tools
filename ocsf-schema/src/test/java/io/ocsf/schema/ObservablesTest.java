/*
 * Copyright 2024 Splunk Inc.
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
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ObservablesTest
{
  /**
   * The Observable type identifiers. MUST be kept in sync with the
   * <a href="https://schema.ocsf.io/objects/observable">Observable</a> object.
   */
  @SuppressWarnings("unused")
  enum TypeID
  {
    Unknown, // 0
    Hostname,
    IP_Address,
    MAC_Address,
    Username,
    Email_Address,
    URL_String,
    File_Name,
    File_Hash,
    Process_Name,
    Resource_UID,

    Reserved11,
    Reserved12,
    Reserved13,
    Reserved14,
    Reserved15,
    Reserved16,
    Reserved17,
    Reserved18,
    Reserved19,

    Endpoint,
    User,
    Email,
    URL,
    File,
    Process,
    Location,
    Container,
    Reg_Key,
    Reg_Value,
    Fingerprint // 30
  }

  private static Schema schema = null;

  @BeforeClass
  public static void setUp()
  {
    schema = new Schema(Paths.get("build/schema.json"), true, true);
  }

  @Test
  public void getObservablesMap()
  {
    final Optional<Map<String, Map<String, Object>>> observables =
      Observables.getObservables(Data.ProcessActivity);

    Assert.assertTrue(observables.isPresent());
    Assert.assertFalse(observables.get().isEmpty());
  }

  @Test
  public void testGetObservablesMap()
  {
    final Optional<Map<String, Map<String, Object>>> observables =
      Observables.getObservables(Data.ProcessActivity, TypeID.Endpoint.ordinal());

    Assert.assertTrue(observables.isPresent());

    observables.get().forEach(
      (name, map) ->
        Assert.assertEquals(TypeID.Endpoint.ordinal(), map.get(Dictionary.TYPE_ID)));
  }

  @Test
  public void getObservables()
  {
    final Optional<List<Map<String, Object>>> observables =
      Observables.observables(Data.ProcessActivity);

    Assert.assertTrue(observables.isPresent());
    Assert.assertFalse(observables.get().isEmpty());
  }

  @Test
  public void testGetObservables()
  {
    final Optional<List<Map<String, Object>>> observables =
      Observables.observables(Data.ProcessActivity, TypeID.Endpoint.ordinal());

    Assert.assertTrue(observables.isPresent());

    observables.get().forEach(
      map ->
        Assert.assertEquals(TypeID.Endpoint.ordinal(), map.get(Dictionary.TYPE_ID)));
  }

  @Test
  public void getClassObservables()
  {
    final Optional<List<Map<String, Object>>> observables =
      schema.getObservables(Data.TEST_CLASS_ID);
    Assert.assertTrue(observables.isPresent());
    Assert.assertFalse(observables.get().isEmpty());
  }

  @Test
  public void testGetHostnameObservables()
  {
    final Optional<Map<String, Map<String, Object>>> observables =
      schema.getObservables(Data.TEST_CLASS_ID, TypeID.Hostname.ordinal());

    Assert.assertTrue(observables.isPresent());
    Assert.assertFalse(observables.get().isEmpty());
  }

  @Test
  public void testGetUserObservables()
  {
    final Optional<Map<String, Map<String, Object>>> observables =
      schema.getObservables(Data.TEST_CLASS_ID, TypeID.Process.ordinal());

    Assert.assertTrue(observables.isPresent());
    Assert.assertFalse(observables.get().isEmpty());
  }

}