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

import io.ocsf.parsers.Parsers;
import io.ocsf.translator.svc.TranslatorsManager;
import io.ocsf.translator.svc.TranslatorsLoader;
import io.ocsf.utils.FuzzyHashMap;
import io.ocsf.translator.event.event.Event;
import io.ocsf.translator.event.event.Sink;
import io.ocsf.translator.event.event.Source;
import io.ocsf.utils.parsers.Parser;

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
    final FuzzyHashMap<Parser>             parsers      = Parsers.parsers();
    final FuzzyHashMap<TranslatorsManager> transformers = TranslatorsLoader.load(rules);

    demuxer = new EventDemuxer(parsers, transformers, in, out, raw);
  }

  /**
   * When an object implementing interface <code>Runnable</code> is used to create a thread,
   * starting the thread causes the object's <code>run</code> method to be called in that separately
   * executing thread.
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
