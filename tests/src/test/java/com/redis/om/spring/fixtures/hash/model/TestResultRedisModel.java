package com.redis.om.spring.fixtures.hash.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(
    staticName = "of"
)
@NoArgsConstructor(
    force = true
)
@RedisHash(
  "TestResultRedis"
)
public class TestResultRedisModel {
  @Id
  @Indexed
  @NonNull
  private Long id;

  @Indexed
  @NonNull
  String uuid;

  @Searchable
  @NonNull
  String filename;

  @Indexed
  @NonNull
  String status;
}
