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

package io.ocsf.schema.cli;

import io.ocsf.utils.Strings;
import io.ocsf.utils.parsers.Json5Parser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * General interface to the event-schema REST services
 */
public class SchemaServices
{
  public static final String SCHEMA_URL    = "https://schema.ocsf.io";
  public static final String VALIDATE_PATH = "/api/validate";

  private final String url;

  /**
   * Use it to override the default Schema url
   *
   * @param url replacer url to the event-schema interfaces
   * @see SchemaServices#SCHEMA_URL
   */
  public SchemaServices(final String url)
  {
    this.url = Strings.isEmpty(url) ? SCHEMA_URL : url;
  }

  /**
   * Posts jsonContent to the event-schema validation service.
   *
   * @param data - json content to be validated.
   * @return The jsonObject of the return results. An empty Map signified success. Otherwise, the
   * Json map has the detailed info on why validation failed.
   * @throws IOException          on http connection failures.
   * @throws InterruptedException on http connection failures
   */
  public Map<String, Object> validate(final String data) throws IOException, InterruptedException
  {
    final String                    validate_url = this.url + VALIDATE_PATH;
    final HttpClient                client       = HttpClient.newHttpClient();
    final HttpRequest.BodyPublisher bp           = HttpRequest.BodyPublishers.ofString(data);

    final HttpRequest request = HttpRequest.newBuilder()
                                           .uri(URI.create(validate_url))
                                           .header("Content-Type", "application/json")
                                           .header("Accept", "application/json")
                                           .version(HttpClient.Version.HTTP_1_1)
                                           .POST(bp)
                                           .build();

    final HttpResponse<String> response =
      client.send(request, HttpResponse.BodyHandlers.ofString());

    final Map<String, Object> jsonObj;
    if (response.statusCode() != 200)
    {
      jsonObj = new HashMap<>();
      jsonObj.put("service", "failed");
      jsonObj.put("code", response.statusCode());
      jsonObj.put("message", response.body());
    }
    else
    {
      jsonObj = Json5Parser.to(response.body());
    }
    return jsonObj;
  }
}
