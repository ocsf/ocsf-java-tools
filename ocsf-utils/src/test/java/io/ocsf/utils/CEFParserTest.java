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

import io.ocsf.utils.parsers.CEFParser;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CEFParserTest
{
  private static void parse(final String text)
  {
    final Map<String, Object> data = CEFParser.parse(text);

    assertEquals(8, data.size());
  }

  @Test
  public void parseNetskope()
  {
    parse(
      "CEF: 0|Netskope|alliances|NULL|audit|NULL|High|auditLogEvent=Access Denied auditType=admin_audit_logs suser=null timestamp=1690957108");
  }

  @Test
  public void parseInfobloxThreatprotect()
  {
    parse(
      "CEF:0|Infoblox|NIOS Threat|8.4.4-386831|120303001|Blocklist:foo.foo.foo|7|src=192.168.1.3 " +
      "spt=63290 dst=192.168.1.2 dpt=53 act=\"DROP\" cat=\"BLOCKLIST UDP FQDN lookup\" nat=0 " +
      "nfpt=0 nlpt=0 fqdn=foo.foo.foo hit_count=4");
  }

  @Test
  public void parseInfobloxThreatprotectWithDomain()
  {
    parse(
      "CEF:0|Infoblox|NIOS Threat|8.4.4-386831|120303001|Blocklist:foo.foo.foo|7|src=192.168.1.3 " +
      "spt=57092 dst=192.168.1.2 dpt=53 act=\"DROP\" cat=\"BLOCKLIST UDP FQDN lookup\" nat=0 " +
      "nfpt=0 nlpt=0 fqdn=foo.foo.foo hit_count=4");
  }

  @Test
  public void parseUnquotedText()
  {
    parse(
      "CEF:0|Microsoft|ATA|1.9.0.0|AbnormalSensitiveGroupMembershipChangeSuspiciousActivity" +
      "|Abnormal modification of sensitive groups|5|start=2018-12-12T18:52:58.0000000Z " +
      "app=GroupMembershipChangeEvent suser=krbtgt msg=krbtgt has uncharacteristically modified " +
      "sensitive group memberships. externalId=2024 cs1Label=url cs1=https://192.168.0" +
      ".220/suspiciousActivity/5c113d028ca1ec1250ca0491");
    parse(
      "CEF:0|Microsoft|ATA|1.9.0.0|LdapBruteForceSuspiciousActivity|Brute force attack using LDAP" +
      " simple bind|5|start=2018-12-12T17:52:10.2350665Z app=Ldap msg=10000 password guess " +
      "attempts were made on 100 accounts from W2012R2-000000-Server. One account password was " +
      "successfully guessed. externalId=2004 cs1Label=url cs1=https://192.168.0" +
      ".220/suspiciousActivity/5c114acb8ca1ec1250cacdcb");
    parse(
      "CEF:0|Microsoft|ATA|1.9.0.0|EncryptionDowngradeSuspiciousActivity|Encryption downgrade " +
      "activity|5|start=2018-12-12T18:10:35.0334169Z app=Kerberos msg=The encryption method of " +
      "the TGT field of TGS_REQ message from W2012R2-000000-Server has been downgraded based on " +
      "previously learned behavior. This may be a result of a Golden Ticket in-use on " +
      "W2012R2-000000-Server. externalId=2009 cs1Label=url cs1=https://192.168.0" +
      ".220/suspiciousActivity/5c114f938ca1ec1250cafcfa");
    parse(
      "CEF:0|Microsoft|ATA|1.9.0.0|EncryptionDowngradeSuspiciousActivity|Encryption downgrade " +
      "activity|5|start=2018-12-12T17:00:31.2975188Z app=Kerberos msg=The encryption method of " +
      "the Encrypted_Timestamp field of AS_REQ message from W2012R2-000000-Server has been " +
      "downgraded based on previously learned behavior. This may be a result of a credential " +
      "theft using Overpass-the-Hash from W2012R2-000000-Server. externalId=2010 cs1Label=url " +
      "cs1=https://192.168.0.220/suspiciousActivity/5c113eaf8ca1ec1250ca0883");
  }

  @Test
  public void parseLongText()
  {
    parse(
      "CEF:0|Check Point|SmartDefense|Check Point|IPS|Heimdal KDC ASN1 DER Length Denial of " +
      "Service|5|act=Detect cp_severity=Medium cnt=69 cs1Label=Threat Prevention Rule Name " +
      "cs2Label=Protection ID cs2=asm_dynamic_prop_AMSN20180109_01 cs3Label=Protection Type " +
      "cs3=IPS cs4Label=Protection Name cs4=Heimdal KDC ASN1 DER Length Denial of Service " +
      "cs4Label=Threat Prevention Rule ID cs4=REDACTED deviceDirection=1 " +
      "flexNumber1Label=Confidence flexNumber1=3 flexNumber2Label=Performance Impact " +
      "flexNumber2=3 flexString2Label=Attack Information flexString2=Heimdal KDC ASN1 DER Length " +
      "Denial of Service in=0 msg=Web Server Enforcement Violation out=0 rt=1591186503000 " +
      "spt=47298 dpt=88 Signature=CVE-2017-17439 cs4Label=Threat Prevention Rule ID");
  }
}