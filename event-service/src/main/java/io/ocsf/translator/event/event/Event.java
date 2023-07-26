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

package io.ocsf.translator.event.event;

import io.ocsf.utils.Json;
import io.ocsf.utils.Maps;

import java.util.Collections;
import java.util.Map;

/**
 * Read-only Event class.
 */
public class Event
{
  /*
   * An empty event object indicating the last event in the event queue.
   */
  private static final Event EOS = new Event(Collections.emptyMap());

  /**
   * Returns the number of key-value mappings in this map.  If the map contains more than
   * <tt>Integer.MAX_VALUE</tt> elements, returns
   * <tt>Integer.MAX_VALUE</tt>.
   *
   * @return the number of key-value mappings in this map
   */
  public int size() {return data.size();}

  /**
   * Returns <tt>true</tt> if this map contains no key-value mappings.
   *
   * @return <tt>true</tt> if this map contains no key-value mappings
   */
  public boolean isEmpty() {return data.isEmpty();}

  private final Map<String, Object> data;

  /**
   * The End of Stream event.
   *
   * @return the EOS event
   */
  public static final Event eos()
  {
    return EOS;
  }

  /**
   * Constructs a new event using the provided data.
   *
   * @param data the event data
   */
  public Event(final Map<String, Object> data)
  {
    this.data = data;
  }

  @SuppressWarnings("unchecked")
  public <T> T get(final String name) {return (T) data.get(name);}

  @SuppressWarnings("unchecked")
  public <T> T getOrDefault(final String name, final T defaultValue)
  {
    return (T) data.getOrDefault(name, defaultValue);
  }

  @SuppressWarnings("unchecked")
  public <T> T getIn(final String path)
  {
    return (T) Maps.getIn(data, path);
  }

  @SuppressWarnings("unchecked")
  public <T> T getIn(final String... path)
  {
    return (T) Maps.getIn(data, path);
  }

  @SuppressWarnings("unchecked")
  public <T> T getInOrDefault(final String path, final T defaultValue)
  {
    final T value = (T) Maps.getIn(data, path);

    return value != null ? value : defaultValue;
  }

  /**
   * Checks if the event is an EOS event.
   *
   * @return true if the event is eos event
   */
  public final boolean isEos()
  {
    return this == EOS;
  }

  /**
   * Checks if the event is not an EOS event.
   *
   * @return true if the event is not EOS event
   */
  public final boolean isNotEos()
  {
    return this != EOS;
  }

  /**
   * Returns the map containing the event data.
   *
   * @return the event data
   */
  public Map<String, Object> data() {return data;}

  /**
   * Returns the event data as "compressed" JSON object.
   *
   * @return a string that represents a JSON object
   */
  public String toJson()
  {
    return Json.toString(data);
  }

  /**
   * Returns the event data as a JSON object in a human-readable format.
   *
   * @return a string that represents a JSON object
   */
  public String format()
  {
    return Json.format(data);
  }

  @Override
  public String toString()
  {
    return toJson();
  }
}
