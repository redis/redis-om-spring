package com.redis.om.spring.annotations.document;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.Developer;
import com.redis.om.spring.annotations.document.fixtures.DeveloperRepository;
import com.redis.om.spring.annotations.document.fixtures.DeveloperState;
import com.redis.om.spring.annotations.document.fixtures.DeveloperType;
import com.redis.om.spring.client.RedisModulesClient;
import lombok.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class EnumeratedTest extends AbstractBaseDocumentTest {

  @Autowired
  DeveloperRepository developerRepository;

  @Autowired
  RedisModulesClient redisModulesClient;

  @BeforeEach
  void loadTestData() {
    var java = Developer.builder().id("1").typeOrdinal(DeveloperType.JAVA).state(DeveloperState.REST).build();
    var cpp = Developer.builder().id("2").typeOrdinal(DeveloperType.CPP).state(DeveloperState.WORK).build();
    var python = Developer.builder().id("3").typeOrdinal(DeveloperType.PYTHON).state(DeveloperState.WORK).build();

    developerRepository.saveAll(List.of(java, cpp, python));
  }

  @Test
  void testSaveEnumAsNumber() {

    var search = redisModulesClient.clientForSearch();
    Gson gson = new Gson();

    var data = search.ftSearch("com.redis.om.spring.annotations.document.fixtures.DeveloperIdx").getDocuments().stream()
      .map(el -> gson.fromJson((String) el.get("$"), DeveloperNative.class))
      .collect(Collectors.toMap(DeveloperNative::getId, Function.identity()));

    assertThat(data.get("1").getOrdinal()).isEqualTo(DeveloperType.JAVA.ordinal());
    assertThat(data.get("2").getOrdinal()).isEqualTo(DeveloperType.CPP.ordinal());
    assertThat(data.get("3").getOrdinal()).isEqualTo(DeveloperType.PYTHON.ordinal());

    assertThat(data.get("1").getState()).isEqualTo(DeveloperState.REST.toString());
    assertThat(data.get("2").getState()).isEqualTo(DeveloperState.WORK.toString());
    assertThat(data.get("3").getState()).isEqualTo(DeveloperState.WORK.toString());

  }

  @Getter
  @Setter
  @Builder
  @AllArgsConstructor(access = AccessLevel.PROTECTED)
  private static class DeveloperNative {

    private String id;
    @SerializedName("typeOrdinal")
    private int ordinal;

    private String state;
  }

}
