package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.AutoComplete;
import com.redis.om.spring.annotations.AutoCompletePayload;
import com.redis.om.spring.annotations.Document;
import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Document
public class Airport {
  @Id
  private String id;
  @AutoComplete @NonNull
  private String name;
  @AutoCompletePayload("name") @NonNull
  private String code;
  @AutoCompletePayload("name") @NonNull
  private String state;
}
