package com.redis.om.spring.fixtures.hash.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor(
    force = true
)
@RequiredArgsConstructor(
    staticName = "of"
)
@RedisHash(
    timeToLive = 5
)
public class ExpiringPerson {
  @Id
  String id;
  @NonNull
  String name;

  @NonNull
  @TimeToLive
  Long ttl;
}
