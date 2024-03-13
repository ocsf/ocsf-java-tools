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

package io.ocsf.translator.event.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Transformer implements Runnable
{
  private static final Logger logger = LogManager.getLogger(Transformer.class);

  private final String        name;
  private final Source<Event> source;
  private final Sink<Event>   sink;

  public Transformer(final String name, final Source<Event> source, final Sink<Event> sink)
  {
    this.name   = name;
    this.source = source;
    this.sink   = sink;
  }

  /**
   * This method is called for each raw event in the source.
   *
   * @param data the event data
   * @return a transform event or <code>null</code>
   * @throws InterruptedException the Transformer's main thread has been interrupted
   */
  protected abstract Event process(final Event data) throws InterruptedException;

  /**
   * This method is called when the Transformer's main thread has been terminated.
   */
  protected void terminated()
  {
    logger.info("{}: main thread terminated", this);
  }

  @Override
  public void run()
  {
    logger.info("{}: main thread started", this);

    try
    {
      for (Event event = source.take(); event.isNotEos(); event = source.take())
      {
        put(process(event));
      }

      // send the eos marker event down the pipe
      sink.put(Event.eos());
    }
    catch (final InterruptedException e)
    {
      logger.info("{}: main thread has been interrupted", this);

      final int available = source.available();
      if (available > 0)
      {
        logger.warn("{}: the source has {} unprocessed events", this, available);
      }
    }
    finally
    {
      final boolean interrupted = Thread.currentThread().isInterrupted();

      terminated();

      if (interrupted)
      {
        // restore the interrupted flag
        Thread.currentThread().interrupt();
      }
    }
  }

  private final void put(final Event event) throws InterruptedException
  {
    if (event != null)
      sink.put(event);
  }

  @Override
  public String toString() {return name;}
}
