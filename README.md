# OCSF Schema Java Tools

## Description and Purpose

The OCSF Schema Java Tools repository provides a collection of Java libraries
and a command line tool designed to facilitate the adoption and utilization of
the OCSF Schema. The `ocsf-parsers` and `ocsf-translator` libraries, along with
the `ocsf-cli` module, aim to streamline the process of parsing and translating
existing events to the OCSF schema. The `ocsf-schema` library simplifies schema
enrichment by assisting in tasks such as adding `type_uid`, enum text values,
and generating `observables` arrays using the schema and event data.

## Data Translation Domain Specific Language (DSL)

In addition to the libraries, the project defines a data translation
domain-specific language, utilizing JSON as a file format. This DSL enables
users to create, manage, and share event translations efficiently. The OCSF
Schema CLI tool can validate the translation rules using
the [OCSF Schema](https://schema.ocsf.io/) server or a locally running OCSF
server.

By leveraging these tools, developers can seamlessly integrate the OCSF Schema
into their Java projects, streamlining event processing and schema enrichment.

## Dependencies

The OCSF Schema Java Tools project has the following dependencies:

1. `ocsf-utils`, `ocsf-parsers`, `ocsf-translator`, and `ocsf-schema`:
   - **Minimum Java Version:** Java 8
   - **Purpose:** These libraries provide the core functionalities for working
     with the OCSF Schema. They handle parsing, translation, and schema
     enrichment tasks for existing events.

2. `ocsf-cli`:
   - **Minimum Java Version:** Java 11
   - **Purpose:** The `ocsf-cli` is a command-line tool that allows users to
     parse, translate, enrich, and validate the translated events. The event
     validation uses the validation API (https://schema.ocsf.io/api/validate) of
     the [OCSF Schema](https://schema.ocsf.io) server.

## Project Status

The project is actively maintained, and we encourage contributions and feedback
from the community.

## Getting Started

To quickly get started with the OCSF Schema Java Tools, follow the installation
instructions in the [Getting Started](docs/getting-started.md) guide.

## Documentation

- **Translations**: For detailed information on how to write translations,
  please refer to the [Translations documentation](docs/translations.md).

- **OCSF Schema**: Learn more about the OCSF Schema by visiting the schema
  repository on [GitHub](https://github.com/ocsf/ocsf-schema).

- **OCSF Schema Server**: Find instructions on how to locally build and run the
  OCSF Schema server, please see the server repository
  on [GitHub](https://github.com/ocsf/ocsf-server).

## Issue Tracking

If you encounter any bugs, have feature requests, or need assistance, please
feel free to open an issue on
our [issue tracker](https://github.com/ocsf/ocsf-java-tools/issues).

## License

This project is licensed under the Apache License, Version 2.0. See
the [LICENSE](LICENSE) file for details.

We hope you find the OCSF Schema Java Tools beneficial and look forward to your
contributions to this open-source project!
