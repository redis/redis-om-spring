package com.redis.om.spring.annotations.hash.fixtures;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import com.redis.om.spring.annotations.AutoComplete;
import com.redis.om.spring.annotations.Bloom;
import com.redis.om.spring.annotations.Indexed;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash("people")
public class Person {

  @Id 
  String id;
  
  @NonNull 
  String name;
  
  @NonNull 
  @AutoComplete 
  @Bloom(name = "bf_person_email", capacity = 100000, errorRate = 0.001)
  String email;
  
  @NonNull
  @Bloom(capacity = 100000, errorRate = 0.001)
  String nickname;
  
  @NonNull
  @Indexed
  Set<String> roles = new HashSet<String>();
  
  @NonNull
  Set<String> favoriteFoods = new HashSet<String>();
}