package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Document;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@RequiredArgsConstructor(staticName = "of")
@Document("country")
public class Country {
  @Id
  @NonNull
  private String id;
}
