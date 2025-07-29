package com.redis.om.spring.fixtures.hash.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.RedisKey;
import com.redis.om.spring.annotations.Searchable;

import lombok.Data;

@Data
@RedisHash
public class HashWithRedisKey {
  @Id
  private String id;
  
  @RedisKey
  private String redisKey;
  
  @Indexed
  private String name;
  
  @Searchable
  private String description;
}