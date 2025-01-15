package com.redis.om.spring.annotations.document;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.RedisJSONKeyValueAdapter;
import com.redis.om.spring.fixtures.document.model.Account;
import com.redis.om.spring.fixtures.document.repository.AccountRepository;
import com.redis.om.spring.fixtures.hash.model.AccountId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CompositeIdDocumentMappingTest extends AbstractBaseDocumentTest {

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  RedisJSONKeyValueAdapter adapter;

  @BeforeEach
  void setUp() {
    accountRepository.deleteAll();
  }

  @Test
  void testIdClassBasicCrud() {
    // Create account with composite key
    Account account = new Account("12345", "SAVINGS", 1000.0);
    accountRepository.save(account);

    // Test findById using composite key
    AccountId id = new AccountId("12345", "SAVINGS");
    Optional<Account> found = accountRepository.findById(id);

    assertTrue(found.isPresent());
    assertEquals("12345", found.get().getAccountNumber());
    assertEquals("SAVINGS", found.get().getAccountType());
    assertEquals(1000.0, found.get().getBalance());

    // Update
    found.get().setBalance(2000.0);
    Account updated = accountRepository.save(found.get());
    assertEquals(2000.0, updated.getBalance());

    // Delete
    accountRepository.deleteById(id);
    assertFalse(accountRepository.findById(id).isPresent());
  }

  @Test
  void testMultipleCompositeIds() {
    // Save multiple accounts
    Account savings = new Account("12345", "SAVINGS", 1000.0);
    Account checking = new Account("12345", "CHECKING", 2000.0);
    accountRepository.saveAll(List.of(savings, checking));

    // Find all accounts
    List<Account> accounts = accountRepository.findAll();
    assertThat(accounts).hasSize(2);

    // Find by example
    Account example = new Account();
    example.setAccountNumber("12345");
    example.setAccountType("SAVINGS");

    Optional<Account> found = accountRepository.findOne(Example.of(example));
    assertTrue(found.isPresent());
    assertEquals(1000.0, found.get().getBalance());
  }

  @Test
  void testFindAllByIdForCompositeIds() {
    // Save multiple accounts
    Account savings = new Account("12345", "SAVINGS", 1000.0);
    Account checking = new Account("12345", "CHECKING", 2000.0);
    accountRepository.saveAll(List.of(savings, checking));

    AccountId id1 = new AccountId("12345", "SAVINGS");
    AccountId id2 = new AccountId("12345", "CHECKING");

    List<Account> accounts = accountRepository.findAllById(List.of(id1, id2));
    assertThat(accounts).hasSize(2);
    assertThat(accounts).containsExactly(savings, checking);
  }

  @Test
  void testGetAllKeysForCompositeIds() {
    // Save multiple accounts
    Account savings = new Account("12345", "SAVINGS", 1000.0);
    Account checking = new Account("12345", "CHECKING", 2000.0);
    accountRepository.saveAll(List.of(savings, checking));

    String keyspace = indexer.getKeyspaceForEntityClass(Account.class);
    assertEquals(2, accountRepository.count());
    List<String> keys = adapter.getAllKeys(keyspace, Account.class);
    assertAll( //
        () -> assertThat(keys).hasSize(2), //
        () -> assertThat(keys).contains(String.format("%s%s", keyspace, "12345:SAVINGS")), //
        () -> assertThat(keys).contains(String.format("%s%s", keyspace, "12345:CHECKING")) //
    );
  }

  @Test
  void testExistsByIdForCompositeIds() {
    // Save multiple accounts
    Account savings = new Account("12345", "SAVINGS", 1000.0);
    accountRepository.save(savings);

    AccountId id1 = new AccountId("12345", "SAVINGS");
    AccountId id2 = new AccountId("12345", "CHECKING");

    assertTrue(accountRepository.existsById(id1));
    assertFalse(accountRepository.existsById(id2));
  }

  @Test
  void testDeleteAllByIdForCompositeIds() {
    // Save multiple accounts
    Account savings = new Account("12345", "SAVINGS", 1000.0);
    Account checking = new Account("12345", "CHECKING", 2000.0);
    accountRepository.saveAll(List.of(savings, checking));

    AccountId id1 = new AccountId("12345", "SAVINGS");
    AccountId id2 = new AccountId("12345", "CHECKING");

    assertTrue(accountRepository.existsById(id1));
    assertTrue(accountRepository.existsById(id2));

    accountRepository.deleteAllById(List.of(id1, id2));

    assertFalse(accountRepository.existsById(id1));
    assertFalse(accountRepository.existsById(id2));
  }

  @Test
  void testRepositoryGetKeyForCompositeIds() {
    // Save multiple accounts
    Account savings = new Account("12345", "SAVINGS", 1000.0);
    Account checking = new Account("12345", "CHECKING", 2000.0);
    accountRepository.saveAll(List.of(savings, checking));

    String savingsKey = accountRepository.getKeyFor(savings);
    String checkingKey = accountRepository.getKeyFor(checking);

    assertThat(savingsKey).isEqualTo("daccounts:12345:SAVINGS");
    assertThat(checkingKey).isEqualTo("daccounts:12345:CHECKING");
  }
}