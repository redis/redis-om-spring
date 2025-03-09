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
public class DocWithEnum {
  @Id
  private String id;

  @Indexed
  @NonNull
  private MyJavaEnum enumProp;

  @Searchable
  @NonNull
  private MyJavaEnum searchableEnumProp;
}
