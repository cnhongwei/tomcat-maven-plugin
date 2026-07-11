<!--
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
-->

Tomcat Maven Plugin Fork
------------------------

This repository contains a Maven multi-module fork of the Tomcat Maven Plugin focused on Tomcat 9 support.

Fork capabilities
-----------------

This fork currently focuses on the Tomcat 9 line and keeps the plugin behavior aligned with that scope.

Supported capabilities include:

* Tomcat 9 plugin goals for local development and deployment workflows
* embedded Tomcat execution for `run`-style goals
* automatic Maven `jdk` toolchain support for `run`-style goals
* zero-configuration fallback to the Maven runtime JDK when no toolchain is selected
* virtual-thread executors for embedded Tomcat connectors on Java 21 or later
* `exec-war`, `exec-war-only`, and standalone WAR runner packaging flows
* Maven Central publishing via the `release-central` profile

For the `run`-style goals, the behavior is fixed:

* if Maven build context already contains a selected `jdk` toolchain, the plugin uses that toolchain's `java`
* if no `jdk` toolchain is available, the plugin keeps using the current Maven JVM

To make the plugin run with a specific JDK, configure `maven-toolchains-plugin` so a `jdk` toolchain is selected before invoking `tomcat9:run`.

Virtual threads
---------------

`run`-style goals enable Tomcat's virtual-thread executor for HTTP, HTTPS, and AJP connectors by default. Virtual threads require Java 21 or later and Tomcat libraries that include `VirtualThreadExecutor`. When either requirement is unavailable, the plugin logs a warning and uses Tomcat's default connector executor instead.

When Maven itself runs on an older JDK, select a Java 21+ `jdk` toolchain to run Tomcat with virtual threads. To opt out, set `maven.tomcat.useVirtualThreads` to `false`:

```xml
<plugin>
  <groupId>org.apache.tomcat.maven</groupId>
  <artifactId>tomcat9-maven-plugin</artifactId>
  <configuration>
    <useVirtualThreads>false</useVirtualThreads>
  </configuration>
</plugin>
```

The same setting can be supplied from the command line with `-Dmaven.tomcat.useVirtualThreads=false`.

Build and test
--------------

To build this project you must have Apache Maven at least 2.2.1 and Java 8 or newer.

Common commands:

* `mvn clean install`: build all modules and run the default test suite.
* `mvn -pl tomcat9-maven-plugin -am test`: run unit tests for the Tomcat 9 plugin and required upstream modules.
* `mvn -pl common-tomcat-maven-plugin test`: run unit tests for shared utilities.

Integration tests
-----------------

`tomcat-maven-plugin-it` provides shared integration-test support code. The actual Tomcat 9 integration tests are declared in `tomcat9-maven-plugin` and are only enabled when the `run-its` profile is active.

To run all Tomcat 9 integration tests, use:

`mvn -pl tomcat9-maven-plugin -am -Prun-its verify`

To run a single integration test, use:

`mvn -pl tomcat9-maven-plugin -am -Prun-its -Dit.test=Tomcat9SimpleWarProjectIT verify`

There are some hardcoded integration-test ports, including HTTP 1973 and AJP 2001, so local port conflicts are possible.

Releasing to Maven Central
--------------------------

Central publishing uses the `release-central` profile and the Sonatype Central Publishing Maven plugin.

Before releasing, ensure:

* `~/.m2/settings.xml` contains a `server` entry with id `central`
* `MAVEN_CENTRAL_USER` is set
* `MAVEN_CENTRAL_PASSWORD` is set
* `GPG_PASS` is set
* the current project version is a `-SNAPSHOT`
* the Tomcat 9 dependency version is set to the target release line, currently `9.0.120`
* `mvn -pl tomcat9-maven-plugin -am test` passes
* `mvn -pl tomcat9-maven-plugin -am -Prun-its verify` passes

The standard release flow is:

1. `mvn release:clean`
2. `mvn release:prepare -DreleaseVersion=<release-version> -DdevelopmentVersion=<next-snapshot-version>`
3. `mvn release:perform`

Example for the current line:

`mvn release:prepare -DreleaseVersion=3.0.120 -DdevelopmentVersion=3.0.121-SNAPSHOT`

Testing staged Tomcat artifacts
-------------------------------

To test staged Tomcat artifacts for a vote process:

* activate the `tc-staging` profile
* pass the staging repository as `-DtcStagedReleaseUrl=...`
* pass the Tomcat version as `-Dtomcat9Version=...`

Example:

`mvn -pl tomcat9-maven-plugin -am -Prun-its -Ptc-staging clean verify -DtcStagedReleaseUrl=stagingrepositoryurl -Dtomcat9Version=9.x`
