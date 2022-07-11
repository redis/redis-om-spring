package com.redis.om.spring.annotations.hash.fixtures;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
@RedisHash(timeToLive = 5)
public class ExpiringPersonWithDefault {
  @Id String id;
  @NonNull
  String name;
}
