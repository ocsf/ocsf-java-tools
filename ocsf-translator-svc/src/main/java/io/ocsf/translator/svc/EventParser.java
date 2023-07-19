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

package io.ocsf.translator.svc;

import io.ocsf.utils.Maps;
import io.ocsf.utils.event.Event;
import io.ocsf.utils.event.Sink;
import io.ocsf.utils.event.Source;
import io.ocsf.utils.event.Transformer;
import io.ocsf.utils.parsers.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Function;

public class EventParser extends Transformer
{
  private static final Logger logger = LoggerFactory.getLogger(EventParser.class);

  private final Parser parser;

  public EventParser(
    final Parser parser, final Source<Event> source, final Sink<Event> sink)
  {
    super(parser.toString(), source, sink);
    this.parser = parser;
  }

  @Override
  protected Event process(final Event event)
  {
    return process(parser, event.data(), Event::new);
  }

  public static <T> T process(
    final Parser parser, final Map<String, Object> data,
    final Function<Map<String, Object>, T> normalize)
  {
    final String raw = (String) data.get(Splunk.RAW_EVENT);

    if (raw != null)
    {
      try
      {
        final Map<String, Object> parsed = parser.parse(raw);
        if (parsed != null)
        {
          Maps.putIn(parsed, Splunk.CUSTOMER_ID, data.get(Splunk.TENANT));
          Maps.putIn(parsed, Splunk.CIM_SOURCE_TYPE, data.get(Splunk.SOURCE_TYPE));

//          parsed.put(Event.RAW_EVENT, raw);

          return normalize.apply(parsed);
        }
      }
      catch (final Exception e)
      {
        logger.warn("{} unable to parse event: {}", parser, data);
      }
    }

    return null;
  }
}
