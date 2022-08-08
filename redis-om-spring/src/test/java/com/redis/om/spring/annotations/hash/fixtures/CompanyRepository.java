package com.redis.om.spring.annotations.hash.fixtures;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;

import com.redis.om.spring.repository.RedisEnhancedRepository;

public interface CompanyRepository extends RedisEnhancedRepository<Company, String> {
  List<Company> findByName(String companyName);

  boolean existsByEmail(String email);

  Optional<Company> findFirstByName(String name);

  Optional<Company> findFirstByEmail(String email);

  List<Company> findByPubliclyListed(boolean publiclyListed);

  List<Company> findByTags(Set<String> tags);

  Iterable<String> getAllTags();

  Iterable<Company> search(String text);
  
  List<Company> findByLocationNear(Point point, Distance distance); 
}
