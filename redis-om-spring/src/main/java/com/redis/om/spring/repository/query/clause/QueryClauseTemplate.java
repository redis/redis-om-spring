package com.redis.om.spring.repository.query.clause;

import org.springframework.data.repository.query.parser.Part;

import io.redisearch.Schema.FieldType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class QueryClauseTemplate {
  @NonNull private FieldType indexType;
  @NonNull private Part.Type queryPartType;
  
  @NonNull @EqualsAndHashCode.Exclude private String querySegmentTemplate;
  @NonNull private Integer numberOfArguments;
}

