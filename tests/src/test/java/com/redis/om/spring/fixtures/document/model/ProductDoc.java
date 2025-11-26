package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;

import lombok.*;

/**
 * Model for testing Issue #676: Gson JSON serialization bug with spaces in projected fields.
 */
@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(force = true)
@Document("productdoc")
public class ProductDoc {
  @Id
  private String id;

  @NonNull
  @Searchable
  private String keyword;

  @NonNull
  @Indexed
  private String category;

  @NonNull
  @Indexed
  private Double price;
}
