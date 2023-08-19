package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Metamodel;
import com.redis.om.spring.annotations.Searchable;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
public class NestLevelNonIndexed1 {
  @NonNull
  @Metamodel
  private String name;

  @NonNull
  @Metamodel
  private String block;

  @NonNull
  @Metamodel
  private NestLevelNonIndexed2 nestLevel2;
}
