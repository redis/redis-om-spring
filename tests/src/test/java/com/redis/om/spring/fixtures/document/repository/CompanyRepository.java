package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.Company;
import com.redis.om.spring.repository.RedisDocumentRepository;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("unused")
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

}
