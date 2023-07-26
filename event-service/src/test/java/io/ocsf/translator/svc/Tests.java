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

public class Tests
{
  protected static final String EVENT_ID     = "id";
  protected static final String EVENT_ORIGIN = "origin";
  protected static final String MESSAGE      = "message";

  protected static final String TEST_MESSAGE = "hello there";

  protected static final int MAX_QUEUE_SIZE = 5;

  protected final EventQueue<Event> in  = new EventQueue<>(MAX_QUEUE_SIZE);
  protected final EventQueue<Event> out = new EventQueue<>(MAX_QUEUE_SIZE);

}
