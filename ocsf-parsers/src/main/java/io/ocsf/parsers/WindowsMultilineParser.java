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

import io.ocsf.schema.Dictionary;
import io.ocsf.utils.Maps;
import io.ocsf.utils.Strings;
import io.ocsf.utils.parsers.Parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Microsoft Windows multiline event parser.
 * <p>
 * The parser grammar is:
 *
 * <pre>
 *   &lt;event&gt;      := &lt;timestamp&gt;&lt;eol&gt;&lt;fields&gt;
 *   &lt;timestamp&gt;  := "M/dd/yyyy h:mm:ss a"
 *   &lt;fields&gt;     := &lt;name&gt;&lt;separator&gt;&lt;eol&gt;{&lt;nested&gt;} | {&lt;field&gt;}
 *   &lt;nested&gt;     := {&lt;space&gt;}&lt;field&gt;
 *   &lt;field&gt;      := &lt;name&gt;&lt;separator&gt;&lt;value&gt;&lt;eol&gt;
 *   &lt;name&gt;       := alpha {alphanumeric | "-" | "_" | " " | "(" | ")"}
 *   &lt;value&gt;      := text | multi-line
 *   &lt;space&gt;      := ' ' | '\t'
 *   &lt;separator&gt;  := ':' | '='
 *   &lt;eol&gt;        := '\n' | '\r\n'
 *  </pre>
 */
public class WindowsMultilineParser implements Parser
{
  public static final String SourceType = "microsoft:windows:multiline";

  @Override
  public Map<String, Object> parse(final String text) throws Exception
  {
    return new MultiLineParser(text).parse();
  }

  @Override
  public String toString() {return SourceType;}

  static final class MultiLineParser
  {
    private static final int Comment    = '#';
    private static final int Separator1 = ':';
    private static final int Separator2 = '=';
    private static final int CR         = '\r';
    private static final int LF         = '\n';
    private static final int TAB        = '\t';
    private static final int SPC        = ' ';

    private static final String Message       = "Message";
    private static final String EventCode     = "EventCode";
    private static final String Context       = "Context:";
    private static final String ScriptBlockID = "ScriptBlock ID:";

    private final char[] buf;

    private int pos     = 0;
    private int dataPos = 0;
    private int namePos = 0;
    private int eventID = 0;

    MultiLineParser(final String text)
    {
      this.buf = text.toCharArray();
    }

    Map<String, Object> parse()
    {
      final Map<String, Object> event = new HashMap<>();

      // skip the comments
      pos = skip(buf, pos, buf.length);

      event.put(Dictionary.REF_EVENT_TIME, eventTime());

      return parse(event);
    }

    Map<String, Object> parse(final Map<String, Object> event)
    {
      // parse the event fields
      String last = null;
      for (String name = name(); name != null; last = name, name = name())
      {
        if (dataPos > 0)
        {
          final int len = namePos - dataPos;
          if (len > 0)
          {
            multiLineValue(event, last, new String(buf, dataPos, len));
          }
        }

        final String value = value(name);
        if (value == null)
        {
          Maps.put(event, name, nested());
        }
        else
        {
          if (EventCode.equals(name))
            eventID = Integer.parseInt(value);

          Maps.put(event, name, value);
        }
      }

      if (dataPos > 1 && buf[dataPos - 1] != LF)
      {
        final int len = namePos - dataPos;
        if (len > 0)
        {
          multiLineValue(event, last, new String(buf, dataPos, len));
        }
      }

      return event;
    }

    private String eventTime()
    {
      return readLine();
    }

    private String name()
    {
      dataPos = 0;

      int start;
      while (pos < buf.length)
      {
        start = skip(buf, pos, buf.length);

        int i = start;
        while (i < buf.length && isLegalChar(buf[i]))
          ++i;

        if (i < buf.length)
        {
          // the name is followed by a separator
          if (buf[i] == Separator1 || buf[i] == Separator2)
          {
            pos = i + 1;

            // remove the trailing white spaces
            while (buf[i - 1] <= SPC)
              --i;

            namePos = start;
            return new String(buf, start, i - start).intern();
          }
          else if (dataPos == 0)
          {
            dataPos = start;
          }
        }
        else
        {
          // end of data
          break;
        }

        // skip the data and try to find the next field name
        pos = i + 1;
      }

      namePos = buf.length;
      return null; // end of data
    }

    private String value(final String name)
    {
      pos = skipSpace(buf, pos, buf.length);

      if (pos >= buf.length)
      {
        return Strings.EMPTY;
      }

      // check for nested values
      if (buf[pos] == LF)
      {
        ++pos;
        return pos < buf.length && isSpaceChar(buf[pos]) ? null : Strings.EMPTY;
      }

      // check for nested values
      if (buf[pos] == CR)
      {
        pos += 2;
        return pos < buf.length && isSpaceChar(buf[pos]) ? null : Strings.EMPTY;
      }

      final int start = pos;
      String    line  = readLine();

      // special parsing for powershell 4104 and 4103 events
      if (eventID == 4104 && Message.equals(name))
      {
        int last = pos;
        while (pos < buf.length)
        {
          line = readLine();
          if (line.startsWith(ScriptBlockID))
          {
            pos = last;
            return new String(buf, start, last - start - 1);
          }
          last = pos;
        }
      }
      else if (eventID == 4103 && Message.equals(name))
      {
        int last = pos;
        while (pos < buf.length)
        {
          line = readLine();
          if (line.startsWith(Context))
          {
            pos = last;
            return new String(buf, start, last - start - 1);
          }
          last = pos;
        }
      }

      return line;
    }

    // parse nested event fields
    public Map<String, Object> nested()
    {
      final Map<String, Object> event = new HashMap<>();

      for (String last = null, name = name(); name != null; last = name, name = name())
      {
        if (dataPos > 0)
        {
          final int len = namePos - dataPos;
          if (len > 0)
          {
            multiLineValue(event, last, new String(buf, dataPos, len));
            if (pos < buf.length)
            {
              final int next = buf[pos];
              if (isSpaceChar(next))
              {
                final int i = skipSpace(buf, pos, buf.length);
                if (buf[i] > SPC)
                {
                  event.put(name, nestedValue());
                  continue;
                }
              }
            }

            // go back to the last field name
            pos -= name.length() + 1;
            break;
          }
        }

        event.put(name, nestedValue());

        if (pos < buf.length)
        {
          final int next = buf[pos];
          if (isSpaceChar(next))
          {
            final int i = skipSpace(buf, pos, buf.length);
            if (buf[i] >= 'A')
              continue;
          }
        }
        break;
      }

      return event.isEmpty() ? null : event;
    }

    private String nestedValue()
    {
      pos = skipSpace(buf, pos, buf.length);

      // check for empty values
      if (pos >= buf.length)
      {
        return Strings.EMPTY;
      }
      if (buf[pos] == LF)
      {
        ++pos;
        return Strings.EMPTY;
      }
      if (buf[pos] == CR)
      {
        pos += 2;
        return Strings.EMPTY;
      }

      return readLine();
    }

    private static void multiLineValue(
      final Map<String, Object> event, final String name,
      final String text)
    {
      final Object last = event.get(name);
      if (last instanceof String)
        event.put(name, values((String) last, text));
    }

    private static List<String> values(final String first, final String text)
    {
      final String[]          values = text.split("\n");
      final ArrayList<String> list   = new ArrayList<>();

      list.add(first);
      for (final String value : values)
      {
        final String v = value.trim();
        // an empty line terminates multi-value list
        if (v.isEmpty())
          break;

        list.add(v);
      }

      return list;
    }

    /*
     * Skip comments and whitespaces, returns the position of the next line or 'len' if end of the
     * buffer is reached.
     */
    private static int skip(final char[] buf, final int pos, final int len)
    {
      for (int i = pos; i < len; ++i)
      {
        if (buf[i] == Comment)
          i = skipLine(buf, i, len);
        else if (!Character.isWhitespace(buf[i]))
          return i;
      }

      return len;
    }

    private static int skipLine(final char[] buf, final int pos, final int len)
    {
      for (int i = pos; i < len; ++i)
        if (buf[i] == LF)
          return i;

      return len;
    }

    private static int skipSpace(final char[] buf, final int pos, final int len)
    {
      for (int i = pos; i < len; ++i)
        if (!isSpaceChar(buf[i]))
          return i;

      return len;
    }

    private String readLine()
    {
      final int start = pos;

      int i = start;
      while (i < buf.length && buf[i] != LF)
        ++i;

      pos = i + 1; // skip the EOL

      if (buf[i - 1] == CR)
        i -= 1;

      return new String(buf, start, i - start);
    }

    private static boolean isSpaceChar(final int ch)
    {
      return ch == SPC || ch == TAB;
    }

    private static final char[] LegalChars = {
      0, 0, 0, 0, 0, 0, 0, 0, 0, TAB, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      SPC, 0, 0, 0, 0, 0, 0, 0, '(', ')', 0, 0, 0, '-', 0, 0,
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 0, 0, 0, 0, 0, 0,
      0, 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
      'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 0, 0, 0, 0, '_',
      0, 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
      'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 0, 0, 0, 0, 0
    };

    private static boolean isLegalChar(final int ch)
    {
      return ch > 0 && ch < LegalChars.length && LegalChars[ch] != 0;
    }

  }
}
