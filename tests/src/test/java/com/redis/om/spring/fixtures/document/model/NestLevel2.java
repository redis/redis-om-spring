package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
public class NestLevel2 {
  @NonNull
  @Indexed
  private String name;

  @NonNull
  @Searchable
  private String block;
}
