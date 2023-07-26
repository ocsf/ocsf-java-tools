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

package io.ocsf.translator.svc;

import io.ocsf.utils.FMap;
import io.ocsf.translator.event.event.Event;
import io.ocsf.translator.event.event.Sink;
import io.ocsf.translator.event.event.Source;
import io.ocsf.translator.event.event.Transformer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class TranslatorBuilderTest extends Tests
{
  private static final AtomicBoolean Done = new AtomicBoolean();

  private static final class SimpleTransformer extends Transformer
  {
    public SimpleTransformer(final String name, final Source<Event> source, final Sink<Event> sink)
    {
      super(name, source, sink);
    }

    @Override
    protected void terminated()
    {
      super.terminated();
      Done.set(true);
    }

    @Override
    protected Event process(final Event event)
    {
      event.data().put(MESSAGE, TEST_MESSAGE);
      return event;
    }
  }

  private final SimpleTransformer transformer = new SimpleTransformer("test", in, out);

  @Before
  public void setUp() throws Exception
  {
    new Thread(transformer).start();

    // send some data in the input queue
    for (int i = 0; i < MAX_QUEUE_SIZE; i++)
    {
      in.put(new Event(FMap.<String, Object>b().p(EVENT_ID, i)));
    }
  }

  @After
  public void tearDown() throws Exception
  {
    // send 'eos' to terminate the transformer's thread
    in.put(Event.eos());
  }

  @SuppressWarnings("BusyWait")
  @AfterClass
  public static void afterClass() throws InterruptedException
  {
    for (int i = 0; !Done.get() && i < MAX_QUEUE_SIZE; i++)
    {
      Thread.sleep(200);
    }
    Assert.assertTrue(Done.get());
  }

  @Test
  public void validate() throws InterruptedException
  {
    for (int i = 0; i < MAX_QUEUE_SIZE; i++)
    {
      final Event data = out.take();

      Assert.assertEquals(2, data.size());
      Assert.assertEquals(Integer.valueOf(i), data.get(EVENT_ID));
      Assert.assertEquals(TEST_MESSAGE, data.get(MESSAGE));
    }

    Assert.assertEquals(0, out.available());
  }
}