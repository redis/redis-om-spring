# Redis OM Spring Documentation Outline

This outline maps the documentation structure and provides a description of what each page covers.

## Introduction

- **overview.adoc**: High-level overview of Redis OM Spring, its purpose, and key features
- **why-roms.adoc**: Benefits and reasons for using Redis OM Spring compared to alternatives

## Getting Started

- **setup.adoc**: Installation instructions, prerequisites, dependency configuration for Maven/Gradle
- **configuration.adoc**: Configuration options, Redis connections, application properties
- **quickstart.adoc**: Quick start tutorial with basic examples to get up and running

## Core Concepts

- **data-models.adoc**: Overview of Redis data models (hashes, JSON) supported by Redis OM Spring
- **object-mapping.adoc**: Explanation of how Java objects map to Redis data structures
- **entity-ids.adoc**: Entity ID management and ULID generation in Redis OM Spring

## Redis Hash Mapping

- **hash-mappings.adoc**: Basic Redis hash mapping and usage with Spring entities
- **enhanced-hash.adoc**: Enhanced Redis hash capabilities with search and indexing
- **hash-repositories.adoc**: CRUD repositories for Redis hash data model

## Redis JSON Mapping

- **json_mappings.adoc**: Redis JSON basics and advantages over hash storage
- **document-annotation.adoc**: The @Document annotation and its configuration options
- **json-repositories.adoc**: Document repositories for working with JSON documents

## Indexing and Search

- **search.adoc**: Redis Query Engine integration for full-text search capabilities
- **index-annotations.adoc**: Annotations for defining searchable fields (@Indexed, @TextIndexed, etc.)
- **index-creation.adoc**: Index creation processes, modes, and configuration
- **multi-tenant-support.adoc**: Multi-tenant support with dynamic index naming via SpEL expressions, RedisIndexContext, and IndexResolver
- **index-migration.adoc**: Index migration strategies (Blue-Green, Dual-Write, In-Place) for zero-downtime schema changes
- **ephemeral-indexes.adoc**: Ephemeral indexes with TTL for temporary, auto-expiring indexes
- **multilanguage.adoc**: Multilanguage support for search and indexing

## Query Capabilities

- **repository-queries.adoc**: Repository query methods using method name conventions
- **query-annotation.adoc**: Using @Query annotation for custom Redis query syntax
- **qbe.adoc**: Query By Example (QBE) pattern for dynamic queries

## Entity Streams

- **entity-streams.adoc**: The fluent EntityStream API for querying and manipulating data
- **metamodel.adoc**: Type-safe queries using generated metamodel classes

## Aggregations

- **entity-streams-aggregations.adoc**: Aggregation capabilities with Entity Streams API

## AI and Vector Search

- **ai-overview.adoc**: Overview of AI integration in Redis OM Spring
- **vector-search.adoc**: Vector similarity search capabilities and configuration
- **embedding-providers.adoc**: Text/image embedding providers (OpenAI, Azure, etc.)
- **azure-openai.adoc**: Azure OpenAI integration with Entra ID support
- **vector-search-examples.adoc**: Examples of vector search applications

## Advanced Features

- **autocomplete.adoc**: Implementing autocomplete functionality
- **bloom-and-cuckoo.adoc**: Using Bloom and Cuckoo filters for probabilistic data
- **references.adoc**: Handling references between entities
- **auditing.adoc**: Entity auditing capabilities (created/modified timestamps)
- **optimistic-locking.adoc**: Optimistic locking for concurrent operations
- **time-to-live.adoc**: Time To Live (TTL) for automatic expiration
- **keyspaces.adoc**: Working with Redis keyspaces and namespaces

## Enterprise Features

- **sentinel.adoc**: Redis Sentinel support for high availability

## Additional/Uncategorized Files

- **index.adoc**: Main landing page redirecting to overview
- **redis-repositories.adoc**: General information about Redis repositories

## Key Documentation Themes

1. **Declarative Approach**: Most Redis OM Spring features are declarative through annotations
2. **Automatic Index Management**: Indexes are primarily managed via annotations, not programmatic API
3. **Redis 8 Standardization**: Query Engine and JSON are standard components in Redis 8
4. **Multiple Repository Types**: Both hash-based and JSON document repositories
5. **Type-Safe Queries**: Metamodel and EntityStream APIs for type-safe operations

## Potential Improvement Areas

1. Better explanation of how indexes are automatically created from annotations without manual intervention
2. Clearer examples from actual codebase usage patterns
3. Consistent terminology regarding "Redis Query Engine" (formerly RediSearch)
4. Improved code examples that match realistic usage patterns
5. More information about integration with Redis 8 features
