package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import com.redis.om.spring.repository.query.SearchLanguage;
import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Document(language = SearchLanguage.SPANISH)
public class SpanishDoc {
  @Id
  private String id;

  @NonNull
  @Indexed
  private String title;

  @NonNull
  @Searchable
  private String body;
}
