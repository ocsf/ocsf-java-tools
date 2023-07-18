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

package io.ocsf.schema;

import io.ocsf.utils.Json;
import io.ocsf.utils.Maps;

import java.util.Collections;
import java.util.Map;

/**
 * Event class encapsulates event data.
 */
public class Event
{
  /**
   * The out-of-band attributes, not available in the raw event data.
   */
  public static final String CUSTOMER_ID = "customer_uid";
  public static final String SOURCE_TYPE = "source_type";

  /*
   * An empty event object indicating that this is the last event in the queue.
   */
  private static final Event EOS = new Event(Collections.emptyMap());

  /**
   * The End of Stream event.
   *
   * @return the EOS event
   */
  public static final Event eos()
  {
    return EOS;
  }

  protected final Map<String, Object> data;

  public Event(final Map<String, Object> data)
  {
    this.data = data;
  }

  @SuppressWarnings("unchecked")
  public <T> T get(final String name) {return (T) data.get(name);}

  @SuppressWarnings("unchecked")
  public <T> T get(final String name, final Object defaultValue)
  {
    return (T) data.getOrDefault(name, defaultValue);
  }

  @SuppressWarnings("unchecked")
  public <T> T getIn(final String name)
  {
    return (T) Maps.getIn(data, name);
  }

  @SuppressWarnings("unchecked")
  public <T> T getIn(final String name, final T defaultValue)
  {
    final T value = (T) Maps.getIn(data, name);

    return value != null ? value : defaultValue;
  }

  public static final <T> T get(final Event event, final String name) {return event.get(name);}

  public static final <T> T get(final Event event, final String name, final Object defaultValue)
  {
    return event.get(name, defaultValue);
  }

  public static final <T> T getIn(final Event event, final String name)
  {
    return event.getIn(name);
  }

  public static final <T> T getIn(final Event event, final String name, final T defaultValue)
  {
    return event.getIn(name, defaultValue);
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

  @Override
  public String toString()
  {
    return toJson();
  }
}
