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

package io.ocsf.utils.parsers;

import java.util.HashMap;
import java.util.Map;

/**
 * ArcSight Common Event Format (CEF) parser. For more information see
 * doc/CEF_White_Paper_20100722.pdf.
 * <p>
 * NOTE: This class is intended for use in a single thread.
 *
 * @author Roumen Roupski
 */
public final class CEFParser
{
  public static final  String CEF_PREFIX     = "CEF:";
  private static final int    CEF_PREFIX_LEN = CEF_PREFIX.length();

  // CEF Prefix Fields
  public static final String VERSION        = "Version";
  public static final String DEVICE_VENDOR  = "DeviceVendor";
  public static final String DEVICE_PRODUCT = "DeviceProduct";
  public static final String DEVICE_VERSION = "DeviceVersion";
  public static final String SIGNATURE_ID   = "SignatureID";
  public static final String NAME           = "Name";
  public static final String SEVERITY       = "Severity";
  public static final String EXTENSION      = "Extension";

  private CEFParser() {}

  /**
   * Parses the given text containing CEF message.
   * <p>
   * CEF:Version|Device Vendor|Device Product|Device Version|Signature ID|Name|Severity|Extension
   *
   * @param text the CEF message to parse
   * @return the parsed CEF data
   * @throws ParserException If syntax error.
   */
  public static Map<String, Object> parse(final String text) throws ParserException
  {
    if (text == null || text.isEmpty()) return null;

    final Map<String, Object> data = parseCEFMessage(text);

    final String extension = (String) data.get(EXTENSION);

    final Map<String, Object> fields = new NameValueParser(extension.toCharArray()).parse();

    data.put(EXTENSION, fields);

    return data;
  }

  // Parses a CEF message, the expected format is:
  // CEF:Version|Device Vendor|Device Product|Device Version|Signature ID|Name|Severity|Extension
  private static Map<String, Object> parseCEFMessage(final String message)
  {
    final int pos = message.indexOf(CEF_PREFIX);

    if (pos == 0)
    {
      final String[] fields = message.substring(CEF_PREFIX_LEN).split("\\|", 8);
      if (fields.length == 8)
      {
        final Map<String, Object> map = new HashMap<>();
        map.put(VERSION, parseInteger(fields[0]));
        map.put(DEVICE_VENDOR, fields[1]);
        map.put(DEVICE_PRODUCT, fields[2]);
        map.put(DEVICE_VERSION, fields[3]);
        map.put(SIGNATURE_ID, fields[4]);
        map.put(NAME, fields[5]);
        map.put(SEVERITY, parseInteger(fields[6]));
        map.put(EXTENSION, fields[7]);

        return map;
      }
    }

    throw new ParserException("Invalid CEF message: " + message);
  }

  private static Object parseInteger(final String s)
  {
    try
    {
      return Integer.parseInt(s.trim());
    }
    catch (final NumberFormatException ex)
    {
      return s;
    }
  }
}
