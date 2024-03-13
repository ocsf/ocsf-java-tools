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

package io.ocsf.translator.svc.concurrent;

import io.ocsf.translator.svc.EventProcessor;
import io.ocsf.translator.svc.Splunk;
import io.ocsf.translator.svc.TranslatorsManager;
import io.ocsf.utils.FuzzyHashMap;
import io.ocsf.translator.event.event.Event;
import io.ocsf.translator.event.event.EventQueue;
import io.ocsf.translator.event.event.Sink;
import io.ocsf.translator.event.event.Source;
import io.ocsf.translator.event.event.Transformer;
import io.ocsf.utils.parsers.Parser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * The EventDemuxer serves as a helper class designed to de-multiplex the incoming raw event stream
 * into multiple streams, each corresponding to the specific event type. Its primary function is to
 * efficiently manage and segregate the various types of events in the raw stream, optimizing data
 * flow and processing.
 * <p>
 * By leveraging the EventDemuxer, developers can streamline event handling, enabling a more
 * organized and focused approach to event processing. This ultimately leads to improved code
 * readability and maintainability, as well as enhanced performance.
 * <p>
 * In summary, the EventDemuxer significantly enhances the event processing pipeline by
 * intelligently distributing events based on their type, thereby offering a more elegant and
 * efficient solution for managing event streams.
 */
public class EventDemuxer extends Transformer
{
  private static final Logger logger = LogManager.getLogger(EventDemuxer.class);

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
   * @param source      the source of the input events
   * @param sink        the sink for the parsed and translated events
   * @param raw         the sink for events that were not translated
   */
  public EventDemuxer(
    final FuzzyHashMap<Parser> parsers,
    final FuzzyHashMap<TranslatorsManager> normalizers,
    final Source<Event> source,
    final Sink<Event> sink,
    final Sink<Event> raw)
  {
    super(EventDemuxer.class.getName(), source, raw);

    this.parsers     = parsers;
    this.normalizers = normalizers;
    this.eventSink   = sink;

    final int size = parsers.size() + 1;
    this.queues = new HashMap<>(size);
  }

  @Override
  protected Event process(final Event data) throws InterruptedException
  {
    final String source = data.get(Splunk.SOURCE_TYPE);
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
      logger.info("{}: the shutdown sequence has been interrupted", this, ex);

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
