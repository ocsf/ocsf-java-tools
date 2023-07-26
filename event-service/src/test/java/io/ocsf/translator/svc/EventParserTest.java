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
import io.ocsf.utils.parsers.Parser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class EventParserTest extends Tests
{
  // create a very simple "parser"
  private final Parser parser =
    text -> FMap.<String, Object>b().p(EVENT_ID, Integer.parseInt(text));

  private static final AtomicBoolean done = new AtomicBoolean();

  @Before
  public void setUp() throws Exception
  {
    new Thread(new EventParser(parser, in, out)
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
            .p(Splunk.RAW_EVENT, Integer.toString(i))
            .p(Splunk.TENANT, "Tenant")
            .p(Splunk.SOURCE_TYPE, TEST_MESSAGE)));
    }
  }

  @After
  public void tearDown() throws Exception
  {
    // send 'eos' event to terminate the parser's thread
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
      final Event data = out.take();

      Assert.assertEquals(3, data.size());
      Assert.assertEquals(Integer.valueOf(i), data.get(EVENT_ID));
      Assert.assertEquals(TEST_MESSAGE, data.getIn(Splunk.CIM_SOURCE_TYPE));
    }

    Assert.assertEquals(0, out.available());
  }

}