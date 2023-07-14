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
import io.ocsf.schema.concurrent.BlockingQueue;
import io.ocsf.schema.concurrent.Sink;
import io.ocsf.schema.concurrent.Source;
import io.ocsf.transformers.Transformers;

/**
 * The EventProcessor concurrently runs a parser and a normalizer in two separate threads.
 * <p>
 * Note, the parser and the normalizer are connected with a small queue (keeps the memory usage low).
 */
public class EventProcessor
{
  private final EventParser parser;
  private final EventNormalizer normalizer;

  public EventProcessor(
      final Parser parser, final Transformers transformers,
      final Source<Event> source, final Sink<Event> sink)
  {
    final BlockingQueue<Event> queue = new BlockingQueue<>(2);

    this.parser = new EventParser(parser, source, queue);
    this.normalizer = new EventNormalizer(transformers, queue, sink);
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
