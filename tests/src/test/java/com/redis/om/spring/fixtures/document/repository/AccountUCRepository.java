package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.AccountUC;
import com.redis.om.spring.repository.RedisDocumentRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountUCRepository extends RedisDocumentRepository<AccountUC, String> {
  
  // Basic queries on regular fields (testing uppercase mapping)
  Optional<AccountUC> findFirstByManager(String manager);
  
  List<AccountUC> findByManager(String manager);
  
  List<AccountUC> findByAccountValueGreaterThan(BigDecimal value);
  
  // Query by nested CUSIP field in Map values
  List<AccountUC> findByPositionsMapContainsCusip(String cusip);
  
  // Query by nested Manager field in Map values  
  List<AccountUC> findByPositionsMapContainsManager(String manager);
  
  // Query by nested numeric field with comparison
  List<AccountUC> findByPositionsMapContainsQuantityGreaterThan(Integer quantity);
  
  // Query by nested numeric field with less than comparison
  List<AccountUC> findByPositionsMapContainsQuantityLessThan(Integer quantity);
  
  // Query by nested price range
  List<AccountUC> findByPositionsMapContainsPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
  
  // Combined query with regular field and nested Map field
  List<AccountUC> findByManagerAndPositionsMapContainsCusip(String manager, String cusip);
  
  // Combined query with regular field and nested Map field (manager in positions)
  List<AccountUC> findByManagerAndPositionsMapContainsManager(String accountManager, String positionManager);
  
  // Multiple nested field conditions (AND)
  List<AccountUC> findByPositionsMapContainsCusipAndPositionsMapContainsQuantityGreaterThan(
      String cusip, Integer quantity);
  
  // Multiple nested field conditions (OR) - Note: Spring Data doesn't directly support OR in method names,
  // but we can test multiple conditions
  List<AccountUC> findByPositionsMapContainsCusipOrManagerContaining(String cusip, String managerPart);
  
  // Query for exact quantity match
  List<AccountUC> findByPositionsMapContainsQuantity(Integer quantity);
  
  // Query combining multiple regular fields with nested Map field
  List<AccountUC> findByCommissionRateAndPositionsMapContainsCusip(Integer rate, String cusip);
  
  // Delete operations
  Long deleteByPositionsMapContainsCusip(String cusip);
  
  Long deleteByManager(String manager);
}