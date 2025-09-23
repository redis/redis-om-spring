package com.redis.om.spring.indexing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.test.annotation.DirtiesContext;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.exceptions.JedisDataException;
import java.util.Objects;

/**
 * Integration test for IndexMigrationService.
 * Tests real Redis index migration operations including aliasing.
 */
@DirtiesContext
public class IndexMigrationServiceIntegrationTest extends AbstractBaseDocumentTest {

    @Autowired
    private IndexMigrationService migrationService;

    @Autowired
    private RediSearchIndexer indexer;

    @Autowired
    private JedisConnectionFactory jedisConnectionFactory;

    private UnifiedJedis jedis;

    @BeforeEach
    void setup() throws InterruptedException {
        // Create JedisPooled manually
        jedis = new JedisPooled(Objects.requireNonNull(jedisConnectionFactory.getPoolConfig()),
            jedisConnectionFactory.getHostName(), jedisConnectionFactory.getPort());

        // Ensure clean state
        if (indexer.indexExistsFor(MigrationIntegrationEntity.class)) {
            indexer.dropIndexFor(MigrationIntegrationEntity.class);
            Thread.sleep(500); // Allow time for drop
        }
    }

    @Test
    void testBlueGreenMigrationWithRealAliasing() throws InterruptedException {
        // Given: Create initial index
        indexer.createIndexFor(MigrationIntegrationEntity.class);
        Thread.sleep(500); // Allow time for index creation

        assertThat(indexer.indexExistsFor(MigrationIntegrationEntity.class))
            .as("Initial index should exist").isTrue();

        String originalIndexName = indexer.getIndexName(MigrationIntegrationEntity.class);
        assertThat(originalIndexName).isNotNull();

        // When: Perform blue-green migration
        MigrationResult result = migrationService.migrateIndex(
            MigrationIntegrationEntity.class,
            MigrationStrategy.BLUE_GREEN
        );

        // Then: Migration should succeed
        assertThat(result.isSuccessful())
            .as("Blue-green migration should succeed")
            .isTrue();
        assertThat(result.getOldIndexName()).isEqualTo(originalIndexName);
        assertThat(result.getNewIndexName()).isNotEqualTo(originalIndexName);
        assertThat(result.getNewIndexName()).contains("_v");

        // Verify alias was actually created in Redis
        String aliasName = MigrationIntegrationEntity.class.getSimpleName().toLowerCase() + "_alias";

        // Check if alias exists and points to new index
        // This will fail because switchAlias doesn't actually create Redis aliases
        String actualIndex = (String) jedis.ftInfo(aliasName).get("index_name");
        assertThat(actualIndex)
            .as("Alias should point to the new versioned index")
            .isEqualTo(result.getNewIndexName());
    }

    @Document
    static class MigrationIntegrationEntity {
        @Id
        private String id;

        @Indexed
        private String name;

        @Indexed
        private Integer version;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getVersion() { return version; }
        public void setVersion(Integer version) { this.version = version; }
    }
}