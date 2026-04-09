package com.redis.om.spring.annotations.document;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.ExpiringPerson;
import com.redis.om.spring.fixtures.document.model.ExpiringPersonDirectFieldAccess;
import com.redis.om.spring.fixtures.document.repository.ExpiringPersonDirectFieldAccessRepository;
import com.redis.om.spring.fixtures.document.repository.ExpiringPersonRepository;
import com.redis.om.spring.util.ObjectUtils;

/**
 * Tests for the JDK 25 classloader / null-getter TTL bug.
 * <p>
 * When an entity has a {@code @TimeToLive} field without a standard JavaBean getter,
 * {@code ObjectUtils.getGetterForField()} returns {@code null}. Prior to the fix,
 * this caused an NPE in {@code getTTLForEntity()} when passed to
 * {@code ReflectionUtils.invokeMethod()}.
 *
 * Reported via customer email.
 */
class DocumentTTLClassLoaderTest extends AbstractBaseDocumentTest {

  @Autowired
  ExpiringPersonDirectFieldAccessRepository directFieldAccessRepository;

  @Autowired
  ExpiringPersonRepository standardGetterRepository;

  @BeforeEach
  void cleanUp() {
    directFieldAccessRepository.deleteAll();
    standardGetterRepository.deleteAll();
  }

  @Test
  void testGetterForFieldReturnsNullWhenNoGetter() {
    // Verify the test entity actually has no getter for the ttl field —
    // this is the precondition for the bug to manifest
    Field ttlField = ReflectionUtils.findField(ExpiringPersonDirectFieldAccess.class, "ttl");
    assertThat(ttlField).isNotNull();

    Method getter = ObjectUtils.getGetterForField(ExpiringPersonDirectFieldAccess.class, ttlField);
    assertThat(getter).isNull();
  }

  @Test
  void testGetterForFieldWorksWithStandardGetter() {
    // Sanity check: standard Lombok-generated getter IS found
    Field ttlField = ReflectionUtils.findField(ExpiringPerson.class, "ttl");
    assertThat(ttlField).isNotNull();

    Method getter = ObjectUtils.getGetterForField(ExpiringPerson.class, ttlField);
    assertThat(getter).isNotNull();
  }

  @Test
  void testSaveEntityWithoutTtlGetterDoesNotThrowNPE() {
    // This is the core regression test: save() must not throw NPE
    // when the @TimeToLive field has no JavaBean getter
    ExpiringPersonDirectFieldAccess entity = ExpiringPersonDirectFieldAccess.of("Alan Turing", 10L);
    directFieldAccessRepository.save(entity);

    assertThat(entity.getId()).isNotNull();
    assertThat(directFieldAccessRepository.existsById(entity.getId())).isTrue();
  }

  @Test
  void testSaveEntityWithoutTtlGetterAppliesTTL() {
    // After the fix, TTL should still be applied via direct field access
    ExpiringPersonDirectFieldAccess entity = ExpiringPersonDirectFieldAccess.of("Grace Hopper", 15L);
    directFieldAccessRepository.save(entity);

    Long expire = directFieldAccessRepository.getExpiration(entity.getId());
    assertThat(expire).isEqualTo(15L);
  }

  @Test
  void testSaveAllEntitiesWithoutTtlGetterDoesNotThrowNPE() {
    // Verify saveAll also works (uses SimpleRedisDocumentRepository.getTTLForEntity)
    var e1 = ExpiringPersonDirectFieldAccess.of("Ada Lovelace", 20L);
    var e2 = ExpiringPersonDirectFieldAccess.of("Charles Babbage", 20L);

    directFieldAccessRepository.saveAll(java.util.List.of(e1, e2));

    Long expire1 = directFieldAccessRepository.getExpiration(e1.getId());
    Long expire2 = directFieldAccessRepository.getExpiration(e2.getId());

    assertThat(expire1).isEqualTo(20L);
    assertThat(expire2).isEqualTo(20L);
  }

  @Test
  void testStandardEntityTTLStillWorks() {
    // Sanity: existing entities with getters still work after the fix
    ExpiringPerson person = ExpiringPerson.of("Mike Woodger", 15L);
    standardGetterRepository.save(person);

    Long expire = standardGetterRepository.getExpiration(person.getId());
    assertThat(expire).isEqualTo(15L);
  }
}
