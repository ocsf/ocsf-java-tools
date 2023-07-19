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

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.Objects;

public class MutableProcessorList<T> extends ProcessorList<T>
{
  private static final Logger logger = LoggerFactory.getLogger(MutableProcessorList.class);

  public MutableProcessorList(final String name)
  {
    super(name);
  }

  /**
   * Register a new processor using the processor's name returned by <code>processor.toString()</code>.
   *
   * @param processor a new processor to be added to the set
   */
  public void register(final T processor)
  {
    Objects.requireNonNull(processor, "the processor cannot be null");

    register(processor.toString(), processor);
  }

  /**
   * Register a new processor using the given name.
   *
   * @param name      the parser name
   * @param processor the event processor to be added
   */
  public void register(final String name, final T processor)
  {
    Objects.requireNonNull(name, "the name cannot be null");
    Objects.requireNonNull(processor, "the processor cannot be null");

    if (processors.put(name, processor) != null)
    {
      logger.warn("Processor {} is already registered", name);
    }
  }
}
