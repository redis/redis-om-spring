package com.redis.om.spring.annotations.document;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.*;
import com.redis.om.spring.search.stream.EntityStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = { "spring.config.location=classpath:application.yaml" })
class ReferenceTest extends AbstractBaseDocumentTest {
  @Autowired
  CityRepository cityRepository;

  @Autowired
  StateRepository stateRepository;

  @Autowired
  CountryRepository countryRepository;

  @Autowired
  StatesRepository statesRepository;

  @Autowired
  RefRepository refRepository;

  @Autowired
  TooManyReferencesRepository tooManyReferencesRepository;

  @Autowired
  EntityStream entityStream;

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

    if (refRepository.count() == 0) {
      Set<Ref> refs = new HashSet<>();
      for (int i = 0; i < 100; i++) {
        refs.add(Ref.of("ref" + i));
      }
      refRepository.saveAll(refs);

      Set<TooManyReferences> tmrs = new HashSet<>();
      List<Ref> refList = new ArrayList<>(refs);
      Random random = new Random();

      for (int i = 0; i < 10000; i++) {
        TooManyReferences tmr = TooManyReferences.of("ref" + i);

        tmr.setRef1(refList.get(random.nextInt(refList.size())));
        tmr.setRef2(refList.get(random.nextInt(refList.size())));
        tmr.setRef3(refList.get(random.nextInt(refList.size())));
        tmr.setRef4(refList.get(random.nextInt(refList.size())));
        tmr.setRef5(refList.get(random.nextInt(refList.size())));
        tmr.setRef6(refList.get(random.nextInt(refList.size())));
        tmr.setRef7(refList.get(random.nextInt(refList.size())));
        tmr.setRef8(refList.get(random.nextInt(refList.size())));
        tmr.setRef9(refList.get(random.nextInt(refList.size())));
        tmr.setRef10(refList.get(random.nextInt(refList.size())));

        tmrs.add(tmr);
      }

      tooManyReferencesRepository.saveAll(tmrs);
    }
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
  void testMultilevelReferencesWithEntityStreams() {
    List<City> cities = entityStream //
      .of(City.class) //
      .filter(City$.ID.eq("Scottsdale")) //
      .collect(Collectors.toList());

    assertThat(cities).hasSize(1);

    var scottsdale = cities.get(0);
    var arizona = scottsdale.getState();
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
  void testReferencedClassCanBeDeserializedWithFullPayloadWithEntityStreams() {
    List<Country> countries = entityStream //
      .of(Country.class) //
      .filter(Country$.ID.eq("USA")) //
      .collect(Collectors.toList());

    assertThat(countries).hasSize(1);
    Country usa = countries.get(0);
    assertThat(usa.getId()).isEqualTo("USA");
  }

  @Test
  void testReferencedClassWithReferencesCanBeDeserializedWithFullPayload() {
    var maybeArizona = stateRepository.findById("AZ");
    assertThat(maybeArizona).isPresent();
    assertThat(maybeArizona.get().getId()).isEqualTo("AZ");
    assertThat(maybeArizona.get().getCountry().getId()).isEqualTo("USA");
  }

  @Test
  void testReferencedClassWithReferencesCanBeDeserializedWithFullPayloadWithEntityStreams() {
    List<State> states = entityStream //
      .of(State.class) //
      .filter(State$.ID.eq("AZ")) //
      .collect(Collectors.toList());

    assertThat(states).hasSize(1);
    State arizona = states.get(0);

    assertThat(arizona.getId()).isEqualTo("AZ");
    assertThat(arizona.getCountry().getId()).isEqualTo("USA");
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

  @Test
  void testReferenceCollectionDeserializationWithEntityStreams() {
    var maybeOh = stateRepository.findById("OH");
    var maybeGa = stateRepository.findById("GA");

    assertThat(maybeOh).isPresent();
    assertThat(maybeGa).isPresent();

    var oh = maybeOh.get();
    var ga = maybeGa.get();

    List<States> states = entityStream //
      .of(States.class) //
      .filter(States$.ID.eq("East\\ Of\\ Mississippi")) //
      .collect(Collectors.toList());

    assertThat(states).hasSize(1);
    States eom = states.get(0);

    assertThat(eom.getId()).isEqualTo("East Of Mississippi");
    assertThat(eom.getStates()).hasSize(2);
    assertThat(eom.getStates()).contains(oh, ga);
  }

  @Test
  void testFindEntitiesByReferenceEq() {
    var maybeOh = stateRepository.findById("OH");
    var maybeColumbus = cityRepository.findById("Columbus");
    var maybeCleveland = cityRepository.findById("Cleveland");
    var maybeCincinnati = cityRepository.findById("Cincinnati");

    assertThat(maybeOh).isPresent();
    var oh = maybeOh.get();
    assertThat(maybeColumbus).isPresent();
    var columbus = maybeColumbus.get();
    assertThat(maybeCleveland).isPresent();
    var cleveland = maybeCleveland.get();
    assertThat(maybeCincinnati).isPresent();
    var cincinnati = maybeCincinnati.get();

    List<City> ohioCities = entityStream //
      .of(City.class) //
      .filter(City$.STATE.eq(oh)) //
      .collect(Collectors.toList());

    assertThat(ohioCities).hasSize(3);
    assertThat(ohioCities).containsExactlyInAnyOrder(cincinnati, cleveland, columbus);
  }

  @Test
  void testFindEntitiesByReferenceNotEq() {
    var maybeOh = stateRepository.findById("OH");
    var maybeColumbus = cityRepository.findById("Columbus");
    var maybeCleveland = cityRepository.findById("Cleveland");
    var maybeCincinnati = cityRepository.findById("Cincinnati");

    assertThat(maybeOh).isPresent();
    var oh = maybeOh.get();
    assertThat(maybeColumbus).isPresent();
    var columbus = maybeColumbus.get();
    assertThat(maybeCleveland).isPresent();
    var cleveland = maybeCleveland.get();
    assertThat(maybeCincinnati).isPresent();
    var cincinnati = maybeCincinnati.get();

    List<City> ohioCities = entityStream //
      .of(City.class) //
      .filter(City$.STATE.notEq(oh)) //
      .collect(Collectors.toList());

    assertThat(ohioCities).hasSize(15);
    assertThat(ohioCities).doesNotContain(cincinnati, cleveland, columbus);
  }

  @Test
  void testExtremeReferencesWithFindAll() {
    List<TooManyReferences> allReferences = tooManyReferencesRepository.findAll();

    assertThat(allReferences).isNotEmpty();

    for (TooManyReferences tmr : allReferences) {
      assertThat(tmr.getRef1()).isNotNull();
      assertThat(tmr.getRef2()).isNotNull();
      assertThat(tmr.getRef3()).isNotNull();
      assertThat(tmr.getRef4()).isNotNull();
      assertThat(tmr.getRef5()).isNotNull();
      assertThat(tmr.getRef6()).isNotNull();
      assertThat(tmr.getRef7()).isNotNull();
      assertThat(tmr.getRef8()).isNotNull();
      assertThat(tmr.getRef9()).isNotNull();
      assertThat(tmr.getRef10()).isNotNull();
    }
  }
}
