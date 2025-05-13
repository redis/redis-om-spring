package com.redis.om.hashes.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import com.redis.om.spring.annotations.Indexed;

import lombok.*;

@Data
@RequiredArgsConstructor(
    staticName = "of"
)
@AllArgsConstructor(
    access = AccessLevel.PROTECTED
)
@NoArgsConstructor
@RedisHash
public class Role {
  @Id
  private String id;

  @Indexed
  @NonNull
  private String roleName;

}
