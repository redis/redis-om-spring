package com.redis.om.spring.annotations.hash;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.annotations.hash.fixtures.*;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.SearchStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ExplicitPrefixesHashTest extends AbstractBaseEnhancedRedisTest {
  @Autowired
  CountryRepository countryRepository;

  @Autowired
  HashWithColonInPrefixRepository hashWithColonInPrefixRepository;

  @Autowired
  EntityStream entityStream;

  @BeforeEach
  void prepare() {
    countryRepository.deleteAll();

    var countriesInBulk = Set.of( //
      Country.of("Mexico"), Country.of("Canada"), //
      Country.of("Panama"), Country.of("Venezuela") //
    );
    countryRepository.saveAll(countriesInBulk);
    countryRepository.save(Country.of("USA"));

    hashWithColonInPrefixRepository.deleteAll();
    var countriesInBulk2 = Set.of( //
      HashWithColonInPrefix.of("Mexico"), HashWithColonInPrefix.of("Canada"), //
      HashWithColonInPrefix.of("Panama"), HashWithColonInPrefix.of("Venezuela") //
    );
    hashWithColonInPrefixRepository.saveAll(countriesInBulk2);
    hashWithColonInPrefixRepository.save(HashWithColonInPrefix.of("USA"));
  }

  @Test
  void testKeyGenerationWithCustomPrefix() {
    try (SearchStream<Country> stream = entityStream.of(Country.class)) {
      var countries = stream.map(Country$._KEY).collect(Collectors.toSet());
      assertThat(countries).containsExactlyInAnyOrder( //
        "country:Mexico", "country:Canada", //
        "country:Panama", "country:Venezuela", //
        "country:USA" //
      );
    }
  }

  @Test
  void testKeyGenerationWithCustomPrefixWithColon() {
    try (SearchStream<HashWithColonInPrefix> stream = entityStream.of(HashWithColonInPrefix.class)) {
      var countries = stream.map(HashWithColonInPrefix$._KEY).collect(Collectors.toSet());
      assertThat(countries).containsExactlyInAnyOrder( //
        "hwcip:Mexico", "hwcip:Canada", //
        "hwcip:Panama", "hwcip:Venezuela", //
        "hwcip:USA" //
      );
    }
  }

  @Test void testFindByIdForCustomPrefixWithColon() {
    Optional<HashWithColonInPrefix> maybePanama = hashWithColonInPrefixRepository.findById("Panama");
    assertThat(maybePanama).isPresent();
  }
}
