package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.SerializationHint;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(
    access = AccessLevel.PROTECTED
)
@Document
public class Developer {

  @Id
  private String id;

  @Indexed(
      serializationHint = SerializationHint.ORDINAL
  )
  private DeveloperType typeOrdinal;

  private DeveloperState state;
}
