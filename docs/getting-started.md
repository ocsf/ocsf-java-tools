# Getting Started with the OCSF Schema Java Tools

To get started with the OCSF Schema Java Tools project using Gradle, you'll need
to perform the following steps:

## Clone the Repository

First, clone the OCSF Schema Java Tools repository to your local machine using
Git.

```bash
git clone https://github.com/ocsf/ocsf-java-tools.git
```

## Navigate to the Project Directory

Change into the project's root directory.

```bash
cd ocsf-java-tools
```

## Build the Project

Use Gradle to build the project. This will compile the source code and create
the necessary artifacts.

```bash
./gradlew build
```

## Set Up Gradle Wrapper (Optional)

If your system does not have Gradle installed, you can use the included Gradle
Wrapper (`gradlew` or `gradlew.bat`) to automatically download and use the
correct version of Gradle for the project.

## Explore the Project

The project structure is organized with the following key components:

- `ocsf-utils`: Core utilities for the OCSF Schema Java Tools.
- `ocsf-parsers`: Library for parsing events to the OCSF schema.
- `ocsf-translator`: Library for translating events to the OCSF schema.
- `ocsf-schema`: Library for schema enrichment tasks, such as adding `type_uid`,
  enum text values, and generating `observables` arrays.
- `ocsf-cli`: Command-line tool for parsing, translating, enriching, and
  validating translations using the OCSF Schema server.

## Include Dependencies (Gradle Dependency)

If you want to use any of the OCSF Schema Java Tools libraries in your own Java
project, you can include them as dependencies in your Gradle build file. For
example, to include the `ocsf-parsers` library, add the following to
your `build.gradle` file:

```gradle
dependencies {
    implementation 'com.ocsf:parsers:1.0.0' // Replace '1.0.0' with the desired version
}
```

## Read the Documentation

- **Translations Documentation**: For more detailed information on writing
  translations and using the OCSF Schema Java Tools, refer to
  the [Translations documentation](event-translation-guide.md).

- **Translation Examples**: Explore translation examples in
  the [examples directory](../ocsf-cli/src/main/dist/examples) of the project.
  These examples will help you understand how to effectively use the OCSF Schema
  Java Tools for different scenarios.

With these steps completed, you should have the OCSF Schema Java Tools set up in
your local environment, and you can start exploring its functionalities and
using the libraries in your own projects. If you encounter any issues or have
questions, refer to the
project's [issue tracker](https://github.com/ocsf/ocsf-java-tools/issues) for
assistance. Happy coding!
