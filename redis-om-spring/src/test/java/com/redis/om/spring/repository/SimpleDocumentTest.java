package com.redis.om.spring.repository;

import com.redis.om.spring.AbstractBaseDocumentTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

class SimpleDocumentTest extends AbstractBaseDocumentTest {

    private static final int SIZE = 20;

    private final SimpleDocumentRepository simpleDocumentRepository;

    @Autowired
    SimpleDocumentTest(SimpleDocumentRepository simpleDocumentRepository) {
        this.simpleDocumentRepository = simpleDocumentRepository;
    }

    @BeforeEach
    void setUp() {
        for (int i = 0; i < SIZE; ++i) {
            simpleDocumentRepository.save(new SimpleDocument());
        }
    }

    @Test
    void testAllSimpleDocumentsReturned() {
        assumeThat(simpleDocumentRepository.count()).isEqualTo(SIZE);

        List<SimpleDocument> documents = simpleDocumentRepository.findAll();
        assertThat(documents).isNotNull().hasSize(SIZE);
    }

    @AfterEach
    void tearDown() {
        simpleDocumentRepository.deleteAll();
    }
}
