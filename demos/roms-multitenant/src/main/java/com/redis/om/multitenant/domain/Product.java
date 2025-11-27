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
 * Product entity with dynamic tenant-aware indexing.
 *
 * <p>The index name is resolved at runtime using SpEL expressions
 * that reference the TenantService bean to get the current tenant context.
 * This allows creating separate indexes per tenant dynamically.
 *
 * <p>Note: The keyPrefix is intentionally left blank to use the default
 * keyspace from Spring Data Redis. All tenants share the same data storage
 * but can have different search indexes. For true data isolation, implement
 * a custom repository or use tenant-aware key generation.
 *
 * <p>Example for tenant "acme":
 * <ul>
 * <li>Index name: products_acme_idx
 * <li>Storage keyspace: com.redis.om.multitenant.domain.Product:
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document
@IndexingOptions(
    indexName = "products_#{@tenantService.getCurrentTenant()}_idx", creationMode = IndexCreationMode.SKIP_IF_EXIST
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
