package com.redislabs.spring.annotations.bloom.fixtures;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import com.redislabs.spring.annotations.Bloom;

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
  @Bloom(name = "bf_person_email", capacity = 100000, errorRate = 0.001)
  String email;
}