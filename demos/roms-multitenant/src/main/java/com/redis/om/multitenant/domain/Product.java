package com.redis.om.multitenant.domain;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.IndexCreationMode;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.IndexingOptions;
import com.redis.om.spring.annotations.Searchable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Product entity with dynamic tenant-aware indexing and storage isolation.
 *
 * <p>Both the index name and key prefix are resolved at runtime using SpEL expressions
 * that reference the TenantService bean to get the current tenant context.
 * This provides complete tenant isolation for both search indexes and data storage.
 *
 * <p>Example for tenant "acme":
 * <ul>
 * <li>Index name: products_acme_idx
 * <li>Storage keyspace: acme:products:
 * </ul>
 *
 * <p>Example for tenant "globex":
 * <ul>
 * <li>Index name: products_globex_idx
 * <li>Storage keyspace: globex:products:
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
@IndexingOptions(
    indexName = "products_#{@tenantService.getCurrentTenant()}_idx",
    keyPrefix = "#{@tenantService.getCurrentTenant()}:products:", creationMode = IndexCreationMode.SKIP_IF_EXIST
)
public class Product {

  @Id
  private String id;

  @Searchable
  private String name;

  @Searchable
  private String description;

  @Indexed
  private String category;

  @Indexed
  private String sku;

  @Indexed
  private Double price;

  @Indexed
  private Integer stockQuantity;

  @Indexed
  private Boolean active;
}
