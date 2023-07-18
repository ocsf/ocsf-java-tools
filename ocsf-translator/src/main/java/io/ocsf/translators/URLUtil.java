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

package io.ocsf.translators;

import io.ocsf.schema.Dictionary;
import io.ocsf.utils.FMap;
import io.ocsf.utils.Strings;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public final class URLUtil
{
  private URLUtil() {}

  static final Map<String, Object> EMPTY = FMap.<String, Object>b()
    .p(Dictionary.Text, Strings.EMPTY)
    .p(Dictionary.Scheme, Strings.EMPTY)
    .p(Dictionary.Hostname, Strings.EMPTY);

  /**
   * Parses a given URL string and extracts the URL components.
   *
   * @param urlStr URL string
   * @return a URL object
   */
  public static Map<String, Object> toUrl(final String urlStr) throws MalformedURLException
  {
    if (Strings.isEmpty(urlStr))
    {
      return EMPTY;
    }

    final URL aURL = new URL(urlStr);

    return FMap.<String, Object>b()
      .p(Dictionary.Text, urlStr)
      .p(Dictionary.Scheme, aURL.getProtocol())
      .p(Dictionary.Hostname, aURL.getHost())
      .p(Dictionary.Port, port(aURL))
      .p(Dictionary.Path, aURL.getPath())
      .o(Dictionary.Query, aURL.getQuery());
  }

  private static Integer port(final URL url)
  {
    final int port = url.getPort();

    return port > 0 ? port : url.getDefaultPort();
  }
}
