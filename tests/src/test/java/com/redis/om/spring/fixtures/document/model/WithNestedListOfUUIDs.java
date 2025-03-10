package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.UUID;

@Document
@Data
@RequiredArgsConstructor(staticName = "of")
public class WithNestedListOfUUIDs {
  @Id
  @NonNull
  private String id;

  @Indexed
  @NonNull
  private List<UUID> uuids;
}
