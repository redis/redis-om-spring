/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.indexing;

import java.util.Optional;

import com.redis.lettucemod.search.Field;
import com.redis.om.sessions.converters.Converter;

public class TagField extends IndexedField {
  private final Optional<Character> separator;

  private TagField(String name, Optional<Class<?>> javaType, Optional<Converter<?>> converter, boolean sortable,
      Optional<Character> separator) {
    super(FieldType.tag, name, javaType, converter, sortable);
    this.separator = separator;
  }

  @Override
  public Field<String> toLettuceModField() {
    com.redis.lettucemod.search.TagField.Builder<String> builder = Field.tag(name).sortable(sortable);
    separator.ifPresent(builder::separator);
    return builder.build();
  }

  public static class Builder extends IndexedField.Builder {
    private Optional<Character> separator = Optional.empty();

    public Builder(String fieldName) {
      super(FieldType.tag, fieldName);
    }

    public IndexedField.Builder separator(Character separator) {
      this.separator = Optional.of(separator);
      return this;
    }

    @Override
    public IndexedField build() {
      return new TagField(name, javaType, converter, sortable, separator);
    }
  }
}
