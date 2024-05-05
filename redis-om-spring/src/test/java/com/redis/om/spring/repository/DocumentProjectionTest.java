package com.redis.om.spring.repository;

import com.redis.om.spring.AbstractBaseDocumentTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DocumentProjectionTest extends AbstractBaseDocumentTest {

    public static final String TEST_NAME = "testName";
    public static final String TEST_PROP_1 = "test1";
    private final DocumentProjectionRepository documentProjectionRepository;

    @Autowired
    DocumentProjectionTest(DocumentProjectionRepository documentProjectionRepository) {
        this.documentProjectionRepository = documentProjectionRepository;
    }

    @BeforeEach
    void setUp() {
        for (int i = 0; i < 2; i++) {
            DocumentProjectionPojo entity = new DocumentProjectionPojo();
            entity.setName(TEST_NAME);
            entity.setTest(TEST_PROP_1);
            documentProjectionRepository.save(entity);
        }
    }

    @Test
    void testEntityProjection() {
        Optional<DocumentProjection> byNameProjection = documentProjectionRepository.findByName(TEST_NAME);
        assertTrue(byNameProjection.isPresent());
        assertEquals(TEST_NAME, byNameProjection.get().getName());
        assertNotNull(byNameProjection.get().getSpelTest());
        assertEquals(TEST_NAME + " " + TEST_PROP_1, byNameProjection.get().getSpelTest());
    }

    @Test
    void testCollectionProjection() {
        Collection<DocumentProjection> byNameProjection = documentProjectionRepository.findAllByName(TEST_NAME);
        assertNotNull(byNameProjection);
        assertEquals(2, byNameProjection.size());
        byNameProjection.forEach(documentProjection -> {
            assertNotNull(documentProjection.getName());
            assertNotNull(documentProjection.getSpelTest());
        });
    }

    @AfterEach
    void tearDown() {
        documentProjectionRepository.deleteAll();
    }

}
