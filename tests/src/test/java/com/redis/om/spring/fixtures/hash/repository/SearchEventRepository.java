package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.hash.model.SearchEvent;
import com.redis.om.spring.repository.RedisEnhancedRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchEventRepository extends RedisEnhancedRepository<SearchEvent, String>, UserIdCount {
  long countBySearchSentence(String sentence);

  List<Long> countBySearchSentence(List<String> sentences);

  long countByHotTerms(String term);

  List<Long> countByHotTerms(List<String> terms);

  long countBySearchWord(String word);

  List<Long> countBySearchWord(List<String> words);
}
