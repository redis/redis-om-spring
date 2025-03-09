package com.redis.om.spring.fixtures.hash.model;

import com.redis.om.spring.annotations.Searchable;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(force = true)
@RedisHash
public class Text {
  @Id
  private String id;

  @NonNull
  @Searchable
  private String body;
}
