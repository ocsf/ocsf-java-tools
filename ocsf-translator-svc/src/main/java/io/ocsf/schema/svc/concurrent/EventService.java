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

package io.ocsf.schema.svc.concurrent;

import io.ocsf.parsers.Parser;
import io.ocsf.schema.Event;

import io.ocsf.schema.concurrent.ProcessorList;
import io.ocsf.schema.concurrent.Sink;
import io.ocsf.schema.concurrent.Source;
import io.ocsf.schema.config.ConfigParsers;
import io.ocsf.schema.config.ConfigTranslators;
import io.ocsf.schema.svc.EventDemuxer;
import io.ocsf.translators.Translators;

import java.io.IOException;

public class EventService implements Runnable
{
  private final EventDemuxer demuxer;

  public EventService(
      final String rules,
      final Source<Event> in,
      final Sink<Event> out,
      final Sink<Event> raw) throws IOException
  {
    final ProcessorList<Parser>      parsers      = ConfigParsers.parsers();
    final ProcessorList<Translators> transformers = ConfigTranslators.load(rules);

    demuxer = new EventDemuxer(parsers, transformers, in, out, raw);
  }

  /**
   * When an object implementing interface <code>Runnable</code> is used to create a thread, starting the thread causes
   * the object's
   * <code>run</code> method to be called in that separately executing thread.
   * <p>
   * The general contract of the method <code>run</code> is that it may take any action whatsoever.
   *
   * @see Thread#run()
   */
  @Override
  public void run()
  {
    demuxer.run();
  }
}
