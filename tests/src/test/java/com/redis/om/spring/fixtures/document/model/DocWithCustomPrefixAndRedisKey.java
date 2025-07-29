package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.RedisKey;
import com.redis.om.spring.annotations.Searchable;

import lombok.Data;

@Data
@Document("custom:doc")
public class DocWithCustomPrefixAndRedisKey {
  @Id
  private String id;
  
  @RedisKey
  private String redisKey;
  
  @Indexed
  private String name;
  
  @Searchable
  private String description;
}