package com.redis.om.spring.annotations.document;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.*;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.SearchStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ExplicitPrefixesDocumentTest extends AbstractBaseDocumentTest {
  @Autowired
  CountryRepository countryRepository;

  @Autowired
  DocWithColonInPrefixRepository docWithColonInPrefixRepository;

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

    docWithColonInPrefixRepository.deleteAll();
    var countriesInBulk2 = Set.of( //
      DocWithColonInPrefix.of("Mexico"), DocWithColonInPrefix.of("Canada"), //
      DocWithColonInPrefix.of("Panama"), DocWithColonInPrefix.of("Venezuela") //
    );
    docWithColonInPrefixRepository.saveAll(countriesInBulk2);
    docWithColonInPrefixRepository.save(DocWithColonInPrefix.of("USA"));
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
    try (SearchStream<DocWithColonInPrefix> stream = entityStream.of(DocWithColonInPrefix.class)) {
      var countries = stream.map(DocWithColonInPrefix$._KEY).collect(Collectors.toSet());
      assertThat(countries).containsExactlyInAnyOrder( //
        "dwcip:Mexico", "dwcip:Canada", //
        "dwcip:Panama", "dwcip:Venezuela", //
        "dwcip:USA" //
      );
    }
  }

  @Test
  void testFindByIdForCustomPrefixWithColon() {
    Optional<DocWithColonInPrefix> maybePanama = docWithColonInPrefixRepository.findById("Panama");
    assertThat(maybePanama).isPresent();
  }
}
