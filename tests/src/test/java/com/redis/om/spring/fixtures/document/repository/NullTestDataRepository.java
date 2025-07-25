package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.NullTestData;
import com.redis.om.spring.repository.RedisDocumentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NullTestDataRepository extends RedisDocumentRepository<NullTestData, String> {

    List<NullTestData> findByTitleIsNull();
    List<NullTestData> findByTitleIsNotNull();
    
    List<NullTestData> findByDescriptionIsNull();
    List<NullTestData> findByDescriptionIsNotNull();
    
    List<NullTestData> findByScoreIsNull();
    List<NullTestData> findByScoreIsNotNull();
    
    List<NullTestData> findByCategoryIsNull();
    List<NullTestData> findByCategoryIsNotNull();
}