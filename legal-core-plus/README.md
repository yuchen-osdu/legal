# legal-core-plus

legal-core-plus is a Spring Boot service that hosts CRUD APIs that enable management of legal tags within the OSDU R2 ecosystem.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

- [Maven 3.8.0+](https://maven.apache.org/download.cgi)
- [JDK17](https://adoptopenjdk.net/)
- [Lombok 1.28 or later](https://projectlombok.org/setup/maven)

# Features of implementation

This is a universal solution created using EPAM OSM, OQM and OBM mappers technology.
It allows you to work with various implementations of KV stores, Message brokers and Blob stores.

## Limitations of the current version

In the current version, the mappers are equipped with several drivers to the stores:

- OSM (mapper for KV-data): Postgres
- OBM (mapper to Blob stores): SeaweedFS
- OQM (mapper to message brokers): RabbitMQ

## Extensibility

To use any other store or message broker, implement a driver for it. With an extensible set of drivers, the solution is unrestrictedly universal and portable without modification to the main code.

Mappers support "multitenancy" with flexibility in how it is implemented.
They switch between datasources of different tenants due to the work of a bunch of classes that implement the following interfaces:

- Destination - takes a description of the current context, e.g., "data-partition-id = opendes"
- DestinationResolver – accepts Destination, finds the resource, connects, and returns Resolution
- DestinationResolution – contains a ready-made connection, the mapper uses it to get to data

## Mapper tuning mechanisms

This service uses specific implementations of DestinationResolvers based on the tenant information provided by the OSDU Partition service.

- for Postgres: osm/PgTenantDestinationResolver.java
- for SeaweedFS: obm/S3DestinationResolver.java
- for RabbitMQ: oqm/MqTenantOqmDestinationResolver.java

#### Their algorithms are as follows

- incoming Destination carries data-partition-id
- resolver accesses the Partition service and gets PartitionInfo
- from PartitionInfo resolver retrieves properties for the connection: URL, username, password etc.
- resolver creates a data source, connects to the resource, remembers the datasource
- resolver gives the datasource to the mapper in the Resolution object

# Configuration

## Service Configuration

### Baremetal

[Baremetal service configuration](docs/baremetal/README.md)

Check that maven is installed:

```bash
$ mvn --version
Apache Maven 3.8.7
Maven home: /usr/share/maven
Java version: 17.0.7
...
```

You may need to configure access to the remote maven repository that holds the OSDU dependencies. This file should live within `~/.mvn/community-maven.settings.xml`:

```bash
$ cat ~/.m2/settings.xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>community-maven-via-private-token</id>
            <!-- Treat this auth token like a password. Do not share it with anyone, including Microsoft support. -->
            <!-- The generated token expires on or before 11/14/2019 -->
             <configuration>
              <httpHeaders>
                  <property>
                      <name>Private-Token</name>
                      <value>${env.COMMUNITY_MAVEN_TOKEN}</value>
                  </property>
              </httpHeaders>
             </configuration>
        </server>
    </servers>
</settings>
```
- Navigate to Legal service's root folder and run:
 
```bash
mvn clean install   
```

- If you wish to see the coverage report then go to testing/target/site/jacoco-aggregate and open index.html

- If you wish to build the project without running tests

```bash
mvn clean install -DskipTests
```

- Drivers should be downloaded.
```bash
    - mvn dependency:copy -DrepoUrl=$OSM_PACKAGE_REGISTRY_URL -Dartifact="org.opengroup.osdu:os-osm-postgres:$OSM_VERSION:jar:plugin" -Dtransitive=false -DoutputDirectory="./tmp"
    - mvn dependency:copy -DrepoUrl=$OBM_PACKAGE_REGISTRY_URL -Dartifact="org.opengroup.osdu:os-obm-s3:$OBM_VERSION:jar:plugin" -Dtransitive=false -DoutputDirectory="./tmp"
    - mvn dependency:copy -DrepoUrl=$OQM_PACKAGE_REGISRTY_URL -Dartifact="org.opengroup.osdu:os-oqm-rabbitmq:$OQM_VERSION:jar:plugin" -Dtransitive=false -DoutputDirectory="./tmp"

```
After configuring your environment as specified above, you can follow these steps to build and run the application. These steps should be invoked from the *repository root.*

```bash
cd legal-core-plus/target 
java --add-opens java.base/java.lang=ALL-UNNAMED \
         --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
         -Djava.security.egd=file:/dev/./urandom \
         -Dserver.port=${PORT} \
         -Dlog4j.formatMsgNoLookups=true \
         -Dloader.path=tmp/ \
         -Dloader.debug=true \
         -Dloader.main=org.opengroup.osdu.legal.LegalApplication \
         -jar /legal-core-plus-${Version}-spring-boot.jar
```

## Testing

Navigate to legal service's root folder and run all the tests:

```bash
# build + install integration test core
$ (cd testing/legal-test-core/ && mvn clean install)
```

### Running E2E Tests

This section describes how to run cloud OSDU E2E tests.

### Baremetal test configuration

[Baremetal service configuration](docs/baremetal/README.md)


## Licence

Copyright © Google LLC
Copyright © EPAM Systems

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
