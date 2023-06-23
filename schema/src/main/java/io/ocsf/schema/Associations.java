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

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A helper class to work with attribute associations.
 */
public final class Associations
{
  private final Map<String, List<String>> map;

  public Associations(final Map<String, List<String>> map)
  {
    this.map = map;
  }

  /**
   * Returns the first associated attribute with the given name.
   *
   * @param name attribute name
   * @return the associated attribute name
   */
  public Optional<String> first(final String name)
  {
    final List<String> associations = map.get(name);

    if (associations != null && !associations.isEmpty())
      return Optional.of(associations.get(0));

    return Optional.empty();
  }

  /**
   * Returns all associated attributes with the given name.
   *
   * @param name attribute name
   * @return the associated attribute name
   */
  public Optional<List<String>> get(final String name)
  {
    return Optional.ofNullable(Maps.typecast(map.get(name)));
  }
}
