package com.redis.om.spring.repository.query.clause;

import org.springframework.data.repository.query.parser.Part;
import redis.clients.jedis.search.Schema;
import redis.clients.jedis.search.Schema.FieldType;

public class QueryClauseTemplate {
  
  private final FieldType indexType;
  
  private final Part.Type queryPartType;

  
  private final String querySegmentTemplate;
  
  private final Integer numberOfArguments;

  private QueryClauseTemplate( Schema.FieldType indexType,  Part.Type queryPartType,
       String querySegmentTemplate,  Integer numberOfArguments) {
    this.indexType = indexType;
    this.queryPartType = queryPartType;
    this.querySegmentTemplate = querySegmentTemplate;
    this.numberOfArguments = numberOfArguments;
  }

  public static QueryClauseTemplate of( Schema.FieldType indexType,  Part.Type queryPartType,
       String querySegmentTemplate,  Integer numberOfArguments) {
    return new QueryClauseTemplate(indexType, queryPartType, querySegmentTemplate, numberOfArguments);
  }

  public  FieldType getIndexType() {
    return this.indexType;
  }

  public  Part.Type getQueryPartType() {
    return this.queryPartType;
  }

  public  String getQuerySegmentTemplate() {
    return this.querySegmentTemplate;
  }

  public  Integer getNumberOfArguments() {
    return this.numberOfArguments;
  }
}

