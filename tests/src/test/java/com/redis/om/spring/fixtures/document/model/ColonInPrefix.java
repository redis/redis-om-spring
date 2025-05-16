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
@Document(
  "aaa:bbb:ccc"
)
public class ColonInPrefix {
  @Id
  private Long id;

  @NonNull
  @Searchable(
      sortable = true
  )
  private String name;

  @Indexed
  private boolean taken;
}
