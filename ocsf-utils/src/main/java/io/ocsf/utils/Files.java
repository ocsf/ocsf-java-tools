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

package io.ocsf.utils;

import io.ocsf.utils.parsers.Json5Parser;
import io.ocsf.utils.parsers.ParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * A helper class to read/write JSON files.
 */
public final class Files
{
  private Files() {}

  public static final String JSON_FILE_EXT = ".json";

  public static String readFile(final String filename) throws IOException
  {
    return readFile(Paths.get(filename));
  }

  public static String readFile(final File file) throws IOException
  {
    return readFile(file.toPath());
  }

  public static String readFile(final Path path) throws IOException
  {
    return new String(java.nio.file.Files.readAllBytes(path), StandardCharsets.UTF_8);
  }

  /**
   * Reads a JSON object from a file. The method ensures that the file is closed when all bytes have
   * been read or an I/O error, or other runtime exception, is thrown.
   *
   * @param path the path to the JSON file
   * @param <T>  the type of the parsed JSON data
   * @return the content of the file as an object (JSON object)
   * @throws IOException     if an I/O error occurs reading from the stream
   * @throws ParserException if the file contains an invalid JSON object
   */
  @SuppressWarnings("unchecked")
  public static <T> T readJson(final Path path) throws IOException
  {
    return (T) Json5Parser.parse(java.nio.file.Files.readAllBytes(path));
  }

  public static <T> T readJson(final String filename) throws IOException
  {
    return readJson(Paths.get(filename));
  }

  /**
   * Writes the bytes to a file. If the file already exists, then the file will be overwritten.
   *
   * @param path  the path to the file
   * @param bytes the byte array with the bytes to write
   * @throws IOException if an I/O error occurs writing to or creating the file
   */
  public static void write(final Path path, final byte[] bytes) throws IOException
  {
    java.nio.file.Files.write(
      path, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
  }

  /**
   * Encodes the object as JSON and writes to a file. If the file already exists, then the file will
   * be overwritten.
   *
   * @param path the path to the file
   * @param obj  the object to write
   * @throws IOException if an I/O error occurs writing to or creating the file
   */
  public static void writeJson(final Path path, final Object obj) throws IOException
  {
    writeJson(path, obj, true);
  }

  /**
   * Encodes the object as JSON and writes to a file. If the file already exists, then the file will
   * be overwritten.
   *
   * @param path   the path to the file
   * @param obj    the object to write
   * @param pretty the JSON format flag
   * @throws IOException if an I/O error occurs writing to or creating the file
   */
  public static void writeJson(final Path path, final Object obj, final boolean pretty)
    throws IOException
  {
    final byte[] bytes =
      (pretty ? Json.format(obj) : Json.toString(obj)).getBytes(StandardCharsets.UTF_8);

    java.nio.file.Files.write(
      path, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
  }

  public static void writeJson(final String filename, final Object obj) throws IOException
  {
    writeJson(Paths.get(filename), obj);
  }

  public static void writeJson(final String filename, final Object obj, final boolean pretty)
    throws IOException
  {
    writeJson(Paths.get(filename), obj, pretty);
  }

  public static <T> T readJsonFromResource(final Path path) throws IOException
  {
    return readJsonFromResource(path.toString());
  }

  @SuppressWarnings("unchecked")
  public static <T> T readJsonFromResource(final String filename) throws IOException
  {
    // get a file from resources folder
    // works everywhere, IDEA, unit test and JAR file
    // The class loader that loaded the class
    final ClassLoader classLoader = Files.class.getClassLoader();
    final InputStream inputStream = classLoader.getResourceAsStream(filename);

    if (inputStream != null)
    {
      try
      {
        final int size = inputStream.available();
        if (size > 0)
        {
          final byte[] data = new byte[size];
          if (inputStream.read(data) == size)
            return (T) Json5Parser.parse(data);
        }
      }
      finally
      {
        inputStream.close();
      }

      throw new IOException("Invalid JSON file: " + filename);
    }

    throw new FileNotFoundException(filename);
  }
}
