package com.redis.om.spring.fixtures.document.model;

import java.util.Set;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(
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
