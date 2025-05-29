package com.redis.om.spring.metamodel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides access to field information for search operations in the metamodel.
 * This class encapsulates the metadata needed to access and query fields in
 * RediSearch operations, including field paths, aliases, and type information.
 * 
 * <p>SearchFieldAccessor instances are created during metamodel generation to
 * provide efficient access to field information during query construction and
 * result mapping. They maintain the relationship between Java fields and their
 * corresponding search index representations.</p>
 * 
 * @since 1.0.0
 */
public class SearchFieldAccessor {
  private final List<Field> fields = new ArrayList<>();
  private final String searchAlias;
  private final String jsonPath;
  private final Class<?> targetClass;
  private final Class<?> declaringClass;

  /**
   * Creates a new SearchFieldAccessor with the specified search alias, JSON path, and fields.
   * 
   * @param searchAlias the alias used for search operations
   * @param jsonPath    the JSON path for accessing the field value
   * @param fields      the Java reflection fields associated with this accessor
   */
  public SearchFieldAccessor(String searchAlias, String jsonPath, Field... fields) {
    this.searchAlias = searchAlias;
    this.jsonPath = jsonPath;
    this.fields.addAll(Arrays.asList(fields));
    this.targetClass = this.fields.get(0).getType();
    this.declaringClass = this.fields.get(0).getDeclaringClass();
  }

  /**
   * Returns the primary Java reflection field associated with this accessor.
   * 
   * @return the primary field
   */
  public Field getField() {
    return fields.get(0);
  }

  /**
   * Returns the search alias used in RediSearch queries.
   * 
   * @return the search alias
   */
  public String getSearchAlias() {
    return searchAlias;
  }

  /**
   * Returns the JSON path for accessing the field value in documents.
   * 
   * @return the JSON path
   */
  public String getJsonPath() {
    return jsonPath;
  }

  /**
   * Returns the target class type of the field.
   * 
   * @return the field's target class
   */
  public Class<?> getTargetClass() {
    return targetClass;
  }

  /**
   * Returns the class that declares this field.
   * 
   * @return the declaring class
   */
  public Class<?> getDeclaringClass() {
    return declaringClass;
  }
}
