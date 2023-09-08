# Event Translation Guide

** DRAFT **

Event translation is the process of converting data from one format to another, so that it can be used by different applications or systems. Data mapping and data transformation are two key tasks involved in event translation. Each of these tasks plays a crucial role in converting raw event data into structured events that adhere to the OCSF schema.

 - Data mapping focuses on the translation of source fields, which represent attributes in the original event data, to their corresponding destination fields, which follow the structure of the OCSF schema.

 - Data transformation involves the conversion of data from its original format to a new format required by the OCSF schema. The process can include other tasks like cleansing, restructuring, and normalization to create a cohesive and accurate representation of the original event data in the OCSF format.

## Summary

This document serves as a guide to establishing rules for both data mapping and data transformation. By using these rules, organizations can translate raw event data into a structured format that adheres to the OCSF schema.

For further information regarding the specifics of the OCSF schema and its requirements, refer to the OCSF Schema documentation: [https://schema.ocsf.io](https://schema.ocsf.io).

## Rule File Format

A rule file contains a single JSON object with the following structure:

```json
{
  "caption": "Rule Caption",
  "description": "Rule description.",
  "references": [
    "https://schema.ocsf.io"
  ],
  "when": "<expression>",
  "rules": [
    {
      // Short format of a translation rule
      "<src-name1>": {
        "<operand>": "<dst-name>"
      },
      // Long format of a translation rule
      "<src-name2>": {
        "<operand>": {
         "name": "<dst-name>",
         "when": "<expression>",
         "type": "<type-name>",
         "overwrite": [true, false],
         "default": "<data>"
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

### Boolean Expressions

Boolean expressions are logical statements that evaluate to either `true` or `false`. They are used in the when clause to define criteria that determine when specific rules or translations should be applied.

A Boolean expression combines **factors** and **terms** using **operators**.

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
  * `contains`: Performs a sub-string matching. Example: `name contains "admin"`.
  * `like`: Performs pattern matching using wildcards (`*` or `?`). Example: `name like "admin*"`.
  * `match`: Performs a regex pattern match. Example: `name regex "admin.*"`.
  * `starts_with`: Checks if a string value starts with a given sub-string. Example: `name starts_with "admin"`.
  * `ends_with`: Checks if a string value ends with a given sub-string. Example: `name ends_with "tor"`.
  * `exec`: Executes a sub-expression against embedded array or object. Example: `user exec (name like "admin*" or name = "root")`.

Here are some examples of how to use boolean expressions in the when clause:
```
   # This rule will only be applied if the port is equal to 80
   when port = 80

   # This rule will only be applied if the name contains "root" or starts with "admin"
   when name contains "root" or name starts_with "admin"

   # This rule will only be applied if the user is not an administrator
   when not user exec (role = "admin")

      # Or, here is an alternative way to write the same rule:
      when user.role != "admin"

   # This rule will only be applied if the user.age is between 18 and 65
   when user exec (age > 18 and age < 65)
   
      # Or, here is an alternative way to write the same rule:
      when user.age > 18 and user.age < 65
```

## Translation Rules

The general translation rule format is shown below, and the section below show details about each operation.

```json
{
   "<src-name>": {
      "<operand>": {
         "name": "<dst-name>",
         "when": "<expression>",
         "type": "<type-name>",
         "overwrite": [true, false],
         "default": "<data>"
      }
   }
}
```


### Move

The `move` operation transfers an attribute value from the source data to the translated data. The translated attribute name can either remain the same or differ from the source attribute name. The original value is removed from the source data.

#### Short format

The short format for the `move` operation is:

```json
{
   "<src-name>": {
      "@move": "<dst-name>"
   }
}
```

**Example**

Consider the following two rules:

```json
{
   "src_ip": {
      "@move": "src_endpoint.ip"
   },
   "user.name": {
      "@move": "src_user.name"
   }    
}
```

Using the input data:

```json
{
   "src_ip": "1.2.3.4",
   "dst_ip": "5.6.7.8",
   "user": {
      "name": "joe",
      "uid": 0
   }
}
```

The output data will be:

```json
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

In this example, the values of the `src_ip` and `user.name` attributes are moved to new locations within the translated data. Simultaneously, the original attributes that have not undergone translation are saved in the "unmapped" object. This methodology guarantees the preservation of all source data, even when certain data remains untranslated.

#### Long format

The Long format for the `move` operation: (using a single attribute, which is the most common usage):

```json
{
   "<src-name>": {
      "@move": {
         "name": "<dst-name>",
         "when": "<expression>",
         "type": "<type-name>",
         "overwrite": [true, false],
         "default": "<data>"
      }
   }
}
```

**Where**

   - **name** : Translated attribute name (required).
   - **when** : A Boolean guard that specifies the conditions under which the translation rule should be applied (optional).
   - **type** : Type of the translated value (optional). See below for the available types.
   - **overwrite** : A flag to indicate whether the translated attribute value should be overwritten if it already exists (optional, default: false).
   - **default** : The default value to be used if the source attribute is missing (optional).


**Example**

The example above can be rewritten using the long rule format as:

```json
{
   "src_ip": {
      "@move": {
         "name": "src_endpoint.ip"
      }
   },
   "user.name": {
      "@move": {
         "name": "src_user.name"
      }
   }
}
```

**Example with a conditional translation**

The example above can be rewritten using the long rule format as:

```json
{
   "src_ip": {
      "@move": {
         "name": "src_endpoint.ip"
      }
   },
   "user.name": {
      "@move": {
         "when": "user.name != null",
         "name": "src_user.name"
      }
   }
}
```

#### Long format using multiple attributes

The Long format for the `move` operation using multiple attributes for concatenation. Use this format where you need to concatenate values from multiple source attributes into a single destination attribute:

```json
{
   "<src-name-1>, <src-name-2>, ...": {
      "@move": {
         "name": "<dst-name>",
         "when": "<expression>",
         "type": "<type-name>",
         "separator": "<joiner>",
         "overwrite": [true, false],
         "default": "<data>"
      }
   }
}
```

**Where**

   - **name** : Translated attribute name (required).
   - **when** : A Boolean guard that specifies the conditions under which the translation rule should be applied (optional).
   - **type** : Type of the translated value (optional). See below for the available types.
   - **separator**   : Joiner used as a separator (optional). Defaults to an empty string.
   - **overwrite** : A flag to indicate whether the translated attribute value should be overwritten if it already exists (optional, default: false).
   - **default** : The default value to be used if the source attribute is missing (optional).

**Example**

```json
{
   "path, name": {
      "@move": {
         "name": "process.file",
         "type": "file",
         "separator": "/"
      }
   }
}
```

### Copy

Copy an attribute value from the source data to the translated data.

Note that the translated attribute name can be the same or different from the source attribute name. The source data remains unaffected.

#### Short format

```json
{
   "<src-name>": {
      "@copy": "<dst-name>"
   }
}
```

#### Long format

```json
{
   "<src-name>": {
      "@copy": {
         "name": "<dst-name>",
         "when": "<expression>",
         "type": "<type-name>",
         "overwrite": [true, false],
         "default": "<data>"
      }
   }
}
```

#### Long format with multiple attributes

Use a comma-separated list of attributes to join data from multiple attributes.

```json
{
   "<src-name-name1>, <src-name-name2>, ...": {
      "@copy": {
         "name": "<dst-name>",
         "when": "<expression>",
         "type": "<type-name>",
         "separator": "<joiner>",
         "overwrite": [true, false],
         "default": "<data>"
      }
   }
}
```

### Remove

Remove an attribute from the source data.

#### Short format

```json
{
   "<src-name>": {
      "@remove": true
   }
}
```

#### Long format

```json
{
   "<src-name>": {
      "@copy": {
         "name": "<dst-name>",
         "when": "<expression>"
      }
   }
}
```

### Value

Set an attribute in the translated data to the specified value. The value type can be any valid JSON type. The source data remains unaffected.

#### Format

```json
{
   // Use underscore to specify that there is no src-name  
   "_": {
     // JSON data
   }
}
```

**Example**

```json
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

Given this input data:

```json
{
   "src_ip": "1.2.3.4",
   "dst_ip": "5.6.7.8",
   "user": {
      "name": "joe",
      "uid": 0
   }    
}
```

The output data will be:

```json
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

### Enum/Lookup

The `enum` translation rule creates enum values from raw data values. The attribute is removed from the source data.

#### Format

```json
{
   "<src-name>": {
      "@enum": {
         "name"  : "<dst-name>",
         "when": "<expression>",
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

```json
{
   "<src-name>": {
      "@lookup": {
         "name"  : "<dst-name>",
         "when": "<expression>",
         "values": "<lookup-table>",
         "other" : "<type-name>",
         "overwrite": [true, false],
         "default": "<data>"
      }
   }
}
```

**Example**

```json
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