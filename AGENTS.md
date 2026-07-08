# Repository Guidelines

## Project Structure & Module Organization
This repository is a Maven multi-module build rooted at `pom.xml`.

- `tomcat9-maven-plugin/`: main Maven plugin implementation, with mojos under `src/main/java` and plugin-level tests under `src/test/java`.
- `common-tomcat-maven-plugin/`: shared deployment, config, and runtime utilities used by the plugin.
- `tomcat9-war-runner/`: executable WAR runner support classes.
- `tomcat-maven-plugin-it/`: reusable integration test base classes and fixtures.
- `tomcat-maven-archetype/`: Maven archetype templates and archetype tests.
- `src/site/`: APT-based project documentation and site content.

Avoid editing `target/`; it is generated output.

## Build, Test, and Development Commands
- `mvn clean install`: full multi-module build with compilation and tests.
- `mvn -pl tomcat9-maven-plugin -am test`: test the main plugin and required upstream modules.
- `mvn -pl common-tomcat-maven-plugin test`: run unit tests for shared utilities.
- `mvn -pl tomcat9-maven-plugin -am -Prun-its verify`: run the Tomcat 9 integration tests.
- `mvn -DskipTests clean verify`: fast verification when changing build metadata or docs.
- `mvn -Prelease-central -DskipTests -pl tomcat9-maven-plugin -am clean deploy`: publish the Tomcat 9 plugin and required artifacts to Maven Central.

Run commands from the repository root unless a module-specific workflow is intentional.

## Coding Style & Naming Conventions
Java sources use standard 4-space indentation and conventional Maven/Apache naming. Keep package names stable unless a coordinated refactor is required. Name mojos with the existing pattern, such as `RunMojo`, `DeployMojo`, and `Abstract...Mojo`. Test classes typically end in `Test` or `IT`. Preserve XML formatting already used in each POM or site file.

## Testing Guidelines
JUnit 4 is used for tests. Unit tests live in `src/test/java`; integration-style support code lives in `tomcat-maven-plugin-it`. Prefer focused tests near the module you changed. For plugin behavior changes, run:

`mvn -pl tomcat9-maven-plugin -am test`

To execute the Tomcat 9 integration tests, run:

`mvn -pl tomcat9-maven-plugin -am -Prun-its verify`

`tomcat-maven-plugin-it` provides shared integration-test support code. The actual integration tests are declared in `tomcat9-maven-plugin` and are only enabled when the `run-its` profile is active.

For broad dependency or packaging changes, run `mvn clean install` before opening a PR.

## Commit & Pull Request Guidelines
Recent history favors short, imperative commit subjects such as `Reduce the fork to Tomcat 9 only` and `Update maven-compiler-plugin to 3.8.1`. Keep the first line concise and action-oriented.

PRs should include a clear summary, affected modules, validation commands run, and any publishing impact. Link related issues when applicable. Include logs or screenshots only when they clarify behavior or release flow changes.

## Release & Configuration Notes
Central publishing uses the `release-central` profile. Ensure `~/.m2/settings.xml` contains the `central` server entry and that `MAVEN_CENTRAL_USER`, `MAVEN_CENTRAL_PASSWORD`, and `GPG_PASS` are set before deploys.
