/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.indexing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.redis.om.sessions.Constants;

import lombok.Getter;

@Getter
public class RedisIndexConfiguration {
  private Map<String, IndexedField> fields;
  private Optional<String> name;

  private RedisIndexConfiguration(Optional<String> name, Map<String, IndexedField> fields) {
    this.fields = fields;
    this.name = name;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final Map<String, IndexedField> fields = new HashMap<>();
    private Optional<String> name;

    private Builder() {
      fields.put(Constants.SIZE_FIELD_NAME, IndexedField.numeric(Constants.SIZE_FIELD_NAME).sortable(true).javaType(
          Long.class).build());
    }

    public Builder fields(List<IndexedField> indexedFields) {
      indexedFields.forEach(f -> this.fields.put(f.getName(), f));
      return this;
    }

    public Builder withField(IndexedField indexedField) {
      this.fields.put(indexedField.getName(), indexedField);
      return this;
    }

    public Builder name(String name) {
      this.name = Optional.of(name);
      return this;
    }

    public RedisIndexConfiguration build() {
      return new RedisIndexConfiguration(this.name, this.fields);
    }
  }
}
