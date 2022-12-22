package com.redis.om.spring.annotations.hash.fixtures;

import com.redis.om.spring.annotations.*;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisHash;

import java.util.HashSet;
import java.util.Set;

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
  private Set<String> tag = new HashSet<>();
}
