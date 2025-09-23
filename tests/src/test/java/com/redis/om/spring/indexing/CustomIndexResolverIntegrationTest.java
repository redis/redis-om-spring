package com.redis.om.spring.indexing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.data.annotation.Id;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

/**
 * Integration test for custom IndexResolver implementations.
 */
@DirtiesContext
public class CustomIndexResolverIntegrationTest extends AbstractBaseDocumentTest {

    @Autowired
    private RediSearchIndexer indexer;

    @Test
    void testIndexResolverWithCustomPrefix() throws InterruptedException {
        // Given: First ensure the test entity doesn't have an index already
        if (indexer.indexExistsFor(CustomResolverTestEntity.class)) {
            indexer.dropIndexFor(CustomResolverTestEntity.class);
        }

        // Give time for drop to complete
        Thread.sleep(500);

        // Given: A custom IndexResolver that adds a prefix
        IndexResolver customResolver = new IndexResolver() {
            @Override
            public String resolveIndexName(Class<?> entityClass, RedisIndexContext context) {
                return "custom_" + entityClass.getSimpleName() + "_idx";
            }

            @Override
            public String resolveKeyPrefix(Class<?> entityClass, RedisIndexContext context) {
                return "custom:" + entityClass.getSimpleName().toLowerCase() + ":";
            }
        };

        // When: Create index using the custom resolver
        String indexName = customResolver.resolveIndexName(CustomResolverTestEntity.class, null);
        String keyPrefix = customResolver.resolveKeyPrefix(CustomResolverTestEntity.class, null);

        boolean created = indexer.createIndexFor(CustomResolverTestEntity.class, indexName, keyPrefix);

        // Then: Index should be created with custom name
        assertThat(indexName).isEqualTo("custom_CustomResolverTestEntity_idx");
        assertThat(created).as("Index creation should succeed").isTrue();

        // Give time for index creation to complete
        Thread.sleep(500);

        assertThat(indexer.indexExistsFor(CustomResolverTestEntity.class, indexName))
            .as("Index should exist after creation").isTrue();

        // Cleanup
        indexer.dropIndexFor(CustomResolverTestEntity.class, indexName);
    }

    @Document
    static class CustomResolverTestEntity {
        @Id
        private String id;

        @Indexed
        private String name;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}