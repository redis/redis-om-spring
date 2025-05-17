package com.redis.om.spring.id;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.Date;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.util.TypeInformation;

import com.github.f4b6a3.ulid.Ulid;
import com.google.gson.JsonObject;
import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.fixtures.document.model.BadDoc;
import com.redis.om.spring.fixtures.document.model.DocWithExplicitUlidId;
import com.redis.om.spring.fixtures.document.model.DocWithIntegerId;
import com.redis.om.spring.fixtures.document.repository.BadDocRepository;
import com.redis.om.spring.fixtures.document.repository.DocWithExplicitUlidIdRepository;
import com.redis.om.spring.fixtures.document.repository.DocWithIntegerIdRepository;
import com.redis.om.spring.fixtures.hash.model.Person;
import com.redis.om.spring.fixtures.hash.repository.PersonRepository;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.json.JSONOperations;

@SuppressWarnings(
  "SpellCheckingInspection"
)
class ULIDIdentifierTest extends AbstractBaseEnhancedRedisTest {

  private final ULIDIdentifierGenerator generator = ULIDIdentifierGenerator.INSTANCE;

  @Autowired
  PersonRepository repository;

  @Autowired
  BadDocRepository badDocRepo;

  @Autowired
  DocWithExplicitUlidIdRepository docWithUlidRepo;

  @Autowired
  DocWithIntegerIdRepository docWithIntRepo;

  @Autowired
  RedisModulesOperations<String> modulesOperations;

  @Test
  void testMonotonicallyIncreasingUlidAssignment() {
    Person ofer = Person.of("Ofer Bengal", "ofer@redis.com", "ofer");
    String oferId = repository.save(ofer).getId();
    Person yiftach = Person.of("Yiftach Shoolman", "yiftach@redis.com", "yiftach");
    String yiftachId = repository.save(yiftach).getId();
    // get the Ulid objects from the String ids
    Ulid oferUlid = Ulid.from(oferId);
    Ulid yiftachUlid = Ulid.from(yiftachId);
    assertTrue(oferUlid.getInstant().isBefore(yiftachUlid.getInstant()));
  }

  @Test
  void testUnsupportedIdTypesThrowException() {
    BadDoc badDoc = new BadDoc();
    InvalidDataAccessApiUsageException exception = Assertions.assertThrows(InvalidDataAccessApiUsageException.class,
        () -> badDocRepo.save(badDoc));

    String expectedErrorMessage = String.format(
        "Identifier cannot be generated for %s. Supported types are: ULID, String, Integer, and Long.", BigInteger.class
            .getName());
    Assertions.assertEquals(expectedErrorMessage, exception.getMessage());
  }

  @Test
  void testExplicitUlid() {
    JSONOperations<String> ops = modulesOperations.opsForJSON();

    DocWithExplicitUlidId ulidDoc = new DocWithExplicitUlidId();
    Ulid generatedId = docWithUlidRepo.save(ulidDoc).getId();

    JsonObject rawJSON = ops.get(DocWithExplicitUlidId.class.getName() + ":" + generatedId.toString(),
        JsonObject.class);
    String ulidAsString = Objects.requireNonNull(rawJSON).get("id").getAsString();
    Ulid ulidFromRawJSON = Ulid.from(ulidAsString);

    assertThat(ulidFromRawJSON).isEqualByComparingTo(generatedId);
  }

  @Test
  void shouldThrowExceptionForUnsupportedType() {
    assertThatExceptionOfType(InvalidDataAccessApiUsageException.class).isThrownBy(() -> generator
        .generateIdentifierOfType(TypeInformation.of(Date.class)));
  }

  @Test
  void shouldGenerateUlidValueCorrectly() {

    Object value = generator.generateIdentifierOfType(TypeInformation.of(Ulid.class));

    assertThat(value).isNotNull().isInstanceOf(Ulid.class);
  }

  @Test
  void testIntegerId() {
    JSONOperations<String> ops = modulesOperations.opsForJSON();

    DocWithIntegerId intDoc = new DocWithIntegerId();
    Integer generatedId = docWithIntRepo.save(intDoc).getId();

    JsonObject rawJSON = ops.get(DocWithIntegerId.class.getName() + ":" + generatedId.toString(), JsonObject.class);

    assertThat(Objects.requireNonNull(rawJSON).get("id").getAsInt()).isEqualByComparingTo(generatedId);
  }

}
