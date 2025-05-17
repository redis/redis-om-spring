package com.redis.om.spring.fixtures.document.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;

import com.redis.om.spring.fixtures.document.model.Company;
import com.redis.om.spring.repository.RedisDocumentRepository;
import com.redis.om.spring.search.stream.SearchStream;

@SuppressWarnings(
  "unused"
)
public interface CompanyRepository extends RedisDocumentRepository<Company, String> {
  List<Company> findByName(String companyName);

  boolean existsByEmail(String email);

  List<Company> findByEmployees_name(String name);

  Optional<Company> findFirstByName(String name);

  Optional<Company> findFirstByEmail(String email);

  List<Company> findByEmailStartingWith(String prefix);

  List<Company> findByEmailEndingWith(String prefix);

  List<Company> findByPubliclyListed(boolean publiclyListed);

  List<Company> findByTags(Set<String> tags);

  // find one by property
  Optional<Company> findOneByName(String name);

  // geospatial query
  Iterable<Company> findByLocationNear(Point point, Distance distance);

  // starting with/ending with
  Iterable<Company> findByNameStartingWith(String prefix);

  List<Company> findByMetaList_stringValue(String value);

  List<Company> findByMetaList_numberValue(Integer value);

  List<Company> findByMetaList_tagValues(Set<String> tags);

  // order by
  List<Company> findByYearFoundedOrderByNameAsc(int year);

  List<Company> findByYearFoundedOrderByNameDesc(int year);

  // Methods for advanced sentinel test
  SearchStream<Company> findByYearFoundedGreaterThan(int year);

  List<Company> findByYearFoundedGreaterThanOrderByNameAsc(int year);

  List<Company> findByYearFoundedGreaterThanOrderByNameDesc(int year);

  List<Company> findByYearFoundedBetweenOrderByNameAsc(int start, int end);
}
