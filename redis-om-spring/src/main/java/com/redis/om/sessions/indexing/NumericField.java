/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.indexing;

import java.util.Optional;

import com.redis.lettucemod.search.Field;
import com.redis.om.sessions.converters.Converter;

public class NumericField extends IndexedField {
  protected NumericField(String name, Optional<Class<?>> javaType, Optional<Converter<?>> converter, boolean sortable) {
    super(FieldType.numeric, name, javaType, converter, sortable);
  }

  @Override
  public Field<String> toLettuceModField() {
    return Field.numeric(name).sortable(sortable).build();
  }

  public static class Builder extends IndexedField.Builder {

    protected Builder(String name) {
      super(FieldType.numeric, name);
    }

    @Override
    public IndexedField build() {
      return new NumericField(name, javaType, converter, sortable);
    }
  }
}
