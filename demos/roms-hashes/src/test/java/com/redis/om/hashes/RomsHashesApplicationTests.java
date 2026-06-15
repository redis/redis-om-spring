package com.redis.om.hashes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.redis.om.hashes.domain.Role;
import com.redis.om.hashes.domain.User;
import com.redis.om.hashes.repositories.RoleRepository;
import com.redis.om.hashes.repositories.UserRepository;
import com.redis.testcontainers.RedisStackContainer;

/**
 * Regression test for gh-755: saving a @RedisHash entity whose Map<String,Object>
 * field contains nested Map/List values (as produced by Jackson deserialization)
 * must not throw a MappingException.
 */
@SpringBootTest
@Testcontainers
class RomsHashesApplicationTests {

  @Container
  static final RedisStackContainer REDIS = new RedisStackContainer(
      DockerImageName.parse(
          RedisStackContainer.DEFAULT_IMAGE_NAME.withTag(RedisStackContainer.DEFAULT_TAG).toString()))
      .withExposedPorts(6379);

  @DynamicPropertySource
  static void redisProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", REDIS::getHost);
    registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
  }

  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleRepository roleRepository;

  @AfterEach
  void tearDown() {
    userRepository.deleteAll();
    roleRepository.deleteAll();
  }

  @Test
  void contextLoads() {
  }

  @Test
  void saveUserWithFlatMetadataDoesNotThrow() {
    Role role = roleRepository.save(Role.of("ADMIN"));
    User user = User.of("alice@example.com", "Alice", "Smith", role);
    user.setMetadata(Map.of("department", "engineering", "level", 3));

    assertThatNoException().isThrownBy(() -> userRepository.save(user));
  }

  /**
   * Reproduces gh-755: metadata value is itself a LinkedHashMap (typical output
   * from Jackson's ObjectMapper when deserializing into Map<String,Object>).
   * Previously threw: MappingException: Couldn't find PersistentEntity for type
   * class java.util.LinkedHashMap
   */
  @Test
  void saveUserWithNestedMapMetadataDoesNotThrow() {
    Role role = roleRepository.save(Role.of("ADMIN"));

    LinkedHashMap<String, Object> address = new LinkedHashMap<>();
    address.put("city", "Tel Aviv");
    address.put("zip", "6100000");

    User user = User.of("bob@example.com", "Bob", "Jones", role);
    user.setMetadata(Map.of("address", address, "score", 99));

    assertThatNoException().isThrownBy(() -> userRepository.save(user));
  }

  @Test
  void saveAllUsersWithNestedMapMetadataDoesNotThrow() {
    Role role = roleRepository.save(Role.of("USER"));

    LinkedHashMap<String, Object> prefs = new LinkedHashMap<>();
    prefs.put("theme", "dark");
    prefs.put("notifications", Map.of("email", true, "sms", false));

    User u1 = User.of("carol@example.com", "Carol", "White", role);
    u1.setMetadata(Map.of("preferences", prefs));

    User u2 = User.of("dave@example.com", "Dave", "Black", role);
    u2.setMetadata(Map.of("plain", "value"));

    assertThatNoException().isThrownBy(() -> userRepository.saveAll(List.of(u1, u2)));
    assertThat(userRepository.count()).isEqualTo(2);
  }
}
