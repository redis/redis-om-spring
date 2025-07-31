package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.Company;
import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface to test findTop/findFirst limiting functionality.
 */
public interface CompanyRepositoryWithLimiting extends RedisDocumentRepository<Company, String> {
    Optional<Company> findFirstByTags(String tag);
    Optional<Company> findFirstByOrderByYearFoundedAsc();
    Optional<Company> findTopByOrderByYearFoundedDesc();
    List<Company> findTop2ByOrderByYearFoundedAsc();
    Optional<Company> findFirstByTagsOrderByYearFoundedAsc(String tag);
    
    // Additional test cases for various limiting scenarios
    List<Company> findTop5ByOrderByNameAsc();
    List<Company> findFirst3ByOrderByEmailDesc();
    Optional<Company> findTopByTagsOrderByNameAsc(String tag);
    List<Company> findTop10ByYearFoundedGreaterThanOrderByNameAsc(int year);
}