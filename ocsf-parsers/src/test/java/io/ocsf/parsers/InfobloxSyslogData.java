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

public final class InfobloxSyslogData
{
  public static final String[] Data = {
    "<30>May 19 10:36:09 10.160.20.42 dhcpd[10006]: DHCPRELEASE of 10.26.74.112 from " +
    "b8:80:4f:04:6a:28 (density-B2EYH033) via eth2 (found) TransID 571a3184 uid " +
    "01:b8:80:4f:04:6a:28",
    "<30>May 19 10:51:10 10.160.20.42 dhcpd[10006]: DHCPRELEASE of 10.26.74.112 from " +
    "b8:80:4f:04:6a:28 (density-B2EYH033) via eth2 (found) TransID ce55a8ff uid " +
    "01:b8:80:4f:04:6a:28",
    "<30>May 19 11:06:10 10.160.20.42 dhcpd[10006]: DHCPRELEASE of 10.26.74.112 from " +
    "b8:80:4f:04:6a:28 (density-B2EYH033) via eth2 (found) TransID 88638b33 uid " +
    "01:b8:80:4f:04:6a:28",
    "<30>May 19 11:21:10 10.160.20.42 dhcpd[10006]: DHCPRELEASE of 10.26.74.112 from " +
    "b8:80:4f:04:6a:28 (density-B2EYH033) via eth2 (found) TransID 7ee6ef96 uid " +
    "01:b8:80:4f:04:6a:28",
    "<30>May 19 11:30:20 10.160.20.42 dhcpd[10006]: DHCPACK on 10.127.16.36 to 00:e0:4c:08:00:7e " +
    "(PF345215) via eth2 relay 10.127.16.1 lease-duration 43200 (RENEW) uid 01:00:e0:4c:08:00:7e",
    "<30>May 19 11:30:20 10.160.20.42 dhcpd[10006]: DHCPACK on 10.132.153.40 to 00:10:49:44:2b:22" +
    " (p8001049442B22) via eth2 relay 10.132.153.1 lease-duration 43140 (RENEW) uid " +
    "01:00:10:49:44:2b:22",
    "<30>May 19 11:30:20 10.160.20.42 dhcpd[10006]: DHCPACK on 10.140.15.66 to 00:50:56:96:40:66 " +
    "(netyce.sv.acme.com) via eth2 relay eth2 lease-duration 900",
    "<30>May 19 11:30:20 10.160.20.42 dhcpd[10006]: DHCPACK on 10.141.66.8 to a0:36:9f:ef:ea:44 " +
    "(sv3-orca-0413p22) via eth2 relay eth2 lease-duration 86400 (RENEW)",
    "<30>May 19 11:32:36 10.160.20.42 dhcpd[10006]: DHCPACK on 10.141.16.80 to 00:50:56:96:85:8b " +
    "(io-itsintly-ui5) via eth2 relay 10.141.16.1 lease-duration 43200",
    "<30>May 19 11:33:43 10.136.2.7 dhcpd[17296]: DHCPEXPIRE on 10.141.6.141 to 00:50:56:ae:f1:8f",
    "<30>May 19 11:33:43 10.160.20.42 dhcpd[10006]: DHCPEXPIRE on 10.141.6.141 to " +
    "00:50:56:ae:f1:8f",
    "<30>May 19 11:35:11 10.136.2.7 dhcpd[17296]: DHCPEXPIRE on 10.26.98.71 to 38:f9:d3:bf:df:d8",
    "<30>May 19 11:35:11 10.160.20.42 dhcpd[10006]: DHCPEXPIRE on 10.26.98.71 to 38:f9:d3:bf:df:d8",
    "<30>May 19 11:36:10 10.136.2.7 dhcpd[17296]: DHCPEXPIRE on 10.26.74.112 to b8:80:4f:04:6a:28",
    "<30>May 19 11:36:10 10.160.20.42 dhcpd[10006]: DHCPRELEASE of 10.26.74.112 from " +
    "b8:80:4f:04:6a:28 (density-B2EYH033) via eth2 (found) TransID af3da74f uid " +
    "01:b8:80:4f:04:6a:28"
  };

  private InfobloxSyslogData() {}
}
