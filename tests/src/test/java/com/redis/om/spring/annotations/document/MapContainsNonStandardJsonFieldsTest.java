package com.redis.om.spring.annotations.document;

import com.google.gson.Gson;
import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.AccountUC;
import com.redis.om.spring.fixtures.document.repository.AccountUCRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MapContainsNonStandardJsonFieldsTest extends AbstractBaseDocumentTest {
  
  @Autowired
  AccountUCRepository repository;
  
  @BeforeEach
  void loadTestData() throws IOException, InterruptedException {
    // Clear any existing data
    repository.deleteAll();
    
    // Wait for index to be recreated
    Thread.sleep(2000);
    
    // Load test data from JSON file with non-standard (uppercase) field names
    Gson gson = new Gson();
    ClassPathResource resource = new ClassPathResource("data/uppercase-json-fields-subset.json");
    
    try (InputStreamReader reader = new InputStreamReader(resource.getInputStream())) {
      AccountUC[] accounts = gson.fromJson(reader, AccountUC[].class);
      
      // Save all accounts to Redis
      List<AccountUC> savedAccounts = repository.saveAll(Arrays.asList(accounts));
      
      // Verify data was saved
      System.out.println("Saved " + savedAccounts.size() + " accounts to Redis");
      for (AccountUC account : savedAccounts) {
        System.out.println("Account " + account.getAccountId() + " has " + account.getPositions().size() + " positions");
      }
    }
  }
  
  @Test
  void testMapContainsWithUppercaseJsonFields() {
    // Query for accounts with TSLA positions
    List<AccountUC> accounts = repository.findByPositionsMapContainsCusip("TSLA");
    
    // Verify we found the expected accounts (5 out of 7 have TSLA)
    assertThat(accounts).hasSize(5);
    
    // Verify the account IDs match expected (5 accounts have TSLA positions)
    List<String> expectedIds = List.of("ACC-001", "ACC-002", "ACC-003", "ACC-004", "ACC-005");
    List<String> actualIds = accounts.stream()
        .map(AccountUC::getAccountId)
        .sorted()
        .toList();
    assertThat(actualIds).containsExactlyInAnyOrderElementsOf(expectedIds);
    
    // Verify each account actually has TSLA positions
    for (AccountUC account : accounts) {
      boolean hasTSLA = account.getPositions().values().stream()
          .anyMatch(position -> "TSLA".equals(position.getCusip()));
      assertThat(hasTSLA)
          .as("Account %s should have TSLA position", account.getAccountId())
          .isTrue();
    }
  }
  
  @Test
  void testMapContainsWithNonMatchingCusip() {
    // Query for accounts with a CUSIP that doesn't exist in our subset
    List<AccountUC> accounts = repository.findByPositionsMapContainsCusip("GOOGL");
    
    // Should return empty list
    assertThat(accounts).isEmpty();
  }
  
  @Test
  void testMapContainsWithOtherCusips() {
    // Query for accounts with AAPL positions
    List<AccountUC> accounts = repository.findByPositionsMapContainsCusip("AAPL");
    
    // 6 out of 7 accounts have AAPL
    assertThat(accounts).hasSize(6);
    
    // Verify each account actually has AAPL positions
    for (AccountUC account : accounts) {
      boolean hasAAPL = account.getPositions().values().stream()
          .anyMatch(position -> "AAPL".equals(position.getCusip()));
      assertThat(hasAAPL)
          .as("Account %s should have AAPL position", account.getAccountId())
          .isTrue();
    }
  }
}