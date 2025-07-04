package com.redis.romsmultiaclaccount;

import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.SearchStream;
import com.redis.om.spring.tuple.Fields;
import com.redis.om.spring.tuple.Pair;
import com.redis.romsmultiaclaccount.model.Customer;
import com.redis.romsmultiaclaccount.model.Customer$;
import com.redis.romsmultiaclaccount.repository.read.ReadCustomerRepository;
import com.redis.romsmultiaclaccount.repository.write.WriteCustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class RedisACLIntegrationTest extends AbstractTest {

  @Autowired
  WriteCustomerRepository writeCustomerRepository;

  @Autowired
  ReadCustomerRepository readCustomerRepository;

  @Autowired
  private EntityStream entityStream;

  @Test
  void testWriteAndReadWithSeparateACLConnections() {
    writeCustomerRepository.deleteAll();


    // ✅ Write using userA
    Customer saved = writeCustomerRepository.save(new Customer("Raphael", "raphael@redis.com"));
    assertThat(saved).isNotNull();
    assertThat(saved.getName()).isEqualTo("Raphael");

    // ✅ Read using userB
    Customer fetched = readCustomerRepository.findById(saved.getId()).orElse(null);
    assertThat(fetched).isNotNull();
    assertThat(fetched.getEmail()).isEqualTo("raphael@redis.com");

    // ❌ Try writing using userB (should throw an exception)
    Customer readOnlyAttempt = new Customer("EvilHacker", "oops@fail.com");
    assertThatThrownBy(() -> readCustomerRepository.save(readOnlyAttempt))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("NOPERM");

    // Read using EntityStream
    SearchStream<Customer> searchStream = entityStream.of(Customer.class);
    List<Pair<Customer, Customer>> matching = searchStream
            .filter(Customer$.NAME.containing("Raph"))
            .map(Fields.of(Customer$._THIS, Customer$._THIS))
            .collect(Collectors.toList());

    assertThat(matching.getFirst().getFirst().getEmail()).isEqualTo("raphael@redis.com");
  }
}