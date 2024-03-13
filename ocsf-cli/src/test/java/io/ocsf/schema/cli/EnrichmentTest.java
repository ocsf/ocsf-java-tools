package io.ocsf.schema.cli;

import io.ocsf.schema.Schema;
import io.ocsf.utils.Json;
import io.ocsf.utils.parsers.Json5Parser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class EnrichmentTest
{
  private EnrichmentTest() {}

  public static void main(final String[] args) throws IOException
  {
    final Path schemaFile = Path.of(args[0]);
    final Path eventFile = Path.of(args[1]);

    final Schema schema = new Schema(schemaFile, true, true);
    @SuppressWarnings("unchecked")
    final Map<String, Object> event
        = (Map<String, Object>) Json5Parser.parse(Files.readAllBytes(eventFile));

    final Map<String, Object> enriched = schema.enrich(event);
    System.out.println(Json.format(enriched));
  }
}
