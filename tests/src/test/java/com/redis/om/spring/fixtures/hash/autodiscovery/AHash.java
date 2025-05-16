package com.redis.om.spring.fixtures.hash.autodiscovery;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.*;

@Data
@EqualsAndHashCode(
    of = "id"
)
@NoArgsConstructor
@RequiredArgsConstructor(
    staticName = "of"
)
@AllArgsConstructor(
    access = AccessLevel.PROTECTED
)
@RedisHash
public class AHash {
  @Id
  private String id;

  @NonNull
  private String name;
}
