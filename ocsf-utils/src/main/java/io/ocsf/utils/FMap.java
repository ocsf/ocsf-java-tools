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

package io.ocsf.utils;

import java.util.HashMap;
import java.util.Map;

public class FMap<K, V> extends HashMap<K, V> implements Maps.Supplier<V>
{
  public FMap()                                      {}

  public FMap(final Map<? extends K, ? extends V> m) {super(m);}

  public FMap(final K key, final V value)            {put(key, value);}

  public FMap<K, V> p(final K key, final V value)
  {
    put(key, value);
    return this;
  }

  public FMap<K, V> o(final K key, final V value)
  {
    if (value != null) put(key, value);
    return this;
  }

  public static <K, V> FMap<K, V> b()                           {return new FMap<>();}

  public static <K, V> FMap<K, V> b(final Map<K, V> map)        {return new FMap<>(map);}

  public static <K, V> FMap<K, V> s(final K key, final V value) {return new FMap<>(key, value);}

  @Override
  public V get(final String name)
  {
    return super.get(name);
  }
}
