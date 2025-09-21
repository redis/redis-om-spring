package com.redis.om.spring.indexing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.IndexingOptions;
import com.redis.om.spring.services.TenantResolver;

/**
 * Edge case tests for entity classes with SpEL expressions.
 * These tests should have been written FIRST to cover all the edge cases.
 */
@TestPropertySource(properties = {
    "app.tenant=test_tenant",
    "app.environment=test",
    "app.version=1.0.0",
    "app.malformed=",
    "app.null.value="
})
@DirtiesContext
@SpringBootTest(classes = EntityClassSpelEdgeCasesTest.TestConfig.class)
public class EntityClassSpelEdgeCasesTest {

    @SpringBootApplication
    @Configuration
    @EnableRedisDocumentRepositories(basePackageClasses = EntityClassSpelEdgeCasesTest.class)
    static class TestConfig {
        @Bean
        public TenantResolver tenantResolver() {
            return new TenantResolver();
        }

        @Bean
        public FailingService failingService() {
            return new FailingService();
        }

        @Bean
        public NullReturningService nullReturningService() {
            return new NullReturningService();
        }
    }

    @Autowired
    private RediSearchIndexer indexer;

    @Autowired
    private Environment environment;

    @BeforeEach
    void cleanup() {
        // Clean up any existing indices
        try {
            indexer.dropIndexFor(MalformedSpelEntity.class);
            indexer.dropIndexFor(ThrowingSpelEntity.class);
            indexer.dropIndexFor(NullReturningSpelEntity.class);
            indexer.dropIndexFor(EmptyStringSpelEntity.class);
            indexer.dropIndexFor(VeryLongSpelEntity.class);
            indexer.dropIndexFor(SqlInjectionSpelEntity.class);
            indexer.dropIndexFor(RecursiveSpelEntity.class);
            indexer.dropIndexFor(SpecialCharacterSpelEntity.class);
            indexer.dropIndexFor(UnicodeSpelEntity.class);
            indexer.dropIndexFor(NumberSpelEntity.class);
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    @Test
    void testMalformedSpelExpression_FallsBackToDefault() {
        // Given: Entity with malformed SpEL expression
        Class<?> entityClass = MalformedSpelEntity.class;

        // When: Creating index
        indexer.createIndexFor(entityClass);

        // Then: Should fall back to default naming
        String indexName = indexer.getIndexName(entityClass);
        assertThat(indexName).isEqualTo("com.redis.om.spring.indexing.EntityClassSpelEdgeCasesTest$MalformedSpelEntityIdx");
    }

    @Test
    void testThrowingBeanMethod_FallsBackToDefault() {
        // Given: Entity with SpEL that calls method throwing exception
        Class<?> entityClass = ThrowingSpelEntity.class;

        // When: Creating index
        indexer.createIndexFor(entityClass);

        // Then: Should fall back to default naming
        String indexName = indexer.getIndexName(entityClass);
        assertThat(indexName).isEqualTo("com.redis.om.spring.indexing.EntityClassSpelEdgeCasesTest$ThrowingSpelEntityIdx");
    }

    @Test
    void testNullReturningBean_HandlesNullGracefully() {
        // Given: Entity with SpEL that returns null
        Class<?> entityClass = NullReturningSpelEntity.class;

        // When: Creating index
        indexer.createIndexFor(entityClass);

        // Then: Should handle null return gracefully
        String indexName = indexer.getIndexName(entityClass);
        assertThat(indexName).satisfiesAnyOf(
            name -> assertThat(name).isEqualTo("null_idx"),
            name -> assertThat(name).isEqualTo("com.redis.om.spring.indexing.EntityClassSpelEdgeCasesTest$NullReturningSpelEntityIdx")
        );
    }

    @Test
    void testEmptyStringProperty_HandlesEmptyString() {
        // Given: Entity with SpEL referencing empty property
        Class<?> entityClass = EmptyStringSpelEntity.class;

        // When: Creating index
        indexer.createIndexFor(entityClass);

        // Then: Should handle empty string appropriately
        String indexName = indexer.getIndexName(entityClass);
        assertThat(indexName).satisfiesAnyOf(
            name -> assertThat(name).isEqualTo("_idx"),
            name -> assertThat(name).isEqualTo("com.redis.om.spring.indexing.EntityClassSpelEdgeCasesTest$EmptyStringSpelEntityIdx")
        );
    }

    @Test
    void testVeryLongSpelExpression_HandlesLongString() {
        // Given: Entity with very long SpEL expression
        Class<?> entityClass = VeryLongSpelEntity.class;

        // When: Creating index
        indexer.createIndexFor(entityClass);

        // Then: Should handle long result appropriately
        String indexName = indexer.getIndexName(entityClass);
        assertThat(indexName).hasSizeGreaterThan(50);
        assertThat(indexName).contains("VERY_LONG_INDEX_NAME");
    }

    @Test
    void testSqlInjectionAttempt_SafelyHandled() {
        // Given: Entity with SQL injection-like SpEL
        Class<?> entityClass = SqlInjectionSpelEntity.class;

        // When: Creating index
        indexer.createIndexFor(entityClass);

        // Then: Should safely handle the expression
        String indexName = indexer.getIndexName(entityClass);
        assertThat(indexName).doesNotContain("DROP");
        assertThat(indexName).doesNotContain("DELETE");
    }

    @Test
    void testRecursiveSpelExpression_DoesNotCauseStackOverflow() {
        // Given: Entity with potentially recursive SpEL
        Class<?> entityClass = RecursiveSpelEntity.class;

        // When: Creating index (should not hang or throw StackOverflowError)
        indexer.createIndexFor(entityClass);

        // Then: Should complete without error
        String indexName = indexer.getIndexName(entityClass);
        assertThat(indexName).isNotNull();
    }

    @Test
    void testSpecialCharacters_ProperlyEscaped() {
        // Given: Entity with special characters in SpEL
        Class<?> entityClass = SpecialCharacterSpelEntity.class;

        // When: Creating index
        indexer.createIndexFor(entityClass);

        // Then: Should handle special characters
        String indexName = indexer.getIndexName(entityClass);
        assertThat(indexName).contains("special");
        // Should not contain unescaped special characters that could break Redis
        assertThat(indexName).doesNotContain(" ");
        assertThat(indexName).doesNotContain("\n");
        assertThat(indexName).doesNotContain("\t");
    }

    @Test
    void testUnicodeCharacters_ProperlyHandled() {
        // Given: Entity with Unicode characters in SpEL
        Class<?> entityClass = UnicodeSpelEntity.class;

        // When: Creating index
        indexer.createIndexFor(entityClass);

        // Then: Should handle Unicode appropriately
        String indexName = indexer.getIndexName(entityClass);
        assertThat(indexName).contains("unicode");
    }

    @Test
    void testNumbericResult_ConvertedToString() {
        // Given: Entity with SpEL returning number
        Class<?> entityClass = NumberSpelEntity.class;

        // When: Creating index
        indexer.createIndexFor(entityClass);

        // Then: Should convert number to string
        String indexName = indexer.getIndexName(entityClass);
        assertThat(indexName).isEqualTo("42_idx");
    }

    @Test
    void testConcurrentIndexCreation_ThreadSafe() throws InterruptedException {
        // Given: Multiple threads creating indices simultaneously
        Class<?>[] entityClasses = {
            MalformedSpelEntity.class,
            ThrowingSpelEntity.class,
            NullReturningSpelEntity.class
        };

        String[] results = new String[entityClasses.length];
        Thread[] threads = new Thread[entityClasses.length];

        // When: Creating indices concurrently
        for (int i = 0; i < entityClasses.length; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    indexer.createIndexFor(entityClasses[index]);
                    results[index] = indexer.getIndexName(entityClasses[index]);
                } catch (Exception e) {
                    results[index] = "ERROR: " + e.getMessage();
                }
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then: All should complete successfully
        for (String result : results) {
            assertThat(result).isNotNull();
            assertThat(result).doesNotStartWith("ERROR:");
        }
    }

    // Test entity classes with edge cases

    @Document
    @IndexingOptions(indexName = "#{unclosed.bracket")
    static class MalformedSpelEntity {
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
    @IndexingOptions(indexName = "#{@failingService.throwException()}")
    static class ThrowingSpelEntity {
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
    @IndexingOptions(indexName = "#{@nullReturningService.returnNull()}_idx")
    static class NullReturningSpelEntity {
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
    @IndexingOptions(indexName = "#{@environment.getProperty('app.malformed')}_idx")
    static class EmptyStringSpelEntity {
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
    @IndexingOptions(indexName = "#{'VERY_LONG_INDEX_NAME_' + T(java.util.Collections).nCopies(10, 'REPEATED_SEGMENT_').toString().replace('[', '').replace(']', '').replace(', ', '_')}")
    static class VeryLongSpelEntity {
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
    @IndexingOptions(indexName = "#{'safe_index'; DROP TABLE users; --'}")
    static class SqlInjectionSpelEntity {
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
    @IndexingOptions(indexName = "#{T(java.lang.Math).max(1, 2) > 0 ? 'recursive_idx' : @environment.getProperty('app.tenant')}")
    static class RecursiveSpelEntity {
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
    @IndexingOptions(indexName = "#{'special_chars_' + T(java.net.URLEncoder).encode('test@domain.com#section?param=value&other=test', 'UTF-8').replace('%', '_')}")
    static class SpecialCharacterSpelEntity {
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
    @IndexingOptions(indexName = "#{'unicode_测试_مرحبا_こんにちは_' + @environment.getProperty('app.tenant')}")
    static class UnicodeSpelEntity {
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
    @IndexingOptions(indexName = "#{42}_idx")
    static class NumberSpelEntity {
        @org.springframework.data.annotation.Id
        private String id;

        @Indexed
        private String name;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    // Helper services for testing
    static class FailingService {
        public String throwException() {
            throw new RuntimeException("Bean method failed");
        }
    }

    static class NullReturningService {
        public String returnNull() {
            return null;
        }
    }
}