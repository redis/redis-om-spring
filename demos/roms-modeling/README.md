# Redis OM Spring Demo - Modeling

This demo showcases how to use Redis OM Spring for modeling your application domain entities and interacting with them using Spring repositories.

## Features

- Demonstrates how to model domain entities for Redis with proper annotations
- Shows how to create and use Redis OM repositories 
- Provides examples of querying and filtering data using Redis OM's powerful search capabilities
- Illustrates best practices for working with Redis OM Spring

## Key Components

The demo includes a simple text data management application with the following components:

- **Model**: Defines the `TextData` entity with search-indexable fields
- **Repository**: Provides query methods for the TextData entity
- **Service**: Implements business logic for storing and retrieving text data
- **Controller**: Exposes REST endpoints for interacting with the application

## Running the Demo

To run this demo:

```bash
./mvnw install -Dmaven.test.skip && ./mvnw spring-boot:run -pl demos/roms-modeling
```

## API Endpoints

The application exposes the following REST endpoints:

- **POST /textdata** - Create a new text data entry
- **GET /textdata** - Get all text data entries
- **GET /textdata/{id}** - Get text data by ID
- **PUT /textdata/{id}** - Update text data
- **DELETE /textdata/{id}** - Delete text data
- **GET /textdata/search?query=** - Search text data by content

## Configuration

The application is configured to connect to Redis using the default settings (localhost:6379).
You can modify the Redis connection settings in the `application.properties` file.