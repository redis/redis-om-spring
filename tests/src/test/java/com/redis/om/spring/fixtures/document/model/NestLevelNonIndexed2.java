package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Metamodel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
public class NestLevelNonIndexed2 {
  @NonNull
  @Metamodel
  private String name;

  @NonNull
  @Metamodel
  private String block;
}
