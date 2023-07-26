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

package io.ocsf.utils;

import java.util.HashMap;
import java.util.Objects;

/**
 * A hash map with fuzzy keys refers to a data structure that allows for approximate or fuzzy
 * matching of keys when performing lookups or retrievals. Instead of requiring an exact match of
 * keys, this type of hash map enables you to perform searches using keys that are similar or
 * partially match the desired key.
 * <p>
 * The fuzzy matching algorithm uses case-insensitive comparison and wildcards (pattern matching),
 * using the String.matches() method along with regular expressions.
 *
 * @param <V> – the type of mapped values
 */
public class FuzzyHashMap<V> extends HashMap<String, V>
{
  private final String name;

  /**
   * Constructs an empty <tt>FuzzyHashMap</tt> with the specified name.
   *
   * @param name the name of the FuzzyHashMap instance
   */
  public FuzzyHashMap(final String name)
  {
    super();
    this.name = name;
  }

  /**
   * Constructs an empty <tt>FuzzyHashMap</tt> with the specified initial capacity and load factor.
   *
   * @param name            the name of the FuzzyHashMap instance
   * @param initialCapacity the initial capacity
   * @param loadFactor      the load factor
   * @throws IllegalArgumentException if the initial capacity is negative or the load factor is
   *                                  nonpositive
   */
  public FuzzyHashMap(final String name, final int initialCapacity, final float loadFactor)
  {
    super(initialCapacity, loadFactor);
    this.name = name;
  }

  /**
   * Associates the specified value with its "name", the string returned by
   * <code>value.toString()</code>.
   *
   * @param value value to be associated with itself
   * @return the previous value associated with <tt>key</tt>, or <tt>null</tt> if there was no
   * mapping for <tt>key</tt>.
   */
  public V put(final V value)
  {
    return put(value.toString(), value);
  }

  /**
   * Associates the specified value with the specified key in this map. If the map previously
   * contained a mapping for the key, the old value is replaced.
   *
   * @param key   key with which the specified value is to be associated
   * @param value value to be associated with the specified key
   * @return the previous value associated with <tt>key</tt>, or <tt>null</tt> if there was no
   * mapping for <tt>key</tt>.
   */

  @Override
  public V put(final String key, final V value)
  {
    Objects.requireNonNull(key, "the key cannot be null");
    Objects.requireNonNull(value, "the value cannot be null");

    return super.put(key, value);
  }

  /**
   * Returns the value to which the specified key is mapped, or {@code null} if this map contains no
   * mapping for the key.
   */
  @Override
  public V get(final Object key)
  {
    return get(key.toString());
  }

  public V get(final String name)
  {
    // Check for exact match
    final V v = super.get(name);
    if (v != null)
      return v;

    // Perform fuzzy matching logic to find approximate key matches
    for (final String key : keySet())
    {
      if (isApproximateMatch(key, name))
      {
        return super.get(key);
      }
    }

    return null; // No approximate match found
  }

  /*
   * Custom fuzzy matching logic.
   * <p>
   * Implement your fuzzy matching algorithm here Compare key and name using a string similarity
   * algorithm or any other approach
   */
  private static boolean isApproximateMatch(final String key, final String name)
  {
    final int keyLen = key.length() - 1;

    if (name.length() < keyLen)
      return false;

    // Wild card match
    return (key.endsWith("*") && name.startsWith(key.substring(0, keyLen)));
  }


  @Override
  public String toString() {return name;}
}
