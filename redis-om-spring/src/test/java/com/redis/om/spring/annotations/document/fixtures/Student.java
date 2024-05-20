package com.redis.om.spring.annotations.document.fixtures;

import com.google.gson.annotations.SerializedName;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
@Document
public class Student {

  @Id
  private Long id;

  @Indexed(alias = "User-Name")
  @SerializedName("User-Name")
  @NonNull
  private String userName;

  @Indexed(alias = "Event-Timestamp")
  @SerializedName("Event-Timestamp")
  @NonNull
  private LocalDateTime eventTimestamp;

}
