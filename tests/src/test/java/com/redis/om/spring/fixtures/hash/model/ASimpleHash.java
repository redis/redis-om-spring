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
public class ASimpleHash {
  @Id
  private String id;

  @Searchable(sortable = true)
  @NonNull
  private String first;

  @Searchable(sortable = true)
  private String second;

  @Searchable(sortable = true)
  private String third;
}
