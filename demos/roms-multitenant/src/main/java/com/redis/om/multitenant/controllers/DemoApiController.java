package com.redis.om.multitenant.controllers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.redis.om.multitenant.domain.Product;
import com.redis.om.multitenant.repositories.ProductRepository;
import com.redis.om.multitenant.services.TenantService;
import com.redis.om.spring.indexing.EphemeralIndexService;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.ops.search.SearchOperations;

import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;

/**
 * Demo API controller for the interactive web UI.
 *
 * <p>Provides endpoints for demonstrating dynamic indexing features:
 * <ul>
 * <li>Multi-tenant index operations
 * <li>SpEL expression resolution
 * <li>Ephemeral indexes with TTL
 * <li>Index migration with aliasing
 * <li>Bulk index operations
 * </ul>
 */
@RestController
@RequestMapping(
  "/api/demo"
)
public class DemoApiController {

  @Autowired
  private RediSearchIndexer indexer;

  @Autowired
  private TenantService tenantService;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private StringRedisTemplate redisTemplate;

  @Autowired
  private RedisModulesOperations<String> modulesOperations;

  @Autowired
  private Environment environment;

  @Autowired(
      required = false
  )
  private EphemeralIndexService ephemeralIndexService;

  // Track ephemeral indexes for demo UI
  private final Map<String, Long> ephemeralIndexTtls = new ConcurrentHashMap<>();

  // Track migration state for demo
  private boolean v1IndexExists = false;
  private boolean v2IndexExists = false;

  /**
   * Get the dynamic index name for the Product entity based on current tenant context.
   * This method uses the framework's getIndexName which re-evaluates SpEL expressions at runtime.
   */
  private String getProductIndexName() {
    return indexer.getIndexName(Product.class);
  }

  /**
   * Get the dynamic key prefix for the Product entity based on current tenant context.
   * This method uses the framework's getKeyspacePrefix which re-evaluates SpEL expressions at runtime.
   */
  private String getProductKeyPrefix() {
    return indexer.getKeyspacePrefix(Product.class);
  }

  // =============================================
  // Database Reset
  // =============================================

  /**
   * Reset the database - drop all indexes and delete all keys.
   */
  @PostMapping(
    "/reset"
  )
  public ResponseEntity<Map<String, Object>> resetDatabase() {
    Map<String, Object> response = new HashMap<>();

    try {
      // Drop all managed indexes
      indexer.dropIndexes();

      // Delete keys for known tenants
      String[] tenants = { "acme_corp", "globex_inc", "initech", "default", "demo_tenant", "ephemeral_demo",
          "migration_v1", "migration_v2" };
      int deletedKeys = 0;

      for (String tenant : tenants) {
        // Delete product keys for each tenant
        Set<String> keys = redisTemplate.keys(tenant + ":products:*");
        if (keys != null && !keys.isEmpty()) {
          redisTemplate.delete(keys);
          deletedKeys += keys.size();
        }
      }

      // Drop tenant indexes that may exist
      for (String tenant : tenants) {
        tenantService.setCurrentTenant(tenant);
        try {
          if (indexer.indexExistsFor(Product.class)) {
            indexer.dropIndexFor(Product.class);
          }
        } catch (Exception ignored) {
        } finally {
          tenantService.clearTenant();
        }
      }

      // Reset migration state
      v1IndexExists = false;
      v2IndexExists = false;
      ephemeralIndexTtls.clear();

      response.put("success", true);
      response.put("message", "Database reset complete");
      response.put("deletedKeys", deletedKeys);

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      response.put("success", false);
      response.put("error", e.getMessage());
      return ResponseEntity.internalServerError().body(response);
    }
  }

  // =============================================
  // Multi-Tenancy Operations
  // =============================================

  /**
   * Get index info for a tenant.
   */
  @GetMapping(
    "/tenants/{tenant}/index-info"
  )
  public ResponseEntity<Map<String, Object>> getTenantIndexInfo(@PathVariable String tenant) {
    tenantService.setCurrentTenant(tenant);
    try {
      String indexName = getProductIndexName();
      String keyPrefix = getProductKeyPrefix();

      // Check if index exists by trying to get its info
      boolean exists = false;
      try {
        SearchOperations<String> searchOps = modulesOperations.opsForSearch(indexName);
        searchOps.search(new Query("*").limit(0, 0));
        exists = true;
      } catch (Exception e) {
        exists = false;
      }

      Map<String, Object> response = new HashMap<>();
      response.put("tenant", tenant);
      response.put("indexName", indexName);
      response.put("keyPrefix", keyPrefix);
      response.put("exists", exists);
      return ResponseEntity.ok(response);
    } finally {
      tenantService.clearTenant();
    }
  }

  /**
   * Create index for a tenant.
   */
  @PostMapping(
    "/tenants/{tenant}/index"
  )
  public ResponseEntity<Map<String, Object>> createTenantIndex(@PathVariable String tenant) {
    tenantService.setCurrentTenant(tenant);
    try {
      indexer.createIndexFor(Product.class);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("tenant", tenant);
      // Return dynamically computed values from framework (SpEL re-evaluated at runtime)
      response.put("indexName", getProductIndexName());
      response.put("keyPrefix", getProductKeyPrefix());
      return ResponseEntity.ok(response);
    } finally {
      tenantService.clearTenant();
    }
  }

  /**
   * Add sample products for a tenant.
   * Uses direct JSON operations to store products with tenant-specific key prefix.
   */
  @PostMapping(
    "/tenants/{tenant}/sample-products"
  )
  public ResponseEntity<Map<String, Object>> addSampleProducts(@PathVariable String tenant) {
    tenantService.setCurrentTenant(tenant);
    try {
      // Ensure index exists
      if (!indexer.indexExistsFor(Product.class)) {
        indexer.createIndexFor(Product.class);
      }

      // Create sample products based on tenant
      List<Product> products = getSampleProductsForTenant(tenant);
      // Use dynamically computed key prefix from framework (SpEL re-evaluated at runtime)
      String keyPrefix = getProductKeyPrefix();

      // Save products using direct JSON operations
      JSONOperations<String> jsonOps = modulesOperations.opsForJSON();
      List<Map<String, Object>> saved = new ArrayList<>();

      for (Product product : products) {
        // Generate ULID for the product ID
        String id = com.github.f4b6a3.ulid.UlidCreator.getUlid().toString();
        product.setId(id);

        String key = keyPrefix + id;
        jsonOps.set(key, product);

        Map<String, Object> p = new HashMap<>();
        p.put("id", product.getId());
        p.put("name", product.getName());
        p.put("description", product.getDescription());
        p.put("category", product.getCategory());
        p.put("sku", product.getSku());
        p.put("price", product.getPrice());
        p.put("stockQuantity", product.getStockQuantity());
        p.put("active", product.getActive());
        saved.add(p);
      }

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("tenant", tenant);
      response.put("count", saved.size());
      response.put("products", saved);
      response.put("keyPrefix", keyPrefix);
      response.put("indexName", getProductIndexName());
      return ResponseEntity.ok(response);
    } finally {
      tenantService.clearTenant();
    }
  }

  /**
   * Get all products for a tenant.
   * Uses direct Redis search operations with dynamically computed index name from framework.
   */
  @GetMapping(
    "/tenants/{tenant}/products"
  )
  public ResponseEntity<Map<String, Object>> getTenantProducts(@PathVariable String tenant) {
    Map<String, Object> response = new HashMap<>();
    response.put("tenant", tenant);

    tenantService.setCurrentTenant(tenant);
    try {
      String indexName = getProductIndexName();
      try {
        // Use direct search to get all products
        SearchOperations<String> searchOps = modulesOperations.opsForSearch(indexName);
        SearchResult result = searchOps.search(new Query("*").limit(0, 100));

        List<Map<String, Object>> products = new ArrayList<>();
        JSONOperations<String> jsonOps = modulesOperations.opsForJSON();

        result.getDocuments().forEach(doc -> {
          String key = doc.getId();
          try {
            Product product = jsonOps.get(key, Product.class);
            if (product != null) {
              Map<String, Object> p = new HashMap<>();
              p.put("id", product.getId());
              p.put("name", product.getName());
              p.put("description", product.getDescription());
              p.put("category", product.getCategory());
              p.put("sku", product.getSku());
              p.put("price", product.getPrice());
              p.put("stockQuantity", product.getStockQuantity());
              p.put("active", product.getActive());
              products.add(p);
            }
          } catch (Exception e) {
            // Skip products that can't be deserialized
          }
        });

        response.put("products", products);
        response.put("count", products.size());
        response.put("indexName", indexName);
      } catch (Exception e) {
        // Index doesn't exist
        response.put("products", List.of());
        response.put("count", 0);
        response.put("note", "Index not created yet for tenant: " + tenant);
      }
    } finally {
      tenantService.clearTenant();
    }

    return ResponseEntity.ok(response);
  }

  /**
   * Search products for a tenant.
   * Uses direct Redis search operations with dynamically computed index name from framework.
   */
  @GetMapping(
    "/tenants/{tenant}/products/search"
  )
  public ResponseEntity<Map<String, Object>> searchTenantProducts(@PathVariable String tenant, @RequestParam String q) {
    Map<String, Object> response = new HashMap<>();
    response.put("tenant", tenant);
    response.put("query", q);

    tenantService.setCurrentTenant(tenant);
    try {
      String indexName = getProductIndexName();
      try {
        // Use direct search with the query
        SearchOperations<String> searchOps = modulesOperations.opsForSearch(indexName);
        SearchResult result = searchOps.search(new Query("@name:" + q + "*").limit(0, 100));

        List<Map<String, Object>> products = new ArrayList<>();
        JSONOperations<String> jsonOps = modulesOperations.opsForJSON();

        result.getDocuments().forEach(doc -> {
          String key = doc.getId();
          try {
            Product product = jsonOps.get(key, Product.class);
            if (product != null) {
              Map<String, Object> p = new HashMap<>();
              p.put("id", product.getId());
              p.put("name", product.getName());
              p.put("description", product.getDescription());
              p.put("category", product.getCategory());
              p.put("sku", product.getSku());
              p.put("price", product.getPrice());
              p.put("stockQuantity", product.getStockQuantity());
              p.put("active", product.getActive());
              products.add(p);
            }
          } catch (Exception e) {
            // Skip products that can't be deserialized
          }
        });

        response.put("products", products);
        response.put("count", products.size());
        response.put("indexName", indexName);
      } catch (Exception e) {
        // Index doesn't exist
        response.put("products", List.of());
        response.put("count", 0);
        response.put("note", "Index not created yet for tenant: " + tenant);
      }
    } finally {
      tenantService.clearTenant();
    }

    return ResponseEntity.ok(response);
  }

  // =============================================
  // SpEL Expression Demo
  // =============================================

  /**
   * Resolve SpEL expressions for a tenant.
   * Shows how SpEL expressions in @IndexingOptions are resolved at runtime using framework methods.
   */
  @GetMapping(
    "/spel/resolve"
  )
  public ResponseEntity<Map<String, Object>> resolveSpel(@RequestParam String tenant) {
    Map<String, Object> response = new HashMap<>();
    response.put("tenant", tenant);

    // Set tenant context and use framework methods to resolve SpEL expressions
    tenantService.setCurrentTenant(tenant);
    try {
      // Framework methods re-evaluate SpEL expressions at runtime
      response.put("productIndexName", getProductIndexName());
      response.put("productKeyPrefix", getProductKeyPrefix());
    } finally {
      tenantService.clearTenant();
    }

    // Environment property (used by Order entity)
    // @IndexingOptions(indexName = "orders_#{@environment.getProperty('app.environment')}_idx")
    String env = environment.getProperty("app.environment", "development");
    response.put("environment", env);
    response.put("orderIndexName", "orders_" + env + "_idx");
    response.put("orderKeyPrefix", env + ":orders:");

    // Show the SpEL expressions used
    response.put("spelExpressions", Map.of("Product.indexName", "products_#{@tenantService.getCurrentTenant()}_idx",
        "Product.keyPrefix", "#{@tenantService.getCurrentTenant()}:products:", "Order.indexName",
        "orders_#{@environment.getProperty('app.environment')}_idx", "Order.keyPrefix",
        "#{@environment.getProperty('app.environment')}:orders:"));

    return ResponseEntity.ok(response);
  }

  // =============================================
  // Ephemeral Index Operations
  // =============================================

  /**
   * List all ephemeral indexes.
   */
  @GetMapping(
    "/ephemeral"
  )
  public ResponseEntity<Map<String, Object>> listEphemeralIndexes() {
    Map<String, Object> response = new HashMap<>();

    if (ephemeralIndexService == null) {
      response.put("available", false);
      response.put("indexes", List.of());
      return ResponseEntity.ok(response);
    }

    List<Map<String, Object>> indexes = new ArrayList<>();
    long now = System.currentTimeMillis();

    for (Map.Entry<String, Long> entry : ephemeralIndexTtls.entrySet()) {
      long remaining = (entry.getValue() - now) / 1000;
      if (remaining > 0 && ephemeralIndexService.isEphemeralIndex(entry.getKey())) {
        Map<String, Object> idx = new HashMap<>();
        idx.put("name", entry.getKey());
        idx.put("remainingTtl", remaining);
        indexes.add(idx);
      } else {
        ephemeralIndexTtls.remove(entry.getKey());
      }
    }

    response.put("available", true);
    response.put("indexes", indexes);
    return ResponseEntity.ok(response);
  }

  /**
   * Create an ephemeral index.
   */
  @PostMapping(
    "/ephemeral"
  )
  public ResponseEntity<Map<String, Object>> createEphemeralIndex(@RequestBody EphemeralRequest request) {
    Map<String, Object> response = new HashMap<>();

    if (ephemeralIndexService == null) {
      response.put("success", false);
      response.put("error", "EphemeralIndexService not available");
      return ResponseEntity.badRequest().body(response);
    }

    tenantService.setCurrentTenant("ephemeral_demo");
    try {
      boolean created = ephemeralIndexService.createEphemeralIndex(Product.class, request.indexName, Duration.ofSeconds(
          request.ttlSeconds));

      if (created) {
        ephemeralIndexTtls.put(request.indexName, System.currentTimeMillis() + (request.ttlSeconds * 1000L));
      }

      response.put("success", created);
      response.put("indexName", request.indexName);
      response.put("ttlSeconds", request.ttlSeconds);
      response.put("isEphemeral", ephemeralIndexService.isEphemeralIndex(request.indexName));
      return ResponseEntity.ok(response);
    } finally {
      tenantService.clearTenant();
    }
  }

  /**
   * Extend TTL of an ephemeral index.
   */
  @PostMapping(
    "/ephemeral/{indexName}/extend"
  )
  public ResponseEntity<Map<String, Object>> extendEphemeralTtl(@PathVariable String indexName, @RequestParam(
      defaultValue = "30"
  ) int seconds) {
    Map<String, Object> response = new HashMap<>();

    if (ephemeralIndexService == null) {
      response.put("success", false);
      response.put("error", "EphemeralIndexService not available");
      return ResponseEntity.badRequest().body(response);
    }

    boolean extended = ephemeralIndexService.extendTTL(indexName, Duration.ofSeconds(seconds));

    if (extended && ephemeralIndexTtls.containsKey(indexName)) {
      ephemeralIndexTtls.put(indexName, ephemeralIndexTtls.get(indexName) + (seconds * 1000L));
    }

    response.put("success", extended);
    response.put("indexName", indexName);
    response.put("additionalSeconds", seconds);
    return ResponseEntity.ok(response);
  }

  // =============================================
  // Index Migration Operations
  // =============================================

  /**
   * Migration Step 1: Create v2 index.
   */
  @PostMapping(
    "/migration/create-v2"
  )
  public ResponseEntity<Map<String, Object>> migrationCreateV2() {
    Map<String, Object> response = new HashMap<>();

    try {
      // First ensure v1 exists
      tenantService.setCurrentTenant("migration_v1");
      try {
        if (!v1IndexExists) {
          indexer.createIndexFor(Product.class);
          v1IndexExists = true;
        }
      } finally {
        tenantService.clearTenant();
      }

      // Create v2 index with different tenant context
      tenantService.setCurrentTenant("migration_v2");
      try {
        indexer.createIndexFor(Product.class);
        v2IndexExists = true;

        response.put("success", true);
        response.put("indexName", "products_migration_v2_idx");
        response.put("message", "Created v2 index for migration");
        return ResponseEntity.ok(response);
      } finally {
        tenantService.clearTenant();
      }
    } catch (Exception e) {
      response.put("success", false);
      response.put("error", e.getMessage());
      return ResponseEntity.internalServerError().body(response);
    }
  }

  /**
   * Migration Step 2: Reindex data.
   */
  @PostMapping(
    "/migration/reindex"
  )
  public ResponseEntity<Map<String, Object>> migrationReindex() {
    Map<String, Object> response = new HashMap<>();

    try {
      // Get products from v1
      tenantService.setCurrentTenant("migration_v1");
      List<Product> v1Products;
      try {
        if (!indexer.indexExistsFor(Product.class)) {
          indexer.createIndexFor(Product.class);
        }
        v1Products = productRepository.findAll();

        // If no products, add some sample data
        if (v1Products.isEmpty()) {
          Product p1 = Product.builder().name("Migration Test Product 1").category("Electronics").sku("MIG-001").price(
              99.99).stockQuantity(50).active(true).build();
          Product p2 = Product.builder().name("Migration Test Product 2").category("Books").sku("MIG-002").price(29.99)
              .stockQuantity(100).active(true).build();
          v1Products = List.of(productRepository.save(p1), productRepository.save(p2));
        }
      } finally {
        tenantService.clearTenant();
      }

      // Copy to v2
      tenantService.setCurrentTenant("migration_v2");
      try {
        if (!indexer.indexExistsFor(Product.class)) {
          indexer.createIndexFor(Product.class);
        }
        for (Product p : v1Products) {
          Product copy = Product.builder().name(p.getName()).category(p.getCategory()).sku(p.getSku()).price(p
              .getPrice()).stockQuantity(p.getStockQuantity()).active(p.getActive()).description(p.getDescription())
              .build();
          productRepository.save(copy);
        }
      } finally {
        tenantService.clearTenant();
      }

      response.put("success", true);
      response.put("count", v1Products.size());
      response.put("message", "Reindexed " + v1Products.size() + " documents to v2");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      response.put("success", false);
      response.put("error", e.getMessage());
      return ResponseEntity.internalServerError().body(response);
    }
  }

  /**
   * Migration Step 3: Switch alias.
   */
  @PostMapping(
    "/migration/switch-alias"
  )
  public ResponseEntity<Map<String, Object>> migrationSwitchAlias() {
    Map<String, Object> response = new HashMap<>();

    try {
      tenantService.setCurrentTenant("migration_v2");
      try {
        String v2IndexName = indexer.getIndexName(Product.class);

        // Create alias pointing to v2
        boolean created = indexer.createAlias(v2IndexName, "products_alias");

        response.put("success", created);
        response.put("aliasName", "products_alias");
        response.put("targetIndex", v2IndexName);
        response.put("message", "Alias now points to v2 index");
        return ResponseEntity.ok(response);
      } finally {
        tenantService.clearTenant();
      }
    } catch (Exception e) {
      response.put("success", false);
      response.put("error", e.getMessage());
      return ResponseEntity.internalServerError().body(response);
    }
  }

  /**
   * Migration Step 4: Cleanup old index.
   */
  @PostMapping(
    "/migration/cleanup"
  )
  public ResponseEntity<Map<String, Object>> migrationCleanup() {
    Map<String, Object> response = new HashMap<>();

    try {
      tenantService.setCurrentTenant("migration_v1");
      try {
        indexer.dropIndexFor(Product.class);
        v1IndexExists = false;

        response.put("success", true);
        response.put("message", "Dropped v1 index");
        return ResponseEntity.ok(response);
      } finally {
        tenantService.clearTenant();
      }
    } catch (Exception e) {
      response.put("success", false);
      response.put("error", e.getMessage());
      return ResponseEntity.internalServerError().body(response);
    }
  }

  // =============================================
  // Alias Operations
  // =============================================

  /**
   * Create an alias.
   */
  @PostMapping(
    "/alias"
  )
  public ResponseEntity<Map<String, Object>> createAlias(@RequestParam String indexName,
      @RequestParam String aliasName) {
    Map<String, Object> response = new HashMap<>();

    try {
      boolean created = indexer.createAlias(indexName, aliasName);
      response.put("success", created);
      response.put("indexName", indexName);
      response.put("aliasName", aliasName);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      response.put("success", false);
      response.put("error", e.getMessage());
      return ResponseEntity.internalServerError().body(response);
    }
  }

  /**
   * Update an alias to point to a different index.
   */
  @PostMapping(
    "/alias/update"
  )
  public ResponseEntity<Map<String, Object>> updateAlias(@RequestParam String indexName,
      @RequestParam String aliasName) {
    Map<String, Object> response = new HashMap<>();

    try {
      boolean updated = indexer.updateAlias(null, indexName, aliasName);
      response.put("success", updated);
      response.put("indexName", indexName);
      response.put("aliasName", aliasName);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      response.put("success", false);
      response.put("error", e.getMessage());
      return ResponseEntity.internalServerError().body(response);
    }
  }

  /**
   * Remove an alias.
   */
  @DeleteMapping(
    "/alias"
  )
  public ResponseEntity<Map<String, Object>> removeAlias(@RequestParam String indexName,
      @RequestParam String aliasName) {
    Map<String, Object> response = new HashMap<>();

    try {
      boolean removed = indexer.removeAlias(indexName, aliasName);
      response.put("success", removed);
      response.put("indexName", indexName);
      response.put("aliasName", aliasName);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      response.put("success", false);
      response.put("error", e.getMessage());
      return ResponseEntity.internalServerError().body(response);
    }
  }

  // =============================================
  // Bulk Index Operations
  // =============================================

  /**
   * List all indexes.
   */
  @GetMapping(
    "/indexes"
  )
  public ResponseEntity<Map<String, Object>> listIndexes() {
    Set<String> indexes = indexer.listIndexes();
    Map<String, Object> response = new HashMap<>();
    response.put("count", indexes.size());
    response.put("indexes", indexes);
    return ResponseEntity.ok(response);
  }

  /**
   * Create all registered indexes.
   */
  @PostMapping(
    "/indexes/create-all"
  )
  public ResponseEntity<Map<String, Object>> createAllIndexes() {
    indexer.createIndexes();
    Set<String> indexes = indexer.listIndexes();
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("count", indexes.size());
    response.put("indexes", indexes);
    return ResponseEntity.ok(response);
  }

  /**
   * Drop all managed indexes.
   */
  @DeleteMapping(
    "/indexes"
  )
  public ResponseEntity<Map<String, Object>> dropAllIndexes() {
    indexer.dropIndexes();
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("message", "All indexes dropped");
    return ResponseEntity.ok(response);
  }

  // =============================================
  // Helper Methods
  // =============================================

  private List<Product> getSampleProductsForTenant(String tenant) {
    switch (tenant) {
      case "acme_corp":
        return Arrays.asList(Product.builder().name("Acme Rocket Launcher").description("Trusted by coyotes worldwide")
            .category("Weapons").sku("ACME-001").price(999.99).stockQuantity(10).active(true).build(), Product.builder()
                .name("Acme Giant Rubber Band").description("For all your contraption needs").category("Supplies").sku(
                    "ACME-002").price(49.99).stockQuantity(100).active(true).build(), Product.builder().name(
                        "Acme Invisible Paint").description("Now you see it, now you don't").category("Paint").sku(
                            "ACME-003").price(79.99).stockQuantity(50).active(true).build());

      case "globex_inc":
        return Arrays.asList(Product.builder().name("Globex Hank Scorpio Action Figure").description(
            "Comes with flamethrower accessory").category("Toys").sku("GLX-001").price(24.99).stockQuantity(200).active(
                true).build(), Product.builder().name("Globex World Domination Kit").description(
                    "Everything you need to take over the world").category("Kits").sku("GLX-002").price(1000000.00)
                    .stockQuantity(1).active(true).build(), Product.builder().name("Globex Casual Friday Hammock")
                        .description("For relaxing during takeovers").category("Furniture").sku("GLX-003").price(299.99)
                        .stockQuantity(15).active(true).build());

      case "initech":
        return Arrays.asList(Product.builder().name("TPS Report Cover Sheet (500 pack)").description(
            "Did you get the memo?").category("Office Supplies").sku("INIT-001").price(12.99).stockQuantity(500).active(
                true).build(), Product.builder().name("Red Swingline Stapler").description(
                    "The stapler everyone wants to steal").category("Office Supplies").sku("INIT-002").price(34.99)
                    .stockQuantity(3).active(true).build(), Product.builder().name("Flair Collection (37 pieces)")
                        .description("Express yourself!").category("Apparel").sku("INIT-003").price(89.99)
                        .stockQuantity(25).active(true).build());

      default:
        return Arrays.asList(Product.builder().name("Generic Product 1").description("A generic product").category(
            "General").sku("GEN-001").price(19.99).stockQuantity(100).active(true).build(), Product.builder().name(
                "Generic Product 2").description("Another generic product").category("General").sku("GEN-002").price(
                    29.99).stockQuantity(50).active(true).build());
    }
  }

  /**
   * Request body for ephemeral index creation.
   */
  public static class EphemeralRequest {
    public String indexName;
    public int ttlSeconds;
  }
}
