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

package io.ocsf.schema;

import io.ocsf.utils.Json;
import io.ocsf.utils.parsers.Json5Parser;

import java.util.Map;

final class Data
{
  static final int TEST_CLASS_ID    = 1001; // File System Activity
  static final int TEST_ACTIVITY_ID = 3; // Update

  static final Map<String, Object> ProcessActivity = Json5Parser.to(
    "{" +
    "  \"severity\": \"Informational\"," +
    "  \"activity_name\": \"Launch\"," +
    "  \"metadata\": {" +
    "    \"profiles\": [\"host\"]," +
    "    \"uid\": \"2e020cbd-bbef-4eb8-9d65-ef0304348301\"," +
    "    \"product\": {" +
    "      \"feature\": {" +
    "        \"name\": \"Microsoft-Windows-PowerShell/Operational\"" +
    "      }," +
    "      \"name\": \"Microsoft Windows\"," +
    "      \"vendor_name\": \"Microsoft\"" +
    "    }," +
    "    \"original_time\": \"10/07/2020 10:29:00 PM\"," +
    "    \"version\": \"0.33.0\"" +
    "  }," +
    "  \"category_uid\": 1," +
    "  \"process\": {" +
    "    \"loaded_modules\": \"Set-StrictMode\"," +
    "    \"pid\": -1," +
    "    \"file\": {" +
    "      \"name\": \"PSReadLine.psm1\"," +
    "      \"path\": \"C:\\\\Program Files\\\\WindowsPowerShell\\\\Modules\\\\PSReadline\\\\1" +
    ".2\\\\PSReadLine.psm1\"," +
    "      \"parent_folder\": \"C:\\\\Program " +
    "Files\\\\WindowsPowerShell\\\\Modules\\\\PSReadline\\\\1.2\"," +
    "      \"type\": \"Regular File\"," +
    "      \"type_id\": 1" +
    "    }," +
    "    \"cmd_line\": \"CommandInvocation(Set-StrictMode): " +
    "\\\"Set-StrictMode\\\"ParameterBinding(Set-StrictMode): name=\\\"Off\\\"; " +
    "value=\\\"True\\\"\"" +
    "  }," +
    "  \"type_name\": \"Process Activity: Launch\"," +
    "  \"category_name\": \"System Activity\"," +
    "  \"message\": \"Powershell Module Logging\"," +
    "  \"unmapped\": {" +
    "    \"Context\": {" +
    "      \"Runspace ID\": \"377d8ad3-3bae-46b1-aa6d-0a8ffd3c8288\"," +
    "      \"Engine Version\": \"5.1.14393.3866\"," +
    "      \"Host ID\": \"548cbebc-322f-4cf8-b2b6-8265f4391cd9\"," +
    "      \"Command Path\": \"\"," +
    "      \"Severity\": \"Informational\"," +
    "      \"Host Name\": \"ConsoleHost\"," +
    "      \"Shell ID\": \"Microsoft.PowerShell\"," +
    "      \"Sequence Number\": \"186\"," +
    "      \"Command Type\": \"Cmdlet\"," +
    "      \"Pipeline ID\": \"21\"," +
    "      \"Connected User\": \"\"," +
    "      \"Host Version\": \"5.1.14393.3866\"" +
    "    }," +
    "    \"User\": \"NOT_TRANSLATED\"," +
    "    \"TaskCategory\": \"Executing Pipeline\"," +
    "    \"EventType\": \"4\"," +
    "    \"SourceName\": \"Microsoft-Windows-PowerShell\"," +
    "    \"EventCode\": \"4103\"," +
    "    \"SidType\": \"0\"," +
    "    \"OpCode\": \"To be used when operation is just executing a method\"," +
    "    \"RecordNumber\": \"225352\"" +
    "  }," +
    "  \"actor\": {" +
    "    \"user\": {" +
    "      \"name\": \"ATTACKRANGE\\\\administrator\"," +
    "      \"uid\": \"S-1-5-21-2825133891-65375684-292279277-500\"," +
    "      \"account_type_id\": 2," +
    "      \"account_type\": \"Windows Account\"" +
    "    }," +
    "    \"process\": {" +
    "      \"cmd_line\": \"powershell\"," +
    "      \"pid\": -1" +
    "    }" +
    "  }," +
    "  \"observables\": [" +
    "    {" +
    "      \"type_id\": 25," +
    "      \"caption\": \"process\"," +
    "      \"type\": \"Process\"" +
    "    }," +
    "    {" +
    "      \"type_id\": 1," +
    "      \"caption\": \"device.hostname\"," +
    "      \"type\": \"Hostname\"," +
    "      \"value\": \"win-dc-1603297.attackrange.local\"" +
    "    }," +
    "    {" +
    "      \"type_id\": 25," +
    "      \"caption\": \"actor.process\"," +
    "      \"type\": \"Process\"" +
    "    }," +
    "    {" +
    "      \"type_id\": 21," +
    "      \"caption\": \"actor.user\"," +
    "      \"type\": \"User\"" +
    "    }," +
    "    {" +
    "      \"type_id\": 24," +
    "      \"caption\": \"process.file\"," +
    "      \"type\": \"File\"" +
    "    }," +
    "    {" +
    "      \"type_id\": 20," +
    "      \"caption\": \"device\"," +
    "      \"type\": \"Endpoint\"" +
    "    }" +
    "  ]," +
    "  \"status_id\": -1," +
    "  \"class_uid\": 1007," +
    "  \"activity_id\": 1," +
    "  \"severity_id\": 1," +
    "  \"time\": 1602134940000," +
    "  \"class_name\": \"Process Activity\"," +
    "  \"device\": {" +
    "    \"hostname\": \"win-dc-1603297.attackrange.local\"," +
    "    \"os\": {" +
    "      \"type\": \"Windows\"," +
    "      \"name\": \"Windows\"," +
    "      \"type_id\": 100" +
    "    }," +
    "    \"type\": \"Unknown\"," +
    "    \"type_id\": 0" +
    "  }," +
    "  \"type_uid\": 100701," +
    "  \"status\": \"None\"" +
    "}");

  private Data() {}

  public static void main(final String... args)
  {
    System.out.println(Json.format(ProcessActivity));
  }
}
