# legal-gc

legal-gc is a Spring Boot service that hosts CRUD APIs that enable management of legal tags within the OSDU R2 ecosystem.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

- [Maven 3.8.0+](https://maven.apache.org/download.cgi)
- [JDK17](https://adoptopenjdk.net/)
- [Lombok 1.28 or later](https://projectlombok.org/setup/maven)
- [GCloud SDK with java (latest version)](https://cloud.google.com/sdk/docs/install)

# Features of implementation

This is a universal solution created using EPAM OSM and OBM mappers technology.
It allows you to work with various implementations of KV stores and Blob stores.

## Limitations of the current version

In the current version, the mappers are equipped with several drivers to the stores:

- OSM (mapper for KV-data): Google Datastore; Postgres
- OBM (mapper to Blob stores): Google Cloud Storage (GCS); MinIO
- OQM (mapper to message brokers): Google PubSub; RabbitMQ

## Extensibility

To use any other store or message broker, implement a driver for it. With an extensible set of drivers, the solution is unrestrictedly universal and portable without modification to the main code.

Mappers support "multitenancy" with flexibility in how it is implemented.
They switch between datasources of different tenants due to the work of a bunch of classes that implement the following interfaces:

- Destination - takes a description of the current context, e.g., "data-partition-id = opendes"
- DestinationResolver – accepts Destination, finds the resource, connects, and returns Resolution
- DestinationResolution – contains a ready-made connection, the mapper uses it to get to data

## Mapper tuning mechanisms

This service uses specific implementations of DestinationResolvers based on the tenant information provided by the OSDU Partition service.

- for Google Datastore: osm/DsTenantDestinationResolver.java
- for Postgres: osm/PgTenantDestinationResolver.java
- for MinIO: obm/MinioDestinationResolver.java
- for RabbitMQ: oqm/MqTenantOqmDestinationResolver.java

#### Their algorithms are as follows

- incoming Destination carries data-partition-id
- resolver accesses the Partition service and gets PartitionInfo
- from PartitionInfo resolver retrieves properties for the connection: URL, username, password etc.
- resolver creates a data source, connects to the resource, remembers the datasource
- resolver gives the datasource to the mapper in the Resolution object
- Google Cloud resolvers do not receive special properties from the Partition service for connection,
  because the location of the resources is unambiguously known - they are in the Google Cloud project.
  And credentials are also not needed - access to data is made on behalf of the Google Identity SA
  under which the service itself is launched. Therefore, resolver takes only
  the value of the **projectId** property from PartitionInfo and uses it to connect to a resource
  in the corresponding Google Cloud project.

# Configuration

## Service Configuration

### Google Cloud

[Google Cloud service configuration](docs/gc/README.md)

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

- Update the Google cloud SDK to the latest version:

```bash
gcloud components update
```

- Set Google Project Id:

```bash
gcloud config set project <YOUR-PROJECT-ID>
```

- Perform a basic authentication in the selected project:

```bash
gcloud auth application-default login
```

- Navigate to search service's root folder and run:

```bash
mvn jetty:run
## Testing
* Navigate to legal service's root folder and run:
 
```bash
mvn clean install   
```

- If you wish to see the coverage report then go to testing/target/site/jacoco-aggregate and open index.html

- If you wish to build the project without running tests

```bash
mvn clean install -DskipTests
```

After configuring your environment as specified above, you can follow these steps to build and run the application. These steps should be invoked from the *repository root.*


```bash
CMD java --add-opens java.base/java.lang=ALL-UNNAMED \
         --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
         -Dloader.main=org.opengroup.osdu.legal.LegalApplication \
         -jar /app/legal-${PROVIDER_NAME}.jar
```

## Testing

Navigate to legal service's root folder and run all the tests:

```bash
# build + install integration test core
$ (cd testing/legal-test-core/ && mvn clean install)
```

### Running E2E Tests

This section describes how to run cloud OSDU E2E tests.

### Google Cloud test configuration

[Google Cloud service configuration](docs/google_cloud/README.md)

## Deployment

- Data-Lake Legal Google Cloud Endpoints on App Engine Flex environment
  - Deploy

    ```sh
    mvn appengine:deploy -pl org.opengroup.osdu.legal:legal -amd
    ```

  - If you wish to deploy the search service without running tests

    ```sh
    mvn appengine:deploy -pl org.opengroup.osdu.legal:legal -amd -DskipTests
    ```

or

- Google Documentation: <https://cloud.google.com/cloud-build/docs/deploying-builds/deploy-appengine>

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
