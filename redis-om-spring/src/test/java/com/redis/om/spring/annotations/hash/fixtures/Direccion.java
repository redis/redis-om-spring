package com.redis.om.spring.annotations.hash.fixtures;

import com.redis.om.spring.annotations.TagIndexed;
import com.redis.om.spring.annotations.TextIndexed;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
public class Direccion {
  @NonNull
  @TagIndexed(alias = "ciudad")
  private String city;

  @NonNull
  @TextIndexed(alias = "calle", nostem = true)
  private String street;
}
