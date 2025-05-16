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
@AllArgsConstructor(
    access = AccessLevel.PROTECTED
)
@NoArgsConstructor(
    force = true
)
@Document
public class Doc4 {
  @Id
  private String id;

  @Searchable(
      sortable = true
  )
  @NonNull
  private String first;

  @Indexed
  private String second;

  @NonNull
  private String third;
}
