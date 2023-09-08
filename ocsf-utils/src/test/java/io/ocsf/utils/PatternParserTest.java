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

import io.ocsf.utils.parsers.Parser;
import io.ocsf.utils.parsers.PatternParser;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class PatternParserTest
{

  @Test
  public void helloWorld() throws Exception
  {
    final Parser p = PatternParser.create("hello world");

    Assert.assertTrue(p.parse("hello world").isEmpty());
    Assert.assertTrue(p.parse("hello world!").isEmpty());

    Assert.assertNull(p.parse(""));
    Assert.assertNull(p.parse("goodbye world!"));
    Assert.assertNull(p.parse("this is the hello world test"));
  }

  @Test
  public void ignoreData() throws Exception
  {
    final Parser p = PatternParser.create("hello #{_name}");

    Assert.assertTrue(p.parse("hello world").isEmpty());
    Assert.assertTrue(p.parse("hello wild world!").isEmpty());
    Assert.assertNull(p.parse("goodbye hello world!"));
  }

  @Test
  public void simpleData() throws Exception
  {
    final Parser p = PatternParser.create("hello #{name}");

    final Map<String, Object> data1 = p.parse("hello world");
    Assert.assertEquals(1, data1.size());
    Assert.assertEquals("world", data1.get("name"));

    final Map<String, Object> data2 = p.parse("hello wild world");
    Assert.assertEquals(1, data2.size());
    Assert.assertEquals("wild world", data2.get("name"));

    Assert.assertNull(p.parse("goodbye hello world!"));
  }

  @Test
  public void simpleData2() throws Exception
  {
    final Parser p = PatternParser.create("hello #{name}!");

    final Map<String, Object> data1 = p.parse("hello world!");
    Assert.assertEquals(1, data1.size());
    Assert.assertEquals("world", data1.get("name"));

    Assert.assertNull(p.parse("hello world"));
  }

  @Test
  public void cisco() throws Exception
  {
    final String text =
      "Feb 11 08:09:08 10.192.0.1 : %ASA-4-722051: Group <sslvpn> User <harry0houdini0> IP <192.0" +
      ".2.250> IPv4 Address <10.10.20.1> IPv6 address <::> assigned to session";
    final String pattern =
      "#{event_time: string(15)} #{host_ip} : %#{product}-#{severity: integer}-#{ref_event_uid}: " +
      "Group <#{group}> User <#{user}> IP <#{ip}> IPv4 Address <#{ipv4}> IPv6 address <#{ipv6}> " +
      "#{message}";

    final Parser p = PatternParser.create(pattern);

    final Map<String, Object> data = p.parse(text);
    Assert.assertEquals(11, data.size());
  }

  @Test
  public void webGateway() throws Exception
  {
    final String text =
      "[01/Jul/2014:17:17:01 -0700] \"sghadarg\" 10.34.246.186 192.0.2.34 1130 200 " +
      "TCP_MISS_RELOAD \"GET https://d29r7idq0wxsiz.cloudfront" +
      ".net/6d20156b-f214968326%2F5d877b17-ca06-3851-b4d7-727333d868d8" +
      ".ts?response-content-disposition=attachment%3B%20filename%3D%225d877b17-ca06-3851-b4d7" +
      "-727333d868d8.ts%22&Expires=1404957820&Signature=3bef0z682JceFs4HFl0JjHDtaVSKNzy-o4Xd1" +
      "-oOTRgwknAn6Fbx7kfp7Ms3~V2zOemmAP7PXzVU-9mq5nU1IcuOfDUtaFioe6u-Q-xkdxgG21yHF-bTt" +
      "~q3995uFaT7ghEEsq2EXd~MHD63dDw4KXCQT1hL0hgMuITIzMJPoSY_&Key-Pair-Id=APKAJVZTZLZ7I5XDXGUQ " +
      "HTTP/1.1\" \"Content Server\" \"Minimal Risk\" \"video/MP2T\" 424246 773 \"Mozilla/5.0 " +
      "(Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 " +
      "Safari/537.36\" \"https://www.amazon" +
      ".com/gp/dmusic/cloudplayer/player?ie=UTF8&ref_=gno_yam_cldplyr\" \"-\" \"0\" \"\" \"-\"";
    final String pattern =
      "[#{event_time: datetime(dd/MMM/yyyy:HH:mm:ss Z)}] \"#{host}\" #{host_ip1} #{host_ip2} " +
      "#{port: integer} #{http.status:integer} #{message} \"#{http.verb} #{http.url} #{http" +
      ".version}\" \"#{server}\" \"#{risk}\" \"#{content_type}\" #{bytes_in: integer} " +
      "#{bytes_out: integer} \"#{agent}\" \"#{ref_url}\" #{_rest}";

    final Parser p = PatternParser.create(pattern);

    final Map<String, Object> data = p.parse(text);
    Assert.assertEquals(14, data.size());
  }

  @Test
  public void json() throws Exception
  {
    final String text =
      "Feb 11 08:09:08 10.192.0.1: {\"customer_uid\": \"test_tenant\", \"Task\": \"12544\", " +
      "\"Correlation\": {\"ActivityID\": \"{C31DFEBE-8DAB-000B-BFFE-1DC3AB8DD601}\"}, " +
      "\"Keywords\": \"0x8020000000000000\", \"Channel\": \"Security\", \"Opcode\": \"0\", " +
      "\"EventData\": {\"LogonGuid\": \"{88A766D5-5207-DBD6-8A42-FBEF1D9BB821}\", " +
      "\"VirtualAccount\": \"%%1843\", \"LogonType\": \"3\", \"IpPort\": \"59116\", " +
      "\"SubjectLogonId\": \"0x0\", \"KeyLength\": \"0\", \"TargetLogonId\": \"0x19b0004d\", " +
      "\"SubjectUserName\": \"Aa-Bb\", \"TargetLinkedLogonId\": \"0x0\", \"ElevatedToken\": " +
      "\"%%1842\", \"IpAddress\": \"10.141.36.87\", \"TargetUserName\": \"TA-DC-W2012R2$\", " +
      "\"ProcessId\": \"0x0\", \"ImpersonationLevel\": \"%%1833\", \"TargetDomainName\": " +
      "\"CREST-2012R2.COM\", \"LogonProcessName\": \"Kerberos\", \"SubjectUserSid\": \"NULL " +
      "SID\", \"TargetUserSid\": \"CREST-2012R2\\\\TA-DC-W2012R2$\", " +
      "\"AuthenticationPackageName\": \"Kerberos\"}, \"Provider\": {\"Guid\": " +
      "\"{54849625-5478-4994-A5BA-3E3B0328C30D}\", \"Name\": " +
      "\"Microsoft-Windows-Security-Auditing\"}, \"TimeCreated\": {\"SystemTime\": " +
      "\"2020-09-22T09:22:54.268295200Z\"}, \"EventRecordID\": \"12138030\", \"Execution\": " +
      "{\"ThreadID\": \"3656\", \"ProcessID\": \"724\"}, \"Version\": \"2\", \"Computer\": " +
      "\"ta-dc-w2016.crest-2012r2.com\", \"EventID\": \"4624\", \"Level\": \"0\", " +
      "\"TargetUserSid\": \"test\"}";
    final String pattern = "#{event_time: string(15)} #{host_ip}: #{event: json}";

    final Parser p = PatternParser.create(pattern);

    final Map<String, Object> data = p.parse(text);
    Assert.assertEquals(3, data.size());
  }

  @Test
  public void cef() throws Exception
  {
    final String text =
      "<27>Oct  1 12:47:58 192.168.1.2 threat-protect-log[5754]: CEF:0|Infoblox|NIOS Threat|8.4" +
      ".4-386831|120303001|blocklist:foo.foo.foo|7|src=192.168.1.3 spt=63290 dst=192.168.1.2 " +
      "dpt=53 act=\"DROP\" cat=\"BLOCKLIST UDP FQDN lookup\" nat=0 nfpt=0 nlpt=0 fqdn=foo.foo.foo" +
      " hit_count=4";
    final String pattern =
      "<#{facility: integer}>#{event_time: string(15)} #{host_ip}#{log_name}[#{_}]: #{event: cef}";

    final Parser p = PatternParser.create(pattern);

    final Map<String, Object> data = p.parse(text);
    Assert.assertEquals(4, data.size());
  }

  @Test
  public void cefNetskop() throws Exception
  {
    final String text =
      "2023-08-02T06:56:06+00:00 netskopece CEF: 0|Netskope|alliances|NULL|audit|NULL|High|auditLogEvent=Access Denied auditType=admin_audit_logs suser=null timestamp=1690957108";
    final String pattern =
      "#{log_time} netskopece #{data: cef}";

    final Map<String, Object> data = PatternParser.create(pattern).parse(text);
    Assert.assertEquals(2, data.size());
  }

  @Test
  public void panTraffic() throws Exception
  {
    final String text =
      "Oct  5 14:57:17 EFF-N-NYIDC-PA-1.Acme048.ad.net  : 1,2015/10/05 14:57:16,0009C103532," +
      "TRAFFIC,start,1,2015/10/05 14:57:16,192.0.2.115,192.0.2.166,192.0.2.4,192.0.2.166,Preblock" +
      " apps - Skype,acme048-ad\\duquemak,,skype,vsys1,Acme048Net,Internet,ethernet1/2," +
      "ethernet1/1,Log-Panorama,2015/10/05 14:57:16,34205692,1,57474,24017,57912,24017,0x400000," +
      "tcp,allow,552,315,237,7,2015/10/05 14:56:43,0,any,0,13587285028,0x0,US,US,0,4,3,n/a";
    final String pattern =
      "#{event_time: string(15)} #{device_name}  : #{_},#{Receive Time},#{Serial Number},#{Type}," +
      "#{Threat/Content Type},#{_},#{Generated Time},#{Source IP},#{Destination IP},#{NAT Source " +
      "IP},#{NAT Destination IP},#{Rule Name},#{Source User},#{Destination User},#{Application}," +
      "#{Virtual System},#{Source Zone},#{Destination Zone},#{Inbound Interface},#{Outbound " +
      "Interface},#{Log Action},#{FUTURE_USE},#{Session ID},#{Repeat Count},#{Source Port}," +
      "#{Destination Port},#{NAT Source Port},#{NAT Destination Port},#{Flags},#{Protocol}," +
      "#{Action},#{Bytes: integer},#{Bytes Sent: integer},#{Bytes Received: integer},#{Packets: " +
      "integer},#{Start Time},#{Elapsed Time: integer},#{Category},#{_FUTURE_USE},#{Sequence " +
      "Number},#{Action Flags},#{Source Location},#{Destination Location},#{_},#{Packets Sent: " +
      "integer},#{Packets Received: integer},#{rest}";

    final Parser p = PatternParser.create(pattern);

    final Map<String, Object> data = p.parse(text);
    Assert.assertEquals(45, data.size());
  }
}
