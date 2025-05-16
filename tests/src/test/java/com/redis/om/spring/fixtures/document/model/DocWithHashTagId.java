package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.id.IdAsHashTag;
import com.redis.om.spring.id.IdFilter;

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
  "dwht"
)
public class DocWithHashTagId {
  @Id
  @IdFilter(
      value = IdAsHashTag.class
  )
  private String id;

  @Indexed
  @NonNull
  private String name;
}
