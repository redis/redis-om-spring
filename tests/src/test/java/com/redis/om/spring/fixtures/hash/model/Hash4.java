package com.redis.om.spring.fixtures.hash.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;

import lombok.*;

@Data
@RequiredArgsConstructor(
    staticName = "of"
)
@AllArgsConstructor(
    access = AccessLevel.PROTECTED
)
@NoArgsConstructor(
    force = true
)
@RedisHash
public class Hash4 {
  @Id
  private String id;

  @Searchable(
      sortable = true
  )
  @NonNull
  private String first;

  @Indexed
  private String second;

  @NonNull
  private String third;
}
