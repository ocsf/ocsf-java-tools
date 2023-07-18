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

  public static final String Path = "path";
  public static final String Name = "name";

  public static final String TYPE_ID = "type_id";

  public static final String CLASS_NAME = "class_name";
  public static final String CLASS_UID  = "class_uid";

  public static final String ACTIVITY_NAME = "activity_name";
  public static final String ACTIVITY_ID   = "activity_id";

  public static final String TYPE_NAME = "type_name";
  public static final String TYPE_UID  = "type_uid";

  public static final String OBSERVABLES = "observables";

  public static final String RAW_EVENT      = "raw_data";
  public static final String REF_EVENT_TIME = "ref_time";
  public static final String UNMAPPED       = "unmapped";

  public static final Integer UNKNOWN_ID = 0;
  public static final int     OTHER_ID   = 99;
  public static final String  OTHER      = "Other";

  // Fingerprint attributes
  public static final String Fingerprints = "fingerprints";
  public static final String Fingerprint  = "fingerprint";
  public static final String Algorithm    = "algorithm";
  public static final String AlgorithmID  = "algorithm_id";
  public static final String Value        = "value";

  // Hash sizes in bytes
  public static final int MD5_LEN    = 32;
  public static final int SHA1_LEN   = 40;
  public static final int SHA256_LEN = 64;
  public static final int SHA512_LEN = 128;

  // Fingerprint algorithm_id values
  public static final int FPA_ID_MD5    = 1;
  public static final int FPA_ID_SHA1   = 2;
  public static final int FPA_ID_SHA256 = 3;
  public static final int FPA_ID_SHA512 = 4;

  // URL attributes
  public static final String Text     = "url_string";
  public static final String Scheme   = "scheme";
  public static final String Hostname = "hostname";
  public static final String Port     = "port";
  public static final String Query    = "query_string";

  public static final String ParentFolder = "parent_folder";

  private Dictionary() {}
}
