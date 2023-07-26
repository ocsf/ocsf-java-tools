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

import java.util.Map;

public final class WindowsMultiValueTest
{
  private static final String Data0 =
    "12/13/2021 10:12:55 PM\n" +
    "LogName=Security\n" +
    "EventCode=4661\n" +
    "EventType=0\n" +
    "ComputerName=SesTestWin2019DC.SesTestDomain.local\n" +
    "SourceName=Microsoft Windows security auditing.\n" +
    "Type=Information\n" +
    "RecordNumber=126000\n" +
    "Keywords=Audit Success\n" +
    "TaskCategory=SAM\n" +
    "OpCode=Info\n" +
    "Message=A handle to an object was requested.\n" +
    "A handle to an object was requested.\n" +
    "\n" +
    "Subject :\n" +
    "\tSecurity ID:\t\tS-1-5-21-4033139579-163346244-2917651748" +
    "-1011\n" +
    "\tAccount Name:\t\tsplunker\n" +
    "\tAccount Domain:\t\tSESTESTDOMAIN\n" +
    "\tLogon ID:\t\t0x2AB3CEF\n" +
    "\n" +
    "Object :\n" +
    "\tObject Server:\tSecurity Account Manager\n" +
    "\tObject Type:\tSAM_USER\n" +
    "\tObject Name:\tS-1-5-21-4033139579-163346244-2917651748" +
    "-500\n" +
    "\tHandle ID:\t0x15513eef5d0\n" +
    "\n" +
    "Process Information:\n" +
    "\tProcess ID:\t0x254\n" +
    "\tProcess Name:\tC:\\Windows\\System32\\lsass.exe\n" +
    "\n" +
    "Access Request Information:\n" +
    "\tTransaction ID:\t{00000000-0000-0000-0000-000000000000" +
    "}\n" +
    "\tAccesses:\tREAD_CONTROL\n" +
    "\t\t\t\tWritePreferences\n" +
    "\t\t\t\tReadAccount\n" +
    "\t\t\t\tSetPassword (without knowledge of old password)\n" +
    "\t\t\t\t\n" +
    "\tAccess Reasons:\t\t-\n" +
    "\tAccess Mask:\t0x20094\n" +
    "\tPrivileges Used for Access Check:\t-\n" +
    "\tProperties:\t---\n" +
    "\t{bf967aba-0de6-11d0-a285-00aa003049e2}\n" +
    "READ_CONTROL\n" +
    "WritePreferences\n" +
    "ReadAccount\n" +
    "SetPassword (without knowledge of old password)\n" +
    "\t\t{59ba2f42-79a2-11d0-9020-00c04fc2d3cf}\n" +
    "\t\t\t{bf967938-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{5fd42471-1262-11d0-a060-00aa006c33ed}\n" +
    "\t\t\t{bf9679e8-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf967a00-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{3e0abfd0-126a-11d0-a060-00aa006c33ed}\n" +
    "\t\t\t{bf967a6a-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf967953-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t{4c164200-20c0-11d0-a768-00aa006e0529}\n" +
    "\t\t\t{bf967915-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf967a0a-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf967a68-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf967a6d-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t{5f202010-79a5-11d0-9020-00c04fc2d4cf}\n" +
    "\t\t\t{bf96792e-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf967985-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf967986-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf967996-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf967997-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf9679aa-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf9679ab-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf9679ac-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf967a05-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf9679a8-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t{e48d0154-bcf8-11d1-8702-00c04fb96050}\n" +
    "\t\t\t{bf967950-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t{bc0ac240-79a9-11d0-9020-00c04fc2d4cf}\n" +
    "\t\t\t{bf967991-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t{00299570-246d-11d0-a768-00aa006e0529}\n" +
    "\t\t{7ed84960-ad10-11d0-8a92-00aa006e0529}\n" +
    "READ_CONTROL\n" +
    "WritePreferences\n" +
    "ReadAccount\n" +
    "SetPassword (without knowledge of old password)\n" +
    "ListGroups\n" +
    "\t\t{ab721a53-1e2f-11d0-9819-00aa0040529b}\n" +
    "\n" +
    "\tRestricted SID Count:";

  static final String Data1 =
    "11/05/2021 03:17:42 AM\n" +
    "LogName=Microsoft-Windows-PowerShell/Operational\n" +
    "SourceName=Microsoft-Windows-PowerShell\n" +
    "EventCode=4103\n" +
    "EventType=4\n" +
    "Type=Information\n" +
    "ComputerName=DESKTOP-RLNC8NE.splunkcorp.com\n" +
    "User=NOT_TRANSLATED\n" +
    "Sid=S-1-5-21-1783686499-2158177463-2193993347-30889\n" +
    "SidType=0\n" +
    "TaskCategory=Executing Pipeline\n" +
    "OpCode=To be used when operation is just executing a method\n" +
    "RecordNumber=227557\n" +
    "Keywords=None\n" +
    "Message=CommandInvocation(Add-Type): \"Add-Type\"\n" +
    "ParameterBinding(Add-Type): name=\"AssemblyName\"; value=\"System" +
    ".Windows.Forms\"\n" +
    "\n" +
    "\n" +
    "Context:\n" +
    "        Severity = Informational\n" +
    "        Host Name = Windows PowerShell ISE Host\n" +
    "        Host Version = 5.1.19041.1237\n" +
    "        Host ID = 8e56fdda-9a71-4a47-a7f7-c380b21af3dd\n" +
    "        Host Application = " +
    "C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell_ise" +
    ".exe\n" +
    "        Engine Version = 5.1.19041.1237\n" +
    "        Runspace ID = 472e782d-3db9-4bb5-99e1-031e8c45a671\n" +
    "        Pipeline ID = 13\n" +
    "        Command Name = Add-Type\n" +
    "        Command Type = Cmdlet\n" +
    "        Script Name = D:\\Users\\tbrown-admin\\scripts\\New " +
    "folder\\Updated\\Onboarding2.5.9\\Splunkonboard.ps1\n" +
    "        Command Path =\n" +
    "        Sequence Number = 136\n" +
    "        User = SPLUNKCORP\\tbrown-admin\n" +
    "        Connected User =\n" +
    "        Shell ID = Microsoft.PowerShell\n";

  private static final String Data2 =
    "11/11/2021 09:49:53 PM\n" +
    "LogName=Microsoft-Windows-PowerShell/Operational\n" +
    "SourceName=Microsoft-Windows-PowerShell\n" +
    "EventCode=4103\n" +
    "EventType=4\n" +
    "Type=Information\n" +
    "ComputerName=DESKTOP-PGUDSLA.splunkcorp.com\n" +
    "User=NOT_TRANSLATED\n" +
    "Sid=S-1-5-18\n" +
    "SidType=0\n" +
    "TaskCategory=Executing Pipeline\n" +
    "OpCode=To be used when operation is just executing a " +
    "method\n" +
    "RecordNumber=280735\n" +
    "Keywords=None\n" +
    "Message=CommandInvocation(Add-Type): \"Add-Type\"\n" +
    "ParameterBinding(Add-Type): name=\"TypeDefinition\"; " +
    "value=\"using System.Net;\n" +
    "using System;\n" +
    "namespace WorkSpaces{\n" +
    "public class WebClientEx : WebClient{\n" +
    "protected override WebRequest GetWebRequest(Uri uri){\n" +
    "WebRequest w=base.GetWebRequest(uri);\n" +
    "w.Timeout=7000;\n" +
    "return w;\n" +
    "}\n" +
    "}\n" +
    "}\"\n" +
    "ParameterBinding(Add-Type): name=\"Language\"; " +
    "value=\"CSharp\"\n" +
    "ParameterBinding(Add-Type): name=\"IgnoreWarnings\"; " +
    "value=\"True\"\n" +
    "ParameterBinding(Add-Type): name=\"ErrorAction\"; " +
    "value=\"SilentlyContinue\"\n" +
    "CommandInvocation(Out-Null): \"Out-Null\"\n" +
    "\n" +
    "\n" +
    "Context:\n" +
    "        Severity = Informational\n" +
    "        Host Name = ConsoleHost\n" +
    "        Host Version = 5.1.19041.1320\n" +
    "        Host ID = b9f1c562-c6bd-4be4-b571-2844ea548f59\n" +
    "        Host Application = " +
    "C:\\Windows\\system32\\WindowsPowerShell\\v1.0\\powershell" +
    ".exe -ExecutionPolicy unrestricted -NoProfile " +
    "-NonInteractive . 'C:\\Program " +
    "Files\\Amazon\\Ec2ConfigService\\Scripts\\UserScript" +
    ".ps1'\n" +
    "        Engine Version = 5.1.19041.1320\n" +
    "        Runspace ID = 6e4a58da-60fe-4966-b16f-d3445da2a901" +
    "\n" +
    "        Pipeline ID = 1\n" +
    "        Command Name = Add-Type\n" +
    "        Command Type = Cmdlet\n" +
    "        Script Name = C:\\Program " +
    "Files\\Amazon\\Ec2ConfigService\\Scripts\\UserScript.ps1\n" +
    "        Command Path =\n" +
    "        Sequence Number = 16\n" +
    "        User = SPLUNKCORP\\SYSTEM\n" +
    "        Connected User =\n" +
    "        Shell ID = Microsoft.PowerShell\n" +
    "\n" +
    "\n" +
    "User Data:\n";

  private static final String Data3 =
    "12/13/2021 09:48:19 PM\n" +
    "LogName=Security\n" +
    "EventCode=4661\n" +
    "EventType=0\n" +
    "ComputerName=SesTestWin2019DC.SesTestDomain.local\n" +
    "SourceName=Microsoft Windows security auditing.\n" +
    "Type=Information\n" +
    "RecordNumber=125978\n" +
    "Keywords=Audit Success\n" +
    "TaskCategory=SAM\n" +
    "OpCode=Info\n" +
    "Message=A handle to an object was requested.\n" +
    "\n" +
    "Subject :\n" +
    "\tSecurity ID:\t\tS-1-5-18\n" +
    "\tAccount Name:\t\tSESTESTWIN2019D$\n" +
    "\tAccount Domain:\t\tSESTESTDOMAIN\n" +
    "\tLogon ID:\t\t0x3E7\n" +
    "\n" +
    "Object:\n" +
    "\tObject Server:\tSecurity Account Manager\n" +
    "\tObject Type:\tSAM_DOMAIN\n" +
    "\tObject Name:\tCN=Builtin,DC=SesTestDomain,DC=local\n" +
    "\tHandle ID:\t0x15513eeff70\n" +
    "\n" +
    "Process Information:\n" +
    "\tProcess ID:\t0x254\n" +
    "\tProcess Name:\tC:\\Windows\\System32\\lsass.exe\n" +
    "\n" +
    "Access Request Information:\n" +
    "\tTransaction ID:\t{00000000-0000-0000-0000-000000000000" +
    "}\n" +
    "\tAccesses:\tDELETE\n" +
    "\t\t\t\tREAD_CONTROL\n" +
    "\t\t\t\tWRITE_DAC\n" +
    "\t\t\t\tWRITE_OWNER\n" +
    "\t\t\t\tReadPasswordParameters\n" +
    "\t\t\t\tWritePasswordParameters\n" +
    "\t\t\t\tReadOtherParameters\n" +
    "\t\t\t\tWriteOtherParameters\n" +
    "\t\t\t\tCreateUser\n" +
    "\t\t\t\tCreateGlobalGroup\n" +
    "\t\t\t\tCreateLocalGroup\n" +
    "\t\t\t\tGetLocalGroupMembership\n" +
    "\t\t\t\tListAccounts\n" +
    "\t\t\t\t\n" +
    "\tAccess Reasons:\t\t-\n" +
    "\tAccess Mask:\t0xF01FF\n" +
    "\tPrivileges Used for Access Check:\t-\n" +
    "\tProperties:\t---\n" +
    "\t{19195a5a-6da0-11d0-afd3-00c04fd930c9}\n" +
    "DELETE\n" +
    "READ_CONTROL\n" +
    "WRITE_DAC\n" +
    "WRITE_OWNER\n" +
    "ReadPasswordParameters\n" +
    "WritePasswordParameters\n" +
    "ReadOtherParameters\n" +
    "WriteOtherParameters\n" +
    "CreateUser\n" +
    "CreateGlobalGroup\n" +
    "CreateLocalGroup\n" +
    "GetLocalGroupMembership\n" +
    "ListAccounts\n" +
    "\t\t{c7407360-20bf-11d0-a768-00aa006e0529}\n" +
    "\t\t\t{bf9679a4-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf9679a5-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf9679a6-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf9679bb-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf9679c2-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf9679c3-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf967a09-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf967a0b-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t{b8119fd0-04f6-4762-ab7a-4986c76b3f9a}\n" +
    "\t\t\t{bf967a34-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf967a33-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf9679c5-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf967a61-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf967977-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf96795e-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t\t{bf9679ea-0de6-11d0-a285-00aa003049e2}\n" +
    "\t\t{ab721a52-1e2f-11d0-9819-00aa0040529b}\n" +
    "\n" +
    "\tRestricted SID Count:\t0";

  private static final String Data4 =
    "01/07/2022 12:06:59 PM\n" +
    "LogName=Security\n" +
    "EventCode=4672\n" +
    "EventType=0\n" +
    "ComputerName=SesWin2012-1.SesTest.local\n" +
    "SourceName=Microsoft Windows security auditing.\n" +
    "Type=Information\n" +
    "RecordNumber=131873\n" +
    "Keywords=Audit Success\n" +
    "TaskCategory=Special Logon\n" +
    "OpCode=Info\n" +
    "Message=Special privileges assigned to new logon.\n" +
    "\n" +
    "Subject:\n" +
    "\tSecurity ID:\t\tNT AUTHORITY\\SYSTEM\n" +
    "\tAccount Name:\t\tSESWIN2012-1$\n" +
    "\tAccount Domain:\t\tSESTEST\n" +
    "\tLogon ID:\t\t0x106F3A9\n" +
    "\n" +
    "Privileges:\t\tSeSecurityPrivilege\n" +
    "\t\t\tSeBackupPrivilege\n" +
    "\t\t\tSeRestorePrivilege\n" +
    "\t\t\tSeTakeOwnershipPrivilege\n" +
    "\t\t\tSeDebugPrivilege\n" +
    "\t\t\tSeSystemEnvironmentPrivilege\n" +
    "\t\t\tSeLoadDriverPrivilege\n" +
    "\t\t\tSeImpersonatePrivilege\n";

  private WindowsMultiValueTest() {}

  private static void parse(final String data)
  {
    final WindowsMultilineParser.MultiLineParser
      parser = new WindowsMultilineParser.MultiLineParser(data);

    final Map<String, Object> parsed = parser.parse();

    System.out.println(Json.format(parsed));

  }

  public static void main(final String... args)
  {
    parse(Data0);
    System.out.println("----------------");
    parse(Data1);
    System.out.println("----------------");
    parse(Data2);
    System.out.println("----------------");
    parse(Data3);
    System.out.println("----------------");
    parse(Data4);
  }
}
