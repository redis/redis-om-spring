/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.indexing;

import java.util.Optional;

import com.redis.lettucemod.search.Field;
import com.redis.om.sessions.GeoLoc;
import com.redis.om.sessions.converters.*;

import lombok.Getter;

@Getter
public abstract class IndexedField {
  protected final FieldType fieldType;
  protected final String name;
  protected final Class<?> javaType;
  protected final Converter<?> converter;
  protected final boolean sortable;

  protected IndexedField(FieldType fieldType, String name, Optional<Class<?>> javaType,
      Optional<Converter<?>> converter, boolean sortable) {
    this.fieldType = fieldType;
    this.name = name;
    this.sortable = sortable;
    if (javaType.isPresent()) {
      this.javaType = javaType.get();
    } else {
      this.javaType = defaultClass();
    }

    if (converter.isPresent()) {
      this.converter = converter.get();
    } else {
      this.converter = defaultConverter();
    }
  }

  private Converter<?> defaultConverter() {
    switch (this.fieldType) {
      case geo:
        return new GeoLocConverter();
      case tag:
        if (Enum.class.isAssignableFrom(this.javaType)) {
          return new EnumConverter(this.javaType);
        }
        return new StringConverter();
      case text:
        return new StringConverter();
      case numeric:
        if (this.javaType == Long.class) {
          return new LongConverter();
        } else if (this.javaType == Short.class) {
          return new ShortConverter();
        } else if (this.javaType == Double.class) {
          return new DoubleConverter();
        } else if (this.javaType == Byte.class) {
          return new ByteConverter();
        } else if (this.javaType == Integer.class) {
          return new IntegerConverter();
        } else if (this.javaType == Number.class) {
          return new DoubleConverter();
        } else {
          throw new IllegalArgumentException(String.format("Passed Numeric index type without a valid java type %s",
              this.javaType.getName()));
        }
      case vector:
      default: // TODO build converter for vectors
        throw new IllegalArgumentException("Unusable fieldType");
    }
  }

  private Class<?> defaultClass() {
    switch (this.fieldType) {
      case geo:
        return GeoLoc.class;
      case tag:
      case text:
        return String.class;
      case numeric:
        return Number.class;
      case vector: // TODO how to get float[] clazz?
      default:
        throw new IllegalArgumentException("Unusable fieldType");
    }
  }

  public boolean isKnownOrDefaultClass(Class<?> clazz) {
    if (clazz == this.javaType) {
      return true;
    }

    if (Number.class.isAssignableFrom(clazz)) {
      return true;
    }

    return clazz == defaultClass();
  }

  public static TagField.Builder tag(String name) {
    return new TagField.Builder(name);
  }

  public static TextField.Builder text(String name) {
    return new TextField.Builder(name);
  }

  public static GeoField.Builder geo(String name) {
    return new GeoField.Builder(name);
  }

  public static NumericField.Builder numeric(String name) {
    return new NumericField.Builder(name);
  }

  public abstract Field<String> toLettuceModField();

  public abstract static class Builder {
    protected final FieldType fieldType;
    protected final String name;
    protected boolean sortable;
    protected Optional<Class<?>> javaType = Optional.empty();
    protected Optional<Converter<?>> converter = Optional.empty();

    protected Builder(FieldType fieldType, String name) {
      this.name = name;
      this.fieldType = fieldType;
      this.sortable = false;
    }

    public Builder javaType(Class<?> javaType) {
      this.javaType = Optional.of(javaType);
      return this;
    }

    public Builder converter(Converter<?> converter) {
      this.converter = Optional.of(converter);
      return this;
    }

    public Builder sortable(boolean sortable) {
      this.sortable = sortable;
      return this;
    }

    public abstract IndexedField build();
  }
}
