package com.redis.om.spring.fixtures.hash.model;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RedisHash
public class HashWithNestedMap {
  @Id
  private String id = UUID.randomUUID().toString();

  private Map<String, Object> attributes;
}
