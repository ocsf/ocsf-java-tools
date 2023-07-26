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

import io.ocsf.schema.Dictionary;
import io.ocsf.utils.FMap;
import io.ocsf.utils.Strings;
import io.ocsf.translator.event.event.Event;
import io.ocsf.utils.parsers.Parser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EventProcessorTest extends Tests
{
  // create a very simple "parser"
  private final Parser parser =
    text -> FMap.<String, Object>b().p(EVENT_ID, Integer.parseInt(text));

  private final TranslatorsManager translators = new TranslatorsManager(Strings.EMPTY);

  @Before
  public void setUp() throws Exception
  {
    final EventProcessor processor = new EventProcessor(parser, translators, in, out);

    translators.put("Transformer", data ->
      FMap.<String, Object>b()
          .p(EVENT_ID, data.remove(EVENT_ID))
          .p(EVENT_ORIGIN, data.remove(EVENT_ORIGIN))
          .p(Dictionary.RAW_EVENT, data.remove(Dictionary.RAW_EVENT)));

    processor.start();

    // send some data in the input queue
    for (int i = 0; i < MAX_QUEUE_SIZE; i++)
    {
      in.put(new Event(
        FMap.<String, Object>b()
            .p(Dictionary.RAW_EVENT, Integer.toString(i))
            .p(EVENT_ORIGIN, "Internet")
            .p(MESSAGE, TEST_MESSAGE)));
    }
  }

  @After
  public void tearDown() throws Exception
  {
    // send 'eos' event to terminate the transformer's thread
    in.put(Event.eos());
  }

  @Test
  public void validate() throws InterruptedException
  {
    for (int i = 0; i < MAX_QUEUE_SIZE; i++)
    {
      final Event data = out.take();

      Assert.assertEquals(5, data.size());
      Assert.assertEquals(Integer.valueOf(i), data.get(EVENT_ID));
      Assert.assertEquals(
        TEST_MESSAGE, data.getIn(Dictionary.UNMAPPED, MESSAGE));
    }

    Assert.assertEquals(0, out.available());
  }

}