package com.redis.om.documents.domain;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(
  "lex-repro"
)
public class LexicographicIdRecord {
  @Id
  @Indexed(
      sortable = true, lexicographic = true
  )
  private String id;

  @Indexed
  private String bucket;
}
