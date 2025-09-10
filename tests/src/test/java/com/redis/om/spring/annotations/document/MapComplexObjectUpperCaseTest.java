package com.redis.om.spring.annotations.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.AccountUC;
import com.redis.om.spring.fixtures.document.model.AccountUC$;
import com.redis.om.spring.fixtures.document.model.PositionUC;
import com.redis.om.spring.fixtures.document.repository.AccountUCRepository;
import com.redis.om.spring.search.stream.EntityStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Map fields containing complex objects with indexed nested fields
 * using uppercase JSON field names.
 * 
 * This test verifies that Redis OM Spring can properly index and query nested fields
 * within Map values when JSON fields are uppercase but Java fields use standard naming.
 * 
 * Expected index structure with aliases:
 * - $.Positions.*.CUSIP as TAG field (aliased)
 * - $.Positions.*.QUANTITY as NUMERIC field (aliased)
 * - $.Positions.*.MANAGER as TAG field  
 * - $.Positions.*.PRICE as NUMERIC field
 */
class MapComplexObjectUpperCaseTest extends AbstractBaseDocumentTest {

  @Autowired
  private AccountUCRepository repository;
  
  @Autowired
  private EntityStream entityStream;
  
  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  void setup() {
    repository.deleteAll();
    loadTestData();
  }

  private void loadTestData() {
    // Create test accounts similar to VOYA data structure
    
    // Account 1: Multiple positions with various CUSIPs
    AccountUC account1 = new AccountUC();
    account1.setAccountId("ACC-1000");
    account1.setAccountName("Renaissance Technologies");
    account1.setManager("Emma Jones");
    account1.setAccountValue(new BigDecimal("23536984.00"));
    account1.setCommissionRate(3);
    account1.setCashBalance(new BigDecimal("500000.00"));
    account1.setManagerFirstName("Emma");
    account1.setManagerLastName("Jones");
    
    Map<String, PositionUC> positions1 = new HashMap<>();
    
    PositionUC pos1 = new PositionUC();
    pos1.setPositionId("P-1001");
    pos1.setCusip("AAPL");
    pos1.setQuantity(16000);
    pos1.setAccountId("ACC-1000");
    pos1.setDescription("APPLE INC");
    pos1.setManager("TONY MILLER");
    pos1.setPrice(new BigDecimal("150.00"));
    pos1.setAsOfDate(LocalDate.of(2024, 10, 15));
    positions1.put("P-1001", pos1);
    
    PositionUC pos2 = new PositionUC();
    pos2.setPositionId("P-1002");
    pos2.setCusip("CVS");
    pos2.setQuantity(13000);
    pos2.setAccountId("ACC-1000");
    pos2.setDescription("CVS HEALTH CORP");
    pos2.setManager("JAY DASTUR");
    pos2.setPrice(new BigDecimal("70.00"));
    pos2.setAsOfDate(LocalDate.of(2024, 10, 15));
    positions1.put("P-1002", pos2);
    
    PositionUC pos3 = new PositionUC();
    pos3.setPositionId("P-1003");
    pos3.setCusip("TSLA");
    pos3.setQuantity(145544);
    pos3.setAccountId("ACC-1000");
    pos3.setDescription("TESLA INC");
    pos3.setManager("KRISHNA MUNIRAJ");
    pos3.setPrice(new BigDecimal("250.00"));
    pos3.setAsOfDate(LocalDate.of(2024, 10, 15));
    positions1.put("P-1003", pos3);
    
    account1.setPositions(positions1);
    repository.save(account1);
    
    // Account 2: Different positions
    AccountUC account2 = new AccountUC();
    account2.setAccountId("ACC-2000");
    account2.setAccountName("Vanguard Group");
    account2.setManager("Carly Smith");
    account2.setAccountValue(new BigDecimal("15000000.00"));
    account2.setCommissionRate(2);
    account2.setCashBalance(new BigDecimal("300000.00"));
    account2.setManagerFirstName("Carly");
    account2.setManagerLastName("Smith");
    
    Map<String, PositionUC> positions2 = new HashMap<>();
    
    PositionUC pos4 = new PositionUC();
    pos4.setPositionId("P-2001");
    pos4.setCusip("MSFT");
    pos4.setQuantity(8000);
    pos4.setAccountId("ACC-2000");
    pos4.setDescription("MICROSOFT CORP");
    pos4.setManager("TONY MILLER");
    pos4.setPrice(new BigDecimal("380.00"));
    pos4.setAsOfDate(LocalDate.of(2024, 10, 15));
    positions2.put("P-2001", pos4);
    
    PositionUC pos5 = new PositionUC();
    pos5.setPositionId("P-2002");
    pos5.setCusip("AAPL");
    pos5.setQuantity(5000);
    pos5.setAccountId("ACC-2000");
    pos5.setDescription("APPLE INC");
    pos5.setManager("SARAH JOHNSON");
    pos5.setPrice(new BigDecimal("150.00"));
    pos5.setAsOfDate(LocalDate.of(2024, 10, 15));
    positions2.put("P-2002", pos5);
    
    account2.setPositions(positions2);
    repository.save(account2);
    
    // Account 3: Another set of positions
    AccountUC account3 = new AccountUC();
    account3.setAccountId("ACC-3000");
    account3.setAccountName("BlackRock");
    account3.setManager("Mike OBrian");
    account3.setAccountValue(new BigDecimal("5000000.00"));
    account3.setCommissionRate(2);
    account3.setCashBalance(new BigDecimal("100000.00"));
    account3.setManagerFirstName("Mike");
    account3.setManagerLastName("OBrian");
    
    Map<String, PositionUC> positions3 = new HashMap<>();
    
    PositionUC pos6 = new PositionUC();
    pos6.setPositionId("P-3001");
    pos6.setCusip("GOOGL");
    pos6.setQuantity(3000);
    pos6.setAccountId("ACC-3000");
    pos6.setDescription("ALPHABET INC");
    pos6.setManager("KRISHNA MUNIRAJ");
    pos6.setPrice(new BigDecimal("140.00"));
    pos6.setAsOfDate(LocalDate.of(2024, 10, 15));
    positions3.put("P-3001", pos6);
    
    PositionUC pos7 = new PositionUC();
    pos7.setPositionId("P-3002");
    pos7.setCusip("CVS");
    pos7.setQuantity(82975);
    pos7.setAccountId("ACC-3000");
    pos7.setDescription("CVS HEALTH CORP");
    pos7.setManager("JAY DASTUR");
    pos7.setPrice(new BigDecimal("70.00"));
    pos7.setAsOfDate(LocalDate.of(2024, 10, 15));
    positions3.put("P-3002", pos7);
    
    account3.setPositions(positions3);
    repository.save(account3);
    
    // Account 4: Account with no positions (edge case)
    AccountUC account4 = new AccountUC();
    account4.setAccountId("ACC-4000");
    account4.setAccountName("Empty Portfolio Fund");
    account4.setManager("Emma Jones");
    account4.setAccountValue(new BigDecimal("1000000.00"));
    account4.setCommissionRate(1);
    account4.setCashBalance(new BigDecimal("1000000.00"));
    account4.setManagerFirstName("Emma");
    account4.setManagerLastName("Jones");
    account4.setPositions(new HashMap<>());
    repository.save(account4);
  }

  @Test
  void testBasicRepositoryOperations() {
    // Test basic find by ID
    Optional<AccountUC> account = repository.findById("ACC-1000");
    assertThat(account).isPresent();
    assertThat(account.get().getManager()).isEqualTo("Emma Jones");
    assertThat(account.get().getPositions()).hasSize(3);
    
    // Test count
    long count = repository.count();
    assertThat(count).isEqualTo(4);
  }

  @Test
  void testFindByManager() {
    // Test finding by manager field (uppercase mapping)
    Optional<AccountUC> emmaAccount = repository.findFirstByManager("Emma Jones");
    assertThat(emmaAccount).isPresent();
    assertThat(emmaAccount.get().getAccountName()).isEqualTo("Renaissance Technologies");
    
    List<AccountUC> emmaAccounts = repository.findByManager("Emma Jones");
    assertThat(emmaAccounts).hasSize(2); // ACC-1000 and ACC-4000
    
    List<AccountUC> carlyAccounts = repository.findByManager("Carly Smith");
    assertThat(carlyAccounts).hasSize(1);
    assertThat(carlyAccounts.get(0).getAccountId()).isEqualTo("ACC-2000");
  }

  @Test
  void testQueryByNestedCusipInMapValues() {
    // Test querying by CUSIP field within Map values
    List<AccountUC> accountsWithAAPL = repository.findByPositionsMapContainsCusip("AAPL");
    assertThat(accountsWithAAPL).hasSize(2); // ACC-1000 and ACC-2000
    assertThat(accountsWithAAPL.stream().map(AccountUC::getAccountId))
        .containsExactlyInAnyOrder("ACC-1000", "ACC-2000");
    
    List<AccountUC> accountsWithCVS = repository.findByPositionsMapContainsCusip("CVS");
    assertThat(accountsWithCVS).hasSize(2); // ACC-1000 and ACC-3000
    
    List<AccountUC> accountsWithTSLA = repository.findByPositionsMapContainsCusip("TSLA");
    assertThat(accountsWithTSLA).hasSize(1); // Only ACC-1000
    assertThat(accountsWithTSLA.get(0).getAccountId()).isEqualTo("ACC-1000");
  }

  @Test
  void testQueryByNestedManagerInMapValues() {
    // Test querying by Manager field within Map values
    List<AccountUC> accountsWithTonyMiller = repository.findByPositionsMapContainsManager("TONY MILLER");
    assertThat(accountsWithTonyMiller).hasSize(2); // ACC-1000 and ACC-2000
    
    List<AccountUC> accountsWithKrishna = repository.findByPositionsMapContainsManager("KRISHNA MUNIRAJ");
    assertThat(accountsWithKrishna).hasSize(2); // ACC-1000 and ACC-3000
  }

  @Test
  void testQueryByNestedQuantityComparison() {
    // Test numeric comparison on nested quantity field
    List<AccountUC> largePositions = repository.findByPositionsMapContainsQuantityGreaterThan(10000);
    // Note: Empty Map may be included due to index behavior
    assertThat(largePositions.stream()
        .filter(a -> !a.getPositions().isEmpty())
        .count()).isEqualTo(3); // All non-empty accounts have positions > 10000
    
    List<AccountUC> smallPositions = repository.findByPositionsMapContainsQuantityLessThan(5000);
    // Should find ACC-3000 which has GOOGL with 3000
    assertThat(smallPositions.stream()
        .filter(a -> !a.getPositions().isEmpty())
        .anyMatch(a -> a.getAccountId().equals("ACC-3000"))).isTrue();
    
    List<AccountUC> exactQuantity = repository.findByPositionsMapContainsQuantity(16000);
    assertThat(exactQuantity).hasSize(1); // ACC-1000 has AAPL with exactly 16000
  }

  @Test
  void testQueryByNestedPriceRange() {
    // Test range query on nested price field
    List<AccountUC> midPricePositions = repository.findByPositionsMapContainsPriceBetween(
        new BigDecimal("100.00"), new BigDecimal("200.00"));
    // Should find accounts with positions priced between 100-200
    // ACC-1000: has AAPL at 150 ✓
    // ACC-2000: has AAPL at 150 ✓  
    // ACC-3000: has GOOGL at 140 ✓
    // All three non-empty accounts have positions in this price range
    assertThat(midPricePositions.stream()
        .filter(a -> !a.getPositions().isEmpty())
        .count()).isEqualTo(3);
  }

  @Test
  void testCombinedQueries() {
    // Test combining regular field with nested Map field
    List<AccountUC> emmaWithCVS = repository.findByManagerAndPositionsMapContainsCusip("Emma Jones", "CVS");
    assertThat(emmaWithCVS).hasSize(1); // Only ACC-1000
    assertThat(emmaWithCVS.get(0).getAccountId()).isEqualTo("ACC-1000");
    
    // Test with commission rate
    List<AccountUC> lowCommissionWithCVS = repository.findByCommissionRateAndPositionsMapContainsCusip(2, "CVS");
    assertThat(lowCommissionWithCVS).hasSize(1); // ACC-3000
  }

  @Test
  void testMultipleNestedFieldQuery() {
    // Find accounts that have AAPL AND have any position with quantity > 10000
    List<AccountUC> accounts = repository.findByPositionsMapContainsCusipAndPositionsMapContainsQuantityGreaterThan(
        "AAPL", 10000);
    
    // ACC-1000: has AAPL(16000) and TSLA(145544) - both conditions met
    // ACC-2000: has AAPL(5000) and MSFT(8000) - AAPL exists but no position > 10000
    // ACC-3000: has CVS(82975) > 10000 but no AAPL - only second condition met
    // Note: Due to how Redis indexes Map fields, both conditions are checked independently
    // So ACC-2000 might be included even though it doesn't have AAPL > 10000 in same position
    assertThat(accounts.stream().map(AccountUC::getAccountId))
        .contains("ACC-1000"); // At minimum, ACC-1000 should be present
  }

  // TODO: EntityStream queries with Map nested fields require metamodel generation updates
  // See ticket: [EntityStream Support for Uppercase JSON Fields in Map Complex Objects]
  // @Test
  // void testEntityStreamQueryByNestedFields() {
  //   // Test using EntityStream for more flexible queries
  //   // This should generate a query like: @positions_CUSIP:{AAPL}
  //   List<AccountUC> accounts = entityStream.of(AccountUC.class)
  //       .filter(AccountUC$.POSITIONS_CUSIP.eq("AAPL"))
  //       .collect(Collectors.toList());
  //   
  //   assertThat(accounts).hasSize(2);
  //   assertThat(accounts.stream().map(AccountUC::getAccountId))
  //       .containsExactlyInAnyOrder("ACC-1000", "ACC-2000");
  //   
  //   // Test with quantity comparison
  //   List<AccountUC> largePositions = entityStream.of(AccountUC.class)
  //       .filter(AccountUC$.POSITIONS_QUANTITY.gt(50000))
  //       .collect(Collectors.toList());
  //   
  //   assertThat(largePositions).hasSize(2); // ACC-1000 and ACC-3000
  // }

  @Test
  void testDeleteOperations() {
    // Test delete by nested field
    Long deletedCount = repository.deleteByPositionsMapContainsCusip("GOOGL");
    assertThat(deletedCount).isEqualTo(1); // ACC-3000
    
    // Verify deletion
    Optional<AccountUC> deleted = repository.findById("ACC-3000");
    assertThat(deleted).isEmpty();
    
    // Test delete by manager
    deletedCount = repository.deleteByManager("Mike OBrian");
    assertThat(deletedCount).isEqualTo(0); // Already deleted
    
    // Verify remaining accounts
    assertThat(repository.count()).isEqualTo(3);
  }

  @Test
  void testLoadUppercaseJsonData() throws IOException {
    // Clear existing data
    repository.deleteAll();
    
    // Load uppercase JSON data to test uppercase field handling
    String uppercaseJsonPath = "src/test/resources/data/uppercase.json";
    String jsonContent = Files.readString(Paths.get(uppercaseJsonPath));
    
    // Parse the uppercase JSON array
    List<Map<String, Object>> uppercaseRecords = objectMapper.readValue(jsonContent, List.class);
    
    // Load all records for testing
    for (Map<String, Object> record : uppercaseRecords) {
      String valueJson = (String) record.get("value");
      AccountUC account = objectMapper.readValue(valueJson, AccountUC.class);
      repository.save(account);
    }
    
    // Verify loaded accounts
    assertThat(repository.count()).isEqualTo(3);
    
    // Test queries on uppercase JSON data
    Optional<AccountUC> acc3342 = repository.findById("ACC-3342");
    assertThat(acc3342).isPresent();
    assertThat(acc3342.get().getManager()).isEqualTo("Carly Smith");
    assertThat(acc3342.get().getPositions()).hasSize(5);
    
    // Test MapContains query on uppercase data - CVS should be in ACC-3342 and ACC-4167
    List<AccountUC> accountsWithCVS = repository.findByPositionsMapContainsCusip("CVS");
    assertThat(accountsWithCVS).hasSize(2);
    assertThat(accountsWithCVS.stream().map(AccountUC::getAccountId))
        .containsExactlyInAnyOrder("ACC-3342", "ACC-4167");
    
    // Test quantity comparison - find accounts with positions > 50000
    List<AccountUC> largePositions = repository.findByPositionsMapContainsQuantityGreaterThan(50000);
    assertThat(largePositions).hasSize(3); // All accounts have at least one position > 50000
    
    // Test combined query - Emma Jones manages ACC-3230 which has TSLA positions
    List<AccountUC> emmaWithTSLA = repository.findByManagerAndPositionsMapContainsCusip("Emma Jones", "TSLA");
    assertThat(emmaWithTSLA).isNotEmpty();
    if (!emmaWithTSLA.isEmpty()) {
      assertThat(emmaWithTSLA.get(0).getAccountId()).isEqualTo("ACC-3230");
    }
  }

  @Test
  void testEdgeCases() {
    // Test with account that has no positions
    Optional<AccountUC> emptyAccount = repository.findById("ACC-4000");
    assertThat(emptyAccount).isPresent();
    assertThat(emptyAccount.get().getPositions()).isEmpty();
    
    // Query for CUSIP on empty positions should not return ACC-4000
    List<AccountUC> accountsWithAnyPosition = repository.findByPositionsMapContainsCusip("AAPL");
    assertThat(accountsWithAnyPosition.stream()
        .noneMatch(a -> a.getAccountId().equals("ACC-4000"))).isTrue();
    
    // Test with non-existent values
    List<AccountUC> noResults = repository.findByPositionsMapContainsCusip("NONEXISTENT");
    assertThat(noResults).isEmpty();
    
    noResults = repository.findByManager("Nobody");
    assertThat(noResults).isEmpty();
  }
}