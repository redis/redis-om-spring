package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Document;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;

import java.util.Set;

@Data
@RequiredArgsConstructor(staticName = "of")
@Document("states")
public class States {
  @Id
  @NonNull
  private String id;

  @Reference
  @NonNull
  private Set<State> states;
}
