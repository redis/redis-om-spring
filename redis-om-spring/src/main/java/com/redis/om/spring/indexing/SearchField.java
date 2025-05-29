package com.redis.om.spring.indexing;

import java.lang.reflect.Field;

import redis.clients.jedis.search.schemafields.SchemaField;

/**
 * Represents a search field mapping between a Java field and its RediSearch schema field.
 * This class encapsulates the relationship between a Java reflection Field and the
 * corresponding RediSearch SchemaField definition used for indexing.
 * 
 * <p>SearchField instances are created during index creation to maintain the mapping
 * between entity fields and their search index representations. This enables the
 * framework to properly configure RediSearch indexes based on Java field annotations
 * and types.</p>
 * 
 * @since 1.0.0
 */
public class SearchField {
  private final Field field;
  private final SchemaField schemaField;

  /**
   * Creates a new SearchField with the specified Java field and schema field.
   * 
   * @param field       the Java reflection field
   * @param schemaField the corresponding RediSearch schema field
   */
  public SearchField(Field field, SchemaField schemaField) {
    this.field = field;
    this.schemaField = schemaField;
  }

  /**
   * Static factory method to create a SearchField instance.
   * 
   * @param field       the Java reflection field
   * @param schemaField the corresponding RediSearch schema field
   * @return a new SearchField instance
   */
  public static SearchField of(Field field, SchemaField schemaField) {
    return new SearchField(field, schemaField);
  }

  /**
   * Returns the RediSearch schema field definition.
   * 
   * @return the schema field used for indexing
   */
  public SchemaField getSchemaField() {
    return schemaField;
  }

  /**
   * Returns the Java reflection field.
   * 
   * @return the Java field being indexed
   */
  public Field getField() {
    return field;
  }
}
