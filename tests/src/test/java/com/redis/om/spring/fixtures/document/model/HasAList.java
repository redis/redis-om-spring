package com.redis.om.spring.fixtures.document.model;

import java.util.List;

import com.redis.om.spring.annotations.Indexed;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(
    staticName = "of"
)
public class HasAList {
  @Indexed
  @NonNull
  private List<String> innerList;
}
