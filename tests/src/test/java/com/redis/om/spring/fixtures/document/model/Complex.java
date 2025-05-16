package com.redis.om.spring.fixtures.document.model;

import java.util.List;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Document
@Data
@RequiredArgsConstructor(
    staticName = "of"
)
public class Complex {
  @Id
  @NonNull
  private String id;

  @Indexed
  @NonNull
  private List<HasAList> myList;
}
