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

import io.ocsf.parsers.Parser;
import io.ocsf.schema.Event;
import io.ocsf.schema.RawEvent;
import io.ocsf.schema.concurrent.BlockingQueue;
import io.ocsf.schema.concurrent.ProcessorList;
import io.ocsf.schema.concurrent.Sink;
import io.ocsf.schema.concurrent.Source;
import io.ocsf.schema.concurrent.Transformer;
import io.ocsf.translators.Translators;
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

  private final ProcessorList<Parser> parsers;
  private final ProcessorList<Translators> normalizers;

  // translated events sink
  private final Sink<Event> eventSink;
  private final Map<String, BlockingQueue<Event>> queues;

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
      final ProcessorList<Parser> parsers,
      final ProcessorList<Translators> normalizers,
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
    final String source = (String) data.get(RawEvent.SOURCE_TYPE);

    if (source != null)
    {
      final Parser parser = parsers.get(source);
      if (parser != null)
      {
        final Translators translators = normalizers.get(source);
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
    final String source = (String) data.data().get(RawEvent.SOURCE_TYPE);
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
      for (final BlockingQueue<Event> queue : queues.values())
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
      final Parser      parser     = parsers.get(source);
      final Translators normalizer = normalizers.get(source);

      if (parser != null && normalizer != null)
      {
        final BlockingQueue<Event> queue = new BlockingQueue<>();

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
