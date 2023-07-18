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

import io.ocsf.parser.Parser;
import io.ocsf.schema.Event;
import io.ocsf.schema.RawEvent;
import io.ocsf.schema.concurrent.ProcessorList;
import io.ocsf.schema.config.ConfigParsers;
import io.ocsf.schema.config.ConfigTranslators;
import io.ocsf.translators.Translators;
import io.ocsf.utils.Maps;

import java.io.IOException;
import java.util.Map;

/**
 * Single thread event service.
 * <p>
 * The parsing and the translations are done in the context of the calling thread.
 */
public class EventService
{
  private final ProcessorList<Parser> parsers;
  private final ProcessorList<Translators> normalizers;

  /**
   * Creates a new event service that parses and translates events in a blocking call;
   *
   * @param rules the rules folder
   * @throws IOException when unable to read the rule files from the given folder
   */
  public EventService(final String rules) throws IOException
  {
    this.parsers = ConfigParsers.parsers();
    this.normalizers = ConfigTranslators.load(rules);
  }

  /**
   * Process a single event in a blocking call.
   *
   * @param data the event data to process
   * @return the parsed and normalized event
   * @throws TranslatorException when unable to translate the input data
   */
  public Map<String, Object> process(final Map<String, Object> data) throws TranslatorException
  {
    final String source = (String) data.get(RawEvent.SOURCE_TYPE);

    // validate the input data
    if (source == null)
    {
      throw new TranslatorException(TranslatorException.Reason.MissingSourceType);
    }

    final Parser parser = parsers.get(source);
    if (parser == null)
    {
      throw new TranslatorException(TranslatorException.Reason.NoParser);
    }

    final Translators translators = normalizers.get(source);
    if (translators == null)
    {
      throw new TranslatorException(TranslatorException.Reason.NoTranslator);
    }

    final String raw = (String) data.get(RawEvent.RAW_EVENT);
    if (raw == null)
    {
      throw new TranslatorException(TranslatorException.Reason.MissingRawData);
    }

    final Map<String, Object> parsed;
    try
    {
      parsed = parser.parse(raw);
    }
    catch (final Exception e)
    {
      throw new TranslatorException(TranslatorException.Reason.ParserError, e);
    }

    if (parsed == null)
    {
      throw new TranslatorException(TranslatorException.Reason.UnsupportedEvent);
    }

    Maps.putIn(parsed, Event.CUSTOMER_ID, data.get(RawEvent.TENANT));
    Maps.putIn(parsed, Event.SOURCE_TYPE, data.get(RawEvent.SOURCE_TYPE));

    final Map<String, Object> translated;
    try
    {
      translated = translators.translate(parsed);
    }
    catch (final Exception e)
    {
      throw new TranslatorException(TranslatorException.Reason.TranslatorError, e);
    }

    if (translated == null)
    {
      throw new TranslatorException(TranslatorException.Reason.UnsupportedEvent);
    }

    return translated; // finally, it is all good
  }
}
