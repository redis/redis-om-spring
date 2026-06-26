package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Searchable;
import com.redis.om.spring.annotations.TextIndexed;

import lombok.Data;

@Data
public class WithSuffixTrieAutodetectedNestedItem {
  @Searchable(
      withSuffixTrie = true
  )
  private String searchable;

  @TextIndexed(
      withSuffixTrie = true
  )
  private String text;
}
