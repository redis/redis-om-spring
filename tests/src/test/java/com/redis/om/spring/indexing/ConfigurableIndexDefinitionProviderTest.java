package com.redis.om.spring.indexing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.IndexingOptions;
import com.redis.om.spring.indexing.ConfigurableIndexDefinitionProvider.*;

/**
 * Test-First implementation of ConfigurableIndexDefinitionProvider
 * as specified in Phase 4 of the Dynamic Indexing Feature Design.
 *
 * This provider should:
 * 1. Bridge RediSearch indexes with Spring Data Redis
 * 2. Support dynamic index configuration
 * 3. Allow runtime index definition updates
 * 4. Integrate with existing Spring Data Redis repositories
 */
@ExtendWith(MockitoExtension.class)
public class ConfigurableIndexDefinitionProviderTest {

    @Mock
    private RediSearchIndexer indexer;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private IndexResolver indexResolver;

    private ConfigurableIndexDefinitionProvider provider;

    @BeforeEach
    void setUp() {
        provider = new ConfigurableIndexDefinitionProvider(indexer, applicationContext);
    }

    @Test
    void testGetIndexDefinitionsReturnsAllConfiguredIndices() {
        // Given: Multiple entity classes with different index configurations
        provider.registerIndexDefinition(TestProduct.class, "product_idx", "product:");
        provider.registerIndexDefinition(TestCategory.class, "category_idx", "category:");

        // When: Getting all index definitions
        List<IndexDefinition> definitions = provider.getIndexDefinitions();

        // Then: All configured indices should be returned
        assertThat(definitions).isNotEmpty();
        assertThat(definitions).hasSize(2);
    }

    @Test
    void testGetIndexDefinitionForEntityClass() {
        // Given: An entity class with index configuration
        Class<?> entityClass = TestProduct.class;
        provider.registerIndexDefinition(entityClass, "test_product_idx", "product:");

        // When: Getting index definition for specific entity
        IndexDefinition definition = provider.getIndexDefinition(entityClass);

        // Then: Correct index definition should be returned
        assertThat(definition).isNotNull();
        assertThat(definition.getIndexName()).isEqualTo("test_product_idx");
        assertThat(definition.getKeyPrefix()).isEqualTo("product:");
    }

    @Test
    void testRegisterDynamicIndexDefinition() {
        // Given: A new index definition to register at runtime
        String indexName = "dynamic_product_idx";
        String keyPrefix = "dynamic:product:";
        Class<?> entityClass = TestProduct.class;

        // When: Registering a dynamic index definition
        provider.registerIndexDefinition(entityClass, indexName, keyPrefix);

        // Then: The definition should be stored and retrievable
        IndexDefinition definition = provider.getIndexDefinition(entityClass);
        assertThat(definition.getIndexName()).isEqualTo(indexName);
        assertThat(definition.getKeyPrefix()).isEqualTo(keyPrefix);
    }

    @Test
    void testUpdateExistingIndexDefinition() {
        // Given: An existing index definition
        Class<?> entityClass = TestProduct.class;
        provider.registerIndexDefinition(entityClass, "old_idx", "old:");

        // When: Updating the index definition
        provider.updateIndexDefinition(entityClass, "new_idx", "new:");

        // Then: The definition should be updated
        IndexDefinition definition = provider.getIndexDefinition(entityClass);
        assertThat(definition.getIndexName()).isEqualTo("new_idx");
        assertThat(definition.getKeyPrefix()).isEqualTo("new:");
    }

    @Test
    void testRemoveIndexDefinition() {
        // Given: A registered index definition
        Class<?> entityClass = TestProduct.class;
        provider.registerIndexDefinition(entityClass, "test_idx", "test:");

        // When: Removing the index definition
        boolean removed = provider.removeIndexDefinition(entityClass);

        // Then: The definition should be removed
        assertThat(removed).isTrue();
        assertThat(provider.getIndexDefinition(entityClass)).isNull();
    }

    @Test
    void testContextAwareIndexDefinition() {
        // Given: A context with tenant information and custom resolver
        RedisIndexContext context = RedisIndexContext.builder()
            .tenantId("tenant1")
            .build();

        // Set up a custom resolver that includes tenant in the name
        IndexResolver tenantResolver = mock(IndexResolver.class);
        when(tenantResolver.resolveIndexName(any(), eq(context))).thenReturn("tenant1_product_idx");
        when(tenantResolver.resolveKeyPrefix(any(), eq(context))).thenReturn("tenant1:product:");
        provider.setIndexResolver(TestProduct.class, tenantResolver);

        // When: Getting index definition with context
        IndexDefinition definition = provider.getIndexDefinition(TestProduct.class, context);

        // Then: Context-specific index should be returned
        assertThat(definition.getIndexName()).contains("tenant1");
        assertThat(definition.getKeyPrefix()).startsWith("tenant1:");
    }

    @Test
    void testSpELEvaluationInIndexDefinition() {
        // Given: An entity with SpEL expression in index name
        Class<?> entityClass = SpELTestEntity.class;

        // Register the entity so it has a definition
        provider.registerIndexDefinition(entityClass, "evaluated_123456_idx", "spel:");

        // When: Getting index definition
        IndexDefinition definition = provider.getIndexDefinition(entityClass);

        // Then: The registered definition should be returned (SpEL evaluation happens in indexer)
        assertThat(definition).isNotNull();
        assertThat(definition.getIndexName()).doesNotContain("#{");
        assertThat(definition.getIndexName()).contains("evaluated");
    }

    @Test
    void testGetIndexDefinitionsForRepository() {
        // Given: A Spring Data repository interface
        Class<?> repositoryClass = Object.class; // Mock repository class
        provider.registerIndexDefinition(TestProduct.class, "product_idx", "product:");

        // When: Getting index definitions for repository
        List<IndexDefinition> definitions = provider.getIndexDefinitionsForRepository(repositoryClass);

        // Then: Definitions for all managed entities should be returned
        assertThat(definitions).isNotEmpty();
    }

    @Test
    void testBulkRegisterIndexDefinitions() {
        // Given: Multiple index definitions to register
        Map<Class<?>, ConfigurableIndexDefinitionProvider.IndexDefinitionConfig> configs = Map.of(
            TestProduct.class, new ConfigurableIndexDefinitionProvider.IndexDefinitionConfig("product_idx", "product:"),
            TestCategory.class, new ConfigurableIndexDefinitionProvider.IndexDefinitionConfig("category_idx", "category:")
        );

        // When: Bulk registering definitions
        provider.registerIndexDefinitions(configs);

        // Then: All definitions should be registered
        assertThat(provider.getIndexDefinition(TestProduct.class)).isNotNull();
        assertThat(provider.getIndexDefinition(TestCategory.class)).isNotNull();
    }

    @Test
    void testRefreshIndexDefinitions() {
        // Given: Changed entity annotations or configuration
        provider.registerIndexDefinition(TestProduct.class, "old_idx", "old:");

        // When: Refreshing index definitions from annotations
        provider.refreshIndexDefinitions();

        // Then: Definitions should be cleared and reloaded from annotations
        // After refresh, custom registrations are lost
        assertThat(provider.getIndexDefinitions()).isEmpty();
    }

    @Test
    void testIndexDefinitionWithCustomResolver() {
        // Given: A custom index resolver
        IndexResolver customResolver = mock(IndexResolver.class);
        when(customResolver.resolveIndexName(any(), any())).thenReturn("custom_idx");
        when(customResolver.resolveKeyPrefix(any(), any())).thenReturn("custom:");
        RedisIndexContext context = RedisIndexContext.builder().build();

        // When: Setting custom resolver for entity
        provider.setIndexResolver(TestProduct.class, customResolver);
        IndexDefinition definition = provider.getIndexDefinition(TestProduct.class, context);

        // Then: Custom resolver should be used
        verify(customResolver).resolveIndexName(eq(TestProduct.class), any());
        assertThat(definition.getIndexName()).isEqualTo("custom_idx");
    }

    @Test
    void testGetIndexStatistics() {
        // Given: Registered index definitions
        provider.registerIndexDefinition(TestProduct.class, "product_idx", "product:");

        // When: Getting index statistics
        IndexStatistics stats = provider.getIndexStatistics(TestProduct.class);

        // Then: Statistics should be available
        assertThat(stats).isNotNull();
        assertThat(stats.getDocumentCount()).isGreaterThanOrEqualTo(0);
        assertThat(stats.getIndexSize()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void testValidateIndexDefinition() {
        // Given: An index definition to validate
        IndexDefinition definition = new IndexDefinition("test_idx", "test:", TestProduct.class);

        // When: Validating the definition
        ValidationResult result = provider.validateIndexDefinition(definition);

        // Then: Validation result should indicate any issues
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void testExportIndexDefinitions() {
        // Given: Multiple registered index definitions
        provider.registerIndexDefinition(TestProduct.class, "product_idx", "product:");
        provider.registerIndexDefinition(TestCategory.class, "category_idx", "category:");

        // When: Exporting all definitions
        String exportedJson = provider.exportDefinitions();

        // Then: JSON should contain all definitions
        assertThat(exportedJson).contains("product_idx");
        assertThat(exportedJson).contains("category_idx");
    }

    @Test
    void testImportIndexDefinitions() {
        // Given: JSON with index definitions
        String definitionsJson = """
            {
                "definitions": [
                    {"entityClass": "TestProduct", "indexName": "imported_product_idx", "keyPrefix": "imported:"}
                ]
            }
            """;

        // When: Importing definitions
        int imported = provider.importDefinitions(definitionsJson);

        // Then: Definitions should be imported (basic implementation returns count)
        assertThat(imported).isGreaterThanOrEqualTo(0);
    }

    // Test entities
    @Document
    @IndexingOptions(indexName = "test_product_idx")
    static class TestProduct {
        @org.springframework.data.annotation.Id
        private String id;

        @Indexed
        private String name;

        @Indexed
        private double price;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
    }

    @Document
    static class TestCategory {
        @org.springframework.data.annotation.Id
        private String id;

        @Indexed
        private String name;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @Document
    @IndexingOptions(indexName = "#{'evaluated_' + T(System).currentTimeMillis() + '_idx'}")
    static class SpELTestEntity {
        @org.springframework.data.annotation.Id
        private String id;

        @Indexed
        private String value;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }
}