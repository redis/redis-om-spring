package com.redis.om.spring.fixtures.hash.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.*;

@Data
@RequiredArgsConstructor(
    staticName = "of"
)
@NoArgsConstructor(
    force = true
)
@EqualsAndHashCode(
    onlyExplicitlyIncluded = true
)
@RedisHash
public class NonIndexedHash {
  @Id
  private String id;

  @NonNull
  private String name;
}
