package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.Set;

@Data
@NoArgsConstructor
@Document("games")
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
