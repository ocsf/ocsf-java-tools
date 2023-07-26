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

public final class CiscoSyslogData
{
  public static final String[] Data = {
    "<165>Oct 06 2021 15:02:30 10.160.0.10 : %ASA-5-111010: User 'admin', running 'CLI' from IP 0" +
    ".0.0.0, executed 'dir disk0:/dap.xml'",
    "<111>Mar 12 07:18:12 10.11.12.13 : %ASA-6-713228: Group = dummy_group, Username = " +
    "dummy_user, IP = 10.0.0.1 Assigned private IP address 10.0.0.2 to remote user",
    "<111>Mar 12 07:18:12 10.11.12.13 : %ASA-7-716014: Group dummy_group User dummy_user View " +
    "file dummy_filename .",
    "<111>Mar 12 07:18:12 10.11.12.13 : %ASA-7-716015: Group dummy_group User dummy_user Remove " +
    "file dummy_filename .",
    "<111>Mar 12 07:18:12 10.11.12.13 : %ASA-7-716016: Group dummy_group User dummy_user Rename " +
    "file dummy_old_filename to dummy_new_filename .",
    "<111>Mar 12 07:18:12 10.11.12.13 : %ASA-3-717009: Certificate validation failed. Reason: " +
    "dummy_reason_string .",
    "<111>Mar 12 07:18:12 10.11.12.13 : %ASA-6-717022: Certificate was successfully validated. " +
    "serial number: dummy_serial_number, subject name: cn=dummy_comman_subject_name.",
    "<111>Mar 12 07:18:12 10.11.12.13 : %ASA-3-717027: Certificate chain failed validation. " +
    "dummy_reason_string .",
    "<111>Mar 12 07:18:12 10.11.12.13 : %ASA-6-717028: Certificate chain was successfully " +
    "validated dummy_additional_info .",
    "<111>Mar 12 07:18:12 10.11.12.13 : %ASA-7-717029: Identified client certificate within " +
    "certificate chain. serial number: dummy_serial_number, subject name: " +
    "cn=dummy_comman_subject_name.",
    "<111>Mar 12 07:18:12 10.11.12.13 : %ASA-4-717037: Tunnel group search using certificate maps" +
    " failed for peer certificate: dummy_certificate_identifier .",
    "<111>Mar 12 07:15:14 10.11.12.13 : %ASA-6-722051: Group dummy_group User dummy_user IP 10.0" +
    ".0.1 IPv4 Address 10.0.0.2 IPv6 Address 2001:db8:3333:4444:5555:6666:7777:8888 assigned to " +
    "session"
  };

  private CiscoSyslogData() {}
}
