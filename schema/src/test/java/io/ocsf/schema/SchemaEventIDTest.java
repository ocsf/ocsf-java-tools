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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Map;

public class SchemaEventIDTest
{
  private Schema schema;

  @Before
  public void setUp()
  {
    schema = new Schema(Paths.get("build/schema.json"));
  }

  @Test
  public void enrich()
  {
    final FMap<String, Object> data = FMap.<String, Object>b()
        .p(Dictionary.CLASS_ID, 1001)
        .p(Dictionary.ACTIVITY_ID, 1);

    final Map<String, Object> enriched = schema.enrich(data);

    // should add type_uid to both data and enriched, but not activity_name and class_name
    Assert.assertEquals(data.size(), enriched.size());
  }
}