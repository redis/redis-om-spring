# Redis OM Spring Demo - Documents

This demo showcases the use of Redis OM Spring's `@Document` annotation for storing and retrieving JSON documents in Redis.

## Features

- Demonstrates how to use the `@Document` annotation to map Java objects to Redis JSON documents
- Shows how to create repositories that extend `RedisDocumentRepository`
- Provides examples of querying documents using both method names and explicit queries
- Includes REST API endpoints for CRUD operations

## Key Components

The demo includes three domain models:
- Company
- Person
- Event

Each model is mapped as a Redis JSON document and has corresponding repository and controller classes.

## Swagger Documentation

The API is documented using Swagger UI, which can be accessed at:
http://localhost:8080/swagger-ui/index.html

## Running the Demo

To run this demo:

```bash
./mvnw install -Dmaven.test.skip && ./mvnw spring-boot:run -pl demos/roms-documents
```

## Testing

The demo includes tests demonstrating how to use Redis OM Spring in a test environment.