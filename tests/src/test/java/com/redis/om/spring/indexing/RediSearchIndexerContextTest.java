package com.redis.om.spring.indexing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.IndexingOptions;
import com.redis.om.spring.annotations.Indexed;

import redis.clients.jedis.search.IndexDefinition;

public class RediSearchIndexerContextTest {

    @Mock
    private RediSearchIndexer indexer;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private Environment environment;

    @Mock
    private IndexResolver indexResolver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(applicationContext.getEnvironment()).thenReturn(environment);
        RedisIndexContext.clearContext();
    }

    @Test
    void testCreateIndexForContextWithResolver() {
        // Given: A context and a resolver
        RedisIndexContext context = RedisIndexContext.builder()
            .tenantId("test-tenant")
            .environment("staging")
            .build();

        when(indexResolver.resolveIndexName(ContextTestEntity.class, context))
            .thenReturn("context_test_entity_test-tenant_staging_idx");
        when(indexResolver.resolveKeyPrefix(ContextTestEntity.class, context))
            .thenReturn("test-tenant:context:test:entity:");

        // Mock the indexer to accept our resolved names
        when(indexer.indexExistsFor(eq(ContextTestEntity.class), anyString())).thenReturn(false);
        when(indexer.createIndexFor(eq(ContextTestEntity.class), anyString(), anyString())).thenReturn(true);

        // Use doCallRealMethod for the method we're actually testing
        when(indexer.createIndexFor(eq(ContextTestEntity.class), eq(context), eq(indexResolver)))
            .thenCallRealMethod();

        // When: Creating index with context
        boolean result = indexer.createIndexFor(ContextTestEntity.class, context, indexResolver);

        // Then: Should create index with resolved names
        verify(indexer).createIndexFor(
            ContextTestEntity.class,
            "context_test_entity_test-tenant_staging_idx",
            "test-tenant:context:test:entity:"
        );
        assertThat(result).isTrue();
    }

    @Test
    void testCreateIndexForContextWithNullContext() {
        // Given: No context (null)
        when(indexResolver.resolveIndexName(ContextTestEntity.class, null))
            .thenReturn("context_test_entity_idx");
        when(indexResolver.resolveKeyPrefix(ContextTestEntity.class, null))
            .thenReturn("context:test:entity:");

        when(indexer.indexExistsFor(eq(ContextTestEntity.class), anyString())).thenReturn(false);
        when(indexer.createIndexFor(eq(ContextTestEntity.class), anyString(), anyString())).thenReturn(true);

        // Use doCallRealMethod for the method we're actually testing
        when(indexer.createIndexFor(eq(ContextTestEntity.class), eq((RedisIndexContext)null), eq(indexResolver)))
            .thenCallRealMethod();

        // When: Creating index with null context
        boolean result = indexer.createIndexFor(ContextTestEntity.class, null, indexResolver);

        // Then: Should create index with default resolved names
        verify(indexer).createIndexFor(
            ContextTestEntity.class,
            "context_test_entity_idx",
            "context:test:entity:"
        );
        assertThat(result).isTrue();
    }

    @Test
    void testCreateIndexForContextSkipsIfExists() {
        // Given: Index already exists
        RedisIndexContext context = RedisIndexContext.builder()
            .tenantId("existing-tenant")
            .build();

        when(indexResolver.resolveIndexName(ContextTestEntity.class, context))
            .thenReturn("context_test_entity_existing-tenant_idx");
        when(indexResolver.resolveKeyPrefix(ContextTestEntity.class, context))
            .thenReturn("existing-tenant:context:test:entity:");
        when(indexer.indexExistsFor(eq(ContextTestEntity.class), anyString())).thenReturn(true);

        // Use doCallRealMethod for the method we're actually testing
        when(indexer.createIndexFor(eq(ContextTestEntity.class), eq(context), eq(indexResolver)))
            .thenCallRealMethod();

        // When: Creating index with context but index exists
        boolean result = indexer.createIndexFor(ContextTestEntity.class, context, indexResolver);

        // Then: Should skip creation and return true
        verify(indexer, never()).createIndexFor(eq(ContextTestEntity.class), anyString(), anyString());
        assertThat(result).isTrue();
    }

    @Test
    void testCreateIndexForContextWithThreadLocalContext() {
        // Given: Context set in thread-local
        RedisIndexContext context = RedisIndexContext.builder()
            .tenantId("thread-local-tenant")
            .setAttribute("version", "2.0")
            .build();

        // Set context in thread-local
        RedisIndexContext.setContext(context);

        try {
            when(indexResolver.resolveIndexName(ContextTestEntity.class, context))
                .thenReturn("context_test_entity_thread-local-tenant_2_0_idx");
            when(indexResolver.resolveKeyPrefix(ContextTestEntity.class, context))
                .thenReturn("thread-local-tenant:context:test:entity:");

            when(indexer.indexExistsFor(eq(ContextTestEntity.class), anyString())).thenReturn(false);
            when(indexer.createIndexFor(eq(ContextTestEntity.class), anyString(), anyString())).thenReturn(true);

            // Use doCallRealMethod for the methods we're actually testing
            when(indexer.createIndexForContext(eq(ContextTestEntity.class), eq(indexResolver)))
                .thenCallRealMethod();
            when(indexer.createIndexFor(eq(ContextTestEntity.class), eq(context), eq(indexResolver)))
                .thenCallRealMethod();

            // When: Creating index (should use thread-local context)
            boolean result = indexer.createIndexForContext(ContextTestEntity.class, indexResolver);

            // Then: Should use thread-local context
            verify(indexer).createIndexFor(
                ContextTestEntity.class,
                "context_test_entity_thread-local-tenant_2_0_idx",
                "thread-local-tenant:context:test:entity:"
            );
            assertThat(result).isTrue();
        } finally {
            RedisIndexContext.clearContext();
        }
    }

    @Test
    void testCreateIndexForContextWithMultipleTenants() {
        // Given: Multiple tenant contexts
        String[] tenants = {"tenant-a", "tenant-b", "tenant-c"};

        // Use doCallRealMethod for the method we're actually testing
        when(indexer.createIndexFor(eq(ContextTestEntity.class), any(RedisIndexContext.class), eq(indexResolver)))
            .thenCallRealMethod();

        for (String tenantId : tenants) {
            RedisIndexContext context = RedisIndexContext.builder()
                .tenantId(tenantId)
                .build();

            when(indexResolver.resolveIndexName(ContextTestEntity.class, context))
                .thenReturn("context_test_entity_" + tenantId + "_idx");
            when(indexResolver.resolveKeyPrefix(ContextTestEntity.class, context))
                .thenReturn(tenantId + ":context:test:entity:");

            when(indexer.indexExistsFor(eq(ContextTestEntity.class), anyString())).thenReturn(false);
            when(indexer.createIndexFor(eq(ContextTestEntity.class), anyString(), anyString())).thenReturn(true);

            // When: Creating index for each tenant
            boolean result = indexer.createIndexFor(ContextTestEntity.class, context, indexResolver);

            // Then: Should create tenant-specific index
            verify(indexer).createIndexFor(
                ContextTestEntity.class,
                "context_test_entity_" + tenantId + "_idx",
                tenantId + ":context:test:entity:"
            );
            assertThat(result).isTrue();
        }
    }

    @Test
    void testDropIndexForContext() {
        // Given: A context with existing index
        RedisIndexContext context = RedisIndexContext.builder()
            .tenantId("drop-tenant")
            .build();

        when(indexResolver.resolveIndexName(ContextTestEntity.class, context))
            .thenReturn("context_test_entity_drop-tenant_idx");
        when(indexer.indexExistsFor(eq(ContextTestEntity.class), anyString())).thenReturn(true);
        when(indexer.dropIndexFor(eq(ContextTestEntity.class), anyString())).thenReturn(true);

        // Use doCallRealMethod for the method we're actually testing
        when(indexer.dropIndexFor(eq(ContextTestEntity.class), eq(context), eq(indexResolver)))
            .thenCallRealMethod();

        // When: Dropping index with context
        boolean result = indexer.dropIndexFor(ContextTestEntity.class, context, indexResolver);

        // Then: Should drop the context-specific index
        verify(indexer).dropIndexFor(ContextTestEntity.class, "context_test_entity_drop-tenant_idx");
        assertThat(result).isTrue();
    }

    @Test
    void testIndexExistsForContext() {
        // Given: A context
        RedisIndexContext context = RedisIndexContext.builder()
            .tenantId("exists-tenant")
            .build();

        when(indexResolver.resolveIndexName(ContextTestEntity.class, context))
            .thenReturn("context_test_entity_exists-tenant_idx");
        when(indexer.indexExistsFor(eq(ContextTestEntity.class), anyString())).thenReturn(true);

        // Use doCallRealMethod for the method we're actually testing
        when(indexer.indexExistsFor(eq(ContextTestEntity.class), eq(context), eq(indexResolver)))
            .thenCallRealMethod();

        // When: Checking if index exists with context
        boolean exists = indexer.indexExistsFor(ContextTestEntity.class, context, indexResolver);

        // Then: Should check the context-specific index
        verify(indexer).indexExistsFor(ContextTestEntity.class, "context_test_entity_exists-tenant_idx");
        assertThat(exists).isTrue();
    }

    @Test
    void testGetIndexNameForContext() {
        // Given: A context
        RedisIndexContext context = RedisIndexContext.builder()
            .tenantId("name-tenant")
            .environment("production")
            .build();

        when(indexResolver.resolveIndexName(ContextTestEntity.class, context))
            .thenReturn("context_test_entity_name-tenant_production_idx");

        // Use doCallRealMethod for the method we're actually testing
        when(indexer.getIndexName(eq(ContextTestEntity.class), eq(context), eq(indexResolver)))
            .thenCallRealMethod();

        // When: Getting index name with context
        String indexName = indexer.getIndexName(ContextTestEntity.class, context, indexResolver);

        // Then: Should return the context-specific index name
        assertThat(indexName).isEqualTo("context_test_entity_name-tenant_production_idx");
    }

    @Test
    void testGetKeyspacePrefixForContext() {
        // Given: A context
        RedisIndexContext context = RedisIndexContext.builder()
            .tenantId("keyspace-tenant")
            .build();

        when(indexResolver.resolveKeyPrefix(ContextTestEntity.class, context))
            .thenReturn("keyspace-tenant:context:test:entity:");

        // Use doCallRealMethod for the method we're actually testing
        when(indexer.getKeyspacePrefix(eq(ContextTestEntity.class), eq(context), eq(indexResolver)))
            .thenCallRealMethod();

        // When: Getting keyspace prefix with context
        String keyPrefix = indexer.getKeyspacePrefix(ContextTestEntity.class, context, indexResolver);

        // Then: Should return the context-specific key prefix
        assertThat(keyPrefix).isEqualTo("keyspace-tenant:context:test:entity:");
    }

    // Test entity for context testing
    @Document
    @IndexingOptions(indexName = "context_test_entity_idx", keyPrefix = "context:test:entity:")
    static class ContextTestEntity {
        @org.springframework.data.annotation.Id
        private String id;

        @Indexed
        private String name;

        @Indexed
        private String category;

        // getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }
}