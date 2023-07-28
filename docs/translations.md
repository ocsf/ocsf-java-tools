# Translations

** DRAFT **

This document describes the Data Translation Domain Specific Language syntax.

## Translation File
The translations are saved in JSON files with the following format:

```json5
{
  "caption": "Translation Caption",
  "description": "Translation description.",
  "references": [
    "https://schema.ocsf.io/base_event"
  ],
  
  // Boolean predicate/guard 
  "when": "event_code = 22",

  // List of mapping rules
  "rules": [
    {
      "<src-attribute>": {
        "@operand": {
          // a set of properties, see below for more details
        }
      }
    }
  ]
}

```

Where:
   - `caption` -- short description optional, useful for documentation
   - `description` -- description of the translation rule,  optional, useful for documentation
   - `references` -- list of reference to external sources, optional, useful for documentation

   - `when` -- boolean predicate. If it evaluates as true, then the mappings defined in the rules array are executed in order. The `when` condition is optional. If it is omitted, then the mappings will be executed only is none of the other the mappings are used. Note, you can only have one translation file without the `when` clause.

   - `rules` -- list of mapping rules, each rule is an object. The rule format is show below.

The mapping rule format:
```json5
{
   "<src-attribute>": {
      "@operand": {
         "name": "<dst-attribute>",
         "type": "<type-name>",
         "overwrite": [true, false],
         "default": "<data>"
      }
   }
}
```


### Move

Move an attribute from the source data to the translated data. The translated
attribute name can be the same or different from the source attribute name. 
The attribute is removed from the source data.

#### Short Format

```json
{
   "<src-attribute>": {
      "@move": "<dst-attribute>"
   }
}
```

##### Example

Rule:

```json5
{
   "src_ip": {
      "@move": "src_endpoint.ip"
   },
   "user.name": {
      "@move": "src_user.name"
   }    
}
```

Input data:

```json5
{
   "src_ip": "1.2.3.4",
   "dst_ip": "5.6.7.8",
   "user": {
      "name": "joe",
      "uid": 0
   }    
}
```

Output data:

```json5
{
   "src_endpoint": {
      "ip": "1.2.3.4"
   },
   "src_user": {
      "name": "joe"
   },
   // untranslated data
   "unmapped": {
      "dst_ip": "5.6.7.8",
      "user": {
         "uid": 0
      }
   }
}
```

#### Long Format

```json5
{
   "<src-attribute>": {
      "@move": {
         "name": "<dst-attribute>",
         "type": "<type-name>",
         "overwrite": [true, false],
         "default": "<data>"
      }
   }
}
```

#### Long Format with Multiple attributes

Use a comma-separated attributes to join data from multiple attributes.

```json5
{
   "<src-attribute-name1>, <src-attribute-name2>, ...": {
      "@move": {
         "name": "<dst-attribute>",
         "type": "<type-name>",
         "separator": "<joiner>",
         "overwrite": [true, false],
         "default": "<data>"
      }
   }
}
```

##### Where

   name        the translated attribute name, required.
   type        the type of the translated value, optional.
               See bellow for the available types.
   separator   the joiner used as a separator.
               If joiner is not specified, then it defaults to an empty string.
   overwrite   the flag to overwrite the translated attribute value if one
               already exists, optional, default: false.
   default     the default value if the source does not have the specified
               attribute, optional.

##### Example

```json5
{
   "path, name": {
      "@move": {
         "name": "process.file",
         "type": "file",
         "separator": "\\"
      }
   }
}
```

### Copy

Copy an attribute value from the source data to the translated data.

Note, the translated attribute name can be the same or different from the source
attribute name. The source data is not affected.

#### Short Format

```json5
{
   "<src-attribute>": {
      "@copy": "<dst-attribute>"
   }
}
```

#### Long Format

```json5
{
   "<src-attribute>": {
      "@copy": {
         "name": "<dst-attribute>",
         "type": "<type-name>",
         "overwrite": [true, false],
         "default": "<data>"
      }
   }
}
```

#### Long Format with Multiple attributes

Use a comma-separated attributes to join data from multiple attributes.

```json5
{
   "<src-attribute-name1>, <src-attribute-name2>, ...": {
      "@copy": {
         "name": "<dst-attribute>",
         "type": "<type-name>",
         "separator": "<joiner>",
         "overwrite": [true, false],
         "default": "<data>"
      }
   }
}
```

##### Where

   name        the translated attribute name, required.
   type        the type of the translated value, optional.
               See bellow for the available types.
   separator   the joiner used as a separator.
               If joiner is not specified, then it defaults to an empty string.
   overwrite   the flag to overwrite the translated attribute value if one
               already exists, optional, default: false.
   default     the default value if the source does not have the specified
               attribute, optional.

### Remove

Remove an attribute from the source data.

#### Short Format

```json5
{
   "<src-attribute>": {
      "@remove": true
   }
}
```

#### Value

Set an attribute in the translated data to the specified value. The value type
can be any valid JSON type. The source data is not affected.

#### Format

```json5
{
   // use underscore to specify that there is no src-attribute  
   "_": {
     // json data
   }
}
```

##### Example

```json5
{
   "_": {
     "category_id": 1,
     "class_uid": 1234,
     "activity_id": 1,
     "metadata": {
      "profiles": [
        "host", "linux"
      ],
      "version": "1.0.0-rc.2"
     }
   }
}
```

Input data:

```json5
{
   "src_ip": "1.2.3.4",
   "dst_ip": "5.6.7.8",
   "user": {
      "name": "joe",
      "uid": 0
   }    
}
```

Output data:

```json5
{
   "category_id": 1,
   "class_uid": 1001,
   "activity_id": 1,
   "metadata": {
      "profiles": [
      "host", "linux"
     ],
     "version": "1.0.0-rc.2"
   },
   "src_ip": "1.2.3.4",
   "dst_ip": "5.6.7.8",
   "user": {
      "name": "joe",
      "uid": 0
   }    
}
```

##### Where

   data        the data to set the attribute value, required, s valid JSON data.
   overwrite   the flag to overwrite the translated attribute if one already
               exists, optional, default: false.

### Enum/Lookup

The `enum` translation rule creates enum value from raw data values.
The attribute is removed from the source data.

#### Format

```json5
{
   "<src-attribute>": {
      "@enum": {
         "name"  : "<dst-attribute>",
         "values": "<lookup-table>",
         "other" : "<type-name>",
         "overwrite": [true, false],
         "default": "<data>"
      }
   }
}
```

The `lookup` translation rule creates enum value from raw data values.
The source data is not affected.

#### Format

```json5
{
   "<src-attribute>": {
      "@lookup": {
         "name"  : "<dst-attribute>",
         "values": "<lookup-table>",
         "other" : "<type-name>",
         "overwrite": [true, false],
         "default": "<data>"
      }
   }
}
```

##### Where

   name        the translated attribute name, required.
   values      the lookup table to translate the values to enums, required.
   other       the other is an attribute to save untranslated value, optional.
   overwrite   the flag to overwrite the translated attribute value if one
               already exists, optional, default: false.
   default     the default value if the source does not have the specified
               attribute, optional.

##### Example

```json5
  {
   "Opcode": {
     "@enum": {
      "name": "status_id",
      "values": {
        "0" : 1,          // Success
        "-1": 2           // Failure
      },
      "default": 0,       // used when the Opcode attribute is missing
      "other": "status"   // if the Opcode attribute has value that is not
                          // "0" or "-1", then save the value in the status
                          // attribute
     }
   }
  }
```

## Type Conversions

You can use the `type` to translate the value data to another type, for example
from string to integer. The following types are supported: string, integer,
long, float, double, timestamp, time, path, downcase, and upcase.

### timestamp

Translates a string to a timestamp, parsing local, zoned, or ISO-like date-time
with the offset and zone if available. Examples:
```
   `2011-12-03T10:15:30`
   `2011-12-03T10:15:30+01:00`
   `2011-12-03T10:15:30+01:00[Europe/Paris]`
```

Other supported formats:
   - Local date-time:  `MM/dd/yy HH:mm:ss`
   - Zoned date-time:  `MM/dd/yy HH:mm:ss z`
   - Long number:      `1681160562325`   // UTC time in ms

Note, if the value is `null`, an empty string, or invalid time, then the
translated attribute is set to the current system time in UTC milliseconds.

### path

Translates a string containing a file path to a `File` object. Both Windows and
Unix name separators are supported.

If the value contain a file path, then the following `File` attributes are set:

   path:              the value
   name:              the name element of the path
   parent_folder:     the parent path

If the value does not contain a path name, then only the `path` attribute of
the `File` object is set.

### downcase

Converts a string to all-lower-case letters.

### upcase

Converts a string to ALL-UPPER-CASE letters.
