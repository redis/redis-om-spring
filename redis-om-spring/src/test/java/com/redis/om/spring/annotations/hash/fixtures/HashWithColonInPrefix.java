package com.redis.om.spring.annotations.hash.fixtures;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@RequiredArgsConstructor(staticName = "of")
@RedisHash("hwcip:")
public class HashWithColonInPrefix {
  @Id
  @NonNull
  private String id;
}