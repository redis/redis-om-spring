package com.redis.om.spring.annotations.document;

import com.google.common.collect.Sets;
import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import redis.clients.jedis.json.Path;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
class ExplicitelyIndexedIdFieldsTest extends AbstractBaseDocumentTest {

  @Autowired
  DocWithIndexedIdRepository docWithIndexedIdRepository;

  @Autowired
  DocWithSearchableIdRepository docWithSearchableIdRepository;

  @Autowired
  DocWithTagIndexedIdRepository docWithTagIndexedIdRepository;

  @BeforeEach
  void cleanUp() {
    docWithIndexedIdRepository.saveAll(List.of(DocWithIndexedId.of("DWII01", "DWII01"), DocWithIndexedId.of("DWII02", "DWII02")));
    docWithSearchableIdRepository.saveAll(List.of(DocWithSearchableId.of("DWSI01", "DWSI01"), DocWithSearchableId.of("DWSI02", "DWSI02")));
    docWithTagIndexedIdRepository.saveAll(List.of(DocWithTagIndexedId.of("DWTII01", "DWTII01"), DocWithTagIndexedId.of("DWTII02", "DWTII02")));
  }

  @Test void testFindById() {
    Optional<DocWithIndexedId> maybeDWII01 = docWithIndexedIdRepository.findById("DWII01");
    Optional<DocWithSearchableId> maybeDWSI01 = docWithSearchableIdRepository.findById("DWSI01");
    Optional<DocWithTagIndexedId> maybeDWTII01 = docWithTagIndexedIdRepository.findById("DWTII01");

    assertAll( //
        () -> assertThat(maybeDWII01).isPresent(), //
        () -> assertThat(maybeDWSI01).isPresent(), //
        () -> assertThat(maybeDWTII01).isPresent() //
    );
  }

  @Test void testFindAll() {
    var allDWII = docWithIndexedIdRepository.findAll();
    var allDWSI = docWithSearchableIdRepository.findAll();
    var allDWTII = docWithTagIndexedIdRepository.findAll();

    assertAll( //
        () -> assertThat(allDWII).hasSize(2), //
        () -> assertThat(allDWSI).hasSize(2), //
        () -> assertThat(allDWTII).hasSize(2) //
    );
  }
}
