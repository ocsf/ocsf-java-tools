/*
 * Copyright 2023 Open Cybersecurity Schema Framework
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

package io.ocsf.schema.parsers;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class WinMultiLineParserTest
{
  @Test
  public void parseNestedTest()
  {
    final String Event = "        Severity = Informational\n" +
        "        Host Name = ConsoleHost\n" +
        "        Host Version = 5.1.14393.4583\n" +
        "        Host ID = 92dd5f2a-6df4-4eb7-bc15-2456051d576d\n" +
        "        Host Application = C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe -NoProfile -NonInteractive -NoLogo -WindowStyle hidden -ExecutionPolicy Unrestricted Import-Module C:\\ProgramData\\Amazon\\EC2-Windows\\Launch\\Module\\Ec2Launch.psd1; Set-Wallpaper\n" +
        "        Engine Version = 5.1.14393.4583\n" +
        "        Runspace ID = 48f983a5-33ab-4a10-9bf7-aadfd8e06157\n" +
        "        Pipeline ID = 1\n" +
        "        Command Name = Add-Type\n" +
        "        Command Type = Cmdlet\n" +
        "        Script Name = C:\\ProgramData\\Amazon\\EC2-Windows\\Launch\\Module\\Scripts\\Set-Wallpaper.ps1\n" +
        "        Command Path = \n" +
        "        Sequence Number = 20\n" +
        "        User = ATTACKRANGE\\Administrator\n" +
        "        Connected User = \n" +
        "        Shell ID = Microsoft.PowerShell";

    final Map<String, Object> event = new WinMultiLineParser(Event).nested();

    Assert.assertNotNull(event);
    Assert.assertEquals(16, event.size());
    Assert.assertEquals("Informational", event.get("Severity"));
    Assert.assertEquals("", event.get("Command Path"));
    Assert.assertEquals("Microsoft.PowerShell", event.get("Shell ID"));

//    System.out.println(Json.format(event));
  }

  @Test
  public void parseMultilineValueTest()
  {
    final String Event = "11/30/2020 05:27:13 PM\n" +
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

    final Map<String, Object> event = new WinMultiLineParser(Event).parse();

    Assert.assertNotNull(event);
    Assert.assertEquals(14, event.size());
    Assert.assertEquals(12, ((List<?>)event.get("Privileges")).size());

//    System.out.println(Json.format(event));
  }

}
