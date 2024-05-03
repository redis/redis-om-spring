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
    public static final String TEST_REC_1 = "test1";
    public static final String TEST_REC_2 = "test2";
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
            DocumentProjectionPojo.RecursiveProjection recursiveProjection = new DocumentProjectionPojo.RecursiveProjection();
            recursiveProjection.setRecursiveProp1(TEST_REC_1);
            recursiveProjection.setRecursiveProp2(TEST_REC_2);
            entity.setRecursiveProjection(recursiveProjection);
            documentProjectionRepository.save(entity);
        }
    }

    @Test
    void testEntityProjection() {
        Optional<DocumentProjection> byNameProjection = documentProjectionRepository.findByName(TEST_NAME);
        assertTrue(byNameProjection.isPresent());
        assertEquals(TEST_NAME, byNameProjection.get().getName());
        assertNotNull(byNameProjection.get().getRecursiveProjection());
        assertEquals(TEST_REC_1, byNameProjection.get().getRecursiveProjection().getRecursiveProp1());
    }

    @Test
    void testCollectionProjection() {
        Collection<DocumentProjection> byNameProjection = documentProjectionRepository.findAllByName(TEST_NAME);
        assertNotNull(byNameProjection);
        assertEquals(2, byNameProjection.size());
        byNameProjection.forEach(documentProjection -> {
            assertNotNull(documentProjection.getName());
            assertNotNull(documentProjection.getRecursiveProjection());
        });
    }

    @AfterEach
    void tearDown() {
        documentProjectionRepository.deleteAll();
    }

}
