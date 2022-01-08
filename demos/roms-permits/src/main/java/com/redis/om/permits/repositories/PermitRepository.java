package com.redis.om.permits.repositories;
import java.util.Set;

import com.redis.om.permits.models.Permit;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface PermitRepository extends RedisDocumentRepository<Permit, String> {

  // Numeric range queries:
  // FT.SEARCH permits "@construction_value:[42000,42000]"
  // find by numeric property
  // Result: Every document that has a construction value of exactly 42000, so tst:permit:1.
  Iterable<Permit> findByConstructionValue(long value);
  
  // Performing a text search on all text fields:
  // FT.SEARCH permits "veranda"
  // Result: Documents inside which the word 'veranda' occurs, so tst:permit:1.
  Iterable<Permit> search(String text);
  
  // A fuzzy text search on all text fields:
  // FT.SEARCH permits "%%haus%%" 
  // Result: Documents with words similar to 'haus' (tst:permit:1 and tst:permit:3). The number of % indicates the allowed Levenshtein distance (later more about it). So the query would also match on 'house' because 'haus' and 'house' have a distance of two.
  
  // Performing a text search on a specific field:
  // FT.SEARCH permits "@building_type:detached" 
  Iterable<Permit> findByBuildingType(String buildingType);
  
  // Performing a tag search
  // FT.SEARCH permits "@city:{Lisbon}"
  Iterable<Permit> findByAddress_City(String city);
  
  // search documents that have one of multiple tags (OR condition)
  // FT.SEARCH permits "@work_type:{construction|design}"
  Iterable<Permit> findByWorkType(Set<String> workTypes);
  
  // Search documents that have all of the tags (AND condition):
  // FT.SEARCH permits "@work_type:{construction} @work_type:{design}"
  Iterable<Permit> findByWorkTypeContainingAll(Set<String> workTypes);
  
  // Performing a combined search on two fields (AND):
  // FT.SEARCH permits "@building_type:house @description:new"
  Iterable<Permit> findByBuildingTypeAndDescription(String buildingType, String description);
  
  // Performing a combined search on two fields (OR):
  // FT.SEARCH permits "(@city:{Lagos})|(@description:detached)"
  Iterable<Permit> findByAddress_CityOrDescription(String buildingType, String description);
  
}
