package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.PersonWithPhones;
import com.redis.om.spring.repository.RedisDocumentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonWithPhonesRepository extends RedisDocumentRepository<PersonWithPhones, String> {
  // Basic search by name
  List<PersonWithPhones> findByName(String name);
  
  // Repository methods for searching nested fields in phone arrays
  List<PersonWithPhones> findByPhonesListNumber(String number);
  List<PersonWithPhones> findByPhonesListType(String type);
  List<PersonWithPhones> findByPhonesListNumberAndPhonesListType(String number, String type);
  
  // Additional methods for comprehensive testing
  List<PersonWithPhones> findByNameAndPhonesListType(String name, String type);
}