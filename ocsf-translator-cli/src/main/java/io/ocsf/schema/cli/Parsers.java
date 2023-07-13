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

package io.ocsf.schema.cli;

import io.ocsf.schema.Event;
import io.ocsf.utils.Maps;
import io.ocsf.parsers.Parser;
import io.ocsf.utils.Strings;
import io.ocsf.schema.RawEvent;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Manage a map of named parsers.
 * <p>
 * Each parser is registered with a name, by default the name is the source type.
 */
public class Parsers
{
  private static final Logger logger = LoggerFactory.getLogger(Parsers.class);

  private final Map<String, Parser> parsers = new HashMap<>();

  /**
   * Register a new parser using the parser's name.
   *
   * @param parser the parser to be added
   */
  public void register(final Parser parser)
  {
    Objects.requireNonNull(parser, "parser cannot be null");

    register(parser.toString(), parser);
  }

  /**
   * Register a new parser using the given parser name.
   *
   * @param name   the parser name
   * @param parser the parser to be added
   */
  public void register(final String name, final Parser parser)
  {
    Objects.requireNonNull(name, "parser name cannot be null");
    Objects.requireNonNull(parser, "parser cannot be null");

    if (parsers.put(name, parser) != null)
      logger.warn("Parser {} is already registered", name);
  }

  public Parser parser(final String name)
  {
    return parsers.get(name);
  }

  public Collection<Parser> values()
  {
    return Collections.unmodifiableCollection(parsers.values());
  }

  /**
   * Parse an event using the default parser registration.
   * <p>
   * Note: this function expects 3 attributes in the input event: sourceType, rawEvent, tenant.
   *
   * @param event the event data to be parsed
   * @return the parsed event as a HashMap, or null if a suitable parser is not found
   */
  public Map<String, Object> parse(final Map<String, String> event)
  {
    return parse(event.get(RawEvent.SOURCE_TYPE), event.get(RawEvent.TENANT), event.get(RawEvent.RAW_EVENT));
  }

  /**
   * Parse the given raw event using a parser registered under the <code>name</code>.
   *
   * @param name   the parser name
   * @param tenant the customer identifier, aka tenant
   * @param text   the raw event data
   * @return the parsed event as a HashMap, or null if a suitable parser is not found
   */
  public final Map<String, Object> parse(final String name, final String tenant, final String text)
  {
    if (!Strings.isEmpty(name))
    {
      final Parser parser = parsers.get(name);
      if (parser != null)
      {
        try
        {
          final Map<String, Object> event = parser.parse(text);

          Maps.putIn(event, Event.CUSTOMER_ID, tenant);
          Maps.putIn(event, Event.SOURCE_TYPE, name);

//          event.put(Event.RAW_EVENT, text);

          return event;

        }
        catch (final Exception ex)
        {
          logger.warn("Unable to parse event {}: {}. Error: {}", RawEvent.SOURCE_TYPE, name, ex);
          logger.debug(text);
        }
      }
    }

    return null;
  }

}
