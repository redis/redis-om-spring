package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
public class Address {
  @NonNull
  @Indexed
  private String city;

  @NonNull
  @Searchable(nostem = true)
  private String street;
}
