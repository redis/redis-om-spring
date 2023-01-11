package com.redis.om.spring.annotations.hash.fixtures;

import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.Set;

@Data
@NoArgsConstructor
@RedisHash("games")
public class Game {
  @Id
  private String asin;
  @Searchable
  private String description;
  @Searchable(sortable = true)
  private String title;
  @Searchable(nostem = true, sortable = true)
  private String brand;
  @Indexed
  private Double price;
  private Set<String> categories;
}
