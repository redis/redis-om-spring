package com.redislabs.spring.patterns.bloom.value.fixtures;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import com.redislabs.spring.annotations.RedisProbExists;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash("people")
public class Person {

  @Id 
  String id;
  
  @NonNull 
  String name;
  
  @NonNull 
  @RedisProbExists
  String email;
}