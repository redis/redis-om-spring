package com.redis.om.spring.fixtures.document.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.redis.om.spring.fixtures.document.model.NumericInTestEntity;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface NumericInTestEntityRepository extends RedisDocumentRepository<NumericInTestEntity, String> {
    // Test methods for NUMERIC_IN
    List<NumericInTestEntity> findByAgeIn(Collection<Integer> ages);
    List<NumericInTestEntity> findByAgeIn(Set<Integer> ages);
    List<NumericInTestEntity> findByAgeIn(List<Integer> ages);
    List<NumericInTestEntity> findByAgeIn(Integer... ages);
    
    // Test methods for Long type
    List<NumericInTestEntity> findByScoreIn(Collection<Long> scores);
    List<NumericInTestEntity> findByScoreIn(Long... scores);
    
    // Test methods for Double type
    List<NumericInTestEntity> findByRatingIn(Collection<Double> ratings);
    List<NumericInTestEntity> findByRatingIn(Double... ratings);
    
    // Test methods for NUMERIC_NOT_IN
    List<NumericInTestEntity> findByAgeNotIn(Collection<Integer> ages);
    List<NumericInTestEntity> findByScoreNotIn(Collection<Long> scores);
    List<NumericInTestEntity> findByRatingNotIn(Collection<Double> ratings);
    
    // Combined queries
    List<NumericInTestEntity> findByAgeInAndScoreIn(Collection<Integer> ages, Collection<Long> scores);
    List<NumericInTestEntity> findByNameAndAgeIn(String name, Collection<Integer> ages);
    
    // Edge cases
    List<NumericInTestEntity> findByLevelIn(Collection<Integer> levels); // nullable field
}