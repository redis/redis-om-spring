package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;

@Data
@RequiredArgsConstructor(staticName = "of")
@Document("state")
public class State {
  @Id
  @NonNull
  private String id;

  @NonNull
  @Indexed
  private String name;

  @NonNull
  @Reference
  private Country country;
}
