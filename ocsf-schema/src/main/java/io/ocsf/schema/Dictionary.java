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
 * Attribute names defined in the schema.
 */
public final class Dictionary
{
  public static final String UID = "uid";

  public static final String TYPE_ID = "type_id";

  public static final String CLASS_NAME = "class_name";
  public static final String CLASS_UID = "class_uid";

  public static final String ACTIVITY_NAME = "activity_name";
  public static final String ACTIVITY_ID = "activity_id";

  public static final String TYPE_NAME = "type_name";
  public static final String TYPE_UID = "type_uid";

  public static final String OBSERVABLES = "observables";

  public static final Integer UNKNOWN_ID = 0;

  private Dictionary() {}
}
