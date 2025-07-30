package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import com.redis.om.spring.annotations.TextIndexed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Document
public class StringComparisonTestDoc {
  @Id
  private String id;

  @NonNull
  @Indexed(lexicographic = true)
  private String indexedStringField;

  @NonNull
  @Searchable
  private String searchableStringField;

  @NonNull
  @TextIndexed
  private String textIndexedStringField;

  @NonNull
  private String regularStringField;

  @NonNull
  @Indexed(lexicographic = true)
  private String comId;
}