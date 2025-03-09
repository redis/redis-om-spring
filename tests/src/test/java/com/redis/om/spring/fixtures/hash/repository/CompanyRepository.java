package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.fixtures.hash.model.Company;
import com.redis.om.spring.repository.RedisEnhancedRepository;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("unused")
public interface CompanyRepository extends RedisEnhancedRepository<Company, String> {
  List<Company> findByName(String companyName);

  boolean existsByEmail(String email);

  Optional<Company> findFirstByName(String name);

  Optional<Company> findFirstByEmail(String email);

  List<Company> findByEmailStartingWith(String prefix);

  List<Company> findByEmailEndingWith(String prefix);

  List<Company> findByPubliclyListed(boolean publiclyListed);

  List<Company> findByTags(Set<String> tags);

  Iterable<String> getAllTags();

  Iterable<Company> search(String text);

  List<Company> findByLocationNear(Point point, Distance distance);

  // order by
  List<Company> findByYearFoundedOrderByNameAsc(int year);

  List<Company> findByYearFoundedOrderByNameDesc(int year);
}
