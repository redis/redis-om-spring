/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.indexing;

import java.util.Optional;

import com.redis.lettucemod.search.Field;
import com.redis.om.sessions.converters.Converter;

public class TextField extends IndexedField {
  private TextField(String name, Optional<Class<?>> javaType, Optional<Converter<?>> converter, boolean sortable) {
    super(FieldType.text, name, javaType, converter, sortable);
  }

  @Override
  public Field<String> toLettuceModField() {
    return Field.text(name).sortable(sortable).build();
  }

  public static class Builder extends IndexedField.Builder {
    public Builder(String name) {
      super(FieldType.text, name);
    }

    @Override
    public IndexedField build() {
      return new TextField(name, javaType, converter, sortable);
    }
  }
}
