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

package io.ocsf.schema.svc;

import io.ocsf.schema.Event;
import io.ocsf.schema.RawEvent;
import io.ocsf.schema.concurrent.Sink;
import io.ocsf.schema.concurrent.Source;
import io.ocsf.schema.concurrent.Transformer;
import io.ocsf.utils.Maps;
import io.ocsf.parser.Parser;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

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
      final Parser parser, final Map<String, Object> data, final Function<Map<String, Object>, T> normalize)
  {
    final String raw = (String) data.get(RawEvent.RAW_EVENT);

    if (raw != null)
    {
      try
      {
        final Map<String, Object> parsed = parser.parse(raw);
        if (parsed != null)
        {
          Maps.putIn(parsed, Event.CUSTOMER_ID, data.get(RawEvent.TENANT));
          Maps.putIn(parsed, Event.SOURCE_TYPE, data.get(RawEvent.SOURCE_TYPE));

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
