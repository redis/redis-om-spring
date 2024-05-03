package com.redis.om.spring.repository;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.annotations.hash.fixtures.HashProjection;
import com.redis.om.spring.annotations.hash.fixtures.HashProjectionPojo;
import com.redis.om.spring.annotations.hash.fixtures.HashProjectionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.redis.core.mapping.RedisMappingContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HashProjectionTest extends AbstractBaseEnhancedRedisTest {

    public static final String TEST_NAME = "testName";
    public static final String TEST_PROP_1 = "test1";
    private final HashProjectionRepository hashProjectionRepository;

    @Autowired
    HashProjectionTest(HashProjectionRepository hashProjectionRepository) {
        this.hashProjectionRepository = hashProjectionRepository;
    }

    @BeforeEach
    void setUp() {
        for (int i = 0; i < 2; i++) {
            HashProjectionPojo entity = new HashProjectionPojo();
            entity.setName(TEST_NAME);
            entity.setTest(TEST_PROP_1);
            hashProjectionRepository.save(entity);
        }
    }

    @Test
    void testProjectionSingleEntityReturnType() {
        Optional<HashProjection> byNameProjection = hashProjectionRepository.findByName(TEST_NAME);
        assertTrue(byNameProjection.isPresent());
        assertEquals(TEST_NAME, byNameProjection.get().getName());
        assertNotNull(byNameProjection.get().getSpelTest());
        assertEquals(TEST_NAME + " " + TEST_PROP_1, byNameProjection.get().getSpelTest());
    }

    @Test
    void testProjectionCollectionReturnType() {
        Collection<HashProjection> byNameProjection = hashProjectionRepository.findAllByName(TEST_NAME);
        assertNotNull(byNameProjection);
        assertEquals(2, byNameProjection.size());
        byNameProjection.forEach(documentProjection -> {
            assertNotNull(documentProjection.getName());
            assertNotNull(documentProjection.getSpelTest());
        });
    }

    @Test
    void testProjectionPageReturnType() {
        Page<HashProjection> byNameProjection = hashProjectionRepository.findAllByName(TEST_NAME, Pageable.ofSize(1));
        assertNotNull(byNameProjection);
        assertEquals(1, byNameProjection.getNumberOfElements());
        assertEquals(2, byNameProjection.getTotalPages());
    }

    @AfterEach
    void tearDown() {
        hashProjectionRepository.deleteAll();
    }

}
