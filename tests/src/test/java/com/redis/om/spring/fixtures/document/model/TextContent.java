package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;

import lombok.*;

@Data
@RequiredArgsConstructor(
    staticName = "of"
)
@NoArgsConstructor(
    force = true
)
@Document
public class TextContent {
  @Id
  private String id;

  @NonNull
  @Searchable(
      sortable = true
  )
  private String title;

  @NonNull
  @Indexed
  private String content;

  @NonNull
  @Indexed
  private String category;
}