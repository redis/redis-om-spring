package com.redis.om.spring.fixtures.document.model;

import com.github.f4b6a3.ulid.Ulid;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;

@Document
@Data
@RequiredArgsConstructor(staticName = "of")
public class WithNestedListOfUlids {
  @Id
  @NonNull
  private String id;

  @Indexed
  @NonNull
  private List<Ulid> ulids;
}
