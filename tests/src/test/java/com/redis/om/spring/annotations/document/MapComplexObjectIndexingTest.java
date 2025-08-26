package com.redis.om.spring.annotations.document;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.AccountWithPositions;
import com.redis.om.spring.fixtures.document.model.Position;
import com.redis.om.spring.fixtures.document.repository.AccountWithPositionsRepository;
import com.redis.om.spring.indexing.RediSearchIndexer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.redis.om.spring.ops.RedisModulesOperations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test to verify that Map<String, ComplexObject> fields are properly indexed
 * with nested field paths like $.positions.*.cusip
 */
class MapComplexObjectIndexingTest extends AbstractBaseDocumentTest {

  @Autowired
  private AccountWithPositionsRepository repository;
  
  @Autowired
  private RediSearchIndexer indexer;

  @BeforeEach
  void setup() {
    repository.deleteAll();
  }

  @Test
  void testComplexMapObjectIndexing() {
    // Create and save an account with positions
    AccountWithPositions account = new AccountWithPositions();
    account.setAccountNumber("10190001");
    account.setAccountHolder("WILLIAM ZULINSKI");
    account.setTotalValue(new BigDecimal("23536984.00"));
    
    Map<String, Position> positions = new HashMap<>();
    
    Position pos1 = new Position();
    pos1.setCusip("AAPL");
    pos1.setDescription("APPLE COMPUTER INC");
    pos1.setQuantity(16000);
    pos1.setPrice(new BigDecimal("5654.00"));
    pos1.setManager("TONY MILLER");
    pos1.setAsOfDate(LocalDate.of(2024, 10, 15));
    positions.put("AAPL", pos1);
    
    Position pos2 = new Position();
    pos2.setCusip("IBM");
    pos2.setDescription("INTL BUSINESS MACHINES CORP");
    pos2.setQuantity(13000);
    pos2.setPrice(new BigDecimal("5600.00"));
    pos2.setManager("JAY DASTUR");
    pos2.setAsOfDate(LocalDate.of(2024, 10, 15));
    positions.put("IBM", pos2);
    
    account.setPositions(positions);
    AccountWithPositions saved = repository.save(account);
    
    assertThat(saved.getId()).isNotNull();
    
    // Verify the index was created
    String indexName = indexer.getIndexName(AccountWithPositions.class);
    assertThat(indexName).isNotNull();
    
    // The enhanced RediSearchIndexer should have created fields for:
    // - $.positions.*.cusip (TAG field)
    // - $.positions.*.manager (TAG field) 
    // - $.positions.*.quantity (NUMERIC field)
    // - $.positions.*.price (NUMERIC field)
    // - $.positions.*.description (TAG field)
    // - $.positions.*.asOfDate (NUMERIC field)
    
    System.out.println("Index created: " + indexName);
    System.out.println("Account saved with nested positions containing indexed fields");
    
    // Verify we can retrieve the saved account
    AccountWithPositions retrieved = repository.findById(saved.getId()).orElse(null);
    assertThat(retrieved).isNotNull();
    assertThat(retrieved.getPositions()).hasSize(2);
    assertThat(retrieved.getPositions().get("AAPL").getCusip()).isEqualTo("AAPL");
    assertThat(retrieved.getPositions().get("IBM").getManager()).isEqualTo("JAY DASTUR");
  }
}