package com.redis.om.spring.fixtures.document.model;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;

import com.redis.om.spring.annotations.Document;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(
    staticName = "of"
)
@Document(
  "states"
)
public class States {
  @Id
  @NonNull
  private String id;

  @Reference
  @NonNull
  private Set<State> states;
}
