package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Document(languageField = "language")
public class MultiLingualDoc {
  @Id
  private String id;

  @NonNull
  @Indexed
  private String language;

  @NonNull
  @Searchable
  private String body;
}
