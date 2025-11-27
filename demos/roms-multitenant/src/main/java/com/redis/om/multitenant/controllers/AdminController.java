package com.redis.om.multitenant.controllers;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.redis.om.multitenant.services.TenantService;
import com.redis.om.spring.indexing.EphemeralIndexService;
import com.redis.om.spring.indexing.RediSearchIndexer;

/**
 * Admin REST controller for index management operations.
 *
 * <p>Provides endpoints for:
 * <ul>
 * <li>Listing all managed indexes
 * <li>Creating/dropping indexes
 * <li>Creating ephemeral indexes
 * <li>Tenant-specific index management
 * </ul>
 */
@RestController
@RequestMapping(
  "/api/admin"
)
public class AdminController {

  @Autowired
  private RediSearchIndexer indexer;

  @Autowired
  private TenantService tenantService;

  @Autowired(
      required = false
  )
  private EphemeralIndexService ephemeralIndexService;

  /**
   * List all managed indexes.
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
    response.put("message", "All indexes created");
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
  public ResponseEntity<Map<String, String>> dropAllIndexes() {
    indexer.dropIndexes();
    Map<String, String> response = new HashMap<>();
    response.put("message", "All indexes dropped");
    return ResponseEntity.ok(response);
  }

  /**
   * Create index for a specific tenant.
   */
  @PostMapping(
    "/tenants/{tenantId}/indexes"
  )
  public ResponseEntity<Map<String, Object>> createTenantIndex(@PathVariable String tenantId) {
    tenantService.setCurrentTenant(tenantId);
    try {
      indexer.createIndexFor(Product.class);
      String indexName = indexer.getIndexName(Product.class);
      String keyPrefix = indexer.getKeyspacePrefix(Product.class);

      Map<String, Object> response = new HashMap<>();
      response.put("message", "Index created for tenant: " + tenantId);
      response.put("indexName", indexName);
      response.put("keyPrefix", keyPrefix);
      return ResponseEntity.ok(response);
    } finally {
      tenantService.clearTenant();
    }
  }

  /**
   * Drop index for a specific tenant.
   */
  @DeleteMapping(
    "/tenants/{tenantId}/indexes"
  )
  public ResponseEntity<Map<String, String>> dropTenantIndex(@PathVariable String tenantId) {
    tenantService.setCurrentTenant(tenantId);
    try {
      indexer.dropIndexFor(Product.class);
      Map<String, String> response = new HashMap<>();
      response.put("message", "Index dropped for tenant: " + tenantId);
      return ResponseEntity.ok(response);
    } finally {
      tenantService.clearTenant();
    }
  }

  /**
   * Check if index exists for a tenant.
   */
  @GetMapping(
    "/tenants/{tenantId}/indexes/exists"
  )
  public ResponseEntity<Map<String, Object>> checkTenantIndexExists(@PathVariable String tenantId) {
    tenantService.setCurrentTenant(tenantId);
    try {
      boolean exists = indexer.indexExistsFor(Product.class);
      String indexName = indexer.getIndexName(Product.class);

      Map<String, Object> response = new HashMap<>();
      response.put("tenantId", tenantId);
      response.put("indexName", indexName);
      response.put("exists", exists);
      return ResponseEntity.ok(response);
    } finally {
      tenantService.clearTenant();
    }
  }

  /**
   * Create an ephemeral (temporary) index with TTL.
   */
  @PostMapping(
    "/indexes/ephemeral"
  )
  public ResponseEntity<Map<String, Object>> createEphemeralIndex(@RequestBody EphemeralIndexRequest request) {
    if (ephemeralIndexService == null) {
      Map<String, Object> response = new HashMap<>();
      response.put("error", "EphemeralIndexService is not available");
      return ResponseEntity.badRequest().body(response);
    }

    boolean created = ephemeralIndexService.createEphemeralIndex(Product.class, request.indexName, Duration.ofSeconds(
        request.ttlSeconds));

    Map<String, Object> response = new HashMap<>();
    response.put("indexName", request.indexName);
    response.put("ttlSeconds", request.ttlSeconds);
    response.put("created", created);
    response.put("isEphemeral", ephemeralIndexService.isEphemeralIndex(request.indexName));
    return ResponseEntity.ok(response);
  }

  /**
   * Extend TTL of an ephemeral index.
   */
  @PostMapping(
    "/indexes/ephemeral/{indexName}/extend"
  )
  public ResponseEntity<Map<String, Object>> extendEphemeralIndex(@PathVariable String indexName,
      @RequestParam int additionalSeconds) {
    if (ephemeralIndexService == null) {
      Map<String, Object> response = new HashMap<>();
      response.put("error", "EphemeralIndexService is not available");
      return ResponseEntity.badRequest().body(response);
    }

    boolean extended = ephemeralIndexService.extendTTL(indexName, Duration.ofSeconds(additionalSeconds));

    Map<String, Object> response = new HashMap<>();
    response.put("indexName", indexName);
    response.put("additionalSeconds", additionalSeconds);
    response.put("extended", extended);
    return ResponseEntity.ok(response);
  }

  /**
   * Create an index alias.
   */
  @PostMapping(
    "/indexes/{indexName}/alias/{aliasName}"
  )
  public ResponseEntity<Map<String, Object>> createAlias(@PathVariable String indexName,
      @PathVariable String aliasName) {
    boolean created = indexer.createAlias(indexName, aliasName);
    Map<String, Object> response = new HashMap<>();
    response.put("indexName", indexName);
    response.put("aliasName", aliasName);
    response.put("created", created);
    return ResponseEntity.ok(response);
  }

  /**
   * Remove an index alias.
   */
  @DeleteMapping(
    "/indexes/{indexName}/alias/{aliasName}"
  )
  public ResponseEntity<Map<String, Object>> removeAlias(@PathVariable String indexName,
      @PathVariable String aliasName) {
    boolean removed = indexer.removeAlias(indexName, aliasName);
    Map<String, Object> response = new HashMap<>();
    response.put("indexName", indexName);
    response.put("aliasName", aliasName);
    response.put("removed", removed);
    return ResponseEntity.ok(response);
  }

  /**
   * Request body for creating ephemeral indexes.
   */
  public static class EphemeralIndexRequest {
    public String indexName;
    public int ttlSeconds;
  }
}
