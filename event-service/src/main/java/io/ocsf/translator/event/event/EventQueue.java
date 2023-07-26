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

import java.util.concurrent.ArrayBlockingQueue;

public class EventQueue<T extends Event> implements Source<T>, Sink<T>
{
  private final ArrayBlockingQueue<T> queue;

  /**
   * Creates a BlockingQueue with the given (fixed) capacity and default access policy.
   *
   * @param capacity the capacity of this queue
   */
  public EventQueue(final int capacity)
  {
    this.queue = new ArrayBlockingQueue<>(capacity);
  }

  /**
   * Creates a BlockingQueue with the default capacity.
   */
  public EventQueue()
  {
    this.queue = new ArrayBlockingQueue<>(8);
  }

  @Override
  public void put(final T t) throws InterruptedException {queue.put(t);}

  @Override
  public T take() throws InterruptedException {return queue.take();}

  @Override
  public int available()
  {
    return queue.size();
  }
}
