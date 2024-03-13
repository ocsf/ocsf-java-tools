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

package io.ocsf.translator;

import io.ocsf.schema.Dictionary;
import io.ocsf.translator.util.FileObj;
import io.ocsf.translator.util.FingerprintObj;
import io.ocsf.translator.util.URLObj;
import io.ocsf.utils.*;
import io.ocsf.utils.parsers.Json5Parser;
import io.ocsf.utils.parsers.ParserException;
import io.ocsf.utils.parsers.PatternParser;
import io.ocsf.utils.parsers.RegexParser;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Translator uses a set of rules to map a Map[String, Object] to another Map[String, Object].
 */
public final class TranslatorBuilder
{
  private static final Logger logger = LogManager.getLogger(TranslatorBuilder.class);

  public static final  String RuleList = "rules";
  private static final String RuleSet  = "ruleset";

  private static final String Include    = "@include";
  private static final String MagicValue = "_";

  private static final String DefaultValue = "default";
  private static final String NameField    = "name";
  private static final String OtherField   = "other";
  private static final String Overwrite    = "overwrite";
  private static final String Is_Array     = "is_array";
  private static final String Value        = "value";
  private static final String ValueType    = "type";
  private static final String Values       = "values";
  private static final String Separator    = "separator";
  private static final String Splitter     = "splitter";

  private static final String Predicate    = "when";
  private static final String Parser       = "parser";
  private static final String Parsers      = "parsers";
  private static final String PatternField = "pattern";
  private static final String RegexField   = "regex";
  private static final String OutputField  = "output";

  /**
   * Translator non-conditional interface.
   */
  @FunctionalInterface
  interface NonConditional extends Translator
  {
    default boolean isDefault() {return true;}
  }

  /**
   * Translator conditional interface.
   */
  @FunctionalInterface
  interface Conditional extends Translator
  {
    // No need to override isDefault() since Translator#isDefault returns false
    // default boolean isDefault() {return false;}
  }

  /**
   * Rule Interface.
   */
  @FunctionalInterface
  interface Rule
  {
    void apply(Map<String, Object> data, final Map<String, Object> translated);
  }

  /**
   * JSON reader interface to abstract the location (resources vs filesystem) of the translation
   * rule files.
   */
  @FunctionalInterface
  public interface JsonReader
  {
    Object read(final Path path) throws IOException;
  }

  /**
   * Creates a new translator from a file.
   *
   * @param home the home folder of the rules
   * @param path the path to a file with JSON encoded translation rules
   * @return the translator using the translation rules
   * @throws ParserException invalid json file
   * @throws IOException     unable to read the file
   */
  public static Translator fromFile(final Path home, final Path path) throws IOException
  {
    if (java.nio.file.Files.isRegularFile(path))
      return create(home, home.resolve(path.toRealPath()), Files::readJson);

    return create(home, home.resolve(path), Files::readJson);
  }

  /**
   * Creates a new translator from Java resources.
   *
   * @param home the home folder of the rules
   * @param path the path to a file with JSON encoded translation rules
   * @return the translator using the translation rules
   * @throws ParserException invalid json file
   * @throws IOException     unable to read the file
   */
  public static Translator fromResource(final Path home, final Path path) throws IOException
  {
    return create(home, home.resolve(path), Files::readJsonFromResource);
  }

  /**
   * Creates a new translator.
   *
   * @param home   the home folder of the rules
   * @param path   the path to the JSON encoded translation rules
   * @param reader the JSON reader
   * @return the translator using the translation rules
   * @throws ParserException invalid json
   * @throws IOException     unable to read the resource
   */
  public static Translator create(final Path home, final Path path, final JsonReader reader)
    throws IOException
  {
    return build(home, reader, Maps.typecast(reader.read(path)));
  }

  /*
   * Used by the tests: creates a new translator from a JSON string.
   *
   * NOTE: this function does not support included rules.
   */
  public static Translator fromString(final String json) throws IOException
  {
    return build(null, null, Json5Parser.to(json));
  }

  public static Translator build(
    final Path home, final JsonReader reader, final Map<String, Object> map) throws IOException
  {
    final Translator translator =
      createTranslator(
        (String) map.get(Predicate),
        readParsers(home, reader, map),
        readRules(home, reader, map));

    final Collection<Map<String, Object>> ruleset = Maps.typecast(map.get(RuleSet));
    if (ruleset == null || ruleset.isEmpty())
    {
      return translator;
    }

    {
      final Collection<Translator> list = new ArrayList<>(ruleset.size());
      for (final Map<String, Object> rule : ruleset)
      {
        list.add(
          createSubTranslator(
            (String) rule.get(Predicate),
            readParsers(home, reader, rule),
            readRules(home, reader, rule)));
      }

      return new Translator()
      {
        @Override
        public Map<String, Object> apply(final Map<String, Object> data)
        {
          final Map<String, Object> updated    = translator.apply(data);
          final Map<String, Object> translated = updated != data ? updated : new HashMap<>();

          for (final Translator t : list)
          {
            t.apply(data, translated);
          }

          return translated;
        }

        @Override
        public boolean isDefault()
        {
          return translator.isDefault();
        }
      };
    }
  }

  private interface DataTranslator
  {
    Map<String, Object> parse(final Map<String, Object> map);
  }

  private static class MultiStageParser implements DataTranslator
  {
    private final List<DataTranslator> translators = new ArrayList<>();

    void add(final DataTranslator translator) {translators.add(translator);}

    public Map<String, Object> parse(Map<String, Object> data)
    {
      for (final DataTranslator translator : translators)
      {
        data = translator.parse(data);
      }

      return data;
    }
  }

  private static DataTranslator readParsers(
    final Path home, final JsonReader reader, final Map<String, Object> rule) throws IOException
  {
    final Map<String, Object> parser = Maps.typecast(rule.get(Parser));
    if (parser != null)
    {
      return loadParser(home, reader, parser);
    }

    final List<Map<String, Object>> parsers = Maps.typecast(rule.get(Parsers));
    if (parsers != null)
    {
      return loadParsers(home, reader, parsers, new MultiStageParser());
    }

    return data -> data;
  }

  private static DataTranslator loadParsers(
    final Path home,
    final JsonReader reader,
    final Collection<Map<String, Object>> parsers,
    final MultiStageParser list) throws IOException
  {
    for (final Map<String, Object> p : parsers)
    {
      final String filename = Maps.get(p, Include);

      if (filename != null)
      {
        final Object included = reader.read(home.resolve(filename));
        if (included instanceof Map<?, ?>)
        {
          // includes a single parser
          list.add(createParser(Maps.typecast(included)));
        }
        else if (included instanceof Collection<?>)
        {
          // includes a list of parsers
          loadParsers(home, reader, Maps.typecast(included), list);
        }
      }
      else
      {
        list.add(createParser(p));
      }
    }

    return list;
  }

  private static DataTranslator loadParser(
    final Path home, final JsonReader reader, final Map<String, Object> map) throws IOException
  {
    final String filename = Maps.get(map, Include);

    if (filename != null)
    {
      final Object included = reader.read(home.resolve(filename));
      if (included instanceof Map<?, ?>)
      {
        // includes a single parser
        return createParser(Maps.typecast(included));
      }

      if (included instanceof Collection<?>)
      {
        // includes a list of parsers
        return loadParsers(home, reader, Maps.typecast(included), new MultiStageParser());
      }
    }

    return createParser(map);
  }

  private static DataTranslator createParser(final Map<String, Object> map)
  {
    final String srcKey = (String) map.get(NameField);
    final String dstKey = (String) map.get(OutputField);

    return buildDataTranslator(map, parser -> data -> {
      final String text = (String) Maps.getIn(data, srcKey);
      if (Strings.isNotEmpty(text))
      {
        try
        {
          final Map<String, Object> parsed = parser.parse(text);
          if (parsed != null)
          {
            if (dstKey != null)
            {
              final Map<String, Object> dstMap = Maps.typecast(Maps.getIn(data, dstKey));
              if (dstMap == null)
              {
                Maps.putIn(data, dstKey, parsed);
              }
              else
              {
                dstMap.putAll(parsed);
              }
            }
            else
            {
              data.putAll(parsed);
            }
          }
        }
        catch (final Exception ex)
        {
          logger.warn("Unable to parse {}: {}", Strings.quote(srcKey), Strings.quote(text), ex);
        }
      }

      return data;
    });
  }

  private static DataTranslator buildDataTranslator(
    final Map<String, Object> parser,
    final Function<io.ocsf.utils.parsers.Parser, DataTranslator> builder)
  {
    final String pattern = (String) parser.get(PatternField);
    if (Strings.isNotEmpty(pattern))
    {
      return builder.apply(PatternParser.create(pattern));
    }

    final String regex = (String) parser.get(RegexField);
    if (Strings.isNotEmpty(regex))
    {
      return builder.apply(RegexParser.create(regex));
    }

    return data -> data;
  }


  private static Translator createTranslator(
    final String cond, final DataTranslator translator, final Collection<Map<String, Object>> rules)
  {
    // no rules, no translations
    if (rules == null)
    {
      return (NonConditional) translator::parse;
    }

    final List<Tuple<String, Rule>> compiled = compile(rules);

    // if no conditions, then translate everything
    if (Strings.isEmpty(cond))
    {
      return (NonConditional) data -> apply(compiled, translator.parse(data));
    }

    return new Conditional()
    {
      final Predicate<Map<String, Object>> p = compile(cond);

      @Override
      public Map<String, Object> apply(final Map<String, Object> data)
      {
        return p.test(data) ? TranslatorBuilder.apply(compiled, translator.parse(data)) :
               translator.parse(data);
      }
    };
  }

  private static Translator createSubTranslator(
    final String cond, final DataTranslator translator, final Collection<Map<String, Object>> rules)
  {
    // no rules, no translations
    if (rules == null) return translator::parse;

    final List<Tuple<String, Rule>> compiled = compile(rules);

    // if no conditions, then translate everything
    if (Strings.isEmpty(cond))
    {
      return new NonConditional()
      {
        @Override
        public Map<String, Object> apply(final Map<String, Object> data)
        {
          return TranslatorBuilder.apply(compiled, translator.parse(data));
        }

        @Override
        public Map<String, Object> apply(
          final Map<String, Object> data, final Map<String, Object> translated)
        {
          return TranslatorBuilder.apply(compiled, translator.parse(data), translated);
        }
      };
    }

    return new Conditional()
    {
      final Predicate<Map<String, Object>> p = compile(cond);

      @Override
      public Map<String, Object> apply(final Map<String, Object> data)
      {
        return p.test(data) ? TranslatorBuilder.apply(compiled, translator.parse(data)) :
               translator.parse(data);
      }

      @Override
      public Map<String, Object> apply(
        final Map<String, Object> data, final Map<String, Object> translated)
      {
        return p.test(data) ?
               TranslatorBuilder.apply(compiled, translator.parse(data), translated) :
               translator.parse(data);
      }
    };
  }

  private static Collection<Map<String, Object>> readRules(
    final Path home, final JsonReader reader, final Map<String, Object> map) throws IOException
  {
    final Collection<Map<String, Object>> list = Maps.typecast(map.get(RuleList));

    return list == null || list.isEmpty() ? null : readRules(home, reader, list, new ArrayList<>());
  }

  private static Collection<Map<String, Object>> readRules(
    final Path home, final JsonReader reader, final Collection<Map<String, Object>> list,
    final Collection<Map<String, Object>> rules) throws IOException
  {
    for (final Map<String, Object> rule : list)
    {
      final Object include = Maps.get(rule, Include);

      if (include == null)
      {
        rules.add(rule);
      }
      else if (include instanceof String)
      {
        includeRule(home, (String) include, reader, rules);
      }
      else if (include instanceof List<?>)
      {
        final Collection<String> files = Maps.typecast(include);

        for (final String filename : files)
          includeRule(home, filename, reader, rules);
      }
      else
      {
        // TODO: invalid JSON object
        throw new InvalidObjectException(String.valueOf(include));
      }
    }

    return rules;
  }

  private static void includeRule(
    final Path home, final String filename, final JsonReader reader,
    final Collection<Map<String, Object>> rules) throws IOException
  {
    final Object included = reader.read(home.resolve(filename));
    if (included instanceof Map<?, ?>)
    {
      // includes a single rule
      rules.add(Maps.typecast(included));
    }
    else if (included instanceof Collection<?>)
    {
      // includes a list of rules
      readRules(home, reader, Maps.typecast(included), rules);
    }
    else
    {
      // TODO: invalid JSON object
      throw new InvalidObjectException(String.valueOf(included));
    }
  }

  static Map<String, Object> apply(
    final List<Tuple<String, Rule>> rules, final Map<String, Object> data)
  {
    return apply(rules, data, new HashMap<>());
  }

  static Map<String, Object> apply(
    final List<Tuple<String, Rule>> rules, final Map<String, Object> data,
    final Map<String, Object> translated)
  {
    rules.forEach(rule -> rule.value.apply(data, translated));
    Maps.cleanup(data);
    return translated;
  }

  static List<Tuple<String, Rule>> compile(final Collection<Map<String, Object>> src)
  {
    final List<Tuple<String, Rule>> rules = new ArrayList<>(src.size());

    for (final Map<String, Object> entry : src)
    {
      try
      {
        rules.add(createRule(entry));
      }
      catch (final Exception e)
      {
        throw new ParserException(String.format("Illegal rule %s. %s", entry, e.getMessage()));
      }
    }

    return rules;
  }

  private static Tuple<String, Rule> createRule(final Map<String, Object> rule) throws Exception
  {
    for (final Map.Entry<String, Object> r : rule.entrySet())
    {
      final String name = r.getKey().intern();
      final Object obj  = r.getValue();

      if (obj instanceof Map<?, ?>) return newRule(name, Maps.typecast(obj));

      // handle embedded objects
      if (obj instanceof Collection<?>) return embedded(name, Maps.typecast(obj));
    }

    throw new IllegalArgumentException("Illegal rule");
  }

  private static Tuple<String, Rule> newRule(final String name, final Map<String, Object> map)
    throws Exception
  {
    // first, handle the values
    if (MagicValue.equals(name))
    {
      final Object value = map.get("@value");

      if (value instanceof Map<?, ?>) return merge(name, Maps.typecast(value));

      return merge(name, map);
    }

    for (final Map.Entry<String, Object> entry : map.entrySet())
    {
      switch (entry.getKey())
      {
        case "@move":
          return move(name, entry.getValue());

        case "@copy":
          return copy(name, entry.getValue());

        case "@remove":
          return remove(name, entry.getValue());

        case "@value":
          return value(name, entry.getValue());

        case "@clone":
          return clone(name, entry.getValue());

        case "@enum":
          return lookup(name, Maps::removeIn, entry.getValue());

        case "@lookup":
          return lookup(name, Maps::getIn, entry.getValue());

        default:
          break;  // ignore the other fields
      }
    }

    throw new InvalidExpressionException("Missing transformation statement");
  }

  private static Tuple<String, Rule> embedded(
    final String name, final Collection<Map<String, Object>> ruleData)
  {
    final List<Tuple<String, Rule>> rules = compile(ruleData);

    return new Tuple<>(name, (data, translated) -> {
      if (data != null)
      {
        final Object value = data.get(name);
        if (value instanceof Map<?, ?>)
        {
          translated.putAll(apply(rules, Maps.typecast(value)));
        }
        else if (value instanceof List<?>)
        {
          Maps.<List<Map<String, Object>>>typecast(value)
              .forEach(map -> translated.putAll(apply(rules, map)));
        }
      }
    });
  }

  private static Tuple<String, Rule> merge(final String name, final Map<String, Object> value)
  {
    return new Tuple<>(name, (_data, translated) -> Maps.merge(translated, value, false));
  }

  private static Tuple<String, Rule> value(final String name, final Object ruleData)
  {
    final boolean overwrite;
    final Object  value;

    final Predicate<Map<String, Object>> predicate;
    if (ruleData instanceof Map<?, ?>)
    {
      final Map<String, Object> map = Maps.typecast(ruleData);
      overwrite = Maps.get(map, Overwrite, Boolean.FALSE);
      value     = map.get(Value);

      final String when = (String) map.get(Predicate);
      predicate = Strings.isEmpty(when) ? null : compile(when);
    }
    else
    {
      overwrite = false;
      value     = ruleData;
      predicate = null;
    }

    return new Tuple<>(name, (data, translated) -> {
      if (predicate == null || predicate.test(data)) Maps.putIn(translated, name, value, overwrite);
    });
  }

  private static Tuple<String, Rule> clone(final String name, final Object ruleData)
  {
    final boolean overwrite;
    final String  dest;

    final Predicate<Map<String, Object>> predicate;
    if (ruleData instanceof Map<?, ?>)
    {
      final Map<String, Object> map = Maps.typecast(ruleData);
      overwrite = Maps.get(map, Overwrite, Boolean.FALSE);
      dest      = (String) map.get(NameField);

      final String when = (String) map.get(Predicate);
      predicate = Strings.isEmpty(when) ? null : compile(when);
    }
    else
    {
      overwrite = false;
      dest      = (String) ruleData;
      predicate = null;
    }

    return new Tuple<>(name, (data, translated) -> {
      if (predicate == null || predicate.test(data))
      {
        Maps.putIn(translated, dest, Maps.getIn(translated, name), overwrite);
      }
    });
  }

  private static Tuple<String, Rule> remove(final String name, final Object ruleData)
  {
    final Predicate<Map<String, Object>> predicate;
    if (ruleData instanceof Map<?, ?>)
    {
      final Map<String, Object> map = Maps.typecast(ruleData);

      final String when = (String) map.get(Predicate);
      predicate = Strings.isEmpty(when) ? null : compile(when);
    }
    else
    {
      predicate = null;
    }

    return new Tuple<>(name, (data, translated) -> {
      if (predicate == null || predicate.test(data)) Maps.removeIn(data, name);
    });
  }

  private static Tuple<String, Rule> move(final String name, final Object ruleData) throws Exception
  {
    return rename(name, Maps::removeIn, ruleData);
  }

  private static Tuple<String, Rule> copy(final String name, final Object ruleData) throws Exception
  {
    return rename(name, Maps::getIn, ruleData);
  }

  private static Tuple<String, Rule> rename(
    final String name, final Maps.DataSource source, final Object ruleData) throws Exception
  {
    final String  key;
    final String  type;
    final String  separator;
    final String  splitter;
    final Object  defValue;
    final boolean overwrite;
    final boolean is_array;

    final Predicate<Map<String, Object>> predicate;

    if (ruleData instanceof Map<?, ?>)
    {
      final Map<String, Object> map = Maps.typecast(ruleData);

      key       = Maps.get(map, NameField, name).intern();
      type      = Maps.get(map, ValueType);
      separator = (String) map.get(Separator);
      splitter  = (String) map.getOrDefault(Splitter, Strings.LineSplitter);

      defValue  = map.get(DefaultValue);
      overwrite = Maps.get(map, Overwrite, Boolean.FALSE);
      is_array  = Maps.get(map, Is_Array, Boolean.FALSE);

      final String when = (String) map.get(Predicate);
      predicate = Strings.isEmpty(when) ? null : compile(when);
    }
    else if (ruleData instanceof String)
    {
      key       = ((String) ruleData).intern();
      type      = null;
      separator = null;
      splitter  = Strings.LineSplitter;
      defValue  = null;
      overwrite = false;
      is_array  = false;
      predicate = null;
    }
    else
    {
      throw new Exception("move: unexpected rule type");
    }

    final Maps.DataSource src;

    final int comma = name.indexOf(',');
    if (comma > 0)
    {
      src = new Maps.DataSource()
      {
        // multi-field
        private final String[] names = name.split("\\s*,\\s*");
        private final StringBuilder sb = new StringBuilder();

        @Override
        public Object get(final Map<String, Object> map, final String _name)
        {
          sb.setLength(0);

          for (final String name : names)
          {
            final Object v = source.get(map, name);
            if (v != null)
            {
              if (separator != null && sb.length() > 0)
                sb.append(separator);
              sb.append(v);
            }
          }

          return sb.length() > 0 ? sb.toString() : null;
        }
      };
    }
    else
    {
      src = source;
    }

    return new Tuple<>(name, (data, translated) -> {
      if (predicate == null || predicate.test(data))
      {
        final Object value = src.get(data, name);
        if (value != null)
        {
          if (type != null)
          {
            // special handling of file hashes
            if (!FingerprintObj.put(translated, type, value, key))
            {
              if (is_array)
              {
                final List<Object> list = new ArrayList<>();
                for (final Object o : Strings.toArray(value, splitter))
                {
                  final Object parsed = o != null ? typecast(o, type) : null;
                  list.add(parsed);
                }

                Maps.putIn(translated, key, list, overwrite);
              }
              else
              {
                final Object parsed = typecast(value, type);
                Maps.putIn(translated, key, parsed, overwrite);
              }
            }
          }
          else
          {
            if (is_array)
            {
              Maps.putIn(translated, key, Strings.toArray(value, splitter), overwrite);
            }
            else
            {
              Maps.putIn(translated, key, value, overwrite);
            }
          }
        }
        else if (defValue != null)
        {
          Maps.putIn(translated, key, defaultValue(type, defValue), overwrite);
        }
      }
    });
  }

  private static Tuple<String, Rule> lookup(
    final String name, final Maps.DataSource source, final Object ruleData)
  {
    final Map<String, Object> rule = Maps.typecast(ruleData);

    final String  key       = Maps.get(rule, NameField);
    final String  other     = Maps.get(rule, OtherField);
    final Object  defValue  = rule.get(DefaultValue);
    final boolean overwrite = Maps.get(rule, Overwrite, Boolean.FALSE);
    final String  when      = (String) rule.get(Predicate);

    final Predicate<Map<String, Object>> predicate = Strings.isEmpty(when) ? null : compile(when);

    final Map<String, Object> values = Maps.downcase(Maps.get(rule, Values));

    return new Tuple<>(name, (data, translated) -> {
      if (predicate == null || predicate.test(data))
      {
        final Object value = source.get(data, name);

        if (value != null)
        {
          final String strValue = value.toString();
          final Object id       = values.get(strValue.toLowerCase(Maps.LOCALE));
          if (id != null)
          {
            Maps.putIn(translated, key, id, overwrite);
          }
          else if (other != null)
          {
            Maps.putIn(translated, key, Dictionary.OTHER_ID, overwrite);
            Maps.putIn(translated, other, strValue, overwrite);
          }
        }
        else if (defValue != null)
        {
          Maps.putIn(translated, key, defValue, overwrite);
        }
      }
    });
  }

  static Predicate<Map<String, Object>> compile(final String when) throws InvalidExpressionException
  {
    return condition(BooleanExpression.parse(when));
  }

  private static Predicate<Map<String, Object>> condition(final Tree node)
  {
    return data -> BooleanEvaluator.evaluate(node, key -> Maps.getIn(data, key));
  }

  private static Object defaultValue(final String type, final Object value)
  {
    if (type != null)
    {
      switch (type.toLowerCase(Maps.LOCALE).intern())
      {
        case "timestamp":
          return timestamp(value);
        case "time":
          return iso8601Time(value);
      }
    }

    return value;
  }

  private static Object typecast(final Object o, final String type)
  {
    final String typeName = type.toLowerCase(Maps.LOCALE).intern();
    switch (typeName)
    {
      case "string":
        return o.toString();

      case "downcase":
        return o instanceof String ? ((String) o).toLowerCase(Maps.LOCALE) : o;

      case "upcase":
        return o instanceof String ? ((String) o).toUpperCase(Maps.LOCALE) : o;

      case "integer":
        return toInt(o);

      case "long":
        return toLong(o);

      case "float":
        return toFloat(o);

      case "double":
        return toDouble(o);

      case "timestamp":
        return timestamp(o);

      case "time":
        return iso8601Time(o);

      case "path":
        return FileObj.toFile(o, 1); // default file type id: 1 = file

      case "url":
        return url(o);

      case "anonymize":
        return encode(o);

      default:
      {
        final int pos = typeName.indexOf("path");
        if (pos == 0)
        {
          final String[] parts = typeName.split(":");
          if (parts.length == 2)
          {
            try
            {
              return FileObj.toFile(o, Integer.parseInt(parts[1].trim()));
            }
            catch (final NumberFormatException ex)
            {
              logger.warn("Invalid file type_id: {}",  parts[1]);
            }
          }
        }
      }

      logger.warn("Invalid type: {}", type);
      return o; // ignore the type cast
    }
  }

  private static Object url(final Object o)
  {
    try
    {
      return URLObj.toUrl(o.toString());
    }
    catch (final MalformedURLException e)
    {
      logger.warn("Invalid URL string: {}", Strings.quote(o), e);
      return FMap.<String, Object>b().p(Dictionary.Text, o.toString());
    }
  }

  private static Object toDouble(final Object o)
  {
    if (o instanceof Number) return ((Number) o).doubleValue();

    try
    {
      if (o instanceof String) return Double.valueOf(((String) o).trim());
    }
    catch (final NumberFormatException ignore)
    {
    }

    logger.info("Invalid double value: {}", o);
    return 0.0d;
  }

  private static Object toFloat(final Object o)
  {
    if (o instanceof Number) return ((Number) o).floatValue();

    try
    {
      if (o instanceof String) return Float.valueOf(((String) o).trim());
    }
    catch (final NumberFormatException ignore)
    {
    }

    logger.info("Invalid float value: {}", o);
    return 0.0f;
  }

  private static Object toLong(final Object o)
  {
    if (o instanceof Number) return ((Number) o).longValue();

    if (o instanceof String) try
    {
      final String n = ((String) o).trim();

      if (!n.isEmpty()) return Long.decode(n);
    }
    catch (final NumberFormatException ignore)
    {
      logger.info("Invalid long value: {}", o);
    }
    else
    {
      logger.info("Not a long value: {}", o);
    }

    return 0L;
  }

  private static Object toInt(final Object o)
  {
    if (o instanceof Number) return ((Number) o).intValue();

    if (o instanceof String) try
    {
      final String n = ((String) o).trim();

      if (!n.isEmpty()) return Integer.decode(n);
    }
    catch (final NumberFormatException ignore)
    {
      logger.info("Invalid integer value: {}", o);
    }
    else
    {
      logger.info("Not an integer value: {}", o);
    }

    return 0;
  }

  private static Object timestamp(final Object value)
  {
    if (value instanceof String)
    {
      try
      {
        return Times.parseTime((String) value);
      }
      catch (final Exception ignore)
      {
        // nop
      }
    }
    else if (value instanceof Long)
    {
      return value;
    }

    logger.info("Invalid date/time value: {}", value);
    return System.currentTimeMillis();

  }

  private static Object iso8601Time(final Object value)
  {
    if (value instanceof String)
    {
      try
      {
        return Times.toIso8601String(Times.parse((String) value));
      }
      catch (final Exception ignore)
      {
        // nop
      }
    }
    else if (value instanceof Long)
    {
      return Times.toIso8601String((Long) value);
    }

    logger.info("Invalid date/time value: {}", value);
    return Times.currentIso8601Time();
  }

  private static final Base64.Encoder encoder = Base64.getEncoder();

  private static Object encode(final Object value)
  {
    if (value instanceof String)
      return encoder.encodeToString(((String) value).getBytes(StandardCharsets.UTF_8));

    return (value != null ? value.hashCode() : null);
  }

  private TranslatorBuilder() {}

}
