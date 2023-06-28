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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ocsf.schema.Schema.*;

/**
 * A helper class to query and manipulate the observable data associate with an event.
 * <p>
 * The returned observables are an array of observable objects.
 *
 * @see <a href="https://schema.ocsf.io/objects/observable">OCSF Observable object</a>
 */
public final class Observables
{
  /**
   * The Observable type identifiers. Must be kept in sync with the schema.
   */
  public enum TypeID
  {
    Unknown,
    Hostname,
    IP_Address,
    MAC_Address,
    Username,
    Email_Address,
    URL_String,
    File_Name,
    File_Hash,
    Process_Name,
    Resource_UID,

    Reserved11,
    Reserved12,
    Reserved13,
    Reserved14,
    Reserved15,
    Reserved16,
    Reserved17,
    Reserved18,
    Reserved19,

    Endpoint,
    User,
    Email,
    URL,
    File,
    Process,
    Location,
    Container,
    Reg_Key,
    Reg_Value
  }

  /**
   * Returns the observables associated with the given event.
   *
   * @param event the event data
   * @return a list of observables
   */
  public static Optional<List<Map<String, Object>>> observables(final Map<String, Object> event)
  {
    final List<Map<String, Object>> observables = Maps.typecast(event.get(Dictionary.OBSERVABLES));

    if (observables != null && !observables.isEmpty())
    {
      return Optional.of(observables);
    }

    return Optional.empty();
  }

  /**
   * Returns the observables associated with the given event.
   *
   * @param event the event data
   * @return a map of observables
   */
  public static Optional<Map<String, Map<String, Object>>> getObservables(final Map<String,
    Object> event)
  {
    final List<Map<String, Object>> observables = Maps.typecast(event.get(Dictionary.OBSERVABLES));

    if (observables != null && !observables.isEmpty())
    {
      return Optional.of(transform(observables));
    }

    return Optional.empty();
  }

  /**
   * Returns the observables associated with the given event and observable type ID.
   *
   * @param event  the event data
   * @param typeId the observable type ID as defined in the schema
   * @return a list of observables that matched the type ID
   */
  public static Optional<List<Map<String, Object>>> observables(
    final Map<String, Object> event, final TypeID typeId)
  {
    final List<Map<String, Object>> observables = Maps.typecast(event.get(Dictionary.OBSERVABLES));
    if (observables != null)
    {
      final Integer id = typeId.ordinal();

      final List<Map<String, Object>> acc = new ArrayList<>();

      observables.forEach(map -> {
        if (id.equals(map.get(TYPE_ID)))
          acc.add(map);
      });

      if (!acc.isEmpty())
        return Optional.of(acc);
    }

    return Optional.empty();
  }

  /**
   * Returns the observables associated with the given event and observable type ID.
   *
   * @param event  the event data
   * @param typeId the observable type ID as defined in the schema
   * @return a map of observables that matched the type ID
   */
  public static Optional<Map<String, Map<String, Object>>> getObservables(
    final Map<String, Object> event, final TypeID typeId)
  {
    return filter(Maps.typecast(event.get(Dictionary.OBSERVABLES)), typeId);
  }

  /**
   * Filter the observables by observable type ID.
   *
   * @param observables the observables data
   * @param typeId      the observable type ID as defined in the schema
   * @return observables that matched the type ID
   */
  public static Optional<Map<String, Map<String, Object>>> filter(
    final List<Map<String, Object>> observables, final TypeID typeId)
  {
    if (observables != null)
    {
      final Integer id = typeId.ordinal();

      final HashMap<String, Map<String, Object>> acc = new HashMap<>();

      observables.forEach(o -> {
        if (id.equals(o.get(TYPE_ID)))
        {
          final String name = (String) o.get("name");
          acc.put(name, o);
        }
      });

      if (!acc.isEmpty())
        return Optional.of(acc);
    }

    return Optional.empty();
  }


  private static Map<String, Map<String, Object>> transform(
    final List<Map<String, Object>> observables)
  {
    final HashMap<String, Map<String, Object>> acc = new HashMap<>();

    observables.forEach(o -> {
      final String name = (String) o.get("name");

      acc.put(name, o);
    });

    return acc;
  }

  private Observables() {}
}
