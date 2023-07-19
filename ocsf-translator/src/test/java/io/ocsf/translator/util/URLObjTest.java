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

package io.ocsf.translator.util;

import io.ocsf.schema.Dictionary;
import junit.framework.TestCase;
import org.junit.Assert;

import java.net.MalformedURLException;
import java.util.Map;

public class URLObjTest extends TestCase
{

  public void testToFile()
  {
    final String text = "http://example.com:80/docs/books/tutorial/index" +
                        ".html?name=networking#DOWNLOADING";

    try
    {
      final Map<String, Object> url = URLObj.toUrl(text);

      Assert.assertEquals(text, url.get(Dictionary.Text));
      Assert.assertEquals("http", url.get(Dictionary.Scheme));
      Assert.assertEquals("example.com", url.get(Dictionary.Hostname));
      Assert.assertEquals(80, url.get(Dictionary.Port));
      Assert.assertEquals("/docs/books/tutorial/index.html", url.get(Dictionary.Path));
      Assert.assertEquals("name=networking", url.get(Dictionary.Query));
    }
    catch (final MalformedURLException e)
    {
      Assert.fail(e.toString());
    }
  }
}