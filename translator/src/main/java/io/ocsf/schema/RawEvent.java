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

/**
 * The RawEvent class defines Splunk attribute available in the raw events.
 * <p>
 * The raw events are expected to have a few attributes:source, source type, tenant, and raw data.
 */
public final class RawEvent
{
  public static final String SOURCE_TYPE = "sourceType";
  public static final String RAW_EVENT = "rawEvent";
  public static final String TENANT = "tenant";

  private RawEvent() {}

  /**
   * Returns the event source type.
   *
   * @param event raw event
   * @return the source type
   */
  public static final String source(final Event event)
  {
    return event.get(SOURCE_TYPE);
  }

  /**
   * Returns the event raw data.
   *
   * @param event raw event
   * @return the raw data
   */
  public static final String raw(final Event event)
  {
    return event.get(RAW_EVENT);
  }

  /**
   * Returns the event tenant or customer.
   *
   * @param event raw event
   * @return the tenant identifier
   */
  public static final String tenant(final Event event)
  {
    return event.get(TENANT);
  }

}
