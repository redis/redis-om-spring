package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.AccountWithPositions;
import com.redis.om.spring.repository.RedisDocumentRepository;

import java.math.BigDecimal;
import java.util.List;

public interface AccountWithPositionsRepository extends RedisDocumentRepository<AccountWithPositions, String> {
  
  // Query by nested CUSIP field in Map values
  List<AccountWithPositions> findByPositionsMapContainsCusip(String cusip);
  
  // Query by nested Manager field in Map values  
  List<AccountWithPositions> findByPositionsMapContainsManager(String manager);
  
  // Query by nested numeric field with comparison
  List<AccountWithPositions> findByPositionsMapContainsQuantityGreaterThan(Integer quantity);
  
  // Query by nested price range
  List<AccountWithPositions> findByPositionsMapContainsPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
  
  // Combined query with regular field and nested Map field
  List<AccountWithPositions> findByAccountHolderAndPositionsMapContainsManager(String holder, String manager);
  
  // Multiple nested field conditions
  List<AccountWithPositions> findByPositionsMapContainsCusipAndPositionsMapContainsQuantityGreaterThan(
      String cusip, Integer quantity);
}