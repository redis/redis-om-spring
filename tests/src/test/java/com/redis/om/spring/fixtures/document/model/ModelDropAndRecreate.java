package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.IndexingOptions;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;

import static com.redis.om.spring.annotations.IndexCreationMode.DROP_AND_RECREATE;

@Data
@Document
@IndexingOptions(creationMode = DROP_AND_RECREATE)
public class ModelDropAndRecreate {
  @Id
  private String id;
  @NonNull
  @Indexed
  private String name;
}
