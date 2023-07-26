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

import io.ocsf.utils.Json;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class MultilineParserTest
{
  @Test
  public void parseNestedTest()
  {
    final String Event =
      "        Severity = Informational\n" +
      "        Host Name = ConsoleHost\n" +
      "        Host Version = 5.1.14393.4583\n" +
      "        Host ID = 92dd5f2a-6df4-4eb7-bc15-2456051d576d\n" +
      "        Host Application = C:\\Windows\\System32\\WindowsPowerShell\\v1" +
      ".0\\powershell.exe -NoProfile -NonInteractive -NoLogo -WindowStyle " +
      "hidden -ExecutionPolicy Unrestricted Import-Module " +
      "C:\\ProgramData\\Amazon\\EC2-Windows\\Launch\\Module\\Ec2Launch.psd1; " +
      "Set-Wallpaper\n" +
      "        Engine Version = 5.1.14393.4583\n" +
      "        Runspace ID = 48f983a5-33ab-4a10-9bf7-aadfd8e06157\n" +
      "        Pipeline ID = 1\n" +
      "        Command Name = Add-Type\n" +
      "        Command Type = Cmdlet\n" +
      "        Script Name = C:\\ProgramData\\Amazon\\EC2-Windows\\Launch" +
      "\\Module\\Scripts\\Set-Wallpaper.ps1\n" +
      "        Command Path = \n" +
      "        Sequence Number = 20\n" +
      "        User = ATTACKRANGE\\Administrator\n" +
      "        Connected User = \n" +
      "        Shell ID = Microsoft.PowerShell";

    final Map<String, Object> event = new WindowsMultilineParser.MultiLineParser(Event).nested();

    Assert.assertNotNull(event);
    Assert.assertEquals(16, event.size());
    Assert.assertEquals("Informational", event.get("Severity"));
    Assert.assertEquals("", event.get("Command Path"));
    Assert.assertEquals("Microsoft.PowerShell", event.get("Shell ID"));
  }

  @Test
  public void parseMultilineValueTest()
  {
    final String Event =
      "11/30/2020 05:27:13 PM\n" +
      "LogName=Security\n" +
      "EventCode=4672\n" +
      "EventType=0\n" +
      "ComputerName=W177-RaviR.CDSYS.LOCAL\n" +
      "SourceName=Microsoft Windows security auditing.\n" +
      "Type=Information\n" +
      "RecordNumber=33285\n" +
      "Keywords=Audit Success\n" +
      "TaskCategory=Special Logon\n" +
      "OpCode=Info\n" +
      "Message=Special privileges assigned to new logon.\n" +
      "\n" +
      "Subject:\n" +
      "        Security ID:                S-1-5-18\n" +
      "        Account Name:                W177-RaviR$\n" +
      "        Account Domain:                NT AUTHORITY\n" +
      "        Logon ID:                0x3E7\n" +
      "\n" +
      "Privileges:                SeAssignPrimaryTokenPrivilege\n" +
      "                        SeTcbPrivilege\n" +
      "                        SeSecurityPrivilege\n" +
      "                        SeTakeOwnershipPrivilege\n" +
      "                        SeLoadDriverPrivilege\n" +
      "                        SeBackupPrivilege\n" +
      "                        SeRestorePrivilege\n" +
      "                        SeDebugPrivilege\n" +
      "                        SeAuditPrivilege\n" +
      "                        SeSystemEnvironmentPrivilege\n" +
      "                        SeImpersonatePrivilege\n" +
      "                        SeDelegateSessionUserImpersonatePrivilege";

    final Map<String, Object> event = new WindowsMultilineParser.MultiLineParser(Event).parse();

    Assert.assertNotNull(event);
    Assert.assertEquals(14, event.size());
    Assert.assertEquals(12, ((List<?>) event.get("Privileges")).size());
  }

  @Test
  public void parseMissingValueTest()
  {
    final String Event =
      "09/11/2020 02:54:50 PM\n" +
      "LogName=Security\n" +
      "SourceName=Microsoft Windows security auditing.\n" +
      "EventCode=4776\n" +
      "EventType=0\n" +
      "Type=Information\n" +
      "ComputerName=##Computer_Name##\n" +
      "TaskCategory=Credential Validation\n" +
      "OpCode=Info\n" +
      "RecordNumber=##Record_Number##\n" +
      "Keywords=Audit Success\n" +
      "Message=The computer attempted to validate the credentials for an " +
      "account.\n" +
      "\n" +
      "Authentication Package:        MICROSOFT_AUTHENTICATION_PACKAGE_V1_0\n" +
      "Logon Account:\n" + // missing an account name!
      "Source Workstation:        W238-SMITP\n" +
      "Error Code:        0x0";

    final Map<String, Object> event = new WindowsMultilineParser.MultiLineParser(Event).parse();

    Assert.assertNotNull(event);
    System.out.println(Json.format(event));

    Assert.assertEquals(16, event.size());
    Assert.assertEquals("Security", event.get("LogName"));
    Assert.assertEquals("W238-SMITP", event.get("Source Workstation"));
    Assert.assertEquals("", event.get("Logon Account"));
  }
}
