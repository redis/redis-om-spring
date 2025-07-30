package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import lombok.*;
import org.springframework.data.annotation.Id;

/**
 * Test entity for lexicographic indexing feature.
 * Demonstrates the use of lexicographic=true parameter on both
 * @Indexed and @Searchable annotations to enable sorted set backing
 * for efficient string range queries.
 */
@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(force = true)
@Document
public class LexicographicDoc {
  @Id
  private String id;
  
  @NonNull
  @Indexed(lexicographic = true)
  private String sku;
  
  @NonNull
  @Searchable(lexicographic = true)
  private String name;
  
  @NonNull
  @Indexed(lexicographic = true)
  private String category;
  
  @NonNull
  @Indexed // lexicographic = false by default
  private String status;
}