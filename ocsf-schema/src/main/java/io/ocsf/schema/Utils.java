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

import io.ocsf.utils.Maps;

import java.util.Map;
import java.util.UUID;

/**
 * Schema utility functions.
 */
public final class Utils
{
  // The event class uid path
  private static final String[] EVENT_UID = new String[]{"metadata", "uid"};

  private Utils() {}

  /**
   * Generate a random UUID.
   *
   * @return UUID
   */
  public static String newUuid()
  {
    return UUID.randomUUID().toString();
  }

  /**
   * Adds a randomly generated event UUID to the event in <code>metadata.uid</code>.
   *
   * @param data the event
   * @return the updated event
   */
  public static Map<String, Object> addUuid(final Map<String, Object> data)
  {
    Maps.putIn(data, Utils.EVENT_UID, Utils.newUuid());

    return data;
  }

  /**
   * Returns an event type_uid using class_uid and activity_id values.
   *
   * @param uid the event class_uid
   * @param id  the event activity_id
   * @return the event type_uid
   */
  public static int typeUid(final int uid, final int id)
  {
    return uid * 100 + (id >= 0 ? id : Dictionary.OTHER_ID);
  }

  /**
   * Adds the event {@code type_uid} using the values of {@code activity_id} and {@code class_uid}
   * retrieved from the given event data.
   *
   * @param data the event data
   * @return the updated event containing an event {@code type_uid}.
   */
  public static Map<String, Object> addTypeUid(final Map<String, Object> data)
  {
    final Integer classId =
      (Integer) data.getOrDefault(Dictionary.CLASS_UID, Dictionary.UNKNOWN_ID);

    final Integer activity =
      (Integer) data.getOrDefault(Dictionary.ACTIVITY_ID, Dictionary.UNKNOWN_ID);

    data.put(Dictionary.TYPE_UID, typeUid(classId, activity));

    return data;
  }
}
