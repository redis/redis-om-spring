package com.redis.om.spring.fixtures.hash.model;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisHash;

import com.redis.om.spring.annotations.*;

import lombok.*;

@Data
@NoArgsConstructor(
    force = true
)
@RequiredArgsConstructor(
    staticName = "of"
)
@AllArgsConstructor(
    access = AccessLevel.PROTECTED
)
@RedisHash
public class MyHash {
  @Id
  private String id;

  @NonNull
  @TextIndexed(
      alias = "title", sortable = true
  )
  private String title;

  @NonNull
  @GeoIndexed(
      alias = "location"
  )
  private Point location;

  @NonNull
  @Indexed
  private Point location2;

  @NonNull
  @NumericIndexed
  private Integer aNumber;

  @TagIndexed(
      alias = "tag"
  )
  private Set<String> tag = new HashSet<>();
}
