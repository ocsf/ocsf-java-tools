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

package io.ocsf.utils;

import io.ocsf.utils.parsers.NameValueParser;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class NameValueParserTest
{
  @Test
  public void parseBoxEvent1()
  {
    final String              text =
      "source_item_type=\"file\",source_item_id=\"848930090697\",source_item_name=\"jdk-11.0" +
      ".11_linux-x64_bin.rpm\",source_parent_type=\"folder\",source_parent_name=\"TA-SQL-Setup\"," +
      "source_parent_id=\"121270888195\",source_owned_by_type=\"user\"," +
      "source_owned_by_id=\"12345678901\",source_owned_by_name=\"DummyUser01\"," +
      "source_owned_by_login=\"dummy.user01@xyz.com\",created_by_type=\"user\"," +
      "created_by_id=\"12345678901\",created_by_name=\"DummyUser01\",created_by_login=\"dummy" +
      ".user01@xyz.com\",action_by=\"\",created_at=\"2021-08-20T03:11:06-07:00\"," +
      "event_id=\"f1d6a20b-587a-47f1-bce3-5cdd9584feef\",event_type=\"UPLOAD\",ip_address=\"22.33" +
      ".44.55\",type=\"event\",session_id=\"\",additional_details_size=\"163646728\"," +
      "additional_details_ekm_id=\"06f2c850-213c-4093-811d-21b56aed8b3c\"," +
      "additional_details_version_id=\"910921304697\",additional_details_service_id=\"231318\"," +
      "additional_details_service_name=\"Multiput Uploads\",account_id=12345678901";
    final Map<String, Object> data = NameValueParser.parse((text));
    assertEquals(27, data.size());
  }

  @Test
  public void parseBoxEvent2()
  {
    final String              text =
      ("session_id=\"\",type=\"event\",event_type=\"UPLOAD\",ip_address=\"10.12.15.31\"," +
       "source_parent_id=\"40771239726\",source_parent_name=\"2018-08-10\"," +
       "source_parent_type=\"folder\",source_item_type=\"file\",source_item_id=\"239680722144\"," +
       "source_item_name=\"Hotel.jpeg\",additional_details_ekm_id=\"447bbbdd-5a63-4a3a-88b3" +
       "-940999807ffb\",additional_details_service_id=\"5168\"," +
       "additional_details_size=\"488936\",additional_details_version_id=\"252852293344\"," +
       "additional_details_service_name=\"Box Sync for Mac\"," +
       "created_at=\"2022-09-20T02:16:48-00:00\"," +
       "event_id=\"68cad023-30dd-4da3-ba7f-c431d92ee043\",created_by_id=\"223440415\"," +
       "created_by_name=\"Simon Roma\",created_by_type=\"user\",created_by_login=\"sroma@acme" +
       ".com\"");
    final Map<String, Object> data = NameValueParser.parse((text));
    assertEquals(21, data.size());
  }
}