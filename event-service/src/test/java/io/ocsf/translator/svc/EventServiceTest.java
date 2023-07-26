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

package io.ocsf.translator.svc;

import io.ocsf.parsers.WindowsMultilineParser;
import io.ocsf.parsers.WindowsXmlParser;
import io.ocsf.utils.FMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

public class EventServiceTest
{

  private static final String RulesDir = "src/test/rules";

  private static final String XmlWinEvent =
    "<Event xmlns=\"http://schemas.microsoft" +
    ".com/win/2004/08/events/event\">\n" +
    "   <System>\n" +
    "      <Provider Name=\"Microsoft-Windows-Security" +
    "-Auditing\" " +
    "Guid=\"{54849625-5478-4994-A5BA-3E3B0328C30D}\" />\n" +
    "      <EventID>4625</EventID>\n" +
    "      <Version>0</Version>\n" +
    "      <Level>0</Level>\n" +
    "      <Task>12546</Task>\n" +
    "      <Opcode>0</Opcode>\n" +
    "      <Keywords>0x8010000000000000</Keywords>\n" +
    "      <TimeCreated SystemTime=\"2015-09-08T22:54:54" +
    ".962511700Z\" />\n" +
    "      <EventRecordID>229977</EventRecordID>\n" +
    "      <Correlation />\n" +
    "      <Execution ProcessID=\"516\" ThreadID=\"3240\"" +
    " />\n" +
    "      <Channel>Security</Channel>\n" +
    "      <Computer>DC01.contoso.local</Computer>\n" +
    "      <Security />\n" +
    "   </System>\n" +
    "   <EventData>\n" +
    "      <Data Name=\"SubjectUserSid\">S-1-5-18</Data" +
    ">\n" +
    "      <Data Name=\"SubjectUserName\">DC01$</Data>\n" +
    "      <Data Name=\"SubjectDomainName\">CONTOSO</Data" +
    ">\n" +
    "      <Data Name=\"SubjectLogonId\">0x3e7</Data>\n" +
    "      <Data Name=\"TargetUserSid\">S-1-0-0</Data>\n" +
    "      <Data Name=\"TargetUserName\">Auditor</Data>\n" +
    "      <Data Name=\"TargetDomainName\">CONTOSO</Data" +
    ">\n" +
    "      <Data Name=\"Status\">0xc0000234</Data>\n" +
    "      <Data Name=\"FailureReason\">%%2307</Data>\n" +
    "      <Data Name=\"SubStatus\">0x0</Data>\n" +
    "      <Data Name=\"LogonType\">2</Data>\n" +
    "      <Data Name=\"LogonProcessName\">User32</Data" +
    ">\n" +
    "      <Data Name=\"AuthenticationPackageName" +
    "\">Negotiate</Data>\n" +
    "      <Data Name=\"WorkstationName\">DC01</Data>\n" +
    "      <Data Name=\"TransmittedServices\">-</Data>\n" +
    "      <Data Name=\"LmPackageName\">-</Data>\n" +
    "      <Data Name=\"KeyLength\">0</Data>\n" +
    "      <Data Name=\"ProcessId\">0x1bc</Data>\n" +
    "      <Data Name=\"ProcessName\">C:\\Windows" +
    "\\System32\\winlogon.exe</Data>\n" +
    "      <Data Name=\"IpAddress\">127.0.0.1</Data>\n" +
    "      <Data Name=\"IpPort\">0</Data>\n" +
    "   </EventData>\n" +
    "</Event>";

  private static final String BadXmlWinEvent =
    "<Event xmlns=\"http://schemas.microsoft" +
    ".com/win/2004/08/events/event\">\n" +
    "   <System>\n" +
    "      <Provider Name=\"Microsoft-Windows-Security" +
    "-Auditing\" " +
    "Guid=\"{54849625-5478-4994-A5BA-3E3B0328C30D}\" " +
    "/>\n" +
    "      <EventID>1234</EventID>\n" +
    "      <Version>0</Version>\n" +
    "      <Level>0</Level>\n" +
    "      <Task>12546</Task>\n" +
    "      <Opcode>0</Opcode>\n" +
    "      <Keywords>0x8010000000000000</Keywords>\n" +
    "      <TimeCreated " +
    "SystemTime=\"2015-09-08T22:54:54.962511700Z\" " +
    "/>\n" +
    "      <EventRecordID>229977</EventRecordID>\n" +
    "      <Correlation />\n" +
    "      <Execution ProcessID=\"516\" " +
    "ThreadID=\"3240\" />\n" +
    "      <Channel>Security</Channel>\n" +
    "      <Computer>DC01.contoso.local</Computer>\n" +
    "      <Security />\n" +
    "   </System>\n" +
    "   <EventData>\n" +
    "      <Data Name=\"SubjectUserSid\">S-1-5-18" +
    "</Data>\n" +
    "      <Data Name=\"SubjectUserName\">DC01$</Data" +
    ">\n" +
    "      <Data Name=\"SubjectDomainName\">CONTOSO" +
    "</Data>\n" +
    "      <Data Name=\"SubjectLogonId\">0x3e7</Data" +
    ">\n" +
    "      <Data Name=\"TargetUserSid\">S-1-0-0</Data" +
    ">\n" +
    "      <Data Name=\"TargetUserName\">Auditor</Data" +
    ">\n" +
    "      <Data Name=\"TargetDomainName\">CONTOSO" +
    "</Data>\n" +
    "      <Data Name=\"Status\">0xc0000234</Data>\n" +
    "      <Data Name=\"FailureReason\">%%2307</Data" +
    ">\n" +
    "      <Data Name=\"SubStatus\">0x0</Data>\n" +
    "      <Data Name=\"LogonType\">2</Data>\n" +
    "      <Data Name=\"LogonProcessName\">User32" +
    "</Data>\n" +
    "      <Data Name=\"AuthenticationPackageName" +
    "\">Negotiate</Data>\n" +
    "      <Data Name=\"WorkstationName\">DC01</Data" +
    ">\n" +
    "      <Data Name=\"TransmittedServices\">-</Data" +
    ">\n" +
    "      <Data Name=\"LmPackageName\">-</Data>\n" +
    "      <Data Name=\"KeyLength\">0</Data>\n" +
    "      <Data Name=\"ProcessId\">0x1bc</Data>\n" +
    "      <Data Name=\"ProcessName\">C:\\Windows" +
    "\\System32\\winlogon.exe</Data>\n" +
    "      <Data Name=\"IpAddress\">127.0.0.1</Data>\n" +
    "      <Data Name=\"IpPort\">0</Data>\n" +
    "   </EventData>\n" +
    "</Event>";

  private static final String VeryBadXmlWinEvent =
    "<Event>\n" +
    "   <System>\n" +
    "      <EventID>1234</EventID>\n" +
    "      <Version>0</Version>\n" +
    "      <Level>0</Level>\n" +
    "      <Task>12546</Task>\n" +
    "      <Opcode>0</Opcode>\n" +
    "      <Security />\n" +
    "   </EventData>\n" +
    "</Event>";

  private EventService service;

  @Before
  public void setUp() throws Exception
  {
    service = new EventService(RulesDir);
  }

  @Test
  public void testMissingFields()
  {
    try
    {
      service.process(FMap.b());
    }
    catch (final TranslatorException e)
    {
      Assert.assertEquals(TranslatorException.Reason.MissingSourceType, e.getReason());
    }

    try
    {
      service.process(
        FMap.<String, Object>b()
            .p(Splunk.SOURCE_TYPE, WindowsXmlParser.SourceType));
    }
    catch (final TranslatorException e)
    {
      Assert.assertEquals(TranslatorException.Reason.MissingRawData, e.getReason());
    }
  }

  @Test
  public void testUnsupportedSourceType()
  {
    try
    {
      service.process(
        FMap.<String, Object>b()
            .p(Splunk.SOURCE_TYPE, "syslog"));
    }
    catch (final TranslatorException e)
    {
      Assert.assertEquals(TranslatorException.Reason.NoParser, e.getReason());
    }

    try
    {
      service.process(
        FMap.<String, Object>b()
            .p(Splunk.SOURCE_TYPE, WindowsMultilineParser.SourceType));
    }
    catch (final TranslatorException e)
    {
      Assert.assertEquals(TranslatorException.Reason.NoTranslator, e.getReason());
    }
  }

  @Test
  public void testUnsupportedEvent()
  {
    try
    {
      service.process(
        FMap.<String, Object>b()
            .p(Splunk.SOURCE_TYPE, WindowsXmlParser.SourceType)
            .p(Splunk.RAW_EVENT, BadXmlWinEvent));
    }
    catch (final TranslatorException e)
    {
      Assert.assertEquals(TranslatorException.Reason.UnsupportedEvent, e.getReason());
    }
  }

  @Test
  public void testBadFormatEvent()
  {
    try
    {
      service.process(
        FMap.<String, Object>b()
            .p(Splunk.SOURCE_TYPE, WindowsXmlParser.SourceType)
            .p(Splunk.RAW_EVENT, VeryBadXmlWinEvent));
    }
    catch (final TranslatorException e)
    {
      Assert.assertEquals(TranslatorException.Reason.ParserError, e.getReason());
    }
  }

  @Test
  public void testXmlWinEvent()
  {
    try
    {
      final Map<String, Object> translated = service.process(
        FMap.<String, Object>b()
            .p(Splunk.SOURCE_TYPE, WindowsXmlParser.SourceType)
            .p(Splunk.RAW_EVENT, XmlWinEvent));

      Assert.assertNotNull(translated);
      Assert.assertEquals(16, translated.size());
    }
    catch (final TranslatorException e)
    {
      Assert.fail(e.getMessage());
    }
  }
}