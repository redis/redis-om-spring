package com.redis.om.spring.fixtures.document.model;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(
    force = true
)
@Document
public class NotNullAnnotated {
  @JsonProperty(
      required = true
  )
  @NotNull(
      message = "dbId is required"
  )
  @Id
  private String dbId;

  @JsonProperty(
      required = true
  )
  @Indexed
  private String someOtherString;
}
