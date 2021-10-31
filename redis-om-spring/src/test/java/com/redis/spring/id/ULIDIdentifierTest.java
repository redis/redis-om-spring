package com.redis.spring.id;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.f4b6a3.ulid.Ulid;
import com.redis.spring.AbstractBaseEnhancedRedisTest;
import com.redis.spring.annotations.bloom.fixtures.Person;
import com.redis.spring.annotations.bloom.fixtures.PersonRepository;

public class ULIDIdentifierTest extends AbstractBaseEnhancedRedisTest {

  @Autowired
  PersonRepository repository;
  
  @Test
  public void testMonotonicallyIncreasingUlidAssignment() {
    Person ofer = Person.of("Ofer Bengal", "ofer@redis.com");
    String oferId = repository.save(ofer).getId();
    Person yiftach = Person.of("Yiftach Shoolman", "yiftach@redis.com");
    String yiftachId = repository.save(yiftach).getId();
    // get the Ulid objects from the String ids
    Ulid oferUlid = Ulid.from(oferId);
    Ulid yiftachUlid = Ulid.from(yiftachId);
    assertTrue(oferUlid.getInstant().isBefore(yiftachUlid.getInstant()));
  }

}
