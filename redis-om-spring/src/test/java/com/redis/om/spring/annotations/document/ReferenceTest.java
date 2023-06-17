package com.redis.om.spring.annotations.document;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceTest extends AbstractBaseDocumentTest {
  @Autowired
  CityRepository cityRepository;

  @Autowired
  StateRepository stateRepository;

  @Autowired
  CountryRepository countryRepository;

  @Autowired
  StatesRepository statesRepository;

  @BeforeEach
  void prepare() {
    cityRepository.deleteAll();
    stateRepository.deleteAll();
    countryRepository.deleteAll();
    statesRepository.deleteAll();

    var usa = countryRepository.save(Country.of("USA"));

    var ca = stateRepository.save(State.of("CA", "California", usa));
    var az = stateRepository.save(State.of("AZ", "Arizona", usa));
    var oh = stateRepository.save(State.of("OH", "Ohio", usa));
    var tx = stateRepository.save(State.of("TX", "Texas", usa));
    var wa = stateRepository.save(State.of("WA", "Washington", usa));
    var ga = stateRepository.save(State.of("GA", "Georgia", usa));

    statesRepository.save(States.of("West Of Mississippi", Set.of(ca, az, tx, wa)));
    statesRepository.save(States.of("East Of Mississippi", Set.of(oh, ga)));

    var cities = Set.of( //
        City.of("San Francisco", ca), City.of("San Jose", ca), //
        City.of("Los Angeles", ca), City.of("Scottsdale", az), //
        City.of("Phoenix", az), City.of("Flagstaff", az), //
        City.of("Columbus", oh), City.of("Cleveland", oh), //
        City.of("Cincinnati", oh), City.of("Houston", tx), //
        City.of("Dallas", tx), City.of("Austin", tx), //
        City.of("Seattle", wa), City.of("Spokane", wa), //
        City.of("Tacoma", wa), City.of("Atlanta", ga), //
        City.of("Savannah", ga), City.of("Augusta", ga) //
    );
    cityRepository.saveAll(cities);
  }

  @Test
  void testMultilevelReferences() {
    var maybeScottsdale = cityRepository.findById("Scottsdale");
    assertThat(maybeScottsdale).isPresent();
    var arizona = maybeScottsdale.get().getState();
    assertThat(arizona).isNotNull();
    assertThat(arizona.getId()).isEqualTo("AZ");
    var us = arizona.getCountry();
    assertThat(us).isNotNull();
    assertThat(us.getId()).isEqualTo("USA");
  }

  @Test
  void testReferencedClassCanBeDeserializedWithFullPayload() {
    var maybeUSA = countryRepository.findById("USA");
    assertThat(maybeUSA).isPresent();
    assertThat(maybeUSA.get().getId()).isEqualTo("USA");
  }

  @Test
  void testReferencedClassWithReferencesCanBeDeserializedWithFullPayload() {
    var maybeArizona = stateRepository.findById("AZ");
    assertThat(maybeArizona).isPresent();
    assertThat(maybeArizona.get().getId()).isEqualTo("AZ");
    assertThat(maybeArizona.get().getCountry().getId()).isEqualTo("USA");
  }

  @Test
  void testReferenceCollectionDeserialization() {
    var maybeOh = stateRepository.findById("OH");
    var maybeGa = stateRepository.findById("GA");

    assertThat(maybeOh).isPresent();
    assertThat(maybeGa).isPresent();

    var oh = maybeOh.get();
    var ga = maybeGa.get();

    var maybeStates = statesRepository.findById("East Of Mississippi");
    assertThat(maybeStates).isPresent();
    assertThat(maybeStates.get().getId()).isEqualTo("East Of Mississippi");
    assertThat(maybeStates.get().getStates()).hasSize(2);
    assertThat(maybeStates.get().getStates()).contains(oh, ga);
  }
}
