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

package io.ocsf.translator.util;

import io.ocsf.schema.Dictionary;
import io.ocsf.utils.Maps;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A helper class to create OCSF URL object.
 */
public final class FingerprintObj
{
  private FingerprintObj() {}

  public static boolean put(
    final Map<String, Object> translated, final String type, final Object value, final String key)
  {
    if (Dictionary.Fingerprint.equals(type))
    {
      fingerprint((String) value).
        ifPresent(fp -> Maps.putIn(translated, key + "." + Dictionary.Fingerprints,
                                   Collections.singletonList(fp)));

      return true;
    }

    return false;
  }

  /**
   * Translate a file hash to a fingerprint object.
   *
   * @param value the fingerprint hash value
   * @return the fingerprint object
   * <p>
   * TODO: add the new algorithms: CTPH, TLSH, and quickXorHash
   */
  static Optional<Map<String, Object>> fingerprint(final String value)
  {
    if (value != null)
    {
      final Map<String, Object> fingerprint = new HashMap<>(4);

      final int algorithm_id;
      switch (value.length())
      {
        case Dictionary.MD5_LEN:
          algorithm_id = Dictionary.FPA_ID_MD5;
          break;
        case Dictionary.SHA1_LEN:
          algorithm_id = Dictionary.FPA_ID_SHA1;
          break;
        case Dictionary.SHA256_LEN:
          algorithm_id = Dictionary.FPA_ID_SHA256;
          break;
        case Dictionary.SHA512_LEN:
          algorithm_id = Dictionary.FPA_ID_SHA512;
          break;
        default:
          algorithm_id = Dictionary.OTHER_ID;
          fingerprint.put(Dictionary.Algorithm, value);
          break;
      }

      fingerprint.put(Dictionary.AlgorithmID, algorithm_id);
      fingerprint.put(Dictionary.Value, value);

      return Optional.of(fingerprint);
    }

    return Optional.empty();
  }
}
