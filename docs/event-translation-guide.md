# Event Translation Guide

** DRAFT **

Event translation is a multifaceted process encompassing two key tasks: data mapping and data transformation. Each of these tasks plays a crucial role in converting raw event data into structured events that adhere to the OCSF schema. 

Data mapping focuses on the translation of source fields, which represent attributes in the original event data, to their corresponding destination fields, which follow the structure of the OCSF schema.

Data transformation involves the conversion of data from its original format to a new format required by the OCSF schema. The process can include other tasks like cleansing, restructuring, and normalization to create a cohesive and accurate representation of the original event data in the OCSF format.

## Summary

This document serves as a comprehensive guide to establishing rules for both data mapping and data transformation. By using these rules, organizations can confidently translate raw event data into a structured format that adheres to the OCSF schema.

For further information regarding the specifics of the OCSF schema and its requirements, refer to the OCSF Schema documentation: [https://schema.ocsf.io](https://schema.ocsf.io).

## Rule File Format

The rule files contain a single JSON object with the following structure:

```json5
{
  "caption": "Rule Caption",
  "description": "Rule description.",
  "references": [
    "https://schema.ocsf.io"
  ],
  "when": "<expression>",
  "rules": [
    {
      "<src-attribute>": {
        "<operand>": {
          // a set of properties, see below for more details
        }
      }
    }
    // More mapping rules can be added here...
  ]
}
```

Explanation of each field:

- `caption`: A brief caption summarizing the rule's purpose or title.

- `description`: A detailed description providing additional context or information about the rule.

- `references`: A list of references or links to external resources related to the translation, such as documentation or specifications.

- `when`: A boolean expression (predicate/guard) specifying the conditions under which the translation rule should be applied. It dictates when the translation should occur based on the conditions in the `<expression>`. The `<expression>` is constructed using boolean operators such as `AND`, `OR`, and comparison operators explained below in the [Boolean Expression](#boolean-expressions) section.

- `rules`: An array containing mapping and transformation rules. Each rule is represented as an object with a key-value pair. The key is the name of the source attribute in the input data to be processed, and the value is another object with the `<operand>` key. The `<operand>` object includes properties defining the specific translation operation on the source attribute.

The `<operand>` object properties vary based on the translation operation used (e.g., move, copy, remove, etc.). See below for more information.

Multiple mapping rules within the `rules` array can define different translations for various source attributes.

This JSON format provides a structured way to define translations, enabling clear and specific rules based on conditions specified in the `when` clause.

### Boolean Expressions

Boolean expressions are employed in the `when` clause to define criteria determining when specific rules or translations should be applied. In this context, the `when` predicate typically specifies conditions that must be satisfied for a mapping rule to be triggered or executed.

Boolean expressions combine **factors** and **terms** using specified **operators**.

A **factor** consists of one or more **term** expressions separated by a boolean *operator*. Boolean operators include:

- `AND`: Represents the logical "AND" operator, ensuring two Boolean term expressions are both true.
- `OR`: Represents the logical "OR" operator, checking that either one Boolean term or another is true.
- `NOT`: Represents the logical negation (NOT) of an expression. It takes one argument and changes `true` values to `false` and vice versa.

A **term** defines a boolean condition that evaluates to either `true` or `false`. It can take one of the following forms:

- `(expression)`: Encloses a sub-expression in parentheses to group conditions and determine the order of evaluation.
- `NOT (expression)`: Negates the evaluation of an entire *expression*, represented by the *NOT* operator (`not` or `!`). For example: `not (expression)` or `!(expression)`.

- `field operator value`: Compares a field (associated with an attribute name) with a value. Available comparison operators are:

  * `=` or `==`: Equality comparison. Example: `port = 80`.
  * `!=`: Inequality comparison. Example: `port != 80`.
  * `<`: Less than comparison. Example: `port < 80`.
  * `<=`: Less than or equal to comparison. Example: `port <= 80`.
  * `>`: Greater than comparison. Example: `port > 80`.
  * `>=`: Greater than or equal to comparison. Example: `port => 80`.
  * `in`: Checks if a value is present in a list of values. Example: `port in [80, 8000, 8080]`.
  * `not_in`: Checks if a value is not in a list of values. Example: `port not_in [80, 22]`.
  * `contains`: Performs a substring matching. Example: `name contains "admin"`.
  * `like`: Performs pattern matching using wildcards (`*` or `?`). Example: `name like "admin*"`.
  * `match`: Performs a regex pattern match. Example: `name regex "admin.*"`.
  * `starts_with`: Checks if a string value starts with a given substring. Example: `name starts_with "admin"`.
  * `ends_with`: Checks if a string value ends with a given substring. Example: `name ends_with "tor"`.
  * `exec`: Executes a subexpression against array or object fields. Example: `user exec (name like "admin*" or name = "root")`.

## Mapping Rules

The general format for mapping rules is:

```json5
{
   "<src-attribute>": {
      "<operand>": {
         "name": "<dst-attribute>",
         "when": "<expression>",
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

Use a comma-separated list of attributes to join data from multiple attributes.

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

   name        : Translated attribute name (required).
   type        : Type of the translated value (optional). See below for available types.
   separator   : Joiner used as a separator. Defaults to an empty string if not specified.
   overwrite   : Flag to overwrite the translated attribute value if it already exists (optional, default: false).
   default     : Default value if the source attribute is missing (optional).

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

Note that the translated attribute name can be the same or different from the source attribute name. The source data remains unaffected.

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

Use a comma-separated list of attributes to join data from multiple attributes.

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

   name        : Translated attribute name (required).
   type        : Type of the translated value (optional). See below for available types.
   separator   : Joiner used as a separator. Defaults to an empty string if not specified.
   overwrite   : Flag to overwrite the translated attribute value if it already exists (optional, default: false).
   default     : Default value if the source attribute is missing (optional).

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

Set an attribute in the translated data to the specified value. The value type can be any valid JSON type. The source data remains unaffected.

#### Format

```json5
{
   // Use underscore to specify that there is no src-attribute  
   "_": {
     // JSON data
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

   data        : Data to set the attribute value (required), valid JSON data.
   overwrite   : Flag to overwrite the translated attribute if it already exists (optional, default: false).

### Enum/Lookup

The `enum` translation rule creates enum values from raw data values. The attribute is removed from the source data.

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

The `lookup` translation rule creates enum values from raw data values. The source data remains unaffected.

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

   name        : Translated attribute name (required).
   values      : Lookup table to translate values to enums (required).
   other       : Attribute to save untranslated values (optional).
   overwrite   : Flag to overwrite the translated attribute value if it already exists (optional, default: false).
   default     : Default value if the source attribute is missing (optional).

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
      "default": 0,       // Used when the Opcode attribute is missing
      "other": "status"   // If the Opcode attribute has a value that is not
                          // "0" or "-1", then save the value in the status
                          // attribute
     }
   }
  }
```

## Type Conversions

You can use the `type` property to translate the value data to another type, for example from string to integer. The following types are supported: string, integer, long, float, double, timestamp, time, path, downcase, and upcase.

### timestamp

Translates a string to a timestamp, parsing local, zoned, or ISO-like date-time with the offset and zone if available. Examples:
```
   `2011-12-03T10:15:30`
   `2011-12-03T10:15:30+01:00`
   `2011-12-03T10:15:30+01:00[Europe/Paris]`
```

Other supported formats:
   - Local date-time:  `MM/dd/yy HH:mm:ss`
   - Zoned date-time:  `MM/dd/yy HH:mm:ss z`
   - Long number:      `1681160562325`   // UTC time in ms

Note, if the value is `null`, an empty string, or an invalid time, then the translated attribute is set to the current system time in UTC milliseconds.

### path

Translates a string containing a file path to a `File` object. Both Windows and Unix name separators are supported.

If the value contains a file path, then the following `File` attributes are set:

   - path:              the value
   - name:              the name element of the path
   - parent_folder:     the parent path

If the value does not contain a path name, then only the `path` attribute of the `File` object is set.

### downcase

Converts a string to all-lower-case letters.

### upcase

Converts a string to ALL-UPPER-CASE letters.