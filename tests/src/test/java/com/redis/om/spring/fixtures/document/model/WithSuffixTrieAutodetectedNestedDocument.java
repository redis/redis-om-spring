package com.redis.om.spring.fixtures.document.model;

import java.util.List;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

import lombok.Data;

@Data
@Document
public class WithSuffixTrieAutodetectedNestedDocument {
  @Id
  private String id;

  @Indexed
  private List<WithSuffixTrieAutodetectedNestedItem> autodetectedItems;
}
