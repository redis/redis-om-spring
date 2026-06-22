package com.redis.om.spring.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.fixtures.hash.model.HashWithNestedMap;
import com.redis.om.spring.fixtures.hash.repository.HashWithNestedMapRepository;

/**
 * Regression tests for gh-755: MappingException when saving a @RedisHash entity
 * with Map<String, Object> or List<Object> fields containing nested Map/Collection values
 * (e.g. LinkedHashMap / ArrayList produced by Jackson deserialization).
 * Spring Data Commons 3.2.4+ no longer creates PersistentEntity for Map/Collection types,
 * so the converter must handle them explicitly before reaching getRequiredPersistentEntity().
 */
class HashWithNestedMapTest extends AbstractBaseEnhancedRedisTest {

  @Autowired
  private HashWithNestedMapRepository repository;

  @AfterEach
  void tearDown() {
    repository.deleteAll();
  }

  @Test
  void saveWithFlatMapDoesNotThrow() {
    HashWithNestedMap entity = new HashWithNestedMap();
    entity.setAttributes(Map.of("key1", "value1", "key2", 42));

    assertThatNoException().isThrownBy(() -> repository.save(entity));
  }

  @Test
  void saveWithNestedLinkedHashMapDoesNotThrow() {
    // Simulates a Map<String, Object> where a value is itself a LinkedHashMap,
    // as produced by Jackson's ObjectMapper when deserializing generic Object fields.
    LinkedHashMap<String, Object> nested = new LinkedHashMap<>();
    nested.put("city", "Tel Aviv");
    nested.put("zip", "6100000");

    HashWithNestedMap entity = new HashWithNestedMap();
    entity.setAttributes(Map.of("address", nested, "name", "Acme"));

    assertThatNoException().isThrownBy(() -> repository.save(entity));
  }

  @Test
  void saveWithNestedLinkedHashMapRoundTrips() {
    LinkedHashMap<String, Object> nested = new LinkedHashMap<>();
    nested.put("city", "Tel Aviv");
    nested.put("zip", "6100000");

    HashWithNestedMap entity = new HashWithNestedMap();
    entity.setAttributes(Map.of("address", nested, "name", "Acme"));

    HashWithNestedMap saved = repository.save(entity);

    Optional<HashWithNestedMap> found = repository.findById(saved.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getAttributes()).containsEntry("name", "Acme");
    assertThat(found.get().getAttributes()).containsEntry("address", nested);
  }

  @Test
  void saveAllWithNestedMapsDoesNotThrow() {
    LinkedHashMap<String, Object> nested1 = new LinkedHashMap<>();
    nested1.put("score", 99);
    nested1.put("level", "gold");

    HashWithNestedMap e1 = new HashWithNestedMap();
    e1.setAttributes(Map.of("tier", nested1));

    HashWithNestedMap e2 = new HashWithNestedMap();
    e2.setAttributes(Map.of("plain", "value"));

    assertThatNoException().isThrownBy(() -> repository.saveAll(List.of(e1, e2)));
    assertThat(repository.count()).isEqualTo(2);
  }

  @Test
  void saveWithNestedCollectionInListFieldDoesNotThrow() {
    // Simulates a List<Object> where an element is itself an ArrayList,
    // as produced by Jackson when deserializing generic Object fields.
    ArrayList<Object> nestedList = new ArrayList<>();
    nestedList.add("item1");
    nestedList.add("item2");

    HashWithNestedMap entity = new HashWithNestedMap();
    entity.setItems(List.of(nestedList, "plain-string"));

    assertThatNoException().isThrownBy(() -> repository.save(entity));
  }

  @Test
  void saveWithNestedCollectionInListFieldRoundTrips() {
    ArrayList<Object> nestedList = new ArrayList<>();
    nestedList.add("item1");
    nestedList.add("item2");

    HashWithNestedMap entity = new HashWithNestedMap();
    entity.setItems(List.of(nestedList, "plain-string"));

    HashWithNestedMap saved = repository.save(entity);

    Optional<HashWithNestedMap> found = repository.findById(saved.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getItems()).containsExactly(nestedList, "plain-string");
  }

  @Test
  void saveWithNestedCollectionInMapValueDoesNotThrow() {
    // Map value is an ArrayList — another shape from Jackson deserialization.
    ArrayList<Object> nestedList = new ArrayList<>();
    nestedList.add("a");
    nestedList.add("b");

    HashWithNestedMap entity = new HashWithNestedMap();
    entity.setAttributes(Map.of("tags", nestedList));

    assertThatNoException().isThrownBy(() -> repository.save(entity));
  }

  @Test
  void saveWithNestedCollectionInMapValueRoundTrips() {
    ArrayList<Object> nestedList = new ArrayList<>();
    nestedList.add("a");
    nestedList.add("b");

    HashWithNestedMap entity = new HashWithNestedMap();
    entity.setAttributes(Map.of("tags", nestedList));

    HashWithNestedMap saved = repository.save(entity);

    Optional<HashWithNestedMap> found = repository.findById(saved.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getAttributes()).containsEntry("tags", nestedList);
  }

  @Test
  void savedEntityIsRetrievable() {
    HashWithNestedMap entity = new HashWithNestedMap();
    entity.setAttributes(Map.of("key", "value"));

    HashWithNestedMap saved = repository.save(entity);

    Optional<HashWithNestedMap> found = repository.findById(saved.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getAttributes()).isNotNull();
  }
}
