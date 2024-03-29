/*
 * Copyright 2024 Splunk Inc.
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
import io.ocsf.utils.Strings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.ocsf.utils.Files.readJson;

/**
 * A helper class to enrich event data using the schema.
 * <p>
 * The enrichment adds:
 * <ul>
 *  <li>the <code>type_uid</code> attribute</li>
 *  <li>the textual values, aka siblings, of the enum attributes</li>
 *  <li>the <code>observables</code> associated with the event</li>
 * </ul>
 * <p>
 * The <code>type_uid</code> value is calculated based on the <code>class_uid</code> and
 * <code>activity_id</code> values.
 * </p>
 * <pre>
 *  Activity: type_uid = class_uid * 100 + activity_id
 * </pre>
 */
public final class Schema {
  private static final Logger logger = LogManager.getLogger(Schema.class);

  // Schema metadata/attribute property names
  private static final String TYPES = "types";
  private static final String OBJECTS = "objects";
  private static final String CLASSES = "classes";
  private static final String DICTIONARY_ATTRIBUTES = "dictionary_attributes";

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

  // All event classes: class_id -> class definition
  private final Map<Integer, Map<String, Object>> classes;

  // All objects: object_type -> object definition
  private final Map<String, Map<String, Object>> objects;

  // All types: type name -> type definition
  private final Map<String, Map<String, Object>> types;

  // All dictionary attributes: attribute name -> attribute definition
  private final Map<String, Map<String, Object>> dictionaryAttributes;

  // Observable type_id -> String
  private final Map<Integer, String> observableTypes;

  // All event observables: class_id -> observables (name -> observable)
  private final Object lazyLoadGuardClassToObservablesMap = new Object();
  private Map<Integer, List<Map<String, Object>>> _classToObservablesMap;

  private final boolean defaultAddEnumSiblings;
  private final boolean defaultAddObservables;

  private final boolean schemaLoaded;

  /**
   * Load the schema in memory. The schema enrichment defaults to adding the type_uid only.
   * <p>
   * Note: This class caches the entire schema in memory, thus use a single instance per JVM.
   *
   * @param path the schema JSON file
   */
  public Schema(final Path path) {
    this(path, false, false);
  }

  /**
   * Load the schema in memory.
   * <p>
   * Note: This class caches the entire schema in memory, thus use a single instance per JVM.
   *
   * @param path                   the schema JSON file
   * @param defaultAddEnumSiblings if true, enhance the event data by adding the enumerated text
   *                               values.
   * @param defaultAddObservables  if true, enhance the event data by adding the observables
   *                               associated with the event.
   */
  public Schema(
      final Path path, final boolean defaultAddEnumSiblings, final boolean defaultAddObservables
  ) {
    this.defaultAddEnumSiblings = defaultAddEnumSiblings;
    this.defaultAddObservables = defaultAddObservables;

    if (path != null) {
      if (logger.isInfoEnabled()) {
        logger.info(
            "Using schema file: {}, addEnumSiblings: {}, addObservables: {}",
            Strings.quote(path), this.defaultAddEnumSiblings, this.defaultAddObservables
        );
      }

      if (Files.isRegularFile(path)) {
        try {
          final Map<String, Map<String, Object>> schema = readJson(path);

          this.objects = objects(schema);
          this.classes = classes(schema);
          this.types = types(schema);
          this.dictionaryAttributes = dictionaryAttributes(schema);
          this.observableTypes = observableTypes(objects.get(OBSERVABLE));
          // Lazy load this._classToObservablesMap; it takes roughly half the schema load time
          this.schemaLoaded = true;
          return;
        } catch (final IOException e) {
          throw new IllegalArgumentException("Unable to load the schema file: " + path, e);
        }
      } else {
        logger.warn("Schema file not found: {}", path);
      }
    } else {
      logger.info("No schema file");
    }

    this.objects = Collections.emptyMap();
    this.classes = Collections.emptyMap();
    this.types = Collections.emptyMap();
    this.dictionaryAttributes = Collections.emptyMap();
    this.observableTypes = Collections.emptyMap();
    this._classToObservablesMap = Collections.emptyMap(); // always empty in this case
    this.schemaLoaded = false;
  }

  /**
   * Enriches the event data using the default enrichment configuration.
   *
   * @param data the original event
   * @return enriched event data
   */
  public Map<String, Object> enrich(final Map<String, Object> data) {
    return enrich(data, defaultAddEnumSiblings, defaultAddObservables);
  }

  /**
   * Enriches the event data using the loaded schema.
   *
   * @param data            the original event
   * @param addEnumSiblings if true, enhance the event data by adding the enumerated text values.
   * @param addObservables  if true, enhance the event data by adding the observables associated
   *                        with the event.
   * @return enriched event data
   */
  public Map<String, Object> enrich(
      final Map<String, Object> data, final boolean addEnumSiblings, final boolean addObservables
  ) {
    if (schemaLoaded) {
      final Map<String, Object> classType = eventClassType(data);

      // Only enrich known event classes
      if (classType != null) {
        Utils.addTypeUid(data);

        if (addEnumSiblings || addObservables) {
          // Class-specific attribute path observables definitions are in the optional
          // "observables" attributes of each class. It is a map of attribute paths to
          // observable type_id values.
          final Map<String, Integer> pathObservables;
          // The accumulating list of observables
          final List<Map<String, Object>> observables;
          if (addObservables) {
            pathObservables = Maps.get(classType, Dictionary.OBSERVABLES);
            observables = new ArrayList<>();
          } else {
            pathObservables = null;
            observables = null;
          }
          final Map<String, Object> enriched = new HashMap<>(data.size());

          enrich(
              null, pathObservables, data, classType, addEnumSiblings, addObservables,
              enriched, observables
          );

          if (addObservables && !observables.isEmpty()) {
            enriched.put(Dictionary.OBSERVABLES, observables);
          }

          return enriched;
        }
      }
    }

    return data;
  }

  /**
   * Returns the schema class for the given class ID.
   *
   * @param classId the class ID as defined in the schema
   * @return the class definition
   */
  public Optional<Map<String, Object>> getClass(final int classId) {
    return Optional.ofNullable(classes.get(classId));
  }

  /**
   * Returns the schema object for the given object name.
   *
   * @param name the object name as defined in the schema
   * @return the object definition
   */
  public Optional<Map<String, Object>> getObject(final String name) {
    return Optional.ofNullable(objects.get(name));
  }

  private static Map<String, Map<String, Object>> types(
      final Map<String, Map<String, Object>> schema
  ) {
    return Maps.typecast(schema.get(TYPES));
  }

  private static Map<String, Map<String, Object>> objects(
      final Map<String, Map<String, Object>> schema
  ) {
    return Maps.typecast(schema.get(OBJECTS));
  }

  private static Map<Integer, Map<String, Object>> classes(
      final Map<String, Map<String, Object>> map
  ) {
    final Map<String, Map<String, Object>> schema = Maps.typecast(map.get(CLASSES));
    final Map<Integer, Map<String, Object>> classes = new HashMap<>(schema.size());

    schema.forEach((name, type) -> {
      final Integer uid = (Integer) type.get(UID);
      if (uid != null) {
        classes.put(uid, type);
      } else {
        logger.warn("Class {} does not have uid", Strings.quote(type.get(name)));
      }
    });

    return classes;
  }

  private static Map<String, Map<String, Object>> dictionaryAttributes(
      final Map<String, Map<String, Object>> schema
  ) {
    // This was added later, so may not be present on schemas generated from older OCSF Servers.
    // So this checks for null before returning, and otherwise returns an empty map.
    final Object raw = schema.get(DICTIONARY_ATTRIBUTES);
    if (raw != null) {
      return Maps.typecast(raw);
    }
    return Collections.emptyMap();
  }

  private static Map<Integer, String> observableTypes(final Map<String, Object> observable) {
    final Map<String, Map<String, Object>> types = Maps.typecast(
        Maps.getIn(observable, ATTRIBUTES, TYPE_ID, ENUM));

    final Map<Integer, String> map = new HashMap<>(types.size());

    types.forEach((name, value) -> map.put(Integer.valueOf(name), (String) value.get(CAPTION)));

    return map;
  }


  private Map<String, Object> eventClassType(final Map<String, Object> data) {
    final Object classId = data.get(Dictionary.CLASS_UID);
    if (classId instanceof Integer) {
      final Map<String, Object> classType = classes.get(((Integer) classId));

      if (classType != null) {
        if (logger.isDebugEnabled()) {
          logger.debug(
              "Enriching event of class ID {}: {}",
              classId, Strings.quote(classType.get(CAPTION))
          );
        }
        return classType;
      }
    }

    logger.debug("Event class ID not found: {}", classId);
    return null;
  }

  /**
   * Enrich a class or object, which is passed in via the type parameter. This is a recursive
   * function using the enriched and observables parameters as accumulators.
   *
   * @param parentAttributePath null or the dotted attribute path from the caller
   * @param pathObservables     null or the class-specific attribute path observables map
   * @param data                the class event or object instance to enrich
   * @param type                the class or object type information
   * @param addEnumSiblings     when true, enum sibling text is added to the event
   * @param addObservables      when true, observables are added to the event
   * @param enriched            accumulator for the enriched event or object with enum siblings
   *                            added if addEnumSiblings is true
   * @param observables         accumulator holding observables generated if the addObservables flag
   *                            is set; the caller is responsible for adding this to the event
   * @return enriched event or object for this level used for recursive calls to put into
   * enriched parameter
   */
  @SuppressWarnings("unchecked")
  private Map<String, Object> enrich(
      final String parentAttributePath,
      final Map<String, Integer> pathObservables,
      final Map<String, Object> data,
      final Map<String, Object> type,
      final boolean addEnumSiblings,
      final boolean addObservables,
      final Map<String, Object> enriched,
      final List<Map<String, Object>> observables
  ) {
    final Map<String, Object> attributes = (Map<String, Object>) type.get(ATTRIBUTES);

    data.forEach((attributeName, value) -> {
      final String attributePath
          = parentAttributePath != null ? parentAttributePath + "." + attributeName : attributeName;

      final Map<String, Object> attribute = (Map<String, Object>) attributes.get(attributeName);
      // Only enrich when this is a known attribute AND it isn't the json_t type.
      // The json_t type mean any type, and traversing in to it when it is an array or object
      // would confuse the enrichment logic since it would not have a known OCSF attribute type.
      if (attribute != null && !"json_t".equals(attribute.get(TYPE))) {
        final Map<String, Object> enumeration = (Map<String, Object>) attribute.get(ENUM);

        if (enumeration != null) {
          if (addEnumSiblings) {
            updateEnum(enriched, enumeration, enumSibling(attributeName, attribute), value);
          }
        } else if (value instanceof Map<?, ?>) {
          value = enrichEmbeddedObject(
              attributePath, pathObservables,
              (String) attribute.get(OBJECT_TYPE), (Map<String, Object>) value, addEnumSiblings,
              addObservables, observables
          );
        } else if (value instanceof List<?>) {
          if (Boolean.TRUE.equals(attribute.get(IS_ARRAY))) {
            value = enrichEmbeddedArray(
                attributePath, attribute, attributePath, pathObservables,
                (List<Object>) value, addEnumSiblings, addObservables, observables
            );
          } else {
            if (logger.isDebugEnabled()) {
              logger.debug("SCHEMA: Attribute {} is not an array in the schema",
                  Strings.quote(attributeName)
              );
            }
          }
        } else if (addObservables) {
          // Generate observable for primitive type
          final Integer observableTypeID
              = getObservableTypeID(attributeName, attribute, attributePath, pathObservables);
          addNewObservable(observables, observableTypeID, attributePath, value, addEnumSiblings);
        }
      }

      enriched.put(attributeName, value);
    });

    return enriched;
  }

  private static void updateEnum(
      final Map<String, Object> enriched, final Map<String, Object> enumeration, final String name,
      final Object value
  ) {
    if (name != null && !enriched.containsKey(name)) {
      Maps.put(enriched, name, Maps.getIn(enumeration, String.valueOf(value), CAPTION));
    }
  }


  private static String enumSibling(final String name, final Map<String, Object> enumeration) {
    final String key = (String) enumeration.get(ENUM_SIBLING);
    if (key == null) {
      final int pos = name.indexOf(ENUM_SUFFIX);
      return pos > 0 ? name.substring(0, pos) : null;
    }

    return key;
  }

  private Object enrichEmbeddedObject(
      final String attributePath,
      final Map<String, Integer> pathObservables,
      final String objectType,
      final Map<String, Object> value,
      final boolean addEnumSiblings,
      final boolean addObservables,
      final List<Map<String, Object>> observables
  ) {
    if (objectType != null) {
      final Map<String, Object> object = objects.get(objectType);
      if (object != null) {
        if (logger.isTraceEnabled()) {
          logger.trace(
              "Embedded object - attribute path: {}, object type: {}",
              Strings.quote(attributePath), Strings.quote(objectType)
          );
        }

        if (addObservables) {
          addNewObservable(
              observables, (Integer) object.get(OBSERVABLE), attributePath, addEnumSiblings
          );
        }

        return enrich(
            attributePath, pathObservables, value, object, addEnumSiblings, addObservables,
            new HashMap<>(value.size()), observables
        );
      } else {
        if (logger.isDebugEnabled()) {
          logger.debug(
              "SCHEMA: Attribute {} has invalid object type: {}",
              Strings.quote(attributePath), Strings.quote(objectType)
          );
        }
      }
    } else {
      if (logger.isDebugEnabled()) {
        logger.debug(
            "SCHEMA: Attribute {} is not an object in the schema",
            Strings.quote(attributePath)
        );
      }
    }

    return value;
  }

  @SuppressWarnings("unchecked")
  private Object enrichEmbeddedArray(
      final String attributeName,
      final Map<String, Object> attribute,
      final String attributePath,
      final Map<String, Integer> pathObservables,
      final List<Object> list,
      final boolean addEnumSiblings,
      final boolean addObservables,
      final List<Map<String, Object>> observables
  ) {
    if (!list.isEmpty() && list.get(0) instanceof Map<?, ?>) {
      final String objectType = (String) attribute.get(OBJECT_TYPE);
      if (objectType != null) {
        final Map<String, Object> object = objects.get(objectType);
        if (object != null) {
          final ArrayList<Map<String, Object>> array = new ArrayList<>(list.size());

          if (logger.isTraceEnabled()) {
            logger.trace(
                "Embedded array - attribute path: {}, object type: {}",
                Strings.quote(attributePath), Strings.quote(objectType)
            );
          }

          list.forEach(i -> {
            final Map<String, Object> o = (Map<String, Object>) i;

            array.add(
                enrich(
                    attributeName, pathObservables, o, object, addEnumSiblings, addObservables,
                    new HashMap<>(o.size()), observables
                ));
          });

          return array;
        } else {
          if (logger.isDebugEnabled()) {
            logger.debug(
                "SCHEMA: Attribute {} has invalid object type: {}",
                Strings.quote(attributeName), Strings.quote(objectType)
            );
          }
        }
      } else {
        if (logger.isDebugEnabled()) {
          logger.debug(
              "SCHEMA: Array {} type is not an object in the schema",
              Strings.quote(attributeName)
          );
        }
      }
    } else {
      // Generate observables for primitive types in array
      if (addObservables) {
        final Integer observableTypeID = getObservableTypeID(
            attributeName, attribute, attributePath, pathObservables);
        if (observableTypeID != null) {
          list.forEach(
              value -> addNewObservable(
                  observables, observableTypeID, attributePath, value, addEnumSiblings
              ));
        }
      }
    }

    return list;
  }

  private Integer getObservableTypeID(
      final String attributeName,
      final Map<String, Object> attribute,
      final String attributePath,
      final Map<String, Integer> pathObservables
  ) {
    // First, look for observable type_id by attribute type
    final Object attributeTypeName = attribute.get(TYPE);
    if (attributeTypeName instanceof String) {
      final Map<String, Object> attributeType = types.get(attributeTypeName);
      if (attributeType != null) {
        final Object observableTypeID = attributeType.get(OBSERVABLE);
        if (observableTypeID instanceof Integer) {
          if (logger.isTraceEnabled()) {
            logger.trace(
                "getObservableTypeID - observable by attribute type {} for {}"
                    + " on attribute path {}: {}",
                Strings.quote(attributeTypeName), Strings.quote(attributeName),
                Strings.quote(attributePath), observableTypeID
            );
          }
          return (Integer) observableTypeID;
        } else if (observableTypeID != null) {
          logger.debug("SCHEMA: Attribute type has invalid \"observable\": {}", attributeType);
        }
      } else if (logger.isDebugEnabled()) {
        logger.debug(
            "SCHEMA: Attribute {} has an invalid type: {}",
            Strings.quote(attributeName), attributeTypeName
        );
      }
    } else if (logger.isDebugEnabled()) {
      logger.debug("SCHEMA: Attribute {} does not have type", Strings.quote(attributeName));
    }

    // Second, look for observable type_id by dictionary attribute
    final Object typeIDByDictionaryAttribute
        = Maps.getIn(dictionaryAttributes, attributeName, OBSERVABLE);
    if (typeIDByDictionaryAttribute instanceof Integer) {
      if (logger.isTraceEnabled()) {
        logger.trace("getObservableTypeID - observable by dictionary attribute {}: {}",
            Strings.quote(attributeName), typeIDByDictionaryAttribute
        );
      }
      return (Integer) typeIDByDictionaryAttribute;
    } else if (typeIDByDictionaryAttribute != null && logger.isDebugEnabled()) {
      logger.debug(
          "SCHEMA: Dictionary attribute {} has an invalid \"observable\": {}",
          Strings.quote(attributeName), typeIDByDictionaryAttribute
      );
    }

    // Third, look for observable type_id by type-specific attribute (class / object specific)
    final Object typeIDByAttribute = attribute.get(OBSERVABLE);
    if (typeIDByAttribute instanceof Integer) {
      if (logger.isTraceEnabled()) {
        logger.trace(
            "getObservableTypeID - observable for {} on attribute path {}: {}",
            Strings.quote(attributeName), Strings.quote(attributePath), typeIDByAttribute
        );
      }
      return (Integer) typeIDByAttribute;
    } else if (typeIDByAttribute != null && logger.isDebugEnabled()) {
      logger.debug(
          "SCHEMA: Attribute {} has an invalid \"observable\": {}",
          Strings.quote(attributeName), typeIDByAttribute
      );
    }

    // Third, look for observable by class-path attribute path
    if (pathObservables != null) {
      final Integer typeIDByPath = pathObservables.get(attributePath);
      if (typeIDByPath != null) {
        if (logger.isTraceEnabled()) {
          logger.trace(
              "getObservableTypeID - class-specific observable for {} on attribute path {}: {}",
              Strings.quote(attributeName), Strings.quote(attributePath), typeIDByPath
          );
        }
        return typeIDByPath;
      }
    }

    return null;
  }

  private void addNewObservable(
      final List<Map<String, Object>> observables,
      final Integer typeId,
      final String attributePath,
      final boolean addEnumSiblings
  ) {
    if (typeId != null) {
      if (addEnumSiblings) {
        observables.add(
            Map.of(
                NAME, attributePath,
                TYPE, observableTypes.getOrDefault(typeId, Dictionary.OTHER),
                TYPE_ID, typeId
            )
        );
      } else {
        observables.add(
            Map.of(
                NAME, attributePath,
                TYPE_ID, typeId
            )
        );
      }
    }
  }

  private void addNewObservable(
      final List<Map<String, Object>> observables, final Integer typeId, final String attributePath,
      final Object value, final boolean addEnumSiblings
  ) {
    if (typeId != null && value != null) {
      if (addEnumSiblings) {
        observables.add(
            Map.of(
                NAME, attributePath,
                TYPE, observableTypes.getOrDefault(typeId, Dictionary.OTHER),
                TYPE_ID, typeId,
                VALUE, value.toString()
            )
        );
      } else {
        observables.add(
            Map.of(
                NAME, attributePath, TYPE_ID,
                typeId, VALUE, value.toString()
            )
        );
      }
    }
  }

  private Map<Integer, List<Map<String, Object>>> classToObservablesMap() {
    synchronized (lazyLoadGuardClassToObservablesMap) {
      if (_classToObservablesMap == null) {
        logger.debug("Lazily creating class to observables map");
        _classToObservablesMap = buildClassToObservablesMap(classes);
      }
      return _classToObservablesMap;
    }
  }

  private Map<Integer, List<Map<String, Object>>> buildClassToObservablesMap(
      final Map<Integer, Map<String, Object>> classes
  ) {
    final Map<Integer, List<Map<String, Object>>> classToObservables = new HashMap<>();
    classes.forEach((id, map) -> {
      final List<Map<String, Object>> acc = new ArrayList<>();
      buildClassToObservablesMap(null, map, acc);
      classToObservables.put(id, acc);
    });
    return classToObservables;
  }

  @SuppressWarnings("unchecked")
  private void buildClassToObservablesMap(
      final String parent,
      final Map<String, Object> type,
      final List<Map<String, Object>> classToObservables
  ) {
    final Map<String, Map<String, Object>> attributes
        = (Map<String, Map<String, Object>>) type.get(Schema.ATTRIBUTES);
    attributes.forEach((name, attribute) -> {
      final String path = parent != null ? parent + "." + name : name;
      if (Boolean.TRUE.equals(attribute.get(Schema.IS_ARRAY))) {
        // TODO: for now, ignore the arrays
        logger.debug("Array {} of {}", path, attribute.get(Schema.OBJECT_TYPE));
      } else {
        final String objectType = (String) attribute.get(Schema.OBJECT_TYPE);
        if (objectType != null) {
          buildClassToObservablesMapForObject(path, objects.get(objectType), classToObservables);
        } else {
          final String attrType = (String) attribute.get(Schema.TYPE);
          if (attrType != null) {
            final Map<String, Object> typeObj = types.get(attrType);
            if (typeObj != null) {
              addNewObservable(
                  classToObservables, (Integer) typeObj.get(Schema.OBSERVABLE),
                  path, defaultAddEnumSiblings
              );
            } else {
              logger.warn(
                  "SCHEMA: Attribute {} in class {} has an invalid type: {}",
                  Strings.quote(name), Strings.quote(type.get(Schema.CAPTION)), attrType
              );
            }
          } else {
            logger.warn(
                "SCHEMA: Attribute {} in class {} does not have type",
                Strings.quote(name), Strings.quote(type.get(Schema.CAPTION))
            );
          }
        }
      }
    });
  }

  private void buildClassToObservablesMapForObject(
      final String name,
      final Map<String, Object> object,
      final List<Map<String, Object>> classToObservables
  ) {
    if (object != null) {
      if (logger.isTraceEnabled()) {
        logger.trace("Embedded object, name: {}, type: {}", Strings.quote(name), object);
      }
      if (Strings.isPathLooped(name)) {
        if (logger.isDebugEnabled()) {
          logger.debug(
              "Looped object path {}, object name: {}", Strings.quote(name), object.get("name")
          );
        }
      } else {
        addNewObservable(
            classToObservables, (Integer) object.get(Schema.OBSERVABLE),
            name, defaultAddEnumSiblings
        );
        buildClassToObservablesMap(name, object, classToObservables);
      }
    } else {
      logger.warn("SCHEMA: Attribute {} has invalid object type", Strings.quote(name));
    }
  }

  /**
   * Returns the observables associated with the given class ID.
   *
   * @param classId the class ID as defined in the schema
   * @return a list of observables
   */
  public Optional<List<Map<String, Object>>> getObservables(final int classId) {
    return Optional.ofNullable(classToObservablesMap().get(classId));
  }

  /**
   * Returns the observables associated with the given class ID and observable type ID.
   *
   * @param classId the class ID as defined in the schema
   * @param typeId  the observable type ID as defined in the schema
   * @return a list of observables
   */
  public Optional<Map<String, Map<String, Object>>> getObservables(
      final int classId, final int typeId
  ) {
    return Observables.filter(classToObservablesMap().get(classId), typeId);
  }
}
