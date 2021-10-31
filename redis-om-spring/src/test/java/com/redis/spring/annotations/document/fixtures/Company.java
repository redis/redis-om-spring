package com.redis.spring.annotations.document.fixtures;

import org.springframework.data.annotation.Id;

import com.redis.spring.annotations.Document;
import com.redis.spring.annotations.TextIndexed;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Document("company")
public class Company {
  @Id
  private String id;
  @NonNull
  @TextIndexed
  private String name;
  private boolean publiclyListed;
}
