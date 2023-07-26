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
import io.ocsf.utils.Maps;
import io.ocsf.utils.parsers.Json5Parser;

import java.util.Map;

public final class WindowsEolTest
{
  private static final String DATA =
    "{\"preview\": false, \"offset\": 23199, \"result\": {\"_bkt\": " +
    "\"winevents~0~C8770486-1305-4AC2-8079-385F44BCC0B0\", \"_cd\": \"0:183356\", \"_indextime\":" +
    " \"1638572078\", \"_raw\": \"12/03/2021 10:54:37 " +
    "PM\\nLogName=Security\\nEventCode=4624\\nEventType=0\\nComputerName=SesTestWin2019DC" +
    ".SesTestDomain.local\\nSourceName=Microsoft Windows security auditing" +
    ".\\nType=Information\\nRecordNumber=82083\\nKeywords=Audit " +
    "Success\\nTaskCategory=Logon\\nOpCode=Info\\nMessage=An account was successfully logged on" +
    ".\\r\\n\\r\\nSubject :\\r\\n\\tSecurity ID:\\t\\tS-1-0-0\\r\\n\\tAccount " +
    "Name:\\t\\t-\\r\\n\\tAccount Domain:\\t\\t-\\r\\n\\tLogon ID:\\t\\t0x0\\r\\n\\r\\nLogon " +
    "Information:\\r\\n\\tLogon Type:\\t\\t3\\r\\n\\tRestricted Admin Mode:\\t-\\r\\n\\tVirtual " +
    "Account:\\t\\tNo\\r\\n\\tElevated Token:\\t\\tYes\\r\\n\\r\\nImpersonation " +
    "Level:\\t\\tImpersonation\\r\\n\\r\\nNew Logon:\\r\\n\\tSecurity " +
    "ID:\\t\\tS-1-5-18\\r\\n\\tAccount Name:\\t\\tSESTESTWIN2019D$\\r\\n\\tAccount " +
    "Domain:\\t\\tSESTESTDOMAIN.LOCAL\\r\\n\\tLogon ID:\\t\\t0x10CAE7B\\r\\n\\tLinked Logon " +
    "ID:\\t\\t0x0\\r\\n\\tNetwork Account Name:\\t-\\r\\n\\tNetwork Account " +
    "Domain:\\t-\\r\\n\\tLogon GUID:\\t\\t{87b50547-6d4a-3dfe-ae2c-17be8113692f}\\r\\n\\r" +
    "\\nProcess Information:\\r\\n\\tProcess ID:\\t\\t0x0\\r\\n\\tProcess " +
    "Name:\\t\\t-\\r\\n\\r\\nNetwork Information:\\r\\n\\tWorkstation Name:\\t-\\r\\n\\tSource " +
    "Network Address:\\t::1\\r\\n\\tSource Port:\\t\\t50145\\r\\n\\r\\nDetailed Authentication " +
    "Information:\\r\\n\\tLogon Process:\\t\\tKerberos\\r\\n\\tAuthentication " +
    "Package:\\tKerberos\\r\\n\\tTransited Services:\\t-\\r\\n\\tPackage Name (NTLM only)" +
    ":\\t-\\r\\n\\tKey Length:\\t\\t0\\r\\n\\r\\nThis event is generated when a logon session is " +
    "created. It is generated on the computer that was accessed.\\r\\n\\r\\nThe subject fields " +
    "indicate the account on the local system which requested the logon. This is most commonly a " +
    "service such as the Server service, or a local process such as Winlogon.exe or Services.exe" +
    ".\\r\\n\\r\\nThe logon type field indicates the kind of logon that occurred. The most common" +
    " types are 2 (interactive) and 3 (network).\\r\\n\\r\\nThe New Logon fields indicate the " +
    "account for whom the new logon was created, i.e. the account that was logged on" +
    ".\\r\\n\\r\\nThe network fields indicate where a remote logon request originated. " +
    "Workstation name is not always available and may be left blank in some cases.\\r\\n\\r\\nThe" +
    " impersonation level field indicates the extent to which a process in the logon session can " +
    "impersonate.\\r\\n\\r\\nThe authentication information fields provide detailed information " +
    "about this specific logon request.\\r\\n\\t- Logon GUID is a unique identifier that can be " +
    "used to correlate this event with a KDC event.\\r\\n\\t- Transited services indicate which " +
    "intermediate services have participated in this logon request.\\r\\n\\t- Package name " +
    "indicates which sub-protocol was used among the NTLM protocols.\\r\\n\\t- Key length " +
    "indicates the length of the generated session key. This will be 0 if no session key was " +
    "requested.\", \"_serial\": \"23199\", \"_si\": [\"SESTESTWIN2016\", \"winevents\"], " +
    "\"_sourcetype\": \"WinEventLog:Security\", \"_time\": \"1638572077.000\", \"host\": " +
    "\"SESTESTWIN2019D\", \"index\": \"winevents\", \"linecount\": \"70\", \"source\": " +
    "\"WinEventLog:Security\", \"sourcetype\": \"wineventlog\", \"splunk_server\": " +
    "\"SESTESTWIN2016\"}}\n";

  private WindowsEolTest() {}

  public static void main(final String... args)
  {
    final Map<String, Object> data = Json5Parser.to(DATA);

    final String raw = (String) Maps.getIn(data, "result._raw");

    final WindowsMultilineParser.MultiLineParser
      parser = new WindowsMultilineParser.MultiLineParser(raw);

    final Map<String, Object> parsed = parser.parse();

    System.out.println(Json.format(parsed));
  }
}
