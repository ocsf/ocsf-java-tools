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

import io.ocsf.schema.RawEvent;
import io.ocsf.schema.Schema;
import io.ocsf.schema.svc.EventService;
import io.ocsf.schema.svc.TranslatorException;
import io.ocsf.utils.FMap;
import io.ocsf.utils.Json5Parser;
import io.ocsf.utils.Maps;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * A helper class to run performance tests.
 */
public class LoadRunner
{
  private final EventService service;
  private final Schema schema;

  private long startTime;
  private long duration;

  // Successful and failed event counters
  private int success, failed;

  /**
   * Create a simple, single threaded, runner.
   *
   * @param rules  the rules folder
   * @param schema the schema file
   * @throws IOException is unable to read the rules
   */
  LoadRunner(final String rules, final Schema schema) throws IOException
  {
    this.service = new EventService(rules);
    this.schema = schema;
  }

  public void run(final String path) throws IOException
  {
    final Path p = Paths.get(path);

    if (Files.isRegularFile(p))
    {
      start();
      Files.readAllLines(p).forEach(this::process);
      stop();
    }
    else
    {
      System.err.println(path + " is not a regular file");
    }
  }

  /**
   * Process a single event.
   *
   * @param json the JSON encoded event
   */
  public void process(final String json)
  {
    Map<String, Object> data = Json5Parser.to(json);

    if (data.containsKey("result"))
      data = Maps.typecast(data.get("result"));

    final Map<String, Object> event = FMap.<String, Object>b()
        .p(RawEvent.SOURCE_TYPE, data.get("sourcetype"))
        .p(RawEvent.RAW_EVENT, data.get("_raw"));

    try
    {
      schema.enrich(service.process(event));
      ++success;
    }
    catch (final TranslatorException e)
    {
      System.err.println(e.getMessage());
      ++failed;
    }
  }

  /**
   * Starts the timer.
   */
  public void start() {startTime = System.currentTimeMillis();}

  /**
   * Stops the timer and calculate the elapsed time in milliseconds.
   */
  public void stop()
  {
    duration += System.currentTimeMillis() - startTime;
  }

  /**
   * Calculates and returns the EPS. This function must be called after the stop() method is called.
   *
   * @return the events per second
   */
  public int eps()
  {
    return duration > 0 ? (int) ((success + failed) * 1_000 / duration) : -1;
  }

  public void printResults()
  {
    System.out.printf("Success %,12d  events%n", success);
    System.out.printf("Failed  %,12d  events%n", failed);
    System.out.printf("Total   %,12d  events%n", success + failed);
    System.out.printf("Elapsed %,12d  msec%n", duration);
    System.out.println("----------------------------");
    System.out.printf("Speed   %,12d  EPS%n", eps());
  }
}
