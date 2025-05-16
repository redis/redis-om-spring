package com.redis.om.spring.fixtures.hash.model;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@RedisHash(
  "games"
)
public class Game {
  @Id
  private String asin;
  @Searchable
  private String description;
  @Searchable(
      sortable = true
  )
  private String title;
  @Searchable(
      nostem = true, sortable = true
  )
  private String brand;
  @Indexed
  private Double price;
  private Set<String> categories;
}
