# Redis OM Spring Demo - Hybrid Search

This demo showcases Redis OM Spring's **Hybrid Search** capability, which combines full-text search (BM25) with vector similarity search to provide powerful semantic search functionality.

## What is Hybrid Search?

Hybrid search combines two complementary search approaches:

1. **Full-Text Search (BM25)**: Traditional keyword-based search that matches query terms against document text
2. **Vector Similarity Search**: Semantic search using embeddings that captures meaning and context

The results are scored using a weighted combination:

```
hybrid_score = (1 - alpha) × text_score + alpha × vector_similarity
```

Where:
- `alpha = 0.0` → Pure text search (BM25 only)
- `alpha = 0.5` → Balanced hybrid search
- `alpha = 0.7` → Default, favoring semantic similarity
- `alpha = 1.0` → Pure vector search (semantic only)

## Features

This demo demonstrates:

- **Hybrid search** combining text and vector similarity
- **Filtered hybrid search** (e.g., category + price range filters)
- **Comparison** between text-only, vector-only, and hybrid search
- **Alpha parameter tuning** to adjust text vs. vector weighting
- **Real product data** with mock embeddings

## Prerequisites

- Java 21+
- Redis Stack 7.4+ running locally (for hybrid search support)
- Gradle

## Running Redis Stack

Start Redis Stack using Docker:

```bash
docker run -d -p 6379:6379 -p 8001:8001 redis/redis-stack:latest
```

Or use Docker Compose from the project root:

```bash
docker compose up
```

## Running the Demo

From the project root:

```bash
./mvnw install -Dmaven.test.skip && ./mvnw spring-boot:run -pl demos/roms-hybrid
```

Or using Gradle:

```bash
./gradlew :demos:roms-hybrid:bootRun
```

The application will:
1. Start on port 8080
2. Connect to Redis on localhost:6379
3. Create search indexes
4. Load sample product data

## API Endpoints

### Basic Operations

**Get all products:**
```bash
curl http://localhost:8080/api/products
```

**Get product by ID:**
```bash
curl http://localhost:8080/api/products/elec-001
```

**Get products by category:**
```bash
curl http://localhost:8080/api/products/category/Electronics
```

### Search Operations

**1. Hybrid Search (Recommended)**

Combines text and semantic search for best results:

```bash
curl -X POST "http://localhost:8080/api/products/hybrid-search?text=wireless%20headphones%20for%20music&alpha=0.7&limit=5" \
  -H "Content-Type: application/json" \
  -d '[0.8, 0.2, 0.7, ...]'  # 384-dimensional embedding array as request body
```

**With filters:**
```bash
curl -X POST "http://localhost:8080/api/products/hybrid-search?text=headphones&alpha=0.7&category=Electronics&minPrice=100&maxPrice=300&limit=10" \
  -H "Content-Type: application/json" \
  -d @embedding.json
```

**2. Text-Only Search**

Traditional full-text search:

```bash
curl "http://localhost:8080/api/products/search/text?query=wireless%20headphones&limit=5"
```

**3. Vector-Only Search**

Pure semantic similarity:

```bash
curl -X POST http://localhost:8080/api/products/search/semantic \
  -H "Content-Type: application/json" \
  -d @embedding.json
```

## Sample Data

The demo includes 12 products across 4 categories:

- **Electronics**: Headphones, TV, Laptop, Mouse
- **Home & Kitchen**: Coffee Maker, Cookware, Robot Vacuum
- **Sports & Outdoors**: Yoga Mat, Running Shoes, Tent
- **Books**: Redis Guide, Machine Learning Basics

Each product has:
- Full-text searchable description
- 384-dimensional embedding (mock data based on category/features)
- Filterable attributes (category, price, brand, stock)

## Example Queries

### Find "wireless audio devices" (semantic understanding)

```bash
# Hybrid search will find "Wireless Bluetooth Headphones" even though
# the query says "audio devices" and product says "headphones"
curl -X POST "http://localhost:8080/api/products/hybrid-search?text=audio%20devices&alpha=0.7&limit=5" \
  -H "Content-Type: application/json" \
  -d @headphone_embedding.json
```

### Find Electronics under $500

```bash
curl -X POST "http://localhost:8080/api/products/hybrid-search?text=electronics&alpha=0.5&category=Electronics&maxPrice=500&limit=10" \
  -H "Content-Type: application/json" \
  -d @electronics_embedding.json
```

### Compare Search Methods

Try the same query with different alpha values:
- `alpha=0.0` - Pure text matching (may miss semantic relationships)
- `alpha=0.5` - Balanced (good for most use cases)
- `alpha=1.0` - Pure semantic (may miss exact keyword matches)

## Code Examples

### Using EntityStream API

```java
@Autowired
private EntityStream entityStream;

// Hybrid search with filters
List<Product> products = entityStream.of(Product.class)
    .filter(Product$.CATEGORY.eq("Electronics"))
    .filter(Product$.PRICE.between(100.0, 500.0))
    .hybridSearch(
        "wireless headphones",
        Product$.DESCRIPTION,
        queryEmbedding,
        Product$.EMBEDDING,
        0.7f  // 70% vector, 30% text
    )
    .limit(10)
    .collect(Collectors.toList());
```

### Adjusting Search Behavior

```java
// More text-focused (good for specific keyword matching)
.hybridSearch(text, textField, vector, vectorField, 0.3f)

// Balanced approach
.hybridSearch(text, textField, vector, vectorField, 0.5f)

// More semantic-focused (good for conceptual similarity)
.hybridSearch(text, textField, vector, vectorField, 0.7f)
```

## How It Works

1. **Indexing**: Redis OM Spring creates a search index with:
   - Full-text index on `description` field
   - Vector index on `embedding` field (384D, COSINE distance)
   - Tag/numeric indexes on filterable fields

2. **Query Execution**: Uses RedisVL's `HybridQuery` which:
   - Performs FT.AGGREGATE with text and vector scoring
   - Combines scores using the alpha parameter
   - Applies filters using Redis query syntax
   - Returns results sorted by hybrid score

3. **Result Ranking**: Documents are scored as:
   ```
   hybrid_score = (1-α) × BM25_score + α × (1 - cosine_distance)
   ```

## Real-World Applications

Hybrid search is ideal for:

- **E-commerce**: "Find running shoes similar to these" + "for marathon training"
- **Document Search**: "Contract documents about pricing" (semantic + keyword)
- **Content Discovery**: "Articles like this one" + "about machine learning"
- **Job Matching**: "Software engineer" + similar to candidate profile
- **Customer Support**: "How to reset password" (handles variations semantically)

## Technology Stack

- **Redis Stack 7.4+**: Provides RediSearch with hybrid query support
- **Redis OM Spring**: Object mapping and search capabilities
- **RedisVL for Java**: Hybrid query implementation
- **Spring Boot 3.x**: Application framework
- **Lombok**: Reduces boilerplate code

## Learn More

- [Redis OM Spring Documentation](https://redis.io/docs/clients/om-clients/stack-spring/)
- [RediSearch Hybrid Queries](https://redis.io/docs/interact/search-and-query/advanced-concepts/hybrid-queries/)
- [Vector Similarity Search](https://redis.io/docs/interact/search-and-query/advanced-concepts/vectors/)

## License

This demo is part of Redis OM Spring and follows the same license.
