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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Utility class with Map function helpers.
 */
public final class Maps
{
  public static final Locale LOCALE = Locale.ROOT;

  private static final int NameSeparator = '.';

  private static final String NameSplitRegex = "\\.";

  @SuppressWarnings("unchecked")
  public static <T> T typecast(final Object obj) {return (T) obj;}

  /**
   * Represents a key-based supplier of data.
   *
   * @param <T> the type of the data returned by this supplier
   */
  @FunctionalInterface
  public interface Supplier<T>
  {
    T get(final String name);
  }

  @FunctionalInterface
  public interface DataSource
  {
    Object get(final Map<String, Object> map, final String name);
  }

  public static boolean isEmpty(final Map<?, ?> map)
  {
    return map == null || map.isEmpty();
  }

  /**
   * Remove empty attributes.
   *
   * @param <K>  the type of keys maintained by this map
   * @param <V>  the type of mapped values
   * @param data the map to clean up
   */
  public static <K, V> void cleanup(final Map<K, V> data)
  {
    final Iterator<Map.Entry<K, V>> it = data.entrySet().iterator();
    while (it.hasNext())
    {
      final Map.Entry<K, V> entry = it.next();
      final Object          value = entry.getValue();

      if (value instanceof Map<?, ?>)
      {
        final Map<?, ?> map = typecast(value);
        if (map.isEmpty())
          it.remove();
        else
          cleanup(map);
      }
      else if (value instanceof Collection<?>)
      {
        final Collection<?> c = typecast(value);
        if (c.isEmpty())
          it.remove();
      }
    }
  }

  /**
   * Create a new map with lowercase keys.
   *
   * @param data the map to clean up
   * @return a new map with lowercase keys
   */
  public static Map<String, Object> downcase(final Map<String, Object> data)
  {
    return downcase(data, new HashMap<>(data.size()));
  }

  private static Map<String, Object> downcase(
    final Map<String, Object> data, final Map<String, Object> acc)
  {
    for (final Map.Entry<String, Object> entry : data.entrySet())
    {
      final String key   = entry.getKey().toLowerCase(LOCALE);
      final Object value = entry.getValue();

      if (value instanceof Map<?, ?>)
      {
        final Map<String, Object> m = typecast(value);
        acc.put(key, downcase(m, new HashMap<>(m.size())));
      }
      else if (value instanceof Collection<?>)
      {
        final Collection<Object> c    = typecast(value);
        final Collection<Object> list = new ArrayList<>(c.size());
        for (final Object obj : c)
        {
          if (obj instanceof Map<?, ?>)
          {
            final Map<String, Object> m = typecast(obj);
            list.add(downcase(m, new HashMap<>(m.size())));
          }
          else
          {
            list.add(obj);
          }
        }
        acc.put(key, list);
      }
      else
      {
        acc.put(key, value);
      }
    }

    return acc;
  }

  public static void move(
    final Map<String, Object> src, final String srcName, final Map<String, Object> dst,
    final String dstName)
  {
    put(dst, dstName, src.remove(srcName));
  }

  public static void moveIn(
    final Map<String, Object> src, final String srcName, final Map<String, Object> dst,
    final String dstName)
  {
    putIn(dst, dstName, removeIn(src, srcName));
  }

  public static void put(final Map<String, Object> map, final String name, final Object value)
  {
    if (value != null) map.put(name, value);
  }

  public static void put(
    final Map<String, Object> map, final String name, final Object value, final Object defaultValue)
  {
    if (value != null)
      map.put(name, value);
    else
      map.put(name, defaultValue);
  }

  /**
   * Puts a value in a nested map via the given path, using the <code>map.path.to.value</code>
   * notation.
   *
   * @param map   map to which the specified path/value is to be added
   * @param path  path with which the specified value is to be associated
   * @param value value to be associated with the specified key
   * @return the previous value associated with <code>key</code>, or
   * <code>null</code> if there was no mapping for <code>key</code>.
   * (A <code>null</code> return can also indicate that the map previously associated
   * <code>null</code> with
   * <code>key</code>, if the implementation supports <code>null</code> values.)
   */
  public static Object putIn(
    final Map<String, Object> map, final String[] path, final Object value)
  {
    return value != null ? updateIn(map, path, value) : null;
  }

  public static Object putIn(
    final Map<String, Object> map, final String[] path, final Object value, final boolean overwrite)
  {
    if (value != null)
      return overwrite ? updateIn(map, path, value) : putInIfAbsent(map, path, value);

    return null;
  }

  public static void putIn(
    final Map<String, Object> map, final String path, final Object value)
  {
    if (value != null)
    {
      if (path.indexOf(NameSeparator) > 0)
        updateIn(map, path.split(NameSplitRegex), value);
      else
        map.put(path, value);
    }
  }

  public static void putIn(
    final Map<String, Object> map, final String path, final Object value, final boolean overwrite)
  {
    if (value != null)
    {
      if (path.indexOf(NameSeparator) > 0)
      {
        final String[] keys = path.split(NameSplitRegex);

        if (overwrite)
          updateIn(map, keys, value);
        else
          putInIfAbsent(map, keys, value);
      }
      else if (overwrite)
        map.put(path, value);
      else
        map.putIfAbsent(path, value);
    }
  }

  public static <T> T get(final Map<String, Object> data, final String name)
  {
    return data != null ? typecast(data.get(name)) : null;
  }

  public static <T> T get(final Map<String, Object> data, final String name, final T defaultValue)
  {
    if (data != null)
    {
      final Object o = data.get(name);
      return o != null ? typecast(o) : defaultValue;
    }

    return defaultValue;
  }

  /**
   * Returns the value to which the specified path is mapped, using the
   * <code>map.path.to.value</code> notation, or {@code null} if this map contains no mapping for
   * the path.
   *
   * @param map  the map from which the value associated with the specified path is to be returned
   * @param path the path whose associated value is to be returned
   * @return the value to which the specified path is mapped, or {@code null} if this map contains
   * no mapping for the path
   */
  public static Object getIn(final Map<String, Object> map, final String... path)
  {
    return getNested(map, path);
  }

  /**
   * Returns the value to which the specified path is mapped, using the
   * <code>map.path.to.value</code> notation, or {@code null} if this map contains no mapping for
   * the path.
   *
   * @param map  the map from which the value associated with the specified path is to be returned
   * @param path the path whose associated value is to be returned
   * @return the value to which the specified path is mapped, or {@code null} if this map contains
   * no mapping for the path
   */
  public static Object getIn(final Map<String, Object> map, final String path)
  {
    // handle keys with dots
    final Object value = map.get(path);
    if (value == null && path.indexOf(NameSeparator) > 0)
      return getNested(map, path.split(NameSplitRegex));

    return value;
  }

  /**
   * Deletes a value from a nested map via the given path, using the <code>map.path.to.value</code>
   * notation.
   *
   * @param map  the map from which the value associated with the specified path is to be removed
   * @param path the path whose mapping is to be removed from the map
   * @return the previous value associated with <code>path</code>, or {@code null} if this map
   * contains no mapping for the path
   */
  public static Object removeIn(final Map<String, Object> map, final String path)
  {
    // handle keys with dots
    final Object value = map.remove(path);
    if (value == null && path.indexOf(NameSeparator) > 0)
      return remove(map, path.split(NameSplitRegex));

    return value;
  }

  private static Object getNested(Map<String, Object> map, final String[] path)
  {
    final int last = path.length - 1;
    if (last < 0)
      return null;

    if (last == 0)
      return map.get(path[0]);

    for (int depth = 0; depth < last; ++depth)
    {
      final Object next = map.get(path[depth]);
      if (next instanceof Map<?, ?>)
        map = typecast(next);
      else
        return null; // not found or not a map
    }
    return map.get(path[last]);
  }

  private static Object remove(Map<String, Object> map, final String[] keys)
  {
    final int last = keys.length - 1;
    if (last < 0)
      return null;

    if (last == 0)
      return map.remove(keys[0]);

    for (int depth = 0; depth < last; ++depth)
    {
      final Object next = map.get(keys[depth]);
      if (next instanceof Map<?, ?>)
        map = typecast(next);
      else
        return null; // not found or not a map
    }

    return map.remove(keys[last]);
  }

  private static Object putInIfAbsent(
    Map<String, Object> map, final String[] keys, final Object value)
  {
    final int last = keys.length - 1;

    for (int depth = 0; depth < last; ++depth)
    {
      final Object next = map.get(keys[depth]);
      if (next instanceof Map<?, ?>)
      {
        map = typecast(next);
      }
      else if (next == null)
      {
        do
        {
          final Map<String, Object> map2 = new HashMap<>();
          map.put(keys[depth], map2);
          map = map2;
        }
        while (++depth < last);
        break; // the for loop
      }
      else
      {
        return null; // don't overwrite
      }
    }
    return map.putIfAbsent(keys[last], value);
  }

  private static Object updateIn(Map<String, Object> map, final String[] keys, final Object value)
  {
    final int last = keys.length - 1;
    for (int depth = 0; depth < last; ++depth)
    {
      final Object next = map.get(keys[depth]);
      if (next instanceof Map<?, ?>)
      {
        map = typecast(next);
      }
      else
      {
        do
        {
          final Map<String, Object> map2 = new HashMap<>();
          map.put(keys[depth], map2);
          map = map2;
        }
        while (++depth < last);

        break; // the for loop
      }
    }

    return map.put(keys[last], value);
  }

  private Maps() {}

}
