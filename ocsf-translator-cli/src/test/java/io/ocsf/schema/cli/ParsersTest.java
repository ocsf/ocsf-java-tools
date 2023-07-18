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

package io.ocsf.schema.cli;

import io.ocsf.parser.parsers.*;
import io.ocsf.utils.Files;
import io.ocsf.utils.Json;
import io.ocsf.utils.ParserException;

import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

public final class ParsersTest
{
  private ParsersTest() {}

  private static final Parsers parsers = new Parsers();

  public static void main(final String... args) throws IOException
  {
    parsers.register(new CarbonBlackParser());
    parsers.register(new ProofpointEmailParser());
    parsers.register(new Office365Parser());
    parsers.register(new XmlWinEventLogParser());
    parsers.register(new XmlWinEventSecurityLogParser());
    parsers.register(new XmlWinSysmonEventLogParser());
    parsers.register(new WinEventLogParser());
    parsers.register(new WinEventSecurityLogParser());
    parsers.register(new CiscoSyslogParser());
    parsers.register(new InfobloxDHCPParser());
    parsers.register(new BoxParser());

    if (args.length > 0)
    {
      for (final String arg : args)
      {
        visitAllDirsAndFiles(new File(arg));
      }
    }
    else
    {
      System.err.println("Usage ParsersTest <raw-event.json>...");
    }
  }

  private static void visitAllDirsAndFiles(final File file) throws IOException
  {
    System.out.println("parsing " + file);

    final BasicFileAttributes basicFileAttributes = java.nio.file.Files.readAttributes(file.toPath(), BasicFileAttributes.class);

    if (basicFileAttributes.isRegularFile())
    {
      parseFile(file.getPath());
    }
    else if (basicFileAttributes.isDirectory())
    {
      final String[] children = file.list();

      if (children != null)
        for (final String child : children)
          visitAllDirsAndFiles(new File(file, child));
    }
  }

  private static void parseFile(final String filename) throws IOException
  {
    try
    {
      final Map<String, String> data = Files.readJson(filename);

      final Map<String, Object> parsed = parsers.parse(data);

      if (parsed != null)
        System.out.println(Json.format(parsed));
    }
    catch (final ParserException e)
    {
      System.err.println("bad json file: " + e.getMessage());
      e.printStackTrace(System.err);
    }
  }
}
