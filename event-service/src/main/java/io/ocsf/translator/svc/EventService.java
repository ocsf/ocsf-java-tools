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

import io.ocsf.parsers.Parsers;
import io.ocsf.utils.FuzzyHashMap;
import io.ocsf.utils.Maps;
import io.ocsf.utils.parsers.Parser;

import java.io.IOException;
import java.util.Map;

/**
 * Single thread event service.
 * <p>
 * The parsing and the translations are done in the context of the calling thread.
 */
public class EventService
{
  private final FuzzyHashMap<Parser>             parsers;
  private final FuzzyHashMap<TranslatorsManager> normalizers;

  /**
   * Creates a new event service that parses and translates events in a blocking call;
   *
   * @param rules the rules folder
   * @throws IOException when unable to read the rule files from the given folder
   */
  public EventService(final String rules) throws IOException
  {
    this.parsers     = Parsers.parsers();
    this.normalizers = TranslatorsLoader.load(rules);
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
    final String source = (String) data.get(Splunk.SOURCE_TYPE);

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

    final TranslatorsManager translators = normalizers.get(source);
    if (translators == null)
    {
      throw new TranslatorException(TranslatorException.Reason.NoTranslator);
    }

    final String raw = (String) data.get(Splunk.RAW_EVENT);
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

    Maps.putIn(parsed, Splunk.CUSTOMER_ID, data.get(Splunk.TENANT));
    Maps.putIn(parsed, Splunk.CIM_SOURCE_TYPE, data.get(Splunk.SOURCE_TYPE));

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
