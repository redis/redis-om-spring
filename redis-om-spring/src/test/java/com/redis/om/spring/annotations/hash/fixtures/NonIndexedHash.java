package com.redis.om.spring.annotations.hash.fixtures;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@RequiredArgsConstructor(staticName = "of")
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@RedisHash
public class NonIndexedHash {
  @Id
  private String id;

  @NonNull
  private String name;
}
