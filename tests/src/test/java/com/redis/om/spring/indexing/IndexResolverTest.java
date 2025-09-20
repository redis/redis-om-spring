package com.redis.om.spring.indexing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.IndexingOptions;
import com.redis.om.spring.fixtures.document.model.TenantAwareEntity;
import com.redis.om.spring.indexing.RedisIndexContext;
import com.redis.om.spring.indexing.IndexResolver;
import com.redis.om.spring.indexing.DefaultIndexResolver;

import static org.mockito.Mockito.*;

public class IndexResolverTest {

    private ApplicationContext applicationContext;
    private Environment environment;

    @BeforeEach
    void setUp() {
        applicationContext = mock(ApplicationContext.class);
        environment = mock(Environment.class);
        when(applicationContext.getEnvironment()).thenReturn(environment);
        RedisIndexContext.clearContext();
    }

    @Test
    void testDefaultIndexResolverWithoutContext() {
        // Given: A default resolver and an entity class
        IndexResolver resolver = new DefaultIndexResolver(applicationContext);

        // When: Resolving without context
        String indexName = resolver.resolveIndexName(TestEntity.class, null);
        String keyPrefix = resolver.resolveKeyPrefix(TestEntity.class, null);

        // Then: Should return default values
        assertThat(indexName).isEqualTo("test_entity_idx");
        assertThat(keyPrefix).isEqualTo("test:entity:");
    }

    @Test
    void testDefaultIndexResolverWithContext() {
        // Given: A default resolver and a context
        IndexResolver resolver = new DefaultIndexResolver(applicationContext);
        RedisIndexContext context = RedisIndexContext.builder()
            .tenantId("tenant123")
            .environment("production")
            .build();

        // When: Resolving with context (but TestEntity has plain annotation without SpEL)
        String indexName = resolver.resolveIndexName(TestEntity.class, context);
        String keyPrefix = resolver.resolveKeyPrefix(TestEntity.class, context);

        // Then: Should return plain annotation values (no SpEL means no context processing)
        assertThat(indexName).isEqualTo("test_entity_idx");
        assertThat(keyPrefix).isEqualTo("test:entity:");
    }

    @Test
    void testDefaultIndexResolverWithSpelExpression() {
        // Given: An entity with SpEL expressions in IndexingOptions
        IndexResolver resolver = new DefaultIndexResolver(applicationContext);
        RedisIndexContext context = RedisIndexContext.builder()
            .tenantId("spel-tenant")
            .environment("dev")
            .build();

        when(environment.getProperty("app.region")).thenReturn("us-west");
        when(applicationContext.getBean("environment")).thenReturn(environment);

        // When: Resolving SpEL-based entity
        String indexName = resolver.resolveIndexName(SpelEntity.class, context);
        String keyPrefix = resolver.resolveKeyPrefix(SpelEntity.class, context);

        // Then: Should evaluate SpEL expressions with context
        assertThat(indexName).isEqualTo("spel_entity_spel-tenant_us-west_idx");
        assertThat(keyPrefix).isEqualTo("spel-tenant:region:us-west:");
    }

    @Test
    void testCustomIndexResolver() {
        // Given: A custom resolver implementation
        IndexResolver customResolver = new IndexResolver() {
            @Override
            public String resolveIndexName(Class<?> entityClass, RedisIndexContext context) {
                if (context != null && context.getTenantId() != null) {
                    return String.format("custom_%s_%s", context.getTenantId(), entityClass.getSimpleName().toLowerCase());
                }
                return entityClass.getSimpleName().toLowerCase() + "_custom";
            }

            @Override
            public String resolveKeyPrefix(Class<?> entityClass, RedisIndexContext context) {
                if (context != null && context.getTenantId() != null) {
                    return String.format("custom:%s:", context.getTenantId());
                }
                return "custom:default:";
            }
        };

        RedisIndexContext context = RedisIndexContext.builder()
            .tenantId("custom-tenant")
            .build();

        // When: Using custom resolver
        String indexName = customResolver.resolveIndexName(TestEntity.class, context);
        String keyPrefix = customResolver.resolveKeyPrefix(TestEntity.class, context);

        // Then: Should use custom logic
        assertThat(indexName).isEqualTo("custom_custom-tenant_testentity");
        assertThat(keyPrefix).isEqualTo("custom:custom-tenant:");
    }

    @Test
    void testIndexResolverWithNullContext() {
        // Given: A resolver and null context
        IndexResolver resolver = new DefaultIndexResolver(applicationContext);

        // When: Resolving with null context
        String indexName = resolver.resolveIndexName(TestEntity.class, null);
        String keyPrefix = resolver.resolveKeyPrefix(TestEntity.class, null);

        // Then: Should handle gracefully and return defaults
        assertThat(indexName).isNotNull();
        assertThat(keyPrefix).isNotNull();
    }

    @Test
    void testIndexResolverWithContextAttributes() {
        // Given: A resolver with context containing custom attributes
        IndexResolver resolver = new DefaultIndexResolver(applicationContext);
        RedisIndexContext context = RedisIndexContext.builder()
            .tenantId("attr-tenant")
            .setAttribute("version", "v2")
            .setAttribute("shard", 3)
            .build();

        // When: Resolving with attributes
        String indexName = resolver.resolveIndexName(AttributeAwareEntity.class, context);
        String keyPrefix = resolver.resolveKeyPrefix(AttributeAwareEntity.class, context);

        // Then: Should incorporate attributes
        assertThat(indexName).contains("attr-tenant");
        assertThat(indexName).contains("v2");
        assertThat(keyPrefix).contains("attr-tenant");
        assertThat(keyPrefix).contains("shard3");
    }

    @Test
    void testChainedIndexResolver() {
        // Given: Multiple resolvers chained together
        IndexResolver primaryResolver = new DefaultIndexResolver(applicationContext);
        IndexResolver fallbackResolver = new IndexResolver() {
            @Override
            public String resolveIndexName(Class<?> entityClass, RedisIndexContext context) {
                // Fallback logic
                return "fallback_" + entityClass.getSimpleName().toLowerCase();
            }

            @Override
            public String resolveKeyPrefix(Class<?> entityClass, RedisIndexContext context) {
                return "fallback:";
            }
        };

        IndexResolver chainedResolver = new IndexResolver() {
            @Override
            public String resolveIndexName(Class<?> entityClass, RedisIndexContext context) {
                try {
                    String result = primaryResolver.resolveIndexName(entityClass, context);
                    if (result == null || result.isEmpty()) {
                        return fallbackResolver.resolveIndexName(entityClass, context);
                    }
                    return result;
                } catch (Exception e) {
                    return fallbackResolver.resolveIndexName(entityClass, context);
                }
            }

            @Override
            public String resolveKeyPrefix(Class<?> entityClass, RedisIndexContext context) {
                try {
                    String result = primaryResolver.resolveKeyPrefix(entityClass, context);
                    if (result == null || result.isEmpty()) {
                        return fallbackResolver.resolveKeyPrefix(entityClass, context);
                    }
                    return result;
                } catch (Exception e) {
                    return fallbackResolver.resolveKeyPrefix(entityClass, context);
                }
            }
        };

        // When: Using chained resolver
        String indexName = chainedResolver.resolveIndexName(TestEntity.class, null);

        // Then: Should use primary resolver result
        assertThat(indexName).isEqualTo("test_entity_idx");
    }

    @Test
    void testResolverWithDynamicTenantStrategy() {
        // Given: A resolver that determines tenant from different sources
        IndexResolver dynamicResolver = new DefaultIndexResolver(applicationContext) {
            @Override
            protected String getTenantId(RedisIndexContext context) {
                // Try context first
                if (context != null && context.getTenantId() != null) {
                    return context.getTenantId();
                }
                // Try environment variable
                String envTenant = System.getenv("TENANT_ID");
                if (envTenant != null) {
                    return envTenant;
                }
                // Default
                return "default";
            }
        };

        // When: Resolving an entity without annotation (will use default naming)
        RedisIndexContext dynamicContext = RedisIndexContext.builder()
            .tenantId("default")
            .build();
        String indexName = dynamicResolver.resolveIndexName(NoAnnotationEntity.class, dynamicContext);

        // Then: Should use the tenant from context with default naming
        assertThat(indexName).isEqualTo("no_annotation_entity_default_idx");
    }

    // Test entities
    @Document
    @IndexingOptions(indexName = "test_entity_idx", keyPrefix = "test:entity:")
    static class TestEntity {
        private String id;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }

    @Document
    static class NoAnnotationEntity {
        private String id;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }

    @Document
    @IndexingOptions(
        indexName = "spel_entity_#{#context.tenantId}_#{@environment.getProperty('app.region')}_idx",
        keyPrefix = "#{#context.tenantId}:region:#{@environment.getProperty('app.region')}:"
    )
    static class SpelEntity {
        private String id;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }

    @Document
    @IndexingOptions(
        indexName = "attr_entity_#{#context.tenantId}_#{#context.getAttribute('version')}_idx",
        keyPrefix = "#{#context.tenantId}:shard#{#context.getAttribute('shard')}:"
    )
    static class AttributeAwareEntity {
        private String id;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }
}