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

import io.ocsf.translator.event.event.Event;
import io.ocsf.translator.event.event.EventQueue;
import io.ocsf.translator.event.event.Sink;
import io.ocsf.translator.event.event.Source;
import io.ocsf.utils.parsers.Parser;

/**
 * The EventProcessor concurrently runs a parser and a normalizer in two separate threads.
 * <p>
 * The parser and the normalizer are connected with a small queue to keep the memory usage low.
 */
public class EventProcessor
{
  private final EventParser     parser;
  private final EventNormalizer normalizer;

  public EventProcessor(
    final Parser parser, final TranslatorsManager translators,
    final Source<Event> source, final Sink<Event> sink)
  {
    final EventQueue<Event> queue = new EventQueue<>(2);

    this.parser     = new EventParser(parser, source, queue);
    this.normalizer = new EventNormalizer(translators, queue, sink);
  }

  /**
   * Starts the parser and the normalizer threads.
   * <p>
   * To stop the threads send <code>Event.eos()</code> event on the <code>source</code> queue.
   */
  public void start()
  {
    new Thread(normalizer, normalizer.toString()).start();
    new Thread(parser, parser.toString()).start();
  }
}
