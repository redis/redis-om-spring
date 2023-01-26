package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
@Document(timeToLive = 5)
public class ExpiringPersonWithDefault {
  @Id String id;
  @NonNull
  String name;
}
