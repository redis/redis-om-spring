package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(
    staticName = "of"
)
@Document(
  "city"
)
public class City {
  @Id
  @NonNull
  private String id;

  @Reference
  @Indexed
  @NonNull
  private State state;
}
