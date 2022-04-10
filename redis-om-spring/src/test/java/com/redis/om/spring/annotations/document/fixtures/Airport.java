package com.redis.om.spring.annotations.document.fixtures;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.AutoComplete;
import com.redis.om.spring.annotations.AutoCompletePayload;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Document
public class Airport {
  @Id
  private String id;
  @AutoComplete @NonNull
  private String name;
  @AutoCompletePayload("name") @NonNull
  private String code;
  @AutoCompletePayload("name") @NonNull
  private String state;
}
