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

package io.ocsf.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class with Map function helpers.
 */
public final class Maps {
  public static final Locale LOCALE = Locale.ROOT;

  private static final int NAME_SEPARATOR = '.';
  private static final String NAME_SPLIT_REGEX = "\\.";

  @SuppressWarnings("unchecked")
  public static <T> T typecast(final Object obj) {
    return (T) obj;
  }

  /**
   * Represents a key-based supplier of data.
   *
   * @param <T> the type of the data returned by this supplier
   */
  @FunctionalInterface
  public interface Supplier<T> {
    T get(final String name);
  }

  @FunctionalInterface
  public interface DataSource {
    Object get(final Map<String, Object> map, final String name);
  }

  public static boolean isEmpty(final Map<?, ?> map) {
    return map == null || map.isEmpty();
  }

  /**
   * Remove empty attributes.
   *
   * @param <K>  the type of keys maintained by this map
   * @param <V>  the type of mapped values
   * @param data the map to clean up
   */
  public static <K, V> void cleanup(final Map<K, V> data) {
    final Iterator<Map.Entry<K, V>> it = data.entrySet().iterator();
    while (it.hasNext()) {
      final Map.Entry<K, V> entry = it.next();
      final Object value = entry.getValue();
      if (value instanceof Map<?, ?>) {
        final Map<?, ?> map = typecast(value);
        if (map.isEmpty()) {
          it.remove();
        } else {
          cleanup(map);
        }
      } else if (value instanceof Collection<?>) {
        final Collection<?> c = typecast(value);
        if (c.isEmpty()) {
          it.remove();
        }
      }
    }
  }

  /**
   * Create a new map with lowercase keys.
   *
   * @param data the map to clean up
   * @return a new map with lowercase keys
   */
  public static Map<String, Object> downcase(final Map<String, Object> data) {
    return downcase(data, new HashMap<>(data.size()));
  }

  private static Map<String, Object> downcase(
      final Map<String, Object> data, final Map<String, Object> acc
  ) {
    for (final Map.Entry<String, Object> entry : data.entrySet()) {
      final String key = entry.getKey().toLowerCase(LOCALE);
      final Object value = entry.getValue();
      if (value instanceof Map<?, ?>) {
        final Map<String, Object> m = typecast(value);
        acc.put(key, downcase(m, new HashMap<>(m.size())));
      } else if (value instanceof Collection<?>) {
        final Collection<Object> c = typecast(value);
        final Collection<Object> list = new ArrayList<>(c.size());
        for (final Object obj : c) {
          if (obj instanceof Map<?, ?>) {
            final Map<String, Object> m = typecast(obj);
            list.add(downcase(m, new HashMap<>(m.size())));
          } else {
            list.add(obj);
          }
        }
        acc.put(key, list);
      } else {
        acc.put(key, value);
      }
    }
    return acc;
  }

  public static void move(
      final Map<String, Object> src,
      final String srcName,
      final Map<String, Object> dst,
      final String dstName
  ) {
    put(dst, dstName, src.remove(srcName));
  }

  public static void moveIn(
      final Map<String, Object> src,
      final String srcName,
      final Map<String, Object> dst,
      final String dstName
  ) {
    putIn(dst, dstName, removeIn(src, srcName));
  }

  public static void put(final Map<String, Object> map, final String name, final Object value) {
    if (value != null) {
      map.put(name, value);
    }
  }

  public static void put(
      final Map<String, Object> map,
      final String name,
      final Object value,
      final Object defaultValue
  ) {
    if (value != null) {
      map.put(name, value);
    } else {
      map.put(name, defaultValue);
    }
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
      final Map<String, Object> map, final String[] path, final Object value
  ) {
    return value != null ? updateIn(map, path, value) : null;
  }

  public static Object putIn(
      final Map<String, Object> map,
      final String[] path,
      final Object value,
      final boolean overwrite
  ) {
    if (value != null) {
      return overwrite ? updateIn(map, path, value) : putInIfAbsent(map, path, value);
    }
    return null;
  }

  public static void putIn(
      final Map<String, Object> map, final String path, final Object value
  ) {
    if (value != null) {
      if (path.indexOf(NAME_SEPARATOR) > 0) {
        updateIn(map, path.split(NAME_SPLIT_REGEX), value);
      } else {
        map.put(path, value);
      }
    }
  }

  public static void putIn(
      final Map<String, Object> map, final String path, final Object value, final boolean overwrite
  ) {
    if (value != null) {
      if (path.indexOf(NAME_SEPARATOR) > 0) {
        final String[] keys = path.split(NAME_SPLIT_REGEX);
        if (overwrite) {
          updateIn(map, keys, value);
        } else {
          putInIfAbsent(map, keys, value);
        }
      } else if (overwrite) {
        map.put(path, value);
      } else {
        map.putIfAbsent(path, value);
      }
    }
  }

  public static <T> T get(final Map<String, Object> data, final String name) {
    return data != null ? typecast(data.get(name)) : null;
  }

  public static <T> T get(final Map<String, Object> data, final String name, final T defaultValue) {
    if (data != null) {
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
  public static Object getIn(final Map<String, ?> map, final String... path) {
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
  public static Object getIn(final Map<String, Object> map, final String path) {
    // handle keys with dots
    final Object value = map.get(path);
    if (value == null && path.indexOf(NAME_SEPARATOR) > 0) {
      return getNested(map, path.split(NAME_SPLIT_REGEX));
    }
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
  public static Object removeIn(final Map<String, Object> map, final String path) {
    // handle keys with dots
    final Object value = map.remove(path);
    if (value == null && path.indexOf(NAME_SEPARATOR) > 0) {
      return remove(map, path.split(NAME_SPLIT_REGEX));
    }
    return value;
  }

  /**
   * Represents a function that accepts three arguments and produces a result.
   *
   * @see #deep_merge(Map, Map, Resolver)
   */
  @FunctionalInterface
  public interface Resolver {
    /**
     * This function will be called to resolve the conflicts when merging two maps.
     *
     * @param key    the duplicate key
     * @param value1 the type of the first value (the value of key in map1)
     * @param value2 the type of the second value (the value of key in map2)
     * @return a value to ve associated with the key
     */
    Object apply(final String key, final Object value1, final Object value2);

    default boolean overwrite() { return false; }
  }

  /**
   * Recursively merges two maps into one.
   * <p>
   * All keys in map2 will be added to map1.
   *
   * @param map1      the map where the map2 will be added
   * @param map2      the map to be added to map1
   * @param overwrite the overwrite flag controls how the duplicate keys are handled. If overwrite
   *                  is <code>true</code>, then map2 values overwrite the map1 values.
   * @return the merged map
   */
  public static Map<String, Object> merge(
      final Map<String, Object> map1, final Map<String, Object> map2, final boolean overwrite
  ) {
    return merge(map1, map2, resolver(overwrite));
  }

  /**
   * Recursively merges two maps into one, resolving conflicts through the given resolver function.
   * <p>
   * All keys in map2 will be added to map1. The given resolver function will be invoked when there
   * are duplicate keys; its arguments are key (the duplicate key), value1 (the value of key in
   * map1), and value2 (the value of key in map2). The value returned by resolver is used as the
   * value under key in the resulting map.
   *
   * @param map1     the map where the map2 will be added
   * @param map2     the map to be added to map1
   * @param resolver the conflicts resolver function
   * @return the merged map
   */
  public static Map<String, Object> merge(
      final Map<String, Object> map1, final Map<String, Object> map2, final Resolver resolver
  ) {
    Objects.requireNonNull(resolver);
    if (map1 == null) {
      return map2;
    }
    if (map2 == null) {
      return map1;
    }
    return deep_merge(map1, map2, resolver);
  }

  private static Map<String, Object> deep_merge(
      final Map<String, Object> map1, final Map<String, Object> map2, final Resolver resolver
  ) {
    for (final Map.Entry<String, Object> entry : map2.entrySet()) {
      final String key = entry.getKey();
      final Object value2 = entry.getValue();
      if (value2 != null) {
        map1.merge(key, value2, (v1, v2) -> resolver.apply(key, v1, v2));
      } else if (resolver.overwrite()) {
        map1.remove(key);
      }
    }
    return map1;
  }

  /*
   * The default resolver does not overwrite the existing values.
   */
  private static final class DefaultResolver implements Resolver {
    @Override
    public Object apply(final String key, final Object value1, final Object value2) {
      if (value1 instanceof Map<?, ?> && value2 instanceof Map<?, ?>) {
        final Map<String, Object> map1 = typecast(value1);
        if (map1.isEmpty()) {
          return value2;
        }
        final Map<String, Object> map2 = typecast(value2);
        if (map2.isEmpty()) {
          return value1;
        }
        return deep_merge(map1, map2, this);
      }
      return value1 != null ? value1 : value2;
    }
  }

  private static final class OverwrtingResolver implements Resolver {
    @Override
    public Object apply(final String key, final Object value1, final Object value2) {
      if (value1 instanceof Map<?, ?> && value2 instanceof Map<?, ?>) {
        final Map<String, Object> map1 = typecast(value1);
        if (map1.isEmpty()) {
          return value2;
        }
        final Map<String, Object> map2 = typecast(value2);
        if (map2.isEmpty()) {
          return value1;
        }
        return deep_merge(map1, map2, this);
      }
      return value2;
    }

    @Override
    public boolean overwrite() { return true; }
  }

  private static Resolver resolver(final boolean overwrite) {
    return overwrite ? new OverwrtingResolver() : new DefaultResolver();
  }

  private static Object getNested(Map<String, ?> map, final String[] path) {
    final int last = path.length - 1;
    if (last < 0) {
      return null;
    }
    if (last == 0) {
      return map.get(path[0]);
    }
    for (int depth = 0; depth < last; ++depth) {
      final Object next = map.get(path[depth]);
      if (next instanceof Map<?, ?>) {
        map = typecast(next);
      } else {
        return null; // not found or not a map
      }
    }
    return map.get(path[last]);
  }

  private static Object remove(Map<String, Object> map, final String[] keys) {
    final int last = keys.length - 1;
    if (last < 0) {
      return null;
    }
    if (last == 0) {
      return map.remove(keys[0]);
    }
    for (int depth = 0; depth < last; ++depth) {
      final Object next = map.get(keys[depth]);
      if (next instanceof Map<?, ?>) {
        map = typecast(next);
      } else {
        return null; // not found or not a map
      }
    }
    return map.remove(keys[last]);
  }

  private static Object putInIfAbsent(
      Map<String, Object> map, final String[] keys, final Object value
  ) {
    final int last = keys.length - 1;
    for (int depth = 0; depth < last; ++depth) {
      final Object next = map.get(keys[depth]);
      if (next instanceof Map<?, ?>) {
        map = typecast(next);
      } else if (next == null) {
        do {
          final Map<String, Object> map2 = new HashMap<>();
          map.put(keys[depth], map2);
          map = map2;
        } while (++depth < last);
        break; // the for loop
      } else {
        return null; // don't overwrite
      }
    }
    return map.putIfAbsent(keys[last], value);
  }

  private static Object updateIn(Map<String, Object> map, final String[] keys, final Object value) {
    final int last = keys.length - 1;
    for (int depth = 0; depth < last; ++depth) {
      final Object next = map.get(keys[depth]);
      if (next instanceof Map<?, ?>) {
        map = typecast(next);
      } else {
        do {
          final Map<String, Object> map2 = new HashMap<>();
          map.put(keys[depth], map2);
          map = map2;
        } while (++depth < last);

        break; // the for loop
      }
    }
    return map.put(keys[last], value);
  }

  private Maps() {}
}
