# Redis OM Spring Multi-Tenant Demo

This demo showcases the **Dynamic Indexing** features of Redis OM Spring, enabling multi-tenant SaaS applications with complete data isolation.

## Features Demonstrated

### 1. SpEL-based Dynamic Index Naming

Use Spring Expression Language (SpEL) in `@IndexingOptions` annotations:

```java
@Document
@IndexingOptions(
    indexName = "products_#{@tenantService.getCurrentTenant()}_idx",
    keyPrefix = "#{@tenantService.getCurrentTenant()}:products:"
)
public class Product { ... }
```

### 2. Multi-Tenant Index Isolation

Each tenant gets their own:
- **Search Index**: `products_acme_idx`, `products_globex_idx`
- **Key Prefix**: `acme:products:*`, `globex:products:*`
- **Complete Data Isolation**: Searches only return tenant's own data

### 3. Environment-based Configuration

Configure indexes based on deployment environment:

```java
@IndexingOptions(
    indexName = "orders_#{@environment.getProperty('app.environment')}_idx"
)
```

### 4. Ephemeral Indexes with TTL

Create temporary indexes that auto-delete:

```java
ephemeralIndexService.createEphemeralIndex(
    Product.class,
    "temp_analytics_idx",
    Duration.ofMinutes(30)
);
```

### 5. Index Migration & Aliasing

Zero-downtime schema migrations using Redis aliases:

```java
// Blue-Green deployment
indexer.createAlias("products_v2_idx", "products");
indexer.updateAlias("products_v1_idx", "products_v2_idx", "products");
```

### 6. Bulk Index Operations

Manage all indexes at once:

```java
indexer.createIndexes();  // Create all registered indexes
indexer.dropIndexes();    // Drop all managed indexes
indexer.listIndexes();    // List all index names
```

## Running the Demo

### Prerequisites

1. Redis Stack running on localhost:6379
   ```bash
   docker run -p 6379:6379 redis/redis-stack
   ```

2. Build the project:
   ```bash
   ./gradlew build -x test
   ```

### Start the Application

```bash
./gradlew :demos:roms-multitenant:bootRun
```

Watch the console for a visual walkthrough of all features!

## REST API

### Product Operations (Tenant-Scoped)

```bash
# Create a product for tenant "acme"
curl -X POST http://localhost:8080/api/tenants/acme/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Widget Pro",
    "description": "Professional grade widget",
    "category": "Electronics",
    "sku": "WDG-PRO-001",
    "price": 299.99,
    "stockQuantity": 50,
    "active": true
  }'

# List products for tenant "acme"
curl http://localhost:8080/api/tenants/acme/products

# Search products
curl "http://localhost:8080/api/tenants/acme/products/search?q=widget"

# Find by category
curl http://localhost:8080/api/tenants/acme/products/category/Electronics

# Find by price range
curl "http://localhost:8080/api/tenants/acme/products/price-range?minPrice=100&maxPrice=500"
```

### Admin Operations

```bash
# List all managed indexes
curl http://localhost:8080/api/admin/indexes

# Create all registered indexes
curl -X POST http://localhost:8080/api/admin/indexes/create-all

# Create index for specific tenant
curl -X POST http://localhost:8080/api/admin/tenants/acme/indexes

# Check if tenant index exists
curl http://localhost:8080/api/admin/tenants/acme/indexes/exists

# Create ephemeral index (60 second TTL)
curl -X POST http://localhost:8080/api/admin/indexes/ephemeral \
  -H "Content-Type: application/json" \
  -d '{"indexName": "temp_batch_idx", "ttlSeconds": 60}'

# Create index alias
curl -X POST http://localhost:8080/api/admin/indexes/products_acme_idx/alias/products

# Remove index alias
curl -X DELETE http://localhost:8080/api/admin/indexes/products_acme_idx/alias/products
```

## Multi-Tenant Usage Pattern

```java
@Service
public class ProductService {

    @Autowired
    private TenantService tenantService;

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getProductsForTenant(String tenantId) {
        tenantService.setCurrentTenant(tenantId);
        try {
            return productRepository.findAll();
        } finally {
            tenantService.clearTenant();
        }
    }
}
```

## Configuration

### application.yaml

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379

app:
  environment: development  # Used in environment-based index names
  version: 1.0.0
```

## Key Classes

| Class | Description |
|-------|-------------|
| `TenantService` | ThreadLocal tenant context management |
| `Product` | Entity with dynamic index annotations |
| `ProductController` | Tenant-scoped REST operations |
| `AdminController` | Index management endpoints |
| `DemoRunner` | Visual feature walkthrough |

## Learn More

- [Dynamic Indexing Design Document](https://github.com/redis/redis-om-spring/wiki/Dynamic-Indexing-Feature-Design)
- [Redis OM Spring Documentation](https://github.com/redis/redis-om-spring)
