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

import org.junit.Assert;
import org.junit.Test;

public class FileObjTest
{

  @Test
  public void emptyFilePath()
  {
    Assert.assertArrayEquals(FileObj.EMPTY_PATH, FileObj.parseFilePath(""));
    Assert.assertArrayEquals(FileObj.EMPTY_PATH, FileObj.parseFilePath(null));
  }

  @Test
  public void rootFilePath()
  {
    Assert.assertArrayEquals(
      new String[]{null, "/"}, FileObj.parseFilePath("/")
    );
    Assert.assertArrayEquals(
      new String[]{"/", "test.txt"}, FileObj.parseFilePath("/test.txt")
    );
  }

  @Test
  public void directoryPath()
  {
    Assert.assertArrayEquals(
      new String[]{"/tmp", "test"}, FileObj.parseFilePath("/tmp/test/")
    );
    Assert.assertArrayEquals(
      new String[]{"tmp", "test"}, FileObj.parseFilePath("tmp/test/")
    );
  }
}