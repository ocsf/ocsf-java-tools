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
import io.ocsf.schema.Tests;
import io.ocsf.utils.FMap;
import io.ocsf.schema.transformers.Transformers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventNormalizerTest extends Tests
{
  private final Transformers transformers = new Transformers("test");

  private static final AtomicBoolean done = new AtomicBoolean();

  @Before
  public void setUp() throws Exception
  {
    transformers.put("Transformer", data ->
        FMap.<String, Object>b()
            .o(EVENT_ID, data.remove(EVENT_ID))
            .o(MESSAGE, data.remove(MESSAGE)));

    new Thread(new EventNormalizer(transformers, in, out)
    {
      @Override
      protected void terminated()
      {
        super.terminated();
        done.set(true);
      }
    }).start();

    // send some data in the input queue
    for (int i = 0; i < MAX_QUEUE_SIZE; i++)
    {
      in.put(new Event(
          FMap.<String, Object>b()
              .p(EVENT_ID, i)
              .p(MESSAGE, TEST_MESSAGE)));
    }
  }

  @After
  public void tearDown() throws Exception
  {
    // send 'eos' event to terminate the transformer's thread
    in.put(Event.eos());
  }

  @SuppressWarnings("BusyWait")
  @AfterClass
  public static void afterClass() throws InterruptedException
  {
    for (int i = 0; !done.get() && i < MAX_QUEUE_SIZE; i++)
    {
      Thread.sleep(200);
    }
    Assert.assertTrue(done.get());
  }

  @Test
  public void validate() throws InterruptedException
  {
    for (int i = 0; i < MAX_QUEUE_SIZE; i++)
    {
      final Map<String, Object> data = out.take().data();

      Assert.assertEquals(3, data.size());
      Assert.assertEquals(i, data.get(EVENT_ID));
      Assert.assertEquals(TEST_MESSAGE, data.get(MESSAGE));
    }

    Assert.assertEquals(0, out.available());
  }

}