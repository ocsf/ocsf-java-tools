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

import io.ocsf.utils.event.Sink;
import io.ocsf.utils.event.Source;
import io.ocsf.utils.event.Transformer;
import io.ocsf.utils.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class EventNormalizer extends Transformer
{
  private static final Logger logger = LoggerFactory.getLogger(EventNormalizer.class);

  private final TranslatorsManager translators;

  public EventNormalizer(
    final TranslatorsManager translators, final Source<Event> source, final Sink<Event> sink)
  {
    super(translators.toString(), source, sink);
    this.translators = translators;
  }

  @Override
  protected Event process(final Event data)
  {
    try
    {
      final Map<String, Object> event = translators.translate(data.data());
      if (event != null)
      {
        return new Event(event);
      }
    }
    catch (final Exception e)
    {
      logger.warn("{} unable to normalize event: {}", this, data);
    }

    return null;
  }
}
