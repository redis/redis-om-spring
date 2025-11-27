package com.redis.om.spring.indexing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.IndexingOptions;
import com.redis.om.spring.fixtures.document.model.BeanReferenceIndexEntity;
import com.redis.om.spring.fixtures.document.model.ComplexSpelEntity;
import com.redis.om.spring.fixtures.document.model.DynamicIndexEntity;
import com.redis.om.spring.fixtures.document.model.DynamicPrefixEntity;
import com.redis.om.spring.fixtures.document.model.EnvironmentBasedIndexEntity;
import com.redis.om.spring.fixtures.document.model.FallbackIndexEntity;
import com.redis.om.spring.fixtures.document.model.SystemPropertyEntity;
import com.redis.om.spring.fixtures.document.model.TenantAwareEntity;
import com.redis.om.spring.fixtures.document.model.VersionedIndexEntity;
import com.redis.om.spring.fixtures.document.repository.DynamicIndexEntityRepository;
import com.redis.om.spring.fixtures.document.repository.TenantAwareEntityRepository;
import com.redis.om.spring.services.TenantResolver;
import com.redis.om.spring.services.VersionService;

@TestPropertySource(properties = {
    "app.tenant=acme_corp",
    "app.environment=production",
    "app.version=2.0.1"
})
public class DynamicIndexingSpelTest extends AbstractBaseDocumentTest {

    @Configuration
    static class TestConfig {
        @Bean
        public TenantResolver tenantResolver() {
            return new TenantResolver();
        }

        @Bean
        public VersionService versionService(Environment environment) {
            return new VersionService(environment);
        }
    }

    @Autowired
    private RediSearchIndexer indexer;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Environment environment;

    @Autowired
    private TenantResolver tenantResolver;

    @Autowired
    private VersionService versionService;

    @Autowired(required = false)
    private DynamicIndexEntityRepository dynamicIndexEntityRepository;

    @Autowired(required = false)
    private TenantAwareEntityRepository tenantAwareEntityRepository;

    @BeforeEach
    void setup() {
        // Reset tenant context for each test
        tenantResolver.setCurrentTenant("default");
    }

    @Test
    void testStaticIndexNameWithSpelExpression() {
        // Given: An entity with a SpEL expression that evaluates to a static string
        Class<?> entityClass = DynamicIndexEntity.class;

        // When: The index is created
        indexer.createIndexFor(entityClass);

        // Then: The index should be created with the evaluated name
        assertThat(indexer.indexExistsFor(entityClass)).isTrue();
        String indexName = indexer.getIndexName(entityClass);
        assertThat(indexName).isEqualTo("dynamic_idx_static");
    }

    @Test
    void testEnvironmentPropertyBasedIndexName() {
        // Given: An entity with index name from environment property
        Class<?> entityClass = EnvironmentBasedIndexEntity.class;

        // Then: The index should have been created at startup with environment property value
        assertThat(indexer.indexExistsFor(entityClass)).isTrue();
        String indexName = indexer.getIndexName(entityClass);
        assertThat(indexName).isEqualTo("acme_corp_idx");
    }

    @Test
    void testBeanReferenceInSpelExpression() {
        // Given: An entity using bean reference in SpEL
        Class<?> entityClass = BeanReferenceIndexEntity.class;
        tenantResolver.setCurrentTenant("customer_123");

        // When: The index is created
        indexer.createIndexFor(entityClass);

        // Then: The index name should use the tenant from the bean
        assertThat(indexer.indexExistsFor(entityClass)).isTrue();
        String indexName = indexer.getIndexName(entityClass);
        assertThat(indexName).isEqualTo("tenant_customer_123_idx");
    }

    @Test
    void testMethodInvocationInSpelExpression() {
        // Given: An entity using method invocation in SpEL
        Class<?> entityClass = VersionedIndexEntity.class;

        // When: The index is created
        indexer.createIndexFor(entityClass);

        // Then: The index name should include the version
        assertThat(indexer.indexExistsFor(entityClass)).isTrue();
        String indexName = indexer.getIndexName(entityClass);
        assertThat(indexName).isEqualTo("users_v2_0_1");
    }

    @Test
    void testSpelExpressionFallback() {
        // Given: An entity with an invalid SpEL expression
        Class<?> entityClass = FallbackIndexEntity.class;

        // Then: Should fall back to default naming convention when SpEL fails
        assertThat(indexer.indexExistsFor(entityClass)).isTrue();
        String indexName = indexer.getIndexName(entityClass);
        // The SpEL expression references a non-existent bean, so it should fall back to default
        assertThat(indexName).isEqualTo("com.redis.om.spring.fixtures.document.model.FallbackIndexEntityIdx");
    }

    @Test
    void testDynamicKeyPrefixWithSpel() {
        // Given: An entity with dynamic key prefix
        Class<?> entityClass = DynamicPrefixEntity.class;
        tenantResolver.setCurrentTenant("company_456");

        // When: The index is created
        indexer.createIndexFor(entityClass);

        // Then: The key prefix should be dynamically resolved
        assertThat(indexer.indexExistsFor(entityClass)).isTrue();
        String keyPrefix = indexer.getKeyspacePrefix(entityClass);
        assertThat(keyPrefix).isEqualTo("company_456:");
    }

    @Test
    void testMultiTenantIndexCreation() {
        // Given: Multiple tenants
        String[] tenants = {"tenant_a", "tenant_b", "tenant_c"};

        for (String tenant : tenants) {
            // When: Creating index for each tenant
            tenantResolver.setCurrentTenant(tenant);
            Class<?> entityClass = TenantAwareEntity.class;

            // Force index recreation for each tenant
            indexer.dropIndexFor(entityClass);
            indexer.createIndexFor(entityClass);

            // Then: Each tenant should have its own index
            assertThat(indexer.indexExistsFor(entityClass)).isTrue();
            String indexName = indexer.getIndexName(entityClass);
            assertThat(indexName).contains(tenant);
        }
    }

    @Test
    void testSpelExpressionWithComplexLogic() {
        // Given: An entity with complex SpEL expression
        Class<?> entityClass = ComplexSpelEntity.class;

        // When: The index is created with conditional logic
        indexer.createIndexFor(entityClass);

        // Then: The index name should reflect the complex logic result
        assertThat(indexer.indexExistsFor(entityClass)).isTrue();
        String indexName = indexer.getIndexName(entityClass);
        // In production environment, should use production prefix
        assertThat(indexName).isEqualTo("prod_complex_idx");
    }

    @Test
    void testSpelWithSystemProperties() {
        // Given: System properties are set
        System.setProperty("custom.index.suffix", "test_suffix");

        try {
            Class<?> entityClass = SystemPropertyEntity.class;

            // When: Creating index with system property reference
            indexer.createIndexFor(entityClass);

            // Then: Index name should include system property value
            assertThat(indexer.indexExistsFor(entityClass)).isTrue();
            String indexName = indexer.getIndexName(entityClass);
            assertThat(indexName).endsWith("test_suffix");
        } finally {
            System.clearProperty("custom.index.suffix");
        }
    }

    @Test
    void testRepositoryOperationsWithDynamicIndex() {
        // Given: A tenant-specific index
        tenantResolver.setCurrentTenant("test_tenant");

        // When: Performing repository operations
        TenantAwareEntity entity = new TenantAwareEntity();
        entity.setName("Test Entity");
        entity.setDescription("Test Description");

        TenantAwareEntity saved = tenantAwareEntityRepository.save(entity);

        // Then: Entity should be saved in tenant-specific index
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();

        // Verify retrieval works with dynamic index
        Optional<TenantAwareEntity> retrieved = tenantAwareEntityRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName()).isEqualTo("Test Entity");
    }


}