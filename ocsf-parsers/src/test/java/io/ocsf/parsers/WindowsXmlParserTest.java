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

public final class WindowsXmlParserTest
{
  private static final String xml2 =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    "<Event xmlns=\"http://schemas.microsoft" +
    ".com/win/2004/08/events/event\">\n" +
    "\t<System>\n" +
    "\t\t<Provider Name=\"Microsoft-Windows-Security-Auditing\" " +
    "Guid=\"{54849625-5478-4994-A5BA-3E3B0328C30D}\" />\n" +
    "\t\t<EventID>4776</EventID>\n" +
    "\t\t<Version>0</Version>\n" +
    "\t\t<Level>0</Level>\n" +
    "\t\t<Task>14336</Task>\n" +
    "\t\t<Opcode>0</Opcode>\n" +
    "\t\t<Keywords>0x8020000000000000</Keywords>\n" +
    "\t\t<TimeCreated SystemTime=\"2020-01-22T07:17:40" +
    ".487264400Z\" />\n" +
    "\t\t<EventRecordID>5315358</EventRecordID>\n" +
    "\t\t<Correlation />\n" +
    "\t\t<Execution ProcessID=\"724\" ThreadID=\"2940\" />\n" +
    "\t\t<Channel>Security</Channel>\n" +
    "\t\t<Computer>ta-dc-w2016.crest-2012r2.com</Computer>\n" +
    "\t\t<Security />\n" +
    "\t</System>\n" +
    "\t<EventData>\n" +
    "\t\t<Data Name=\"PackageName" +
    "\">MICROSOFT_AUTHENTICATION_PACKAGE_V1_0</Data>\n" +
    "\t\t<Data Name=\"TargetUserName\">Administrator</Data>\n" +
    "\t\t<Data Name=\"Workstation\">W184-SHUBHAMB</Data>\n" +
    "\t\t<Data Name=\"Status\">0x0</Data>\n" +
    "\t</EventData>\n" +
    "</Event>";

  private static final String xml1 =
    "<Event\n" +
    "  xmlns='http://schemas.microsoft" +
    ".com/win/2004/08/events/event'>\n" +
    "  <System>\n" +
    "    <Provider Name='Microsoft-Windows-Security-Auditing' " +
    "Guid='{54849625-5478-4994-A5BA-3E3B0328C30D}'/>\n" +
    "    <EventID>4662</EventID>\n" +
    "    <Version>0</Version>\n" +
    "    <Level>0</Level>\n" +
    "    <Task>14080</Task>\n" +
    "    <Opcode>0</Opcode>\n" +
    "    <Keywords>0x8020000000000000</Keywords>\n" +
    "    <TimeCreated SystemTime='2020-09-23T10:08:57" +
    ".256916900Z'/>\n" +
    "    <EventRecordID>141717</EventRecordID>\n" +
    "    <Correlation ActivityID='{B0C3715F-875E-0000-6C71" +
    "-C3B05E87D601}'/>\n" +
    "    <Execution ProcessID='612' ThreadID='4028'/>\n" +
    "    <Channel>Security</Channel>\n" +
    "    <Computer>AD-server.tafadtest.local</Computer>\n" +
    "    <Security/>\n" +
    "  </System>\n" +
    "  <EventData>\n" +
    "    <Data Name='SubjectUserSid'>TAFADTEST\\Administrator" +
    "</Data>\n" +
    "    <Data Name='SubjectUserName'>Admini$trator</Data>\n" +
    "    <Data Name='SubjectDomainName'>TAFADTEST</Data>\n" +
    "    <Data Name='SubjectLogonId'>0x99ff8</Data>\n" +
    "    <Data Name='ObjectServer'>DS</Data>\n" +
    "    <Data Name='ObjectType'>groupPolicyContainer</Data>\n" +
    "    <Data Name='ObjectName'>CN={31B2F340-016D-11D2-945F" +
    "-00C04FB984F9},CN=Policies,CN=System,DC=tafadtest," +
    "DC=local</Data>\n" +
    "    <Data Name='OperationType'>Object Access</Data>\n" +
    "    <Data Name='HandleId'>0x0</Data>\n" +
    "    <Data Name='AccessList'>%%1539\n" +
    "                                </Data>\n" +
    "    <Data Name='AccessMask'>0x40000</Data>\n" +
    "    <Data Name='Properties'>%%1539\n" +
    "        groupPolicyContainer\n" +
    "</Data>\n" +
    "    <Data Name='AdditionalInfo'>-</Data>\n" +
    "    <Data Name='AdditionalInfo2'></Data>\n" +
    "  </EventData>\n" +
    "</Event>";

  private WindowsXmlParserTest() {}

  public static void main(final String... args) throws Exception
  {
    final WindowsXmlParser parser = new WindowsXmlParser();

    System.out.println(Json.format(parser.parse(xml1)));
    System.out.println(Json.format(parser.parse(xml2)));
  }
}
