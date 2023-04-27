package com.redis.om.spring.repository.query.clause;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.parser.Part;
import redis.clients.jedis.search.Schema.FieldType;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class QueryClauseTemplate {
  @NonNull private FieldType indexType;
  @NonNull private Part.Type queryPartType;
  
  @NonNull @EqualsAndHashCode.Exclude private String querySegmentTemplate;
  @NonNull private Integer numberOfArguments;
}

