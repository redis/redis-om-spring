package com.redis.om.spring.annotations.document.fixtures;

import com.google.gson.annotations.SerializedName;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
@Builder
@RequiredArgsConstructor(staticName = "of")
@Document
public class Student {

  @Id
  @NonNull
  private Long id;

  @Indexed(alias = "User-Name")//  fieldName = "User-Name", schemaFieldType = SchemaFieldType.TAG
  @SerializedName("User-Name")
  @NonNull
  private String userName;

  @Indexed
  @NonNull
  private LocalDateTime eventTimestamp;

}
