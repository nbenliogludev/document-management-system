# Document Generator CLI

A standalone Java CLI application to rapidly generate documents via the REST API for `document-management-service`.

## Features
- Pure Java 21 `HttpClient` implementation. No heavy frameworks (Spring Boot) required to keep memory and execution overhead low.
- Command-line customizable document boundaries (title prefix, author names, execution delays, counts).
- Built-in summary tracking to output error/success analytics at runtime conclusion.

## Requirements
- Java 21+
- Maven 3+
- Run the `document-management-service` API container or local application. 

## Build Instructions
Navigate to the root `document-generator-cli` directory:

```sh
cd document-generator-cli
mvn clean package
```
This produces an executable fat jar `document-generator-cli-1.0-SNAPSHOT.jar` inside the `/target` structure using `maven-shade-plugin`.

## Configuration
Inside `src/main/resources/generator.properties`, default values are available:

```properties
generator.base-url=http://localhost:8080
generator.count=100
generator.delay-ms=0
generator.author-prefix=Generator
generator.title-prefix=Document
generator.number-prefix=DOC
```

## Running the Application
Launch the standalone application natively:
```sh
java -jar target/document-generator-cli-1.0-SNAPSHOT.jar
```

You can optionally override `generator.properties` behavior inline via arguments:
```sh
java -jar target/document-generator-cli-1.0-SNAPSHOT.jar --base-url=http://localhost:8080 --count=1000 --delay-ms=50 --author-prefix=LoadTester
```

## Expected Console Output

```
Starting Document Generator CLI...
====== Configuration ======
Base URL:      http://localhost:8080
Total Count:   10
Delay (ms):    0
Author Prefix: LoadTester
Title Prefix:  Document
Number Prefix: DOC
===========================

[1/10] Generating document 'Document #1'... SUCCESS
[2/10] Generating document 'Document #2'... SUCCESS
...
====== Generation Summary ======
Total Attempts: 10
Successful:     10
Failed:         0
Elapsed Time:   0.150 s
================================
```
