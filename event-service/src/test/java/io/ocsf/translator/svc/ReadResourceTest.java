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

package io.ocsf.translator.svc;

import io.ocsf.translator.TranslatorBuilder;
import io.ocsf.utils.Files;
import junit.framework.TestCase;
import org.junit.Assert;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class ReadResourceTest extends TestCase
{
  public void testReadJsonFile() throws IOException
  {
    final List<Map<String, Object>> data = Files.readJsonFromResource("rules/common.json");

    Assert.assertFalse(data.isEmpty());
  }

  public void testRules() throws IOException
  {
    final Path rules = Paths.get("rules");

    Assert.assertNotNull(TranslatorBuilder.fromResource(
      rules,
      Paths.get("microsoft/windows/security/xml/translate-4688.json")));
    Assert.assertNotNull(TranslatorBuilder.fromResource(
      rules,
      Paths.get("microsoft/windows/security/xml/translate-4624.json")));
    Assert.assertNotNull(TranslatorBuilder.fromResource(
      rules,
      Paths.get("microsoft/windows/security/xml/translate-4625.json")));
    Assert.assertNotNull(TranslatorBuilder.fromResource(
      rules,
      Paths.get("cisco/111010.json")));
  }

  public void testTranslators() throws IOException
  {
    final TranslatorsManager translators = new TranslatorsManager("rules");

    translators.addResource(
      "4688", Paths.get("microsoft/windows/security/xml/translate-4688.json"));
    translators.addResource(
      "4624", Paths.get("microsoft/windows/security/xml/translate-4624.json"));
    translators.addResource(
      "4625", Paths.get("microsoft/windows/security/xml/translate-4625.json"));
    translators.addResource("111010", Paths.get("cisco/111010.json"));

    assertEquals(4, translators.size());
  }
}
