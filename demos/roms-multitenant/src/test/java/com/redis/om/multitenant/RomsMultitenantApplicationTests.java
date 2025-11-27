package com.redis.om.multitenant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.redis.om.multitenant.domain.Product;
import com.redis.om.multitenant.repositories.ProductRepository;
import com.redis.om.multitenant.services.TenantService;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.testcontainers.RedisStackContainer;

@Testcontainers
@SpringBootTest
class RomsMultitenantApplicationTests {

  @Container
  static RedisStackContainer redis = new RedisStackContainer(
      RedisStackContainer.DEFAULT_IMAGE_NAME.withTag(RedisStackContainer.DEFAULT_TAG));

  @DynamicPropertySource
  static void redisProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", redis::getFirstMappedPort);
  }

  @Autowired
  private TenantService tenantService;

  @Autowired
  private RediSearchIndexer indexer;

  @Autowired
  private ProductRepository productRepository;

  @Test
  void contextLoads() {
    assertThat(tenantService).isNotNull();
    assertThat(indexer).isNotNull();
    assertThat(productRepository).isNotNull();
  }

  @Test
  void testMultiTenantIndexCreation() {
    // Test tenant 1
    tenantService.setCurrentTenant("tenant_a");
    indexer.createIndexFor(Product.class);

    String indexA = indexer.getIndexName(Product.class);
    assertThat(indexA).contains("tenant_a");

    // Test tenant 2
    tenantService.setCurrentTenant("tenant_b");
    indexer.createIndexFor(Product.class);

    String indexB = indexer.getIndexName(Product.class);
    assertThat(indexB).contains("tenant_b");

    // Verify they are different
    assertThat(indexA).isNotEqualTo(indexB);
  }

  @Test
  void testTenantSpecificIndexCreation() {
    // This test verifies that:
    // 1. Different indexes can be created for different tenants
    // 2. The index names are dynamically resolved based on tenant context
    //
    // Note: Full search isolation requires storing documents with tenant-prefixed keys,
    // which requires custom repository implementations or interceptors beyond
    // the scope of basic @IndexingOptions support.

    // Create index for tenant A
    tenantService.setCurrentTenant("test_tenant_a");
    indexer.createIndexFor(Product.class);
    String indexNameA = indexer.getIndexName(Product.class);

    // Create index for tenant B
    tenantService.setCurrentTenant("test_tenant_b");
    indexer.createIndexFor(Product.class);
    String indexNameB = indexer.getIndexName(Product.class);

    // Verify that different tenants get different index names
    assertThat(indexNameA).isEqualTo("products_test_tenant_a_idx");
    assertThat(indexNameB).isEqualTo("products_test_tenant_b_idx");
    assertThat(indexNameA).isNotEqualTo(indexNameB);

    // Verify the index name changes dynamically based on current tenant
    tenantService.setCurrentTenant("test_tenant_a");
    assertThat(indexer.getIndexName(Product.class)).isEqualTo("products_test_tenant_a_idx");

    tenantService.setCurrentTenant("test_tenant_b");
    assertThat(indexer.getIndexName(Product.class)).isEqualTo("products_test_tenant_b_idx");
  }

  @Test
  void testProductSaveAndSearchWithDefaultTenant() {
    // Use default tenant (set at application startup)
    tenantService.setCurrentTenant("default");

    // Save a product
    Product product = Product.builder()
        .name("Test Product")
        .category("TestCategory")
        .price(99.99)
        .active(true)
        .build();
    product = productRepository.save(product);
    assertThat(product.getId()).isNotNull();

    // Search should find the product when using the same tenant context
    List<Product> found = productRepository.findByCategory("TestCategory");
    assertThat(found).hasSize(1);
    assertThat(found.get(0).getName()).isEqualTo("Test Product");

    // Cleanup
    productRepository.delete(product);
  }
}
