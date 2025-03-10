package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@Data
@Builder
@RequiredArgsConstructor(staticName = "of")
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
