package com.redis.om.spring.indexing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redis.om.spring.AbstractBaseOMTest;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.IndexingOptions;
import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import com.redis.om.spring.repository.RedisDocumentRepository;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext
@SpringBootTest(classes = RealIntegrationTest.TestConfig.class)
public class RealIntegrationTest extends AbstractBaseOMTest {

    @SpringBootApplication
    @Configuration
    @EnableRedisDocumentRepositories(basePackageClasses = RealIntegrationTest.class)
    static class TestConfig {
        @Bean
        public IndexMigrationService indexMigrationService(RediSearchIndexer indexer,
                ApplicationContext ctx) {
            return new IndexMigrationService(indexer, ctx);
        }
    }

    @Autowired
    private RediSearchIndexer indexer;

    @Autowired
    private IndexMigrationService migrationService;

    @BeforeEach
    void cleanup() {
        RedisIndexContext.clearContext();
    }

    @Test
    void testBasicSetup() {
        // Verify beans are wired
        assertThat(indexer).isNotNull();
        assertThat(migrationService).isNotNull();
    }

    @Test
    void testCreateVersionedIndex() {
        // When: Create versioned index
        String indexName = migrationService.createVersionedIndex(TestDoc.class);

        // Then: Index should have version
        assertThat(indexName).isNotNull();
        assertThat(indexName).contains("_v");
        assertThat(indexName).contains("_idx");

        // The actual versioning depends on whether the index exists in Redis
        // which may not work correctly in test environment
    }

    @Test
    void testContextWorks() {
        // Given: Context with tenant
        RedisIndexContext ctx = RedisIndexContext.builder()
            .tenantId("test_tenant")
            .build();

        // When: Set context
        RedisIndexContext.setContext(ctx);

        // Then: Context is available
        assertThat(RedisIndexContext.getContext()).isNotNull();
        assertThat(RedisIndexContext.getContext().getTenantId()).isEqualTo("test_tenant");

        // Cleanup
        RedisIndexContext.clearContext();
    }

    @Test
    void testSpELInAnnotation() {
        // Given: Entity with SpEL expression
        TestDocWithSpEL doc = new TestDocWithSpEL();
        doc.setName("Test");

        // This test just verifies the annotation can be parsed
        // Real SpEL evaluation would need DefaultIndexResolver
        IndexingOptions opts = TestDocWithSpEL.class.getAnnotation(IndexingOptions.class);
        assertThat(opts).isNotNull();
        assertThat(opts.indexName()).contains("#{");
    }

    // Entities

    @Document
    static class TestDoc {
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
    @IndexingOptions(indexName = "#{'test_' + T(System).currentTimeMillis() + '_idx'}")
    static class TestDocWithSpEL {
        @org.springframework.data.annotation.Id
        private String id;

        @Indexed
        private String name;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}