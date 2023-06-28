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
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ObservablesTest
{
  @Test
  public void getObservablesMap()
  {
    final Optional<Map<String, Map<String, Object>>> observables =
      Observables.getObservables(Data.ProcessActivity);

    Assert.assertTrue(observables.isPresent());
    Assert.assertTrue(observables.get().size() > 0);
  }

  @Test
  public void testGetObservablesMap()
  {
    final Optional<Map<String, Map<String, Object>>> observables =
      Observables.getObservables(Data.ProcessActivity, Observables.TypeID.Endpoint);

    Assert.assertTrue(observables.isPresent());

    observables.get().forEach(
      (name, map) ->
        Assert.assertEquals(Observables.TypeID.Endpoint.ordinal(), map.get(Dictionary.TYPE_ID)));
  }

  @Test
  public void getObservables()
  {
    final Optional<List<Map<String, Object>>> observables =
      Observables.observables(Data.ProcessActivity);

    Assert.assertTrue(observables.isPresent());
    Assert.assertTrue(observables.get().size() > 0);
  }

  @Test
  public void testGetObservables()
  {
    final Optional<List<Map<String, Object>>> observables =
      Observables.observables(Data.ProcessActivity, Observables.TypeID.Endpoint);

    Assert.assertTrue(observables.isPresent());

    observables.get().forEach(
      map ->
        Assert.assertEquals(Observables.TypeID.Endpoint.ordinal(), map.get(Dictionary.TYPE_ID)));
  }

}