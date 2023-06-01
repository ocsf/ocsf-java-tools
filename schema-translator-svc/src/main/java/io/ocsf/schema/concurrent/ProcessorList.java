/*
 * Copyright 2023 Open Cybersecurity Schema Framework
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

package io.ocsf.schema.concurrent;

import java.util.HashMap;
import java.util.Map;

public class ProcessorList<T>
{
  private final String name;

  protected final Map<String, T> processors = new HashMap<>();

  public ProcessorList(final String name)
  {
    this.name = name;
  }

  public T get(final String name)
  {
    final T t = processors.get(name);
    if (t == null)
    {
      // check for a wild card match
      for (final Map.Entry<String, T> e : processors.entrySet())
      {
        final String key = e.getKey();
        if (key.endsWith("*") && name.startsWith(key.substring(0, key.length() - 1)))
        {
          return e.getValue();
        }
      }
    }

    return t;
  }

  public int size() {return processors.size();}

  @Override
  public String toString() {return name;}
}
