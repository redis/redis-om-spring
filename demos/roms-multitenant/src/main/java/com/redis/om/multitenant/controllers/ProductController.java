package com.redis.om.multitenant.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.redis.om.multitenant.domain.Product;
import com.redis.om.multitenant.repositories.ProductRepository;
import com.redis.om.multitenant.services.TenantService;
import com.redis.om.spring.indexing.RediSearchIndexer;

/**
 * REST controller for tenant-scoped product operations.
 *
 * <p>All operations are scoped to the tenant specified in the URL path.
 * The tenant context is set before each operation, ensuring data isolation.
 */
@RestController
@RequestMapping(
  "/api/tenants/{tenantId}/products"
)
public class ProductController {

  @Autowired
  private TenantService tenantService;

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private RediSearchIndexer indexer;

  /**
   * List all products for a tenant.
   */
  @GetMapping
  public ResponseEntity<List<Product>> listProducts(@PathVariable String tenantId) {
    tenantService.setCurrentTenant(tenantId);
    try {
      // Ensure index exists for this tenant
      ensureIndexExists();
      List<Product> products = productRepository.findAll();
      return ResponseEntity.ok(products);
    } finally {
      tenantService.clearTenant();
    }
  }

  /**
   * Get a specific product by ID.
   */
  @GetMapping(
    "/{productId}"
  )
  public ResponseEntity<Product> getProduct(@PathVariable String tenantId, @PathVariable String productId) {
    tenantService.setCurrentTenant(tenantId);
    try {
      ensureIndexExists();
      Optional<Product> product = productRepository.findById(productId);
      return product.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    } finally {
      tenantService.clearTenant();
    }
  }

  /**
   * Create a new product for a tenant.
   */
  @PostMapping
  public ResponseEntity<Product> createProduct(@PathVariable String tenantId, @RequestBody Product product) {
    tenantService.setCurrentTenant(tenantId);
    try {
      ensureIndexExists();
      Product saved = productRepository.save(product);
      return ResponseEntity.ok(saved);
    } finally {
      tenantService.clearTenant();
    }
  }

  /**
   * Update an existing product.
   */
  @PutMapping(
    "/{productId}"
  )
  public ResponseEntity<Product> updateProduct(@PathVariable String tenantId, @PathVariable String productId,
      @RequestBody Product product) {
    tenantService.setCurrentTenant(tenantId);
    try {
      ensureIndexExists();
      if (!productRepository.existsById(productId)) {
        return ResponseEntity.notFound().build();
      }
      product.setId(productId);
      Product saved = productRepository.save(product);
      return ResponseEntity.ok(saved);
    } finally {
      tenantService.clearTenant();
    }
  }

  /**
   * Delete a product.
   */
  @DeleteMapping(
    "/{productId}"
  )
  public ResponseEntity<Void> deleteProduct(@PathVariable String tenantId, @PathVariable String productId) {
    tenantService.setCurrentTenant(tenantId);
    try {
      ensureIndexExists();
      if (!productRepository.existsById(productId)) {
        return ResponseEntity.notFound().build();
      }
      productRepository.deleteById(productId);
      return ResponseEntity.noContent().build();
    } finally {
      tenantService.clearTenant();
    }
  }

  /**
   * Search products by name (full-text search).
   */
  @GetMapping(
    "/search"
  )
  public ResponseEntity<List<Product>> searchProducts(@PathVariable String tenantId, @RequestParam String q) {
    tenantService.setCurrentTenant(tenantId);
    try {
      ensureIndexExists();
      List<Product> products = productRepository.findByNameContaining(q);
      return ResponseEntity.ok(products);
    } finally {
      tenantService.clearTenant();
    }
  }

  /**
   * Search products by category.
   */
  @GetMapping(
    "/category/{category}"
  )
  public ResponseEntity<List<Product>> findByCategory(@PathVariable String tenantId, @PathVariable String category) {
    tenantService.setCurrentTenant(tenantId);
    try {
      ensureIndexExists();
      List<Product> products = productRepository.findByCategory(category);
      return ResponseEntity.ok(products);
    } finally {
      tenantService.clearTenant();
    }
  }

  /**
   * Find products by price range.
   */
  @GetMapping(
    "/price-range"
  )
  public ResponseEntity<List<Product>> findByPriceRange(@PathVariable String tenantId, @RequestParam Double minPrice,
      @RequestParam Double maxPrice) {
    tenantService.setCurrentTenant(tenantId);
    try {
      ensureIndexExists();
      List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
      return ResponseEntity.ok(products);
    } finally {
      tenantService.clearTenant();
    }
  }

  private void ensureIndexExists() {
    if (!indexer.indexExistsFor(Product.class)) {
      indexer.createIndexFor(Product.class);
    }
  }
}
