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
import io.ocsf.utils.FMap;
import io.ocsf.utils.Strings;

/**
 * A helper class to create OCSF File object.
 */
public final class FileObj
{
  static final String[] EMPTY_PATH = new String[]{null, null};

  private FileObj() {}

  public static Object toFile(final Object value, final int typeId)
  {
    final String               path = value.toString().trim();
    final FMap<String, Object> file = FMap.b();

    if (path.isEmpty())
    {
      file.p(Dictionary.Name, path)
          .p(Dictionary.Path, path)
          .p(Dictionary.TYPE_ID, typeId);
    }
    else
    {
      final String[] parts = parseFilePath(path);

      file.o(Dictionary.ParentFolder, parts[0])
          .p(Dictionary.Name, parts[1])
          .p(Dictionary.Path, path)
          .p(Dictionary.TYPE_ID, typeId);
    }

    return file;
  }

  /**
   * Extracts the file and directory name from the given path name.
   *
   * @param path a path name
   * @return a two-dimensional String array, containing [directory-name, file-name]
   */
  public static String[] parseFilePath(final String path)
  {
    if (Strings.isNotEmpty(path))
    {
      // first, check if it is a unix path
      int pos = path.lastIndexOf('/');
      if (pos >= 0)
      {
        return splitFilePath(path, pos);
      }

      // else, check if it is a windows path
      pos = path.lastIndexOf('\\');
      if (pos >= 0)
      {
        return splitFilePath(path, pos);
      }

      // only a file name
      return new String[]{null, path};
    }

    return EMPTY_PATH;
  }

  private static String[] splitFilePath(final String path, final int pos)
  {
    final int next = pos + 1;
    if (pos == 0)
    {
      // root path
      return path.length() == 1 ?
             new String[]{null, path} :
             new String[]{path.substring(0, next), path.substring(next)};
    }

    if (next == path.length())
    {
      return parseFilePath(path.substring(0, pos));
    }

    return new String[]{path.substring(0, pos), path.substring(pos + 1)};
  }
}
