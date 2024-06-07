package com.redis.om.spring.indexing;

import redis.clients.jedis.search.schemafields.SchemaField;

import java.lang.reflect.Field;

public class SearchField {
  private final Field field;
  private final SchemaField schemaField;

  public SearchField(Field field, SchemaField schemaField) {
    this.field = field;
    this.schemaField = schemaField;
  }

  public static SearchField of(Field field, SchemaField schemaField) {
    return new SearchField(field, schemaField);
  }

  public SchemaField getSchemaField() {
    return schemaField;
  }

  public Field getField() {
    return field;
  }
}
