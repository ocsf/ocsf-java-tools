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
import io.ocsf.utils.Json5Parser;

import java.util.Map;

final class Data
{
  static final Map<String, Object> ProcessActivity = Json5Parser.to(
      "{\n" +
          "  \"severity\": \"Informational\",\n" +
          "  \"activity_name\": \"Launch\",\n" +
          "  \"metadata\": {\n" +
          "    \"profiles\": [\"host\"],\n" +
          "    \"uid\": \"2e020cbd-bbef-4eb8-9d65-ef0304348301\",\n" +
          "    \"product\": {\n" +
          "      \"feature\": {\n" +
          "        \"name\": \"Microsoft-Windows-PowerShell/Operational\"\n" +
          "      },\n" +
          "      \"name\": \"Microsoft Windows\",\n" +
          "      \"vendor_name\": \"Microsoft\"\n" +
          "    },\n" +
          "    \"original_time\": \"10/07/2020 10:29:00 PM\",\n" +
          "    \"version\": \"0.33.0\"\n" +
          "  },\n" +
          "  \"category_uid\": 1,\n" +
          "  \"process\": {\n" +
          "    \"loaded_modules\": \"Set-StrictMode\",\n" +
          "    \"pid\": -1,\n" +
          "    \"file\": {\n" +
          "      \"name\": \"PSReadLine.psm1\",\n" +
          "      \"path\": \"C:\\\\Program Files\\\\WindowsPowerShell\\\\Modules\\\\PSReadline\\\\1.2\\\\PSReadLine.psm1\",\n" +
          "      \"parent_folder\": \"C:\\\\Program Files\\\\WindowsPowerShell\\\\Modules\\\\PSReadline\\\\1.2\",\n" +
          "      \"type\": \"Regular File\",\n" +
          "      \"type_id\": 1\n" +
          "    },\n" +
          "    \"cmd_line\": \"CommandInvocation(Set-StrictMode): \\\"Set-StrictMode\\\"\\nParameterBinding(Set-StrictMode): name=\\\"Off\\\"; value=\\\"True\\\"\"\n" +
          "  },\n" +
          "  \"type_name\": \"Process Activity: Launch\",\n" +
          "  \"category_name\": \"System Activity\",\n" +
          "  \"message\": \"Powershell Module Logging\",\n" +
          "  \"unmapped\": {\n" +
          "    \"Context\": {\n" +
          "      \"Runspace ID\": \"377d8ad3-3bae-46b1-aa6d-0a8ffd3c8288\",\n" +
          "      \"Engine Version\": \"5.1.14393.3866\",\n" +
          "      \"Host ID\": \"548cbebc-322f-4cf8-b2b6-8265f4391cd9\",\n" +
          "      \"Command Path\": \"\",\n" +
          "      \"Severity\": \"Informational\",\n" +
          "      \"Host Name\": \"ConsoleHost\",\n" +
          "      \"Shell ID\": \"Microsoft.PowerShell\",\n" +
          "      \"Sequence Number\": \"186\",\n" +
          "      \"Command Type\": \"Cmdlet\",\n" +
          "      \"Pipeline ID\": \"21\",\n" +
          "      \"Connected User\": \"\",\n" +
          "      \"Host Version\": \"5.1.14393.3866\"\n" +
          "    },\n" +
          "    \"User\": \"NOT_TRANSLATED\",\n" +
          "    \"TaskCategory\": \"Executing Pipeline\",\n" +
          "    \"EventType\": \"4\",\n" +
          "    \"SourceName\": \"Microsoft-Windows-PowerShell\",\n" +
          "    \"EventCode\": \"4103\",\n" +
          "    \"SidType\": \"0\",\n" +
          "    \"OpCode\": \"To be used when operation is just executing a method\",\n" +
          "    \"RecordNumber\": \"225352\"\n" +
          "  },\n" +
          "  \"actor\": {\n" +
          "    \"user\": {\n" +
          "      \"name\": \"ATTACKRANGE\\\\administrator\",\n" +
          "      \"uid\": \"S-1-5-21-2825133891-65375684-292279277-500\",\n" +
          "      \"account_type_id\": 2,\n" +
          "      \"account_type\": \"Windows Account\"\n" +
          "    },\n" +
          "    \"process\": {\n" +
          "      \"cmd_line\": \"powershell\",\n" +
          "      \"pid\": -1\n" +
          "    }\n" +
          "  },\n" +
          "  \"observables\": [\n" +
          "    {\n" +
          "      \"type_id\": 25,\n" +
          "      \"caption\": \"process\",\n" +
          "      \"type\": \"Process\"\n" +
          "    },\n" +
          "    {\n" +
          "      \"type_id\": 1,\n" +
          "      \"caption\": \"device.hostname\",\n" +
          "      \"type\": \"Hostname\",\n" +
          "      \"value\": \"win-dc-1603297.attackrange.local\"\n" +
          "    },\n" +
          "    {\n" +
          "      \"type_id\": 25,\n" +
          "      \"caption\": \"actor.process\",\n" +
          "      \"type\": \"Process\"\n" +
          "    },\n" +
          "    {\n" +
          "      \"type_id\": 21,\n" +
          "      \"caption\": \"actor.user\",\n" +
          "      \"type\": \"User\"\n" +
          "    },\n" +
          "    {\n" +
          "      \"type_id\": 24,\n" +
          "      \"caption\": \"process.file\",\n" +
          "      \"type\": \"File\"\n" +
          "    },\n" +
          "    {\n" +
          "      \"type_id\": 20,\n" +
          "      \"caption\": \"device\",\n" +
          "      \"type\": \"Endpoint\"\n" +
          "    }\n" +
          "  ],\n" +
          "  \"status_id\": -1,\n" +
          "  \"class_uid\": 1007,\n" +
          "  \"activity_id\": 1,\n" +
          "  \"severity_id\": 1,\n" +
          "  \"time\": 1602134940000,\n" +
          "  \"class_name\": \"Process Activity\",\n" +
          "  \"device\": {\n" +
          "    \"hostname\": \"win-dc-1603297.attackrange.local\",\n" +
          "    \"os\": {\n" +
          "      \"type\": \"Windows\",\n" +
          "      \"name\": \"Windows\",\n" +
          "      \"type_id\": 100\n" +
          "    },\n" +
          "    \"type\": \"Unknown\",\n" +
          "    \"type_id\": 0\n" +
          "  },\n" +
          "  \"type_uid\": 100701,\n" +
          "  \"status\": \"None\"\n" +
          "}\n");
  private Data() {}

  public static void main(final String ... args)
  {
    System.out.println(Json.format(ProcessActivity));
  }
}
