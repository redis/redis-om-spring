package com.redis.om.spring.fixtures.document.model;

import java.util.List;

import org.springframework.data.annotation.Id;

import com.github.f4b6a3.ulid.Ulid;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Document
@Data
@RequiredArgsConstructor(
    staticName = "of"
)
public class WithNestedListOfUlids {
  @Id
  @NonNull
  private String id;

  @Indexed
  @NonNull
  private List<Ulid> ulids;
}
