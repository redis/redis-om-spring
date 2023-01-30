package com.redis.om.spring.repository.query.clause;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.repository.query.parser.Part;

import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.util.ObjectUtils;

import redis.clients.jedis.search.Schema.FieldType;

public enum QueryClause {
  // FULL TEXT
  TEXT_ALL( //
      QueryClauseTemplate.of(FieldType.TEXT, Part.Type.SIMPLE_PROPERTY, QueryClause.FIRST_PARAM, 1) //
  ),
  TEXT_SIMPLE_PROPERTY( //
      QueryClauseTemplate.of(FieldType.TEXT, Part.Type.SIMPLE_PROPERTY, QueryClause.FIELD_EQUAL, 1) //
  ),
  TEXT_NEGATING_SIMPLE_PROPERTY( //
      QueryClauseTemplate.of(FieldType.NUMERIC, Part.Type.SIMPLE_PROPERTY, QueryClause.FIELD_NON_EQUAL_PARAM_0, 1) //
  ),
  TEXT_STARTING_WITH( //
      QueryClauseTemplate.of(FieldType.TEXT, Part.Type.STARTING_WITH, QueryClause.FIELD_TEXT_STARTING_WITH, 1) //
  ),
  // TODO: currently not supported with RediSearch - potential work around with aggregations
  TEXT_ENDING_WITH( //
      QueryClauseTemplate.of(FieldType.TEXT, Part.Type.ENDING_WITH, QueryClause.FIELD_EQUAL, 1) //
  ),
  TEXT_LIKE( //
      QueryClauseTemplate.of(FieldType.TEXT, Part.Type.LIKE, QueryClause.FIELD_LIKE, 1) //
  ),
  TEXT_NOT_LIKE( //
      QueryClauseTemplate.of(FieldType.TEXT, Part.Type.NOT_LIKE, QueryClause.FIELD_NOT_LIKE, 1) //
  ),
  TEXT_CONTAINING( //
      QueryClauseTemplate.of(FieldType.TEXT, Part.Type.CONTAINING, QueryClause.FIELD_LIKE, 1) //
  ),
  TEXT_NOT_CONTAINING( //
      QueryClauseTemplate.of(FieldType.TEXT, Part.Type.NOT_CONTAINING, QueryClause.FIELD_NOT_LIKE, 1) //
  ),
  // NUMERIC
  NUMERIC_SIMPLE_PROPERTY( //
      QueryClauseTemplate.of(FieldType.NUMERIC, Part.Type.SIMPLE_PROPERTY, QueryClause.FIELD_NUMERIC_EQUAL_PARAM_0, 1) //
  ),
  NUMERIC_NEGATING_SIMPLE_PROPERTY( //
      QueryClauseTemplate.of(FieldType.NUMERIC, Part.Type.SIMPLE_PROPERTY, QueryClause.FIELD_NUMERIC_NOT_EQUAL, 1) //
  ),
  NUMERIC_BETWEEN( //
      QueryClauseTemplate.of(FieldType.NUMERIC, Part.Type.BETWEEN, QueryClause.FIELD_NUMERIC_BETWEEN, 2) //
  ),
  NUMERIC_LESS_THAN( //
      QueryClauseTemplate.of(FieldType.NUMERIC, Part.Type.LESS_THAN, QueryClause.FIELD_NUMERIC_LESS_THAN, 1) //
  ),
  NUMERIC_LESS_THAN_EQUAL( //
      QueryClauseTemplate.of(FieldType.NUMERIC, Part.Type.LESS_THAN_EQUAL, QueryClause.FIELD_NUMERIC_LESS_THAN_EQUAL, 1) //
  ),
  NUMERIC_GREATER_THAN( //
      QueryClauseTemplate.of(FieldType.NUMERIC, Part.Type.GREATER_THAN, QueryClause.FIELD_NUMERIC_GREATER_THAN, 1) //
  ),
  NUMERIC_GREATER_THAN_EQUAL( //
      QueryClauseTemplate.of(FieldType.NUMERIC, Part.Type.GREATER_THAN_EQUAL, QueryClause.FIELD_NUMERIC_GREATER_THAN_EQUAL, 1) //
  ),
  NUMERIC_BEFORE( //
      QueryClauseTemplate.of(FieldType.NUMERIC, Part.Type.BEFORE, QueryClause.FIELD_NUMERIC_BEFORE, 1) //
  ),
  NUMERIC_AFTER( //
      QueryClauseTemplate.of(FieldType.NUMERIC, Part.Type.AFTER, QueryClause.FIELD_NUMERIC_AFTER, 1) //
  ),
  NUMERIC_CONTAINING( //
      QueryClauseTemplate.of(FieldType.NUMERIC, Part.Type.CONTAINING, QueryClause.FIRST_PARAM, 1) //
  ),
  NUMERIC_CONTAINING_ALL( //
      QueryClauseTemplate.of(FieldType.NUMERIC, Part.Type.CONTAINING, QueryClause.FIRST_PARAM, 1) //
  ),
  // GEO
  GEO_NEAR( //
      QueryClauseTemplate.of(FieldType.GEO, Part.Type.NEAR, QueryClause.FIELD_GEO_NEAR, 2) //
  ),
  GEO_CONTAINING( //
      QueryClauseTemplate.of(FieldType.GEO, Part.Type.CONTAINING, QueryClause.FIRST_PARAM, 1) //
  ),
  GEO_CONTAINING_ALL( //
      QueryClauseTemplate.of(FieldType.GEO, Part.Type.CONTAINING, QueryClause.FIRST_PARAM, 1) //
  ),
  // TAG
  TAG_SIMPLE_PROPERTY( //
      QueryClauseTemplate.of(FieldType.TAG, Part.Type.SIMPLE_PROPERTY, QueryClause.FIELD_TAG_EQUAL, 1) //
  ),
  TAG_NOT_IN( //
      QueryClauseTemplate.of(FieldType.TAG, Part.Type.NOT_IN, QueryClause.FIELD_EQUAL, 1) //
  ),
  TAG_IN( //
      QueryClauseTemplate.of(FieldType.TAG, Part.Type.IN, QueryClause.FIELD_EQUAL, 1) //
  ),
  TAG_CONTAINING( //
      QueryClauseTemplate.of(FieldType.TAG, Part.Type.CONTAINING, QueryClause.FIELD_EQUAL, 1) //
  ),
  TAG_CONTAINING_ALL( //
      QueryClauseTemplate.of(FieldType.TAG, Part.Type.CONTAINING, QueryClause.FIRST_PARAM, 1) //
  );

  private static final String FIRST_PARAM = "$param_0";
  private static final String FIELD_EQUAL ="@$field:$param_0";
  private static final String FIELD_NON_EQUAL_PARAM_0 ="-@$field:$param_0";
  private static final String FIELD_TAG_EQUAL = "@$field:{$param_0}";
  private static final String FIELD_LIKE = "@$field:%%%$param_0%%%";
  private static final String FIELD_NOT_LIKE = "-@$field:%%%$param_0%%%";
  private static final String FIELD_NUMERIC_EQUAL_PARAM_0 = "@$field:[$param_0 $param_0]";
  private static final String FIELD_TEXT_STARTING_WITH ="@$field:$param_0*";
  private static final String FIELD_NUMERIC_NOT_EQUAL = "@$field:-[$param_0 $param_0]";
  private static final String FIELD_NUMERIC_BETWEEN = "@$field:[$param_0 $param_1]";
  private static final String FIELD_NUMERIC_LESS_THAN = "@$field:[-inf ($param_0]";
  private static final String FIELD_NUMERIC_LESS_THAN_EQUAL = "@$field:[-inf $param_0]";
  private static final String FIELD_NUMERIC_GREATER_THAN = "@$field:[($param_0 inf]";
  private static final String FIELD_NUMERIC_GREATER_THAN_EQUAL = "@$field:[$param_0 inf]";
  private static final String FIELD_NUMERIC_BEFORE = "@$field:[-inf ($param_0]";
  private static final String FIELD_NUMERIC_AFTER = "@$field:[($param_0 inf]";
  private static final String FIELD_GEO_NEAR = "@$field:[$param_0 $param_1 $param_2]";

  private final QueryClauseTemplate clauseTemplate;
  private final MappingRedisOMConverter converter = new MappingRedisOMConverter();

  QueryClause(QueryClauseTemplate value) {
    this.clauseTemplate = value;
  }

  public QueryClauseTemplate getClauseTemplate() {
    return clauseTemplate;
  }

  public String prepareQuery(String field, Object... params) {
    String prepared = field.equalsIgnoreCase("__ALL__") ? clauseTemplate.getQuerySegmentTemplate() : clauseTemplate.getQuerySegmentTemplate().replace("$field", field);

    Iterator<Object> iter = Arrays.asList(params).iterator();

    int i = 0;
    while (iter.hasNext()) {
      Object param = iter.next();
      String paramClass = param.getClass().getName();
      switch (paramClass) {
        case "org.springframework.data.geo.Point":
          Point point = (Point) param;
          prepared = prepared.replace("$param_" + i++, Double.toString(point.getX()));
          prepared = prepared.replace("$param_" + i++, Double.toString(point.getY()));
          break;
        case "org.springframework.data.geo.Distance":
          Distance distance = (Distance) param;
          prepared = prepared.replace("$param_" + i++, ObjectUtils.getDistanceAsRedisString(distance));
          break;
        default:
          // unfold collections
          if (param instanceof Collection<?> c) {
            String value;
            if (this == QueryClause.TAG_CONTAINING_ALL) {
              value = c.stream().map(n -> "@" + field + ":{" + QueryUtils.escape(ObjectUtils.asString(n, converter)) + "}").collect(Collectors.joining(" "));
            } else if (this == QueryClause.NUMERIC_CONTAINING) {
              value = c.stream().map(n -> "@" + field + ":[" + QueryUtils.escape(ObjectUtils.asString(n, converter)) + " " + QueryUtils.escape(ObjectUtils.asString(n, converter)) + "]").collect(Collectors.joining("|"));
            } else if (this == QueryClause.NUMERIC_CONTAINING_ALL) {
              value = c.stream().map(n -> "@" + field + ":[" + QueryUtils.escape(ObjectUtils.asString(n, converter)) + " " + QueryUtils.escape(ObjectUtils.asString(n, converter)) + "]").collect(Collectors.joining(" "));
            } else if (this == QueryClause.GEO_CONTAINING) {
              value = c.stream().map(n -> {
                Point p = (Point) n;
                return "@" + field + ":[" + p.getX() + " " + p.getY() + " .000001 ft]";
              }).collect(Collectors.joining("|"));
            } else if (this == QueryClause.GEO_CONTAINING_ALL) {
              value = c.stream().map(n -> {
                Point p = (Point) n;
                return "@" + field + ":[" + p.getX() + " " + p.getY() + " .000001 ft]";
              }).collect(Collectors.joining(" "));
            } else {
              value = c.stream().map(n -> QueryUtils.escape(ObjectUtils.asString(n, converter), true)).collect(Collectors.joining("|"));
            }

            prepared = prepared.replace("$param_" + i++, value);
          } else {
            if (clauseTemplate.getIndexType() == FieldType.TEXT) {
              prepared = prepared.replace("$param_" + i++, param.toString());
            } else {
              prepared = prepared.replace("$param_" + i++, QueryUtils.escape(ObjectUtils.asString(param, converter)));
            }
          }
          break;
      }
    }

    return prepared;
  }

  public static QueryClause get(FieldType fieldType, Part.Type partType) {
    try {
      return QueryClause.valueOf(fieldType.toString() + "_" + partType.name());
    } catch (IllegalArgumentException | NullPointerException e) {
      return TAG_SIMPLE_PROPERTY;
    }
  }

  public static final Map<String,String> methodNameMap = Map.of(
      "IsContainingAll", "IsContaining",
      "ContainingAll", "Containing",
      "ContainsAll", "Contains"
  );

  public static final Pattern CONTAINING_ALL_PATTERN = Pattern.compile("(IsContainingAll|ContainingAll|ContainsAll)");

  public static boolean hasContainingAllClause(String methodName) {
    return CONTAINING_ALL_PATTERN.matcher(methodName).find();
  }

  public static String getPostProcessMethodName(String methodName) {
    if (hasContainingAllClause(methodName)) {
      Optional<String> maybeMatchSubstring = CONTAINING_ALL_PATTERN.matcher(methodName).results().map(mr -> mr.group(1)).findFirst();
      if (maybeMatchSubstring.isPresent()) {
        String matchSubstring = maybeMatchSubstring.get();
        return methodName.replace(matchSubstring, methodNameMap.get(matchSubstring));
      } else {
        return methodName;
      }

    }
    return methodName;
  }
}