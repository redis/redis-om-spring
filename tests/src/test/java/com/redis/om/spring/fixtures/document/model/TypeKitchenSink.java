package com.redis.om.spring.fixtures.document.model;

import java.util.UUID;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

import lombok.*;

@Data
@Builder
@RequiredArgsConstructor(
    staticName = "of"
)
@NoArgsConstructor
@AllArgsConstructor
@Document
public class TypeKitchenSink {
  @Id
  private String id;

  @Indexed
  @NonNull
  private UUID uuid;
}
