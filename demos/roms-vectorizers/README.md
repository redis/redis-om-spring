# Redis OM Spring Demo - Vectorizers

This demo showcases the use of vectorizers in Redis OM Spring, demonstrating how to automatically generate embeddings for text data and perform vector similarity search.

## Features

- Text vectorization using Redis OM Spring's built-in vectorization capabilities
- Vector similarity search (VSS) for semantic text matching
- Integration with AI/ML models for embedding generation
- REST API for interacting with vectorized data

## Key Components

The demo includes:

- **Model**: `TextData` entity with the `@Vectorize` annotation to automatically generate embeddings
- **Repository**: Repository interface for querying and storing vectorized data
- **Service**: Service layer handling the vectorization and search logic
- **Controller**: REST controller exposing endpoints for data manipulation and vector search

## Vector Similarity Search

Vector Similarity Search allows finding semantically similar content even when exact text matches don't exist. This is particularly useful for:

- Semantic search applications
- Content recommendation systems
- Natural language understanding
- Finding conceptually related items

## Running the Demo

To run this demo:

```bash
./mvnw install -Dmaven.test.skip && ./mvnw spring-boot:run -pl demos/roms-vectorizers
```

## API Endpoints

The application exposes the following REST endpoints:

- **POST /textdata** - Create new text data with automatic vectorization
- **GET /textdata** - Retrieve all text data entries
- **GET /textdata/{id}** - Get a specific text data entry by ID
- **GET /textdata/search** - Find semantically similar text using vector search

## Configuration

The application uses Redis OM Spring AI module for vectorization. The default embedding provider is configured in the `application.properties` file.

For complete functionality, ensure your Redis instance has the RediSearch module installed and enabled.