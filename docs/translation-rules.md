# Translation

This document describes the supported translation rules.

## Translation rules

### Move

Move a field from the source data to the translated data. Note, the translated field name can be the same or different
than the input field name. The field is removed from the source data.

#### Short Format

```json
{
    "<src-field-name>": {
        "@move": "<dst-field-name>"
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
    "<src-field-name>": {
        "@move": {
            "name": "<dst-field-name>",
            "type": "<type-name>",
            "overwrite": [true|false],
            "default": "<data>"
        }
    }
}
```

#### Long Format with Multiple Fields

Use a comma-separated fields to join data from multiple fields.

```json5
{
    "<src-field-name1>, <src-field-name2>, ...": {
        "@move": {
            "name": "<dst-field-name>",
            "type": "<type-name>",
            "separator": "<joiner>",
            "overwrite": [true|false],
            "default": "<data>"
        }
    }
}
```

##### Where

    name        the translated field name, required.
    type        the type of the translated value, optional.
                See bellow for the available types.
    separator   the joiner used as a separator. If joiner is not used,
                it defaults to an empty string.
    overwrite   the flag to overwrite the translated field if one already
                exists, optional, default: false.
    default     the default value if the source does not have the specified
                field, optional.

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

Copy a field from the source data to the translated data. Note, the translated field name can be the same or different
than the source field name. The source data is not affected.

#### Short Format

```json5
{
    "<src-field-name>": {
        "@copy": "<dst-field-name>"
    }
}
```

#### Long Format

```json5
{
    "<src-field-name>": {
        "@copy": {
            "name": "<dst-field-name>",
            "type": "<type-name>",
            "overwrite": [true|false],
            "default": "<data>"
        }
    }
}
```

#### Long Format with Multiple Fields

Use a comma-separated fields to join data from multiple fields.

```json5
{
    "<src-field-name1>, <src-field-name2>, ...": {
        "@copy": {
            "name": "<dst-field-name>",
            "type": "<type-name>",
            "separator": "<joiner>",
            "overwrite": [true|false],
            "default": "<data>"
        }
    }
}
```

##### Where

    name        the translated field name, required.
    type        the type of the translated value, optional. See bellow
                for the available types.
    separator   the joiner used as a separator. If joiner is not used,
                it defaults to an empty string.
    overwrite   the flag to overwrite the translated field if one already
                exists, optional, default: false.
    default     the default value if the source does not have the specified
                field, optional.

### Remove

Remove a field from the source data.

#### Short Format

```json5
{
    "<src-field-name>": {
        "@remove": true
    }
}
```

#### Value

Set a field in the translated data to the specified value. The value type can be any valid JSON type. The source data is
not affected.

#### Format

```json5
{
    "_": {  // Note, there is no src-field-name
        <data>
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

    data        the data to set the field value, required, any valid JSON
                type.
    overwrite   the flag to overwrite the translated field if one already
                exists, optional, default: false.

### Enum/Lookup

The `enum` translation rule creates enum value from raw data values. The field is removed from the source data.

#### Format

```json5
{
    "<src-field-name>": {
        "@enum": {
            "name"  : "<dst-field-name>",
            "values": "<lookup-table>",
            "other" : "<type-name>",
            "overwrite": [true|false],
            "default": "<data>"
        }
    }
}
```

The `lookup` translation rule creates enum value from raw data values. The source data is not affected.

#### Format

```json5
{
    "<src-field-name>": {
        "@lookup": {
            "name"  : "<dst-field-name>",
            "values": "<lookup-table>",
            "other" : "<type-name>",
            "overwrite": [true|false],
            "default": "<data>"
        }
    }
}
```

##### Where

    name        the translated field name, required.
    values      the lookup table to translate the values to enums, required.
    other       the other field name to save untranslated value, optional.
    overwrite   the flag to overwrite the translated field if one already
                exists, optional, default: false.
    default     the default value if the source does not have the specified
                field, optional.

##### Example

```json5
  {
    "Opcode": {
      "@enum": {
        "name": "status_id",
        "values": {
          "0" : 1,        // Success
          "-1": 2         // Failure
        },
        "default": 0,     // if Opcode field is missing
        "other": "status" // if Opcode field has value other
                          // than "0" or "-1", then save the value
                          // in the status field
      }
    }
  }
```

## Type conversions

You can use the `type` to translate the value data to another type, for example from string to integer. The following
types are supported: string, integer, long, float, double, timestamp, time, path, downcase, and upcase.

### timestamp

Translates a string to a timestamp, using a parser to parse local, zoned, or ISO-like date-time with the offset and zone
if available, such as:
`2011-12-03T10:15:30`
`2011-12-03T10:15:30+01:00`
`2011-12-03T10:15:30+01:00[Europe/Paris]`

Other supported formats:
Local date-time:    `MM/dd/yy HH:mm:ss`
Zoned date-time:    `MM/dd/yy HH:mm:ss z`
Long number:        `1681160562325` // UTC time in ms

Note, if the value is `null`, an empty string, or invalid time, then the translated field is set to the current system
time in UTC milliseconds.

### path

Translates a string containing a file path to a File object. Both Windows and Unix name separators are supported.

If the value contain a file path then the following File attributes are set:
path:              the value
name:              the name element of the path
parent_folder:     the parent path

If the value does not contain a path name, then only the `path` attribute of the file object is set.

### downcase

Converts a string to all-lower-case letters.

### upcase

Converts a string to all-upper-case letters.
