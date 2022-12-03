package com.redis.om.spring.annotations.document.fixtures;

import lombok.Builder.Default;
import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Document(languageField = "language")
public class MultiLingualDoc {
  @Id
  private String id;

  @NonNull
  @Indexed
  private String language;

  @NonNull
  @Searchable
  private String body;
}
