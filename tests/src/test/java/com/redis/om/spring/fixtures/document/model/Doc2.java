package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(force = true)
@Document
public class Doc2 {
  @Id
  private String id;

  @NonNull
  @Searchable(sortable = true)
  private String text;

  @NonNull
  @Indexed(sortable = true)
  private String tag;
}
