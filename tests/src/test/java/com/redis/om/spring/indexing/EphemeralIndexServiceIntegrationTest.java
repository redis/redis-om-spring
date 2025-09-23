package com.redis.om.spring.indexing;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.test.annotation.DirtiesContext;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

/**
 * Integration test for EphemeralIndexService.
 * Tests ephemeral (temporary) indexes with TTL support.
 */
@DirtiesContext
public class EphemeralIndexServiceIntegrationTest extends AbstractBaseDocumentTest {

    @Autowired
    private EphemeralIndexService ephemeralIndexService;

    @Autowired
    private RediSearchIndexer indexer;

    @BeforeEach
    void setup() throws InterruptedException {
        // Ensure clean state
        String indexName = EphemeralTestEntity.class.getSimpleName().toLowerCase() + "_ephemeral_idx";
        if (indexer.indexExistsFor(EphemeralTestEntity.class, indexName)) {
            indexer.dropIndexFor(EphemeralTestEntity.class, indexName);
            Thread.sleep(500); // Allow time for drop
        }
    }

    @Test
    void testCreateEphemeralIndexWithTTL() throws InterruptedException {
        // Given: Define ephemeral index parameters
        Duration ttl = Duration.ofSeconds(5);
        String indexName = EphemeralTestEntity.class.getSimpleName().toLowerCase() + "_ephemeral_idx";

        // When: Create ephemeral index
        boolean created = ephemeralIndexService.createEphemeralIndex(
            EphemeralTestEntity.class,
            indexName,
            ttl
        );

        // Then: Index should be created
        assertThat(created).as("Ephemeral index creation should succeed").isTrue();

        // Verify index exists immediately
        assertThat(indexer.indexExistsFor(EphemeralTestEntity.class, indexName))
            .as("Ephemeral index should exist immediately after creation").isTrue();

        // Verify index is tracked as ephemeral
        assertThat(ephemeralIndexService.isEphemeralIndex(indexName))
            .as("Index should be tracked as ephemeral").isTrue();

        // Wait for TTL to expire plus buffer
        Thread.sleep(6000);

        // Then: Index should be automatically deleted after TTL
        assertThat(indexer.indexExistsFor(EphemeralTestEntity.class, indexName))
            .as("Ephemeral index should be deleted after TTL expires").isFalse();

        // Verify index is no longer tracked
        assertThat(ephemeralIndexService.isEphemeralIndex(indexName))
            .as("Index should no longer be tracked after deletion").isFalse();
    }

    @Test
    void testExtendEphemeralIndexTTL() throws InterruptedException {
        // Given: Create ephemeral index with short TTL
        Duration initialTtl = Duration.ofSeconds(3);
        String indexName = EphemeralTestEntity.class.getSimpleName().toLowerCase() + "_extendable_idx";

        ephemeralIndexService.createEphemeralIndex(
            EphemeralTestEntity.class,
            indexName,
            initialTtl
        );

        // Wait 2 seconds (index should still exist)
        Thread.sleep(2000);
        assertThat(indexer.indexExistsFor(EphemeralTestEntity.class, indexName))
            .as("Index should still exist before TTL expires").isTrue();

        // When: Extend TTL
        Duration newTtl = Duration.ofSeconds(5);
        boolean extended = ephemeralIndexService.extendTTL(indexName, newTtl);

        // Then: Extension should succeed
        assertThat(extended).as("TTL extension should succeed").isTrue();

        // Wait original TTL duration (3 seconds more)
        Thread.sleep(3000);

        // Index should still exist due to extension
        assertThat(indexer.indexExistsFor(EphemeralTestEntity.class, indexName))
            .as("Index should still exist after original TTL due to extension").isTrue();

        // Wait for extended TTL to expire
        Thread.sleep(3000);

        // Index should now be deleted
        assertThat(indexer.indexExistsFor(EphemeralTestEntity.class, indexName))
            .as("Index should be deleted after extended TTL expires").isFalse();
    }

    @Document
    static class EphemeralTestEntity {
        @Id
        private String id;

        @Indexed
        private String name;

        @Indexed
        private String category;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }
}