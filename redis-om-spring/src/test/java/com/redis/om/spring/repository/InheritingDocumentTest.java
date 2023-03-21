package com.redis.om.spring.repository;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

class InheritingDocumentTest extends AbstractBaseDocumentTest {
    private static final int SIZE = 20;

    private final InheritingDocumentRepository inheritingDocumentRepository;
    private final SearchOperations<?> searchOperations;

    @Autowired
    InheritingDocumentTest(InheritingDocumentRepository inheritingDocumentRepository, RedisModulesOperations<String> redisModulesOperations) {
        this.inheritingDocumentRepository = inheritingDocumentRepository;
        this.searchOperations = redisModulesOperations.opsForSearch(InheritingDocument.class.getName() + "Idx");
    }

    @BeforeEach
    void setUp() {
        for (int i = 0; i < SIZE; ++i) {
            inheritingDocumentRepository.save(new InheritingDocument());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void testIndexCreatedCorrectly() {
        Map<String, Object> info = searchOperations.getInfo();
        Object rawAttributes = info.get("attributes");
        assertThat(rawAttributes).isNotNull().isInstanceOf(List.class);
        List<?> attributes = (List<?>) rawAttributes;
        assertThat(attributes).hasSize(3)
                .allSatisfy(element -> {
                    assertThat(element).isInstanceOf(List.class);
                    assertThat((List<?>) element).allSatisfy(item -> assertThat(item).isInstanceOf(String.class));
                })
                .map(element -> (List<String>) element) // cast is checked through assertion
                .extracting((List<String> attribute) -> {
                    for (int i = 0; i < attribute.size(); i++) {
                        String value = attribute.get(i);
                        if ("attribute".equals(value)) {
                            return attribute.get(i + 1);
                        }
                    }
                    return null;
                })
                .doesNotContain((String) null)
                .containsExactlyInAnyOrderElementsOf(List.of("id", "inherited", "notInherited"));
    }

    @Test
    void testAllInheritedDocumentsReturned() {
        assumeThat(inheritingDocumentRepository.count()).isEqualTo(SIZE);

        List<InheritingDocument> documents = inheritingDocumentRepository.findAll();
        assertThat(documents).isNotNull().hasSize(SIZE);
    }

    @AfterEach
    void tearDown() {
        inheritingDocumentRepository.deleteAll();
    }
}
