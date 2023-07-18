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

package io.ocsf.translators;

import io.ocsf.schema.Dictionary;
import io.ocsf.utils.Maps;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * File fingerprints helper functions.
 */
final class Fingerprint
{
  private static final String Fingerprint = "fingerprint";

  // fingerprint attribute names
  public static final String Fingerprints = "fingerprints";
  private static final String Algorithm = "algorithm";
  private static final String AlgorithmID = "algorithm_id";
  private static final String Value = "value";

  // file hash sizes in bytes
  private static final int MD5_LEN = 32;
  private static final int SHA1_LEN = 40;
  private static final int SHA256_LEN = 64;
  private static final int SHA512_LEN = 128;

  private Fingerprint() {}

  static boolean put(final Map<String, Object> translated, final String type, final Object value, final String key)
  {
    if (Fingerprint.equals(type))
    {
      fingerprint((String) value).
          ifPresent(fp -> Maps.putIn(translated, key + "." + Fingerprints, Collections.singletonList(fp)));

      return true;
    }

    return false;
  }

  /**
   * Translate a file hash to a fingerprint object.
   *
   * @param value the fingerprint hash value
   * @return the fingerprint object
   */
  static Optional<Map<String, Object>> fingerprint(final String value)
  {
    if (value != null)
    {
      final Map<String, Object> fingerprint = new HashMap<>(4);

      final int algorithm_id;
      switch (value.length())
      {
        case MD5_LEN:
          algorithm_id = 1;
          break;
        case SHA1_LEN:
          algorithm_id = 2;
          break;
        case SHA256_LEN:
          algorithm_id = 3;
          break;
        case SHA512_LEN:
          algorithm_id = 4;
          break;
        default:
          algorithm_id = Dictionary.OTHER_ID;
          fingerprint.put(Algorithm, value);
          break;
      }

      fingerprint.put(AlgorithmID, algorithm_id);
      fingerprint.put(Value, value);

      return Optional.of(fingerprint);
    }

    return Optional.empty();
  }
}
