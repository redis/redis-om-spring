/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.indexing;

import java.util.Optional;

import com.redis.lettucemod.search.Field;
import com.redis.om.sessions.converters.Converter;

public class GeoField extends IndexedField {
  protected GeoField(String name, Optional<Class<?>> javaType, Optional<Converter<?>> converter, boolean sortable) {
    super(FieldType.geo, name, javaType, converter, sortable);
  }

  @Override
  public Field<String> toLettuceModField() {
    return Field.geo(name).sortable(sortable).build();
  }

  public static class Builder extends IndexedField.Builder {

    public Builder(String name) {
      super(FieldType.geo, name);
    }

    @Override
    public IndexedField build() {
      return new GeoField(name, javaType, converter, sortable);
    }
  }
}
