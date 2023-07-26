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

package io.ocsf.parsers;

import io.ocsf.utils.Maps;
import io.ocsf.utils.parsers.Parser;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class WindowsXmlParser implements Parser
{
  public static final String SourceType = "microsoft:windows:xml";

  private static final String RenderingInfo = "RenderingInfo";
  private static final String EventData     = "EventData";
  private static final String EmptyValue    = "-";

  // special handling for ContextInfo in event 4103
  private static final String Event4103   = "4103";
  private static final String EventID     = "EventID";
  private static final String ContextInfo = "ContextInfo";

  private final XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();

  @Override
  public Map<String, Object> parse(final String text) throws Exception
  {
    final XMLStreamReader     reader =
      xmlInputFactory.createXMLStreamReader(new StringReader(text));
    final Map<String, Object> event  = new HashMap<>();
    final StringBuilder       buf    = new StringBuilder();

    String key = null;
    while (reader.hasNext() && reader.next() != XMLStreamConstants.END_DOCUMENT)
    {
      switch (reader.getEventType())
      {
        case XMLStreamConstants.START_ELEMENT:
        {
          final String name = reader.getLocalName();
          final int    n    = reader.getAttributeCount();

          if (n > 0)
          {
            final Map<String, Object> data = new HashMap<>();
            for (int i = 0; i < n; ++i)
                 put(data, reader.getAttributeLocalName(i), reader.getAttributeValue(i));

            event.put(name, data);

            if (RenderingInfo.equals(name))
            {
              if (Event4103.equals(event.get(EventID)))
              {
                // skip the RenderingInfo data, which is redundant (see Payload)
                while (reader.hasNext() && reader.next() != XMLStreamConstants.END_DOCUMENT)
                {
                  if (reader.getEventType() == XMLStreamConstants.END_ELEMENT &&
                      RenderingInfo.equals(reader.getLocalName()))
                    break;
                }
              }
            }
          }
          else if (EventData.equals(name))
          {
            event.put(name, parse(reader, name));
          }

          key = name;
          break;
        }

        case XMLStreamConstants.END_ELEMENT:
        {
          if (buf.length() > 0)
          {
            put(event, key, buf.toString());
            buf.setLength(0);
          }
          break;
        }

        case XMLStreamConstants.CHARACTERS:
        {
          if (!reader.isWhiteSpace())
            buf.append(reader.getText());
          break;
        }

        default:
          break;
      }
    }

    return cleanup(event);
  }

  private static Map<String, Object> parse(final XMLStreamReader reader, final String end)
    throws Exception
  {
    final Map<String, Object> data = new HashMap<>();
    final StringBuilder       buf  = new StringBuilder();

    String name = null;
    while (reader.next() != XMLStreamConstants.END_DOCUMENT)
    {
      switch (reader.getEventType())
      {
        case XMLStreamConstants.START_ELEMENT:
        {
          if (reader.getAttributeCount() == 1)
            name = reader.getAttributeValue(0);
          break;
        }

        case XMLStreamConstants.END_ELEMENT:
        {
          if (end.equals(reader.getLocalName()))
            return data;

          if (buf.length() > 0)
          {

            data.put(name, buf.toString());
            buf.setLength(0);
          }
          break;
        }

        case XMLStreamConstants.CHARACTERS:
        {
          if (!reader.isWhiteSpace())
            buf.append(reader.getText());
          break;
        }

        default:
          break;
      }
    }

    return data;
  }

  private static final void put(
    final Map<String, Object> data, final String name, final String text)
  {
    final String value = text.trim();
    if (!EmptyValue.equals(value))
      data.put(name, value);
  }

  private static Map<String, Object> cleanup(final Map<String, Object> event)
  {
    if (Event4103.equals(event.get(EventID)))
    {
      final Map<String, Object> data = Maps.typecast(event.get(EventData));
      if (data != null)
      {
        final String info = (String) Maps.getIn(event, EventData, ContextInfo);
        if (info != null)
        {
          final WindowsMultilineParser.MultiLineParser
            parser = new WindowsMultilineParser.MultiLineParser(info);
          data.put(ContextInfo, parser.nested());
        }
      }
    }

    return event;
  }

  @Override
  public String toString() {return SourceType;}
}
