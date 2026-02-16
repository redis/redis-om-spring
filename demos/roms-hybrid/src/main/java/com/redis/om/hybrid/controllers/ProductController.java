package com.redis.om.hybrid.controllers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.redis.om.hybrid.domain.Product;
import com.redis.om.hybrid.domain.Product$;
import com.redis.om.hybrid.repositories.ProductRepository;
import com.redis.om.spring.search.stream.EntityStream;

/**
 * REST controller for Product operations including hybrid search.
 */
@RestController
@RequestMapping(
  "/api/products"
)
public class ProductController {

  @Autowired
  private ProductRepository productRepository;

  @Autowired
  private EntityStream entityStream;

  /**
   * Get all products
   */
  @GetMapping
  public Iterable<Product> getAllProducts() {
    return productRepository.findAll();
  }

  /**
   * Get a product by ID
   */
  @GetMapping(
    "/{id}"
  )
  public Optional<Product> getProduct(@PathVariable String id) {
    return productRepository.findById(id);
  }

  /**
   * Create or update a product
   */
  @PostMapping
  public Product saveProduct(@RequestBody Product product) {
    return productRepository.save(product);
  }

  /**
   * Delete a product
   */
  @DeleteMapping(
    "/{id}"
  )
  public void deleteProduct(@PathVariable String id) {
    productRepository.deleteById(id);
  }

  /**
   * Search products by category
   */
  @GetMapping(
    "/category/{category}"
  )
  public List<Product> getProductsByCategory(@PathVariable String category) {
    return entityStream.of(Product.class).filter(Product$.CATEGORY.eq(category)).collect(Collectors.toList());
  }

  /**
   * Hybrid search endpoint combining text and vector similarity.
   *
   * @param text      The text query for BM25 full-text search
   * @param embedding The query vector for semantic similarity (384 dimensions)
   * @param alpha     Weight between text (0.0) and vector (1.0) similarity. Default: 0.7
   *                  - 0.0 = pure text search (BM25 only)
   *                  - 0.5 = balanced hybrid search
   *                  - 1.0 = pure vector search (semantic only)
   * @param category  Optional category filter
   * @param minPrice  Optional minimum price filter
   * @param maxPrice  Optional maximum price filter
   * @param limit     Maximum number of results (default: 10)
   * @return List of products ranked by hybrid score
   */
  @PostMapping(
    "/hybrid-search"
  )
  public List<Product> hybridSearch(@RequestParam String text, @RequestBody float[] embedding, @RequestParam(
      defaultValue = "0.7"
  ) float alpha, @RequestParam(
      required = false
  ) String category, @RequestParam(
      required = false
  ) Double minPrice, @RequestParam(
      required = false
  ) Double maxPrice, @RequestParam(
      defaultValue = "10"
  ) int limit) {
    var stream = entityStream.of(Product.class);

    // Apply optional filters
    if (category != null) {
      stream = stream.filter(Product$.CATEGORY.eq(category));
    }
    if (minPrice != null && maxPrice != null) {
      stream = stream.filter(Product$.PRICE.between(minPrice, maxPrice));
    } else if (minPrice != null) {
      stream = stream.filter(Product$.PRICE.ge(minPrice));
    } else if (maxPrice != null) {
      stream = stream.filter(Product$.PRICE.le(maxPrice));
    }

    // Execute hybrid search
    return stream.hybridSearch(text, Product$.DESCRIPTION, embedding, Product$.EMBEDDING, alpha).limit(limit).collect(
        Collectors.toList());
  }

  /**
   * Text-only search using full-text search on description
   */
  @GetMapping(
    "/search/text"
  )
  public List<Product> textSearch(@RequestParam String query, @RequestParam(
      defaultValue = "10"
  ) int limit) {
    return entityStream.of(Product.class).filter(query).limit(limit).collect(Collectors.toList());
  }

  /**
   * Vector-only search for semantic similarity
   */
  @PostMapping(
    "/search/semantic"
  )
  public List<Product> semanticSearch(@RequestBody float[] embedding, @RequestParam(
      defaultValue = "10"
  ) int limit) {
    return entityStream.of(Product.class).filter(Product$.EMBEDDING.knn(limit, embedding)).limit(limit).collect(
        Collectors.toList());
  }

  /**
   * Get product count
   */
  @GetMapping(
    "/count"
  )
  public long count() {
    return productRepository.count();
  }
}
