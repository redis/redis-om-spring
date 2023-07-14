package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Searchable;
import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(force = true)
@Document
public class Doc3 {
  @Id
  private String id;

  @Searchable(sortable = true)
  @NonNull
  private String first;

  @Searchable(sortable = true)
  private String second;

  @Searchable(sortable = true)
  private String third;
}
