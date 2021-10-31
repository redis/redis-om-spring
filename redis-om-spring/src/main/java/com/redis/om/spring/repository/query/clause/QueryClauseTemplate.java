package com.redis.om.spring.repository.query.clause;

import org.springframework.data.repository.query.parser.Part;

import io.redisearch.Schema.FieldType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
public class QueryClauseTemplate {
  @NonNull private FieldType indexType;
  @NonNull private Part.Type queryPartType;
  
  @NonNull @EqualsAndHashCode.Exclude private String querySegmentTemplate;
  @NonNull private Integer numberOfArguments;
}

