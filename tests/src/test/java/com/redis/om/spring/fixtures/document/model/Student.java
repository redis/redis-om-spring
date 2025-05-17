package com.redis.om.spring.fixtures.document.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;

import com.google.gson.annotations.SerializedName;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor(
    staticName = "of"
)
@Document
public class Student {

  @Id
  private Long id;

  @Indexed(
      alias = "User-Name"
  )
  @SerializedName(
    "User-Name"
  )
  @NonNull
  private String userName;

  @Indexed(
      alias = "Event-Timestamp"
  )
  @SerializedName(
    "Event-Timestamp"
  )
  @NonNull
  private LocalDateTime eventTimestamp;

}
