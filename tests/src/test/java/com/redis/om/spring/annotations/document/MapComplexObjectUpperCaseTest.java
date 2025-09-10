package com.redis.om.spring.annotations.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.AccountUC;
import com.redis.om.spring.fixtures.document.model.PositionUC;
import com.redis.om.spring.fixtures.document.repository.AccountUCRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MapComplexObjectUpperCaseTest extends AbstractBaseDocumentTest {

  @Autowired
  private AccountUCRepository repository;
  
  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  void loadTestData() throws IOException {
    // Clear any existing data
    repository.deleteAll();
    
    // Load the uppercase.json file (RIOT export format)
    ClassPathResource resource = new ClassPathResource("data/uppercase.json");
    
    try (InputStream inputStream = resource.getInputStream();
         InputStreamReader reader = new InputStreamReader(inputStream)) {
      
      JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
      Gson gson = new Gson();
      
      for (JsonElement element : jsonArray) {
        JsonObject record = element.getAsJsonObject();
        
        // Extract the key and value from RIOT export format
        String key = record.get("key").getAsString();
        String jsonValue = record.get("value").getAsString();
        
        // Parse the JSON value
        JsonObject accountJson = JsonParser.parseString(jsonValue).getAsJsonObject();
        
        // Create AccountUC object
        AccountUC account = new AccountUC();
        account.setAccountId(accountJson.get("ACCOUNTID").getAsString());
        account.setAccountName(accountJson.get("ACC_NAME").getAsString());
        account.setManager(accountJson.get("MANAGER").getAsString());
        account.setAccountValue(new BigDecimal(accountJson.get("ACC_VALUE").getAsString()));
        
        // Handle optional fields
        if (accountJson.has("COMMISSION_RATE")) {
          account.setCommissionRate(accountJson.get("COMMISSION_RATE").getAsInt());
        }
        if (accountJson.has("CASH_BALANCE")) {
          account.setCashBalance(new BigDecimal(accountJson.get("CASH_BALANCE").getAsString()));
        }
        if (accountJson.has("MANAGER_FNAME")) {
          account.setManagerFirstName(accountJson.get("MANAGER_FNAME").getAsString());
        }
        if (accountJson.has("MANAGER_LNAME")) {
          account.setManagerLastName(accountJson.get("MANAGER_LNAME").getAsString());
        }
        
        // Parse Positions (note the capital P!)
        Map<String, PositionUC> positions = new HashMap<>();
        if (accountJson.has("Positions")) {
          JsonObject positionsJson = accountJson.getAsJsonObject("Positions");
          
          for (Map.Entry<String, JsonElement> posEntry : positionsJson.entrySet()) {
            JsonObject posJson = posEntry.getValue().getAsJsonObject();
            
            PositionUC position = new PositionUC();
            position.setPositionId(posJson.get("POSITIONID").getAsString());
            position.setAccountId(posJson.get("ACCOUNTID").getAsString());
            position.setCusip(posJson.get("CUSIP").getAsString());
            position.setQuantity(posJson.get("QUANTITY").getAsInt());
            
            // Add default values for fields not in the JSON
            position.setManager("DEFAULT_MANAGER");
            position.setDescription("DEFAULT_DESCRIPTION");
            position.setPrice(new BigDecimal("100.00"));
            position.setAsOfDate(LocalDate.now());
            
            positions.put(posEntry.getKey(), position);
          }
        }
        
        account.setPositions(positions);
        repository.save(account);
      }
    }
    
    System.out.println("Loaded " + repository.count() + " accounts from uppercase.json");
  }

  @Test
  void testFindByManager() {
    // This should work because manager field uses @Indexed(alias = "MANAGER")
    List<AccountUC> accounts = repository.findByManager("Emma Jones");
    assertThat(accounts).isNotEmpty();
    assertThat(accounts.get(0).getManager()).isEqualTo("Emma Jones");
  }

  @Test
  void testMapContainsQueriesWithUppercaseFields() {
    // First, let's check what data we actually have
    System.out.println("\n=== Checking loaded data ===");
    List<AccountUC> allAccounts = repository.findAll().stream().toList();
    System.out.println("Total accounts: " + allAccounts.size());
    
    // Let's count how many accounts actually have CVS positions
    int actualCVSCount = 0;
    for (AccountUC acc : allAccounts) {
      boolean hasCVS = false;
      for (PositionUC pos : acc.getPositions().values()) {
        if ("CVS".equals(pos.getCusip())) {
          hasCVS = true;
          break;
        }
      }
      if (hasCVS) actualCVSCount++;
    }
    System.out.println("Actual accounts with CVS: " + actualCVSCount);
    
    // Test 1: Find accounts with CVS positions
    System.out.println("\n=== Testing findByPositionsMapContainsCusip('CVS') ===");
    List<AccountUC> cvsAccounts = repository.findByPositionsMapContainsCusip("CVS");
    System.out.println("Query returned " + cvsAccounts.size() + " accounts with CVS positions");
    
    // This SHOULD find accounts but currently FAILS because:
    // - The Map field is "Positions" (capital P) in the JSON
    // - The repository method expects "positions" (lowercase p)
    // - The indexer doesn't properly handle the alias
    assertThat(cvsAccounts).isNotEmpty(); 
    
    boolean foundCVS = false;
    for (AccountUC account : cvsAccounts) {
      for (PositionUC position : account.getPositions().values()) {
        if ("CVS".equals(position.getCusip())) {
          foundCVS = true;
          break;
        }
      }
    }
    assertTrue(foundCVS, "Should find accounts with CVS CUSIP");
  }
  
  @Test
  void testMapContainsQueriesWithTSLA() {
    // Test 2: Find accounts with TSLA positions
    System.out.println("Testing findByPositionsMapContainsCusip('TSLA')...");
    List<AccountUC> teslaAccounts = repository.findByPositionsMapContainsCusip("TSLA");
    System.out.println("Found " + teslaAccounts.size() + " accounts with TSLA positions");
    
    assertThat(teslaAccounts).isNotEmpty();
    
    boolean foundTSLA = false;
    for (AccountUC account : teslaAccounts) {
      for (PositionUC position : account.getPositions().values()) {
        if ("TSLA".equals(position.getCusip())) {
          foundTSLA = true;
          break;
        }
      }
    }
    assertTrue(foundTSLA, "Should find accounts with TSLA CUSIP");
  }
  
  @Test 
  void testMapContainsQueriesWithQuantityComparison() {
    // Test 3: Find accounts with large positions (quantity > 50000)
    System.out.println("Testing findByPositionsMapContainsQuantityGreaterThan(50000)...");
    List<AccountUC> largePositions = repository.findByPositionsMapContainsQuantityGreaterThan(50000);
    System.out.println("Found " + largePositions.size() + " accounts with positions > 50000");
    
    assertThat(largePositions).isNotEmpty();
    
    boolean foundLarge = false;
    for (AccountUC account : largePositions) {
      for (PositionUC position : account.getPositions().values()) {
        if (position.getQuantity() > 50000) {
          foundLarge = true;
          break;
        }
      }
    }
    assertTrue(foundLarge, "Should find accounts with positions having quantity > 50000");
  }
}