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

public final class WindowsXmlParser4103Test
{
  private static final String xml1 =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    "   <Event xmlns=\"http://schemas.microsoft" +
    ".com/win/2004/08/events/event\">\n" +
    "      <System>\n" +
    "         <Provider Name=\"Microsoft-Windows-PowerShell\" " +
    "Guid=\"{A0C1853B-5C40-4B15-8766-3CF1C58F985A}\" />\n" +
    "         <EventID>4103</EventID>\n" +
    "         <Version>1</Version>\n" +
    "         <Level>4</Level>\n" +
    "         <Task>106</Task>\n" +
    "         <Opcode>20</Opcode>\n" +
    "         <Keywords>0x0</Keywords>\n" +
    "         <TimeCreated SystemTime=\"2021-03-26T10:40:25" +
    ".381297100Z\" />\n" +
    "         <EventRecordID>62141</EventRecordID>\n" +
    "         <Correlation ActivityID=\"{B3FA0929-222B-0002-D80C" +
    "-FAB32B22D701}\" />\n" +
    "         <Execution ProcessID=\"3272\" ThreadID=\"4492\" " +
    "/>\n" +
    "         <Channel>Microsoft-Windows-PowerShell/Operational" +
    "</Channel>\n" +
    "         <Computer>win-dc-683.attackrange" +
    ".local</Computer>\n" +
    "         <Security UserID=\"S-1-5-21-1568124518-47167176" +
    "-2301812064-500\" />\n" +
    "      </System>\n" +
    "      <EventData>\n" +
    "         <Data Name=\"ContextInfo\">Severity = " +
    "Informational\n" +
    "        Host Name = Default Host\n" +
    "        Host Version = 5.1.14393.3866\n" +
    "        Host ID = 8b3eca31-5ae7-4673-9c7e-fd045c7d2edf\n" +
    "        Host Application = " +
    "C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell" +
    ".exe -NoProfile -NonInteractive -ExecutionPolicy " +
    "Unrestricted -EncodedCommand " +
    "JgBjAGgAYwBwAC4AYwBvAG0AIAA2ADUAMAAwADEAIAA" +
    "+ACAAJABuAHUAbABsAAoAJABlAHgAZQBjAF8AdwByAGEAcABwAGUAcgBfAHMAdAByACAAPQAgACQAaQBuAHAAdQB0ACAAfAAgAE8AdQB0AC0AUwB0AHIAaQBuAGcACgAkAHMAcABsAGkAdABfAHAAYQByAHQAcwAgAD0AIAAkAGUAeABlAGMAXwB3AHIAYQBwAHAAZQByAF8AcwB0AHIALgBTAHAAbABpAHQAKABAACgAIgBgADAAYAAwAGAAMABgADAAIgApACwAIAAyACwAIABbAFMAdAByAGkAbgBnAFMAcABsAGkAdABPAHAAdABpAG8AbgBzAF0AOgA6AFIAZQBtAG8AdgBlAEUAbQBwAHQAeQBFAG4AdAByAGkAZQBzACkACgBJAGYAIAAoAC0AbgBvAHQAIAAkAHMAcABsAGkAdABfAHAAYQByAHQAcwAuAEwAZQBuAGcAdABoACAALQBlAHEAIAAyACkAIAB7ACAAdABoAHIAbwB3ACAAIgBpAG4AdgBhAGwAaQBkACAAcABhAHkAbABvAGEAZAAiACAAfQAKAFMAZQB0AC0AVgBhAHIAaQBhAGIAbABlACAALQBOAGEAbQBlACAAagBzAG8AbgBfAHIAYQB3ACAALQBWAGEAbAB1AGUAIAAkAHMAcABsAGkAdABfAHAAYQByAHQAcwBbADEAXQAKACQAZQB4AGUAYwBfAHcAcgBhAHAAcABlAHIAIAA9ACAAWwBTAGMAcgBpAHAAdABCAGwAbwBjAGsAXQA6ADoAQwByAGUAYQB0AGUAKAAkAHMAcABsAGkAdABfAHAAYQByAHQAcwBbADAAXQApAAoAJgAkAGUAeABlAGMAXwB3AHIAYQBwAHAAZQByAA==\n" +
    "        Engine Version = 5.1.14393.3866\n" +
    "        Runspace ID = 3fe0b8d7-99ec-4a5b-b884-519c54b81b30" +
    "\n" +
    "        Pipeline ID = 6\n" +
    "        Command Name = Add-Type\n" +
    "        Command Type = Cmdlet\n" +
    "        Script Name = C:\\Program " +
    "Files\\WindowsPowerShell\\Modules\\PSReadline\\1" +
    ".2\\PSReadLine.psm1\n" +
    "        Command Path =\n" +
    "        Sequence Number = 34\n" +
    "        User = ATTACKRANGE\\Administrator\n" +
    "        Connected User =\n" +
    "        Shell ID = Microsoft.PowerShell</Data>\n" +
    "         <Data Name=\"UserData\" />\n" +
    "         <Data Name=\"Payload\">CommandInvocation(Add-Type)" +
    ": \"Add-Type\"\n" +
    "ParameterBinding(Add-Type): name=\"TypeDefinition\"; " +
    "value=\"using System;\n" +
    "using System.ComponentModel;\n" +
    "using System.Runtime.InteropServices;\n" +
    "\n" +
    "namespace Ansible.Command {\n" +
    "    public class SymLinkHelper {\n" +
    "        [DllImport(\"kernel32.dll\", CharSet=CharSet" +
    ".Unicode, SetLastError=true)]\n" +
    "        public static extern bool DeleteFileW(string " +
    "lpFileName);\n" +
    "\n" +
    "        [DllImport(\"kernel32.dll\", CharSet=CharSet" +
    ".Unicode, SetLastError=true)]\n" +
    "        public static extern bool RemoveDirectoryW(string " +
    "lpPathName);\n" +
    "\n" +
    "        public static void DeleteDirectory(string path) {\n" +
    "            if (!RemoveDirectoryW(path))\n" +
    "                throw new Exception(String.Format" +
    "(\"RemoveDirectoryW({0}) failed: {1}\", path, new " +
    "Win32Exception(Marshal.GetLastWin32Error()).Message));\n" +
    "        }\n" +
    "\n" +
    "        public static void DeleteFile(string path) {\n" +
    "            if (!DeleteFileW(path))\n" +
    "                throw new Exception(String.Format" +
    "(\"DeleteFileW({0}) failed: {1}\", path, new Win32Exception" +
    "(Marshal.GetLastWin32Error()).Message));\n" +
    "        }\n" +
    "    }\n" +
    "}\"</Data>\n" +
    "      </EventData>\n" +
    "      <RenderingInfo Culture=\"en-US\">\n" +
    "         <Message>CommandInvocation(Add-Type): " +
    "\"Add-Type\"\n" +
    "ParameterBinding(Add-Type): name=\"TypeDefinition\"; " +
    "value=\"using System;\n" +
    "using System.ComponentModel;\n" +
    "using System.Runtime.InteropServices;\n" +
    "\n" +
    "namespace Ansible.Command {\n" +
    "    public class SymLinkHelper {\n" +
    "        [DllImport(\"kernel32.dll\", CharSet=CharSet" +
    ".Unicode, SetLastError=true)]\n" +
    "        public static extern bool DeleteFileW(string " +
    "lpFileName);\n" +
    "\n" +
    "        [DllImport(\"kernel32.dll\", CharSet=CharSet" +
    ".Unicode, SetLastError=true)]\n" +
    "        public static extern bool RemoveDirectoryW(string " +
    "lpPathName);\n" +
    "\n" +
    "        public static void DeleteDirectory(string path) {\n" +
    "            if (!RemoveDirectoryW(path))\n" +
    "                throw new Exception(String.Format" +
    "(\"RemoveDirectoryW({0}) failed: {1}\", path, new " +
    "Win32Exception(Marshal.GetLastWin32Error()).Message));\n" +
    "        }\n" +
    "\n" +
    "        public static void DeleteFile(string path) {\n" +
    "            if (!DeleteFileW(path))\n" +
    "                throw new Exception(String.Format" +
    "(\"DeleteFileW({0}) failed: {1}\", path, new Win32Exception" +
    "(Marshal.GetLastWin32Error()).Message));\n" +
    "        }\n" +
    "    }\n" +
    "}\"\n" +
    "\n" +
    "\n" +
    "Context:\n" +
    "        Severity = Informational\n" +
    "        Host Name = Default Host\n" +
    "        Host Version = 5.1.14393.3866\n" +
    "        Host ID = 8b3eca31-5ae7-4673-9c7e-fd045c7d2edf\n" +
    "        Host Application = " +
    "C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell" +
    ".exe -NoProfile -NonInteractive -ExecutionPolicy " +
    "Unrestricted -EncodedCommand " +
    "JgBjAGgAYwBwAC4AYwBvAG0AIAA2ADUAMAAwADEAIAA" +
    "+ACAAJABuAHUAbABsAAoAJABlAHgAZQBjAF8AdwByAGEAcABwAGUAcgBfAHMAdAByACAAPQAgACQAaQBuAHAAdQB0ACAAfAAgAE8AdQB0AC0AUwB0AHIAaQBuAGcACgAkAHMAcABsAGkAdABfAHAAYQByAHQAcwAgAD0AIAAkAGUAeABlAGMAXwB3AHIAYQBwAHAAZQByAF8AcwB0AHIALgBTAHAAbABpAHQAKABAACgAIgBgADAAYAAwAGAAMABgADAAIgApACwAIAAyACwAIABbAFMAdAByAGkAbgBnAFMAcABsAGkAdABPAHAAdABpAG8AbgBzAF0AOgA6AFIAZQBtAG8AdgBlAEUAbQBwAHQAeQBFAG4AdAByAGkAZQBzACkACgBJAGYAIAAoAC0AbgBvAHQAIAAkAHMAcABsAGkAdABfAHAAYQByAHQAcwAuAEwAZQBuAGcAdABoACAALQBlAHEAIAAyACkAIAB7ACAAdABoAHIAbwB3ACAAIgBpAG4AdgBhAGwAaQBkACAAcABhAHkAbABvAGEAZAAiACAAfQAKAFMAZQB0AC0AVgBhAHIAaQBhAGIAbABlACAALQBOAGEAbQBlACAAagBzAG8AbgBfAHIAYQB3ACAALQBWAGEAbAB1AGUAIAAkAHMAcABsAGkAdABfAHAAYQByAHQAcwBbADEAXQAKACQAZQB4AGUAYwBfAHcAcgBhAHAAcABlAHIAIAA9ACAAWwBTAGMAcgBpAHAAdABCAGwAbwBjAGsAXQA6ADoAQwByAGUAYQB0AGUAKAAkAHMAcABsAGkAdABfAHAAYQByAHQAcwBbADAAXQApAAoAJgAkAGUAeABlAGMAXwB3AHIAYQBwAHAAZQByAA==\n" +
    "        Engine Version = 5.1.14393.3866\n" +
    "        Runspace ID = 3fe0b8d7-99ec-4a5b-b884-519c54b81b30" +
    "\n" +
    "        Pipeline ID = 6\n" +
    "        Command Name = Add-Type\n" +
    "        Command Type = Cmdlet\n" +
    "        Script Name =\n" +
    "        Command Path =\n" +
    "        Sequence Number = 34\n" +
    "        User = ATTACKRANGE\\Administrator\n" +
    "        Connected User =\n" +
    "        Shell ID = Microsoft.PowerShell\n" +
    "\n" +
    "\n" +
    "User Data:</Message>\n" +
    "         <Level>Information</Level>\n" +
    "         <Task>Executing Pipeline</Task>\n" +
    "         <Opcode>To be used when operation is just " +
    "executing a method</Opcode>\n" +
    "         <Channel>Microsoft-Windows-PowerShell/Operational" +
    "</Channel>\n" +
    "         <Provider />\n" +
    "         <Keywords />\n" +
    "      </RenderingInfo>\n" +
    "   </Event>\n";

  private WindowsXmlParser4103Test() {}

  public static void main(final String... args) throws Exception
  {
    final Map<String, Object> data = new WindowsXmlParser().parse(xml1);
    System.out.println(Json.format(data));
  }
}
