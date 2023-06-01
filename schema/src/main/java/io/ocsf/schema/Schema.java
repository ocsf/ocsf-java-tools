/*
 * Copyright 2023 Open Cybersecurity Schema Framework
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


import io.ocsf.schema.util.FMap;
import io.ocsf.schema.util.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ocsf.schema.util.Files.readJson;

/**
 * A helper class to enrich event data using the schema.
 * <p>
 * The enrichment adds:
 * <ul>
 *  <li><code>type_uid</code>  and <code>type_name</code> attributes</li>
 *  <li>textual values of the enum attributes</li>
 * </ul>
 * <p>
 * The <code>type_uid</code> value is calculated based on the <code>class_uid</code> and
 * <code>activity_id</code> values.
 * </p>
 * <pre>
 *  Activity: type_uid = class_uid * 100 + activity_id
 * </pre>
 */
public final class Schema
{
  private static final Logger logger = LogManager.getLogger(Schema.class);

  private static final String TYPES = "types";
  private static final String OBJECTS = "objects";
  private static final String CLASSES = "classes";

  static final String ATTRIBUTES = "attributes";

  static final String ENUM = "enum";
  static final String ENUM_SIBLING = "sibling";
  static final String ENUM_SUFFIX = "_id";

  static final String UID = "uid";
  static final String NAME = "name";
  static final String CAPTION = "caption";
  static final String TYPE = "type";
  static final String TYPE_ID = "type_id";
  static final String VALUE = "value";
  static final String IS_ARRAY = "is_array";
  static final String OBJECT_TYPE = "object_type";
  static final String OBSERVABLE = "observable";
  static final String OTHER = "Other";
  static final int OTHER_ID = 99;

  // All event classes: class_id -> class
  private final Map<Integer, Map<String, Object>> classes;

  // All objects: object_type -> object
  private final Map<String, Map<String, Object>> objects;

  // All types: name -> types
  private final Map<String, Map<String, Object>> types;

  // Observable type_id -> String
  private final Map<Integer, String> observableTypes;

  // All event observables: class_id -> observables (name -> observable)
  private final Map<Integer, List<Map<String, Object>>> observables;

  private final boolean translateEnums;

  private final boolean generateObservables;
  private final boolean schemaLoaded;

  /**
   * Load the schema in memory.
   * <p>
   * Note: Use a single instance per JVM.
   *
   * @param path the schema JSON file
   */
  public Schema(final Path path)
  {
    this(path, false, true);
  }

  public Schema(final Path path, final boolean translateEnums)
  {
    this(path, translateEnums, true);
  }

  public Schema(final Path path, final boolean translateEnums, final boolean generateObservables)
  {
    this.translateEnums = translateEnums;
    this.generateObservables = generateObservables;

    if (path != null)
    {
      logger.info("Using schema file: {}, translateEnums: {}", path, translateEnums);

      if (Files.isRegularFile(path))
      {
        try
        {
          final Map<String, Map<String, Object>> schema = readJson(path);

          this.objects = objects(schema);
          this.classes = classes(schema);
          this.types = types(schema);
          this.observableTypes = observableTypes(objects.get(OBSERVABLE));
          this.observables = observables(classes);
          this.schemaLoaded = true;
          return;
        }
        catch (final IOException e)
        {
          throw new IllegalArgumentException("Unable to load the schema file: " + path, e);
        }
      }
      else
      {
        logger.warn("Schema file '{}' not found", path);
      }
    }
    else
    {
      logger.info("No schema file");
    }

    this.objects = Collections.emptyMap();
    this.classes = Collections.emptyMap();
    this.types = Collections.emptyMap();
    this.observableTypes = Collections.emptyMap();
    this.observables = Collections.emptyMap();
    this.schemaLoaded = false;
  }

  /**
   * Enriches the event data using the loaded schema.
   *
   * @param data the original event
   * @return enriched event data
   */
  public Map<String, Object> enrich(final Map<String, Object> data)
  {
    if (schemaLoaded)
    {
      final Map<String, Object> type = eventClassType(data);

      // If the class_id does not exist, then return the data as-is
      return type != null ? enrich(data, type) : data;
    }

    return data;
  }

  /**
   * Returns the schema class for the given class ID.
   *
   * @param classId the class ID as defined in the schema
   * @return the class definition
   */
  public Optional<Map<String, Object>> getClass(final int classId)
  {
    return Optional.ofNullable(classes.get(classId));
  }

  /**
   * Returns the observables associated with the given class ID.
   *
   * @param classId the class ID as defined in the schema
   * @return a list of observables
   */
  public Optional<List<Map<String, Object>>> getObservables(final int classId)
  {
    return Optional.ofNullable(observables.get(classId));
  }

  /**
   * Returns the observables associated with the given class ID and observable type ID.
   *
   * @param classId the class ID as defined in the schema
   * @param typeId  the observable type ID as defined in the schema
   * @return a list of observables
   */
  public Optional<Map<String, Map<String, Object>>> getObservables(final int classId,
    final Observables.TypeID typeId)
  {
    return Observables.filter(observables.get(classId), typeId);
  }

  /**
   * Returns the associations for the given class ID.
   *
   * @param classId the class ID as defined in the schema
   * @return the class associations
   */
  public Optional<Associations> getAssociations(final int classId)
  {
    final Map<String, Object> data = classes.get(classId);

    if (data != null)
    {
      final Map<String, List<String>> associations =
        Maps.typecast(data.get(Dictionary.ASSOCIATIONS));
      if (associations != null && !associations.isEmpty())
        return Optional.of(new Associations(associations));
    }

    return Optional.empty();
  }


  /**
   * Returns the schema object for the given object name.
   *
   * @param name the object name as defined in the schema
   * @return the object definition
   */
  public Optional<Map<String, Object>> getObject(final String name)
  {
    return Optional.ofNullable(objects.get(name));
  }

  static final int makeEventUid(final int classId, final int id)
  {
    return id >= 0 ? classId * 100 + id : Schema.OTHER_ID;
  }

  private static Map<String, Map<String, Object>> types(final Map<String, Map<String, Object>> schema)
  {
    return Maps.typecast(schema.get(TYPES));
  }

  private static Map<String, Map<String, Object>> objects(final Map<String, Map<String, Object>> schema)
  {
    return Maps.typecast(schema.get(OBJECTS));
  }

  private static Map<Integer, Map<String, Object>> classes(final Map<String, Map<String, Object>> map)
  {
    final Map<String, Map<String, Object>>  schema  = Maps.typecast(map.get(CLASSES));
    final Map<Integer, Map<String, Object>> classes = new HashMap<>(schema.size());

    schema.forEach((name, type) -> {
      final Integer uid = (Integer) type.get(UID);
      if (uid != null)
      {
        classes.put(uid, type);
      }
      else
      {
        logger.warn("Class '{}' does not have uid", type.get(name));
      }
    });

    return classes;
  }

  private static Map<Integer, String> observableTypes(final Map<String, Object> observable)
  {
    final Map<String, Map<String, Object>> types = Maps.typecast(Maps.getIn(observable,
      ATTRIBUTES, TYPE_ID, ENUM));

    final Map<Integer, String> map = new HashMap<>(types.size());

    types.forEach((name, value) -> map.put(Integer.valueOf(name), (String) value.get(CAPTION)));

    return map;
  }


  private Map<String, Object> eventClassType(final Map<String, Object> data)
  {
    final Object classId = data.get(Dictionary.CLASS_ID);
    if (classId instanceof Integer)
    {
      final Map<String, Object> type = classes.get(((Integer) classId));

      if (type != null)
      {
        if (logger.isDebugEnabled())
          logger.debug("Translating event class: {}", classId);

        return type;
      }
    }

    logger.warn("Event class ID '{}' not found", classId);
    return null;
  }

  private Map<String, Object> enrich(
    final Map<String, Object> data, final Map<String, Object> type)
  {
    final int classId = (int) type.get(UID);

    final Integer activity = (Integer) data.getOrDefault(Dictionary.ACTIVITY_ID,
      Dictionary.UNKNOWN_ID);

    final int uid = makeEventUid(classId, activity);

    data.put(Dictionary.TYPE_UID, uid);

    if (translateEnums)
    {
      final List<Map<String, Object>> observables = new ArrayList<>();
      final Map<String, Object>       enriched    = new HashMap<>(data.size());

      enrich(null, data, type, enriched, observables);

      if (generateObservables)
      {
        if (!observables.isEmpty())
          enriched.put(Dictionary.OBSERVABLES, observables);
      }

      return enriched;
    }

    return data;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> enrich(
    final String parent,
    final Map<String, Object> data,
    final Map<String, Object> type,
    final Map<String, Object> enriched,
    final List<Map<String, Object>> observables)
  {
    final Map<String, Object> attributes = (Map<String, Object>) type.get(ATTRIBUTES);

    data.forEach((name, value) -> {
      final String path = parent != null ? parent + "." + name : name;

      final Map<String, Object> attribute = (Map<String, Object>) attributes.get(name);
      if (attribute != null)
      {
        final Map<String, Object> enumeration = (Map<String, Object>) attribute.get(ENUM);

        if (enumeration != null)
        {
          updateEnum(enriched, enumeration, enumSibling(name, attribute), value);
        }
        else if (value instanceof Map<?, ?>)
        {
          value = enrichEmbeddedObject(
            path, (String) attribute.get(OBJECT_TYPE), (Map<String, Object>) value, observables);
        }
        else if (value instanceof List<?>)
        {
          if (Boolean.TRUE.equals(attribute.get(IS_ARRAY)))
          {
            value = enrichEmbeddedArray(path, (String) attribute.get(OBJECT_TYPE),
              (List<Object>) value, observables);
          }
          else
          {
            logger.warn("SCHEMA: Attribute {} is not an array in the schema", name);
          }
        }
        else
        {
          final String attrType = (String) attribute.get(TYPE);
          if (attrType != null)
          {
            final Map<String, Object> typeObj = types.get(attrType);
            if (typeObj != null)
            {
              updateObservables(observables, (Integer) typeObj.get(OBSERVABLE), path, value);
            }
            else
            {
              logger.warn("SCHEMA: Attribute '{}' in class {} has an invalid type: {}", name,
                type.get(CAPTION), attrType);
            }
          }
          else
          {
            logger.warn("SCHEMA: Attribute '{}' in class {} does not have type", name,
              type.get(CAPTION));
          }
        }
      }

      enriched.put(name, value);
    });

    return enriched;
  }

  private static void updateEnum(
    final Map<String, Object> enriched,
    final Map<String, Object> enumeration,
    final String name,
    final Object value)
  {
    if (name != null && !enriched.containsKey(name))
    {
      Maps.put(enriched, name, Maps.getIn(enumeration, String.valueOf(value), CAPTION));
    }
  }


  private static String enumSibling(final String name, final Map<String, Object> enumeration)
  {
    final String key = (String) enumeration.get(ENUM_SIBLING);
    if (key == null)
    {
      final int pos = name.indexOf(ENUM_SUFFIX);
      return pos > 0 ? name.substring(0, pos) : null;
    }

    return key;
  }

  private Object enrichEmbeddedObject(
    final String name,
    final String obj,
    final Map<String, Object> value,
    final List<Map<String, Object>> observables)
  {
    if (obj != null)
    {
      final Map<String, Object> object = objects.get(obj);
      if (object != null)
      {
        if (logger.isTraceEnabled())
          logger.trace("Embedded object, name: {}, type: {}", name, obj);

        updateObservables(observables, (Integer) object.get(OBSERVABLE), name);

        return enrich(name, value, object, new HashMap<>(value.size()), observables);
      }
      else
      {
        logger.error("SCHEMA: attribute {} has invalid object type: {}", name, obj);
      }
    }
    else
    {
      logger.warn("SCHEMA: Attribute {} is not an object in the schema", name);
    }

    return value;
  }

  @SuppressWarnings("unchecked")
  private Object enrichEmbeddedArray(
    final String name,
    final String obj,
    final List<Object> list,
    final List<Map<String, Object>> observables)
  {
    if (!list.isEmpty() && list.get(0) instanceof Map<?, ?>)
    {
      if (obj != null)
      {
        final Map<String, Object> object = objects.get(obj);
        if (object != null)
        {
          final ArrayList<Map<String, Object>> array = new ArrayList<>(list.size());

          if (logger.isTraceEnabled())
            logger.trace("Embedded array, name: {}, type: {}", name, obj);

          list.forEach(i -> {
            final Map<String, Object> o = (Map<String, Object>) i;

            array.add(enrich(name, o, object, new HashMap<>(o.size()), observables));
          });

          return array;
        }
        else
        {
          logger.error("SCHEMA: attribute {} has invalid object type: {}", name, obj);
        }
      }
      else
      {
        logger.warn("SCHEMA: Array {}'s type is not an object in the schema", name);
      }
    }

    return list;
  }

  private Map<Integer, List<Map<String, Object>>>
  observables(final Map<Integer, Map<String, Object>> classes
  )
  {
    final Map<Integer, List<Map<String, Object>>> observables = new HashMap<>();

    classes.forEach((id, map) -> {
      final List<Map<String, Object>> acc = new ArrayList<>();

      observables(null, map, acc);
      observables.put(id, acc);
    });

    return observables;
  }

  @SuppressWarnings("unchecked")
  private void observables(
    final String parent,
    final Map<String, Object> type,
    final List<Map<String, Object>> observables)
  {
    final Map<String, Map<String, Object>> attributes =
      (Map<String, Map<String, Object>>) type.get(Schema.ATTRIBUTES);

    attributes.forEach((name, attribute) -> {
      final String path = parent != null ? parent + "." + name : name;

      if (Boolean.TRUE.equals(attribute.get(Schema.IS_ARRAY)))
      {
        // TODO: for now, ignore the arrays
        logger.debug("Array {} of {}", path, attribute.get(Schema.OBJECT_TYPE));
      }
      else
      {
        final String objectType = (String) attribute.get(Schema.OBJECT_TYPE);
        if (objectType != null)
        {
          objectObservables(path, objects.get(objectType), observables);
        }
        else
        {
          final String attrType = (String) attribute.get(Schema.TYPE);
          if (attrType != null)
          {
            final Map<String, Object> typeObj = types.get(attrType);
            if (typeObj != null)
            {
              updateObservables(observables, (Integer) typeObj.get(Schema.OBSERVABLE), path);
            }
            else
            {
              logger.warn("SCHEMA: Attribute '{}' in class {} has an invalid type: {}", name,
                type.get(Schema.CAPTION), attrType);
            }
          }
          else
          {
            logger.warn("SCHEMA: Attribute '{}' in class {} does not have type", name,
              type.get(Schema.CAPTION));
          }
        }
      }
    });

  }

  private void objectObservables(
    final String name,
    final Map<String, Object> object,
    final List<Map<String, Object>> observables)
  {
    if (object != null)
    {
      if (logger.isTraceEnabled())
        logger.trace("Embedded object, name: {}, type: {}", name, object);

      if (!name.endsWith("parent_process.parent_process"))
      {
        updateObservables(observables, (Integer) object.get(Schema.OBSERVABLE), name);
        observables(name, object, observables);
      }
    }
    else
    {
      logger.error("SCHEMA: attribute {} has invalid object type", name);
    }
  }

  private void updateObservables(
    final List<Map<String, Object>> observables, final Integer typeId, final String name)
  {
    if (typeId != null)
    {
      final FMap<String, Object> observable = FMap.<String, Object>b()
        .p(NAME, name)
        .p(TYPE, observableTypes.getOrDefault(typeId, OTHER))
        .p(TYPE_ID, typeId);

      observables.add(observable);
    }
  }

  private void updateObservables(
    final List<Map<String, Object>> observables, final Integer typeId, final String name,
    final Object value)
  {
    if (typeId != null)
    {
      final FMap<String, Object> observable = FMap.<String, Object>b()
        .p(NAME, name)
        .p(TYPE, observableTypes.getOrDefault(typeId, "Other"))
        .p(TYPE_ID, typeId)
        .p(VALUE, value);

      observables.add(observable);
    }
  }
}
