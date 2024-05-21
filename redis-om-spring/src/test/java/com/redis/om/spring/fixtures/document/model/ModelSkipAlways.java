package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.IndexingOptions;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;

import static com.redis.om.spring.annotations.IndexCreationMode.SKIP_ALWAYS;

@Data
@Document
@IndexingOptions(creationMode = SKIP_ALWAYS)
public class ModelSkipAlways {
  @Id
  private String id;
  @NonNull
  @Indexed
  private String name;
}
