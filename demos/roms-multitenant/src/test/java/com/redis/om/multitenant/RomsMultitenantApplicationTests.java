package com.redis.om.multitenant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.redis.om.multitenant.domain.Product;
import com.redis.om.multitenant.repositories.ProductRepository;
import com.redis.om.multitenant.services.TenantService;
import com.redis.om.spring.indexing.RediSearchIndexer;
import com.redis.testcontainers.RedisStackContainer;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

@Testcontainers
@SpringBootTest(classes = RomsMultitenantApplicationTests.Config.class,
    properties = { "spring.main.allow-bean-definition-overriding=true" })
class RomsMultitenantApplicationTests {

  @SpringBootApplication
  @Configuration
  static class Config extends TestConfig {
  }

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

  @Autowired
  private StringRedisTemplate redisTemplate;

  @BeforeEach
  void setUp() {
    // Reset to default tenant before each test
    tenantService.setCurrentTenant("default");
  }

  @AfterEach
  void tearDown() {
    // Clean up tenant context after each test
    tenantService.clearTenant();
  }

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
  void testTenantSpecificIndexAndKeyspaceCreation() {
    // This test verifies that:
    // 1. Different indexes can be created for different tenants
    // 2. The index names are dynamically resolved based on tenant context
    // 3. The keyspace is also dynamically resolved for tenant isolation

    // Create index for tenant A
    tenantService.setCurrentTenant("test_tenant_a");
    indexer.createIndexFor(Product.class);
    String indexNameA = indexer.getIndexName(Product.class);
    String keyspaceA = indexer.getKeyspaceForEntityClass(Product.class);

    // Create index for tenant B
    tenantService.setCurrentTenant("test_tenant_b");
    indexer.createIndexFor(Product.class);
    String indexNameB = indexer.getIndexName(Product.class);
    String keyspaceB = indexer.getKeyspaceForEntityClass(Product.class);

    // Verify that different tenants get different index names
    assertThat(indexNameA).isEqualTo("products_test_tenant_a_idx");
    assertThat(indexNameB).isEqualTo("products_test_tenant_b_idx");
    assertThat(indexNameA).isNotEqualTo(indexNameB);

    // Verify that different tenants get different keyspaces
    assertThat(keyspaceA).isEqualTo("test_tenant_a:products:");
    assertThat(keyspaceB).isEqualTo("test_tenant_b:products:");
    assertThat(keyspaceA).isNotEqualTo(keyspaceB);

    // Verify the index name and keyspace change dynamically based on current tenant
    tenantService.setCurrentTenant("test_tenant_a");
    assertThat(indexer.getIndexName(Product.class)).isEqualTo("products_test_tenant_a_idx");
    assertThat(indexer.getKeyspaceForEntityClass(Product.class)).isEqualTo("test_tenant_a:products:");

    tenantService.setCurrentTenant("test_tenant_b");
    assertThat(indexer.getIndexName(Product.class)).isEqualTo("products_test_tenant_b_idx");
    assertThat(indexer.getKeyspaceForEntityClass(Product.class)).isEqualTo("test_tenant_b:products:");
  }

  @Test
  void testTenantSearchIsolation() {
    // This test verifies complete tenant isolation:
    // - Data saved by tenant A is only visible to tenant A
    // - Data saved by tenant B is only visible to tenant B
    // - Documents are stored with tenant-prefixed keys

    // Setup: Create indexes for both tenants
    tenantService.setCurrentTenant("acme");
    indexer.createIndexFor(Product.class);

    tenantService.setCurrentTenant("globex");
    indexer.createIndexFor(Product.class);

    // Save product as tenant "acme"
    tenantService.setCurrentTenant("acme");
    Product acmeProduct = Product.builder()
        .name("Acme Widget")
        .category("Widgets")
        .price(29.99)
        .active(true)
        .build();
    acmeProduct = productRepository.save(acmeProduct);
    String acmeProductId = acmeProduct.getId();

    // Verify the key is stored with tenant prefix
    Boolean acmeKeyExists = redisTemplate.hasKey("acme:products:" + acmeProductId);
    assertThat(acmeKeyExists).isTrue();

    // Save product as tenant "globex"
    tenantService.setCurrentTenant("globex");
    Product globexProduct = Product.builder()
        .name("Globex Gadget")
        .category("Gadgets")
        .price(49.99)
        .active(true)
        .build();
    globexProduct = productRepository.save(globexProduct);
    String globexProductId = globexProduct.getId();

    // Verify the key is stored with tenant prefix
    Boolean globexKeyExists = redisTemplate.hasKey("globex:products:" + globexProductId);
    assertThat(globexKeyExists).isTrue();

    // Verify search isolation: tenant "acme" should only see their products
    tenantService.setCurrentTenant("acme");
    List<Product> acmeProducts = productRepository.findByCategory("Widgets");
    assertThat(acmeProducts).hasSize(1);
    assertThat(acmeProducts.get(0).getName()).isEqualTo("Acme Widget");

    // Acme should NOT see Globex's products
    List<Product> acmeGadgets = productRepository.findByCategory("Gadgets");
    assertThat(acmeGadgets).isEmpty();

    // Verify search isolation: tenant "globex" should only see their products
    tenantService.setCurrentTenant("globex");
    List<Product> globexProducts = productRepository.findByCategory("Gadgets");
    assertThat(globexProducts).hasSize(1);
    assertThat(globexProducts.get(0).getName()).isEqualTo("Globex Gadget");

    // Globex should NOT see Acme's products
    List<Product> globexWidgets = productRepository.findByCategory("Widgets");
    assertThat(globexWidgets).isEmpty();

    // Cleanup
    tenantService.setCurrentTenant("acme");
    productRepository.delete(acmeProduct);

    tenantService.setCurrentTenant("globex");
    productRepository.delete(globexProduct);
  }

  @Test
  void testProductSaveAndSearchWithDefaultTenant() {
    // Use default tenant (set at application startup)
    tenantService.setCurrentTenant("default");
    indexer.createIndexFor(Product.class);

    // Save a product
    Product product = Product.builder()
        .name("Test Product")
        .category("TestCategory")
        .price(99.99)
        .active(true)
        .build();
    product = productRepository.save(product);
    assertThat(product.getId()).isNotNull();

    // Verify the key is stored with tenant prefix
    Boolean keyExists = redisTemplate.hasKey("default:products:" + product.getId());
    assertThat(keyExists).isTrue();

    // Search should find the product when using the same tenant context
    List<Product> found = productRepository.findByCategory("TestCategory");
    assertThat(found).hasSize(1);
    assertThat(found.get(0).getName()).isEqualTo("Test Product");

    // Cleanup
    productRepository.delete(product);
  }
}
