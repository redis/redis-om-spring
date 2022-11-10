package com.redis.om.spring.annotations.hash.fixtures;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisHash;

import com.redis.om.spring.annotations.GeoIndexed;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.NumericIndexed;
import com.redis.om.spring.annotations.TagIndexed;
import com.redis.om.spring.annotations.TextIndexed;

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
@RedisHash
public class MyHash {
  @Id
  private String id;
  
  @NonNull
  @TextIndexed(alias = "title", sortable = true)
  private String title;
  
  @NonNull
  @GeoIndexed(alias = "location")
  private Point location;
  
  @NonNull
  @Indexed
  private Point location2;
  
  @NonNull
  @NumericIndexed
  private Integer aNumber;
  
  @TagIndexed(alias = "tag")
  private Set<String> tag = new HashSet<String>();
}
