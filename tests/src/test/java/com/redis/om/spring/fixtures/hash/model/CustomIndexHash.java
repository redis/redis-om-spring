package com.redis.om.spring.fixtures.hash.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import com.redis.om.spring.annotations.IndexingOptions;
import com.redis.om.spring.annotations.Searchable;

import lombok.*;

@Data
@RequiredArgsConstructor(
    staticName = "of"
)
@AllArgsConstructor(
    access = AccessLevel.PROTECTED
)
@NoArgsConstructor(
    force = true
)
@IndexingOptions(
    indexName = "MyCustomHashIndex"
)
@RedisHash(
  "custom_prefix"
)
public class CustomIndexHash {
  @Id
  private String id;

  @NonNull
  @Searchable(
      sortable = true
  )
  private String first;

  @NonNull
  @Searchable(
      sortable = true
  )
  private String second;
}
