package com.redis.om.spring.annotations;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash
public class AHash {
  @Id
  private String id;

  @NonNull
  private String name;
}
