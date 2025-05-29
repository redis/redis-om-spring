package com.redis.om.spring.repository.query.clause;

import org.springframework.data.repository.query.parser.Part;

import redis.clients.jedis.search.Schema;
import redis.clients.jedis.search.Schema.FieldType;

/**
 * Template class for defining query clause structures used in Redis search operations.
 * <p>
 * This class encapsulates the metadata and template information needed to construct
 * Redis search queries for different field types and query operations. It serves as
 * a blueprint for generating specific query segments based on field types, query part types,
 * and parameter counts.
 * </p>
 * <p>
 * Each template includes information about the Redis field type (TEXT, NUMERIC, TAG, GEO),
 * the Spring Data query part type, the query template string, and the expected number
 * of arguments for the query operation.
 * </p>
 *
 * @see QueryClause
 * @see redis.clients.jedis.search.Schema.FieldType
 * @see org.springframework.data.repository.query.parser.Part.Type
 * @since 0.1.0
 */
public class QueryClauseTemplate {

  private final FieldType indexType;

  private final Part.Type queryPartType;

  private final String querySegmentTemplate;

  private final Integer numberOfArguments;

  private QueryClauseTemplate(Schema.FieldType indexType, Part.Type queryPartType, String querySegmentTemplate,
      Integer numberOfArguments) {
    this.indexType = indexType;
    this.queryPartType = queryPartType;
    this.querySegmentTemplate = querySegmentTemplate;
    this.numberOfArguments = numberOfArguments;
  }

  /**
   * Factory method to create a new QueryClauseTemplate instance.
   *
   * @param indexType            the Redis field type (TEXT, NUMERIC, TAG, GEO)
   * @param queryPartType        the Spring Data query part type (SIMPLE_PROPERTY, CONTAINING, etc.)
   * @param querySegmentTemplate the query template string with placeholders
   * @param numberOfArguments    the expected number of arguments for this query type
   * @return a new QueryClauseTemplate instance
   */
  public static QueryClauseTemplate of(Schema.FieldType indexType, Part.Type queryPartType, String querySegmentTemplate,
      Integer numberOfArguments) {
    return new QueryClauseTemplate(indexType, queryPartType, querySegmentTemplate, numberOfArguments);
  }

  /**
   * Gets the Redis field type for this query template.
   *
   * @return the field type (TEXT, NUMERIC, TAG, GEO)
   */
  public FieldType getIndexType() {
    return this.indexType;
  }

  /**
   * Gets the Spring Data query part type for this template.
   *
   * @return the query part type (SIMPLE_PROPERTY, CONTAINING, etc.)
   */
  public Part.Type getQueryPartType() {
    return this.queryPartType;
  }

  /**
   * Gets the query template string with placeholders.
   * <p>
   * The template contains placeholders like $field and parameter markers that
   * will be replaced during query construction.
   * </p>
   *
   * @return the query segment template string
   */
  public String getQuerySegmentTemplate() {
    return this.querySegmentTemplate;
  }

  /**
   * Gets the expected number of arguments for this query template.
   *
   * @return the number of arguments this query type expects
   */
  public Integer getNumberOfArguments() {
    return this.numberOfArguments;
  }
}
