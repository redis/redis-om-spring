package com.redis.om.spring.fixtures.hash.model;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;

import lombok.*;

/**
 * Test entity for SearchStream with properly indexed fields
 */
@Data
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash("hash_with_search_stream")
public class HashWithSearchStream {

  @Id
  String id;

  @NonNull
  @Searchable
  String name;

  @NonNull
  @Indexed
  String email;

  @NonNull
  @Indexed
  String department;
  
  @NonNull
  @Indexed
  Integer age;
  
  @NonNull
  @Indexed
  Boolean active;

  @NonNull
  @Indexed
  Set<String> skills = new HashSet<>();
}