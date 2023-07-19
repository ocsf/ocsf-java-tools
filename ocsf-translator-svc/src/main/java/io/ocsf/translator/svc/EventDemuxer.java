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

import io.ocsf.utils.event.Event;
import io.ocsf.utils.parsers.Parser;
import io.ocsf.utils.event.EventQueue;
import io.ocsf.utils.FuzzyHashMap;
import io.ocsf.utils.event.Sink;
import io.ocsf.utils.event.Source;
import io.ocsf.utils.event.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * The EventDemuxer is a helper class to de-multiplex the incoming raw event stream into multiple streams using on the
 * event source type (sourceType).
 */
public class EventDemuxer extends Transformer
{
  private static final Logger logger = LoggerFactory.getLogger(EventDemuxer.class);

  private final FuzzyHashMap<Parser>             parsers;
  private final FuzzyHashMap<TranslatorsManager> normalizers;

  // translated events sink
  private final Sink<Event>                    eventSink;
  private final Map<String, EventQueue<Event>> queues;

  /**
   * Creates a new event demuxer.
   *
   * @param parsers     the parsers registered with the source type
   * @param normalizers the normalizers registered with the source type
   * @param source      the input raw events
   * @param sink        the parsed and translated events
   * @param unparsed    the events that were not the parsed and translated
   */
  public EventDemuxer(
      final FuzzyHashMap<Parser> parsers,
      final FuzzyHashMap<TranslatorsManager> normalizers,
      final Source<Event> source,
      final Sink<Event> sink,
      final Sink<Event> unparsed)
  {
    super(EventDemuxer.class.getName(), source, unparsed);

    this.parsers = parsers;
    this.normalizers = normalizers;
    this.eventSink = sink;

    final int size = parsers.size() + 1;
    this.queues = new HashMap<>(size);
  }

  /**
   * Process a single event in a blocking call.
   *
   * @param data the event data to process
   * @return the parsed and normalized event
   */
  public Map<String, Object> process(final Map<String, Object> data)
  {
    final String source = (String) data.get(Splunk.SOURCE_TYPE);

    if (source != null)
    {
      final Parser parser = parsers.get(source);
      if (parser != null)
      {
        final TranslatorsManager translators = normalizers.get(source);
        if (translators != null)
        {
          return EventParser.process(parser, data, translators::translate);
        }

        logger.warn("Missing event normalizer for source type: {}", source);
      }
      else
      {
        logger.warn("Missing event parser for source type: {}", source);
      }
    }
    else
    {
      logger.warn("Missing source type in: {}", data);
    }

    // return null if the event cannot be parsed
    return null;
  }

  @Override
  protected Event process(final Event data) throws InterruptedException
  {
    final String source = (String) data.data().get(Splunk.SOURCE_TYPE);
    if (source != null)
    {
      final Sink<Event> sink = sink(source);
      if (sink != null)
      {
        sink.put(data);
        return null;
      }
    }
    else
    {
      logger.warn("Missing source type in: {}", data);
    }

    // return the events that cannot be parsed and translated
    return data;
  }

  @Override
  protected void terminated()
  {
    super.terminated();
    try
    {
      for (final EventQueue<Event> queue : queues.values())
      {
        queue.put(Event.eos());
      }
    }
    catch (final InterruptedException ex)
    {
      logger.info("{}: the shutdown sequence has been interrupted", this);

      // restore the interrupted flag
      Thread.currentThread().interrupt();
    }
  }

  private Sink<Event> sink(final String source)
  {
    final Sink<Event> sink = queues.get(source);

    if (sink == null)
    {
      final Parser             parser     = parsers.get(source);
      final TranslatorsManager normalizer = normalizers.get(source);

      if (parser != null && normalizer != null)
      {
        final EventQueue<Event> queue = new EventQueue<>();

        new EventProcessor(parser, normalizer, queue, eventSink).start();

        queues.put(source, queue);

        return queue;
      }
      else
      {
        if (parser == null)
          logger.warn("Missing event parser for source type: {}", source);

        if (normalizer == null)
          logger.warn("Missing event normalizer for source type: {}", source);
        return null;
      }
    }

    return sink;
  }
}
