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
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class WindowsMultilineParserTest
{
  @Test
  public void parseEventTest() throws Exception
  {
    final String Event =
      "01/15/2015 03:20:28 AM\n" +
      "LogName=Security\n" +
      "SourceName=Microsoft Windows security auditing.\n" +
      "EventCode=4624\n" +
      "EventType=0\n" +
      "Type=Information\n" +
      "ComputerName=szusoidc1.soi.dir.acme080.com\n" +
      "TaskCategory=Logon\n" +
      "OpCode=Info\n" +
      "RecordNumber=989284571\n" +
      "CaseID=AD_4624_1_1\n" +
      "Keywords=Audit Success\n" +
      "Message=An account was successfully logged on.\n" +
      "Subject:\n" +
      "    Security ID:        NULL SID\n" +
      "    Account Name:       -\n" +
      "    Account Domain:     -\n" +
      "    Logon ID:       0x0\n" +
      "Logon Type:         3\n" +
      "New Logon:\n" +
      "    Security ID:        SOI\\iiwu\n" +
      "    Account Name:       iiwu\n" +
      "    Account Domain:     SOI\n" +
      "    Logon ID:       0xd22e9734\n" +
      "    Logon GUID:     {1498989B-4798-8546-4F20-18536E283594}\n" +
      "Process Information:\n" +
      "    Process ID:     0x0\n" +
      "    Process Name:       -\n" +
      "Network Information:\n" +
      "    Workstation Name:\n" +
      "    Source Network Address: 10.35.140.130\n" +
      "    Source Port:        the64090\n" +
      "Detailed Authentication Information:\n" +
      "    Logon Process:      Kerberos\n" +
      "    Authentication Package: Kerberos\n" +
      "    Transited Services: -\n" +
      "    Package Name (NTLM only):   -\n" +
      "    Key Length:     0\n" +
      "\n" +
      "This event is generated when a logon session is created. It is " +
      "generated on the computer that was accessed.\n" +
      "\n" +
      "The subject fields indicate the account on the local system which " +
      "requested the logon. This is most commonly a service such as the Server" +
      " service, or a local process such as Winlogon.exe or Services.exe.\n" +
      "\n" +
      "The logon type field indicates the kind of logon that occurred. The " +
      "most common types are 2 (interactive) and 3 (network).\n" +
      "\n" +
      "The New Logon fields indicate the account for whom the new logon was " +
      "created, i.e. the account that was logged on.\n" +
      "\n" +
      "The network fields indicate where a remote logon request originated. " +
      "Workstation name is not always available and may be left blank in some " +
      "cases.\n" +
      "\n" +
      "The authentication information fields provide detailed information " +
      "about this specific logon request.\n" +
      " - Logon GUID is a unique identifier that can be used to correlate this" +
      " event with a KDC event.\n" +
      " - Transited services indicate which intermediate services have " +
      "participated in this logon request.\n" +
      " - Package name indicates which sub-protocol was used among the NTLM " +
      "protocols.\n" +
      " - Key length indicates the length of the generated session key. This " +
      "will be 0 if no session key was requested.\n" +
      "\n";

    final WindowsMultilineParser parser = new WindowsMultilineParser();

    final Map<String, Object> event = parser.parse(Event);

    Assert.assertEquals(19, event.size());
    Assert.assertEquals("4624", event.get("EventCode"));
    Assert.assertEquals(4, ((Map<?, ?>) event.get("Subject")).size());
    Assert.assertEquals(5, ((Map<?, ?>) event.get("New Logon")).size());
    Assert.assertEquals(3, ((Map<?, ?>) event.get("Network Information")).size());
    Assert.assertEquals(5, ((Map<?, ?>) event.get("Detailed Authentication Information")).size());
    Assert.assertEquals(
      "0", Maps.getIn(event, "Detailed Authentication Information", "Key Length"));
  }

  @Test
  public void parseCRLFEventTest() throws Exception
  {
    final String Event = "01/31/2022 04:51:46 PM\nLogName=Security\nEventCode=4634\n" +
                         "EventType=0\nComputerName=SesWin2019DC1.SesTest" +
                         ".local\nSourceName=Microsoft Windows security auditing.\n" +
                         "Type=Information\nRecordNumber=378587\nKeywords=Audit " +
                         "Success\nTaskCategory=Logoff\nOpCode=Info\n" +
                         "Message=An account was logged off.\r\n\r\nSubject:\r\n\tSecurity " +
                         "ID:\t\tSESTEST\\SESWIN2019DC2$\r\n\t" +
                         "Account Name:\t\tSESWIN2019DC2$\r\n\tAccount " +
                         "Domain:\t\tSESTEST\r\n\tLogon ID:\t\t0x44F8DC\r\n\r\n" +
                         "Logon Type:\t\t\t3\r\n\r\nThis event is generated when a logon session " +
                         "is destroyed. " +
                         "It may be positively correlated with a logon event using the Logon ID " +
                         "value. Logon IDs are only unique " +
                         "between reboots on the same computer.";
    final WindowsMultilineParser parser = new WindowsMultilineParser();

    final Map<String, Object> event = parser.parse(Event);

    Assert.assertEquals(14, event.size());
    Assert.assertEquals("01/31/2022 04:51:46 PM", event.get("ref_time"));
    Assert.assertEquals("4634", event.get("EventCode"));
    Assert.assertEquals(4, ((Map<?, ?>) event.get("Subject")).size());
    Assert.assertEquals("SESTEST", Maps.getIn(event, "Subject", "Account Domain"));
    Assert.assertEquals("3", event.get("Logon Type"));
  }

  @Test
  public void parseEOFEventTest() throws Exception
  {
    final String Event = "01/31/2022 04:51:46 PM\r\nLogName=Security\r\nEventCode=4634\r\n" +
                         "EventType=0\nComputerName=SesWin2019DC1.SesTest" +
                         ".local\nSourceName=Microsoft Windows security auditing.\n" +
                         "Type=Information\nRecordNumber=378587\nKeywords=Audit " +
                         "Success\nTaskCategory=Logoff\nOpCode=Info\n" +
                         "Message=An account was logged off.\r\n\r\nSubject:\r\n\tSecurity " +
                         "ID:\t\tSESTEST\\SESWIN2019DC2$\r\n\t" +
                         "Account Name:\t\tSESWIN2019DC2$\r\n\tAccount " +
                         "Domain:\t\tSESTEST\r\n\tLogon ID:";

    final WindowsMultilineParser parser = new WindowsMultilineParser();

    final Map<String, Object> event = parser.parse(Event);

    Assert.assertEquals(13, event.size());
    Assert.assertEquals("01/31/2022 04:51:46 PM", event.get("ref_time"));
    Assert.assertEquals(4, ((Map<?, ?>) event.get("Subject")).size());
    Assert.assertEquals("", Maps.getIn(event, "Subject", "Logon ID"));
  }


  @Test
  public void parseSpacesTest() throws Exception
  {
    final String Event = "10/07/2020 10:29:00 PM\n" +
                         "LogName=Microsoft-Windows-PowerShell/Operational\n" +
                         "SourceName=Microsoft-Windows-PowerShell\n" +
                         "EventCode=4103\n" +
                         "EventType=4\n" +
                         "Type=Information\n" +
                         "ComputerName=win-dc-1603297.attackrange.local\n" +
                         "User=NOT_TRANSLATED\n" +
                         "Sid=S-1-5-21-2825133891-65375684-292279277-500\n" +
                         "SidType=0\n" +
                         "TaskCategory=Executing Pipeline\n" +
                         "OpCode=To be used when operation is just executing a method\n" +
                         "RecordNumber=225352\n" +
                         "Keywords=None\n" +
                         "Message=CommandInvocation(Set-StrictMode): \"Set-StrictMode\"\n" +
                         "ParameterBinding(Set-StrictMode): name=\"Off\"; value=\"True\"\n" +
                         "Context:\n" +
                         "        Severity = Informational\n" +
                         "        Host Name = ConsoleHost\n" +
                         "        Host Version = 5.1.14393.3866\n" +
                         "        Host ID = 548cbebc-322f-4cf8-b2b6-8265f4391cd9\n" +
                         "        Host Application = powershell\n" +
                         "        Engine Version = 5.1.14393.3866\n" +
                         "        Runspace ID = 377d8ad3-3bae-46b1-aa6d-0a8ffd3c8288\n" +
                         "        Pipeline ID = 21\n" +
                         "        Command Name = Set-StrictMode\n" +
                         "        Command Type = Cmdlet\n" +
                         "        Script Name = C:\\Program " +
                         "Files\\WindowsPowerShell\\Modules\\PSReadline\\1.2\\PSReadLine.psm1\n" +
                         "        Command Path =\n" +
                         "        Sequence Number = 186\n" +
                         "        User = ATTACKRANGE\\administrator\n" +
                         "        Connected User =\n" +
                         "        Shell ID = Microsoft.PowerShell\n" +
                         "User Data:\n";
    final WindowsMultilineParser parser = new WindowsMultilineParser();

    final Map<String, Object> event = parser.parse(Event);

    Assert.assertEquals(17, event.size());
    Assert.assertEquals("Informational", Maps.getIn(event, "Context", "Severity"));
    Assert.assertEquals("", Maps.get(event, "User Data"));
  }
}