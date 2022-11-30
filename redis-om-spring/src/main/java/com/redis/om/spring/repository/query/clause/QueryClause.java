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

import io.redisearch.Schema.FieldType;

public enum QueryClause {
  // FULL TEXT
  FullText_ALL( //
      QueryClauseTemplate.of(FieldType.FullText, Part.Type.SIMPLE_PROPERTY, "$param_0", 1) //
  ),
  FullText_SIMPLE_PROPERTY( //
      QueryClauseTemplate.of(FieldType.FullText, Part.Type.SIMPLE_PROPERTY, "@$field:$param_0", 1) //
  ),
  FullText_NEGATING_SIMPLE_PROPERTY( //
      QueryClauseTemplate.of(FieldType.Numeric, Part.Type.SIMPLE_PROPERTY, "-@$field:$param_0", 1) //
  ),
  FullText_STARTING_WITH( //
      QueryClauseTemplate.of(FieldType.FullText, Part.Type.STARTING_WITH, "@$field:$param_0*", 1) //
  ),
  // TODO: currently not supported with RediSearch - potential work around with aggregations
  FullText_ENDING_WITH( //
      QueryClauseTemplate.of(FieldType.FullText, Part.Type.ENDING_WITH, "@$field:$param_0", 1) //
  ),
  FullText_LIKE( //
      QueryClauseTemplate.of(FieldType.FullText, Part.Type.LIKE, "@$field:%%%$param_0%%%", 1) //
  ),
  FullText_NOT_LIKE( //
      QueryClauseTemplate.of(FieldType.FullText, Part.Type.NOT_LIKE, "-@$field:%%%$param_0%%%", 1) //
  ),
  FullText_CONTAINING( //
      QueryClauseTemplate.of(FieldType.FullText, Part.Type.CONTAINING, "@$field:%%%$param_0%%%", 1) //
  ),
  FullText_NOT_CONTAINING( //
      QueryClauseTemplate.of(FieldType.FullText, Part.Type.NOT_CONTAINING, "-@$field:%%%$param_0%%%", 1) //
  ),
  // NUMERIC
  Numeric_SIMPLE_PROPERTY( //
      QueryClauseTemplate.of(FieldType.Numeric, Part.Type.SIMPLE_PROPERTY, "@$field:[$param_0 $param_0]", 1) //
  ),
  Numeric_NEGATING_SIMPLE_PROPERTY( //
      QueryClauseTemplate.of(FieldType.Numeric, Part.Type.SIMPLE_PROPERTY, "@$field:-[$param_0 $param_0]", 1) //
  ),
  Numeric_BETWEEN( //
      QueryClauseTemplate.of(FieldType.Numeric, Part.Type.BETWEEN, "@$field:[$param_0 $param_1]", 2) //
  ),
  Numeric_LESS_THAN( //
      QueryClauseTemplate.of(FieldType.Numeric, Part.Type.LESS_THAN, "@$field:[-inf ($param_0]", 1) //
  ),
  Numeric_LESS_THAN_EQUAL( //
      QueryClauseTemplate.of(FieldType.Numeric, Part.Type.LESS_THAN_EQUAL, "@$field:[-inf $param_0]", 1) //
  ),
  Numeric_GREATER_THAN( //
      QueryClauseTemplate.of(FieldType.Numeric, Part.Type.GREATER_THAN, "@$field:[($param_0 inf]", 1) //
  ),
  Numeric_GREATER_THAN_EQUAL( //
      QueryClauseTemplate.of(FieldType.Numeric, Part.Type.GREATER_THAN_EQUAL, "@$field:[$param_0 inf]", 1) //
  ),
  Numeric_BEFORE( //
      QueryClauseTemplate.of(FieldType.Numeric, Part.Type.BEFORE, "@$field:[-inf ($param_0]", 1) //
  ),
  Numeric_AFTER( //
      QueryClauseTemplate.of(FieldType.Numeric, Part.Type.AFTER, "@$field:[($param_0 inf]", 1) //
  ),
  Numeric_CONTAINING( //
      QueryClauseTemplate.of(FieldType.Numeric, Part.Type.CONTAINING, "$param_0", 1) //
  ),
  Numeric_CONTAINING_ALL( //
      QueryClauseTemplate.of(FieldType.Numeric, Part.Type.CONTAINING, "$param_0", 1) //
  ),
  // GEO
  Geo_NEAR( //
      QueryClauseTemplate.of(FieldType.Geo, Part.Type.NEAR, "@$field:[$param_0 $param_1 $param_2]", 2) //
  ),
  Geo_CONTAINING( //
      QueryClauseTemplate.of(FieldType.Geo, Part.Type.CONTAINING, "$param_0", 1) //
  ),
  Geo_CONTAINING_ALL( //
      QueryClauseTemplate.of(FieldType.Geo, Part.Type.CONTAINING, "$param_0", 1) //
  ),
  // TAG
  Tag_SIMPLE_PROPERTY( //
      QueryClauseTemplate.of(FieldType.Tag, Part.Type.SIMPLE_PROPERTY, "@$field:{$param_0}", 1) //
  ),
  Tag_NOT_IN( //
      QueryClauseTemplate.of(FieldType.Tag, Part.Type.NOT_IN, "@$field:$param_0", 1) //
  ),
  Tag_IN( //
      QueryClauseTemplate.of(FieldType.Tag, Part.Type.IN, "@$field:$param_0", 1) //
  ),
  Tag_CONTAINING( //
      QueryClauseTemplate.of(FieldType.Tag, Part.Type.CONTAINING, "@$field:$param_0", 1) //
  ),
  Tag_CONTAINING_ALL( //
      QueryClauseTemplate.of(FieldType.Tag, Part.Type.CONTAINING, "$param_0", 1) //
  );

  private final QueryClauseTemplate value;
  private final MappingRedisOMConverter converter = new MappingRedisOMConverter();

  private QueryClause(QueryClauseTemplate value) {
    this.value = value;
  }

  public QueryClauseTemplate getValue() {
    return value;
  }

  public String prepareQuery(String field, Object... params) {
    String prepared = field.equalsIgnoreCase("__ALL__") ? value.getQuerySegmentTemplate() : value.getQuerySegmentTemplate().replace("$field", field);

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
          if (param instanceof Collection<?>) {
            @SuppressWarnings("rawtypes")
            Collection<?> c = (Collection) param;
            String value = "";
            if (this == QueryClause.Tag_CONTAINING_ALL) {
              value = c.stream().map(n -> "@" + field + ":{" + QueryUtils.escape(ObjectUtils.asString(n, converter)) + "}").collect(Collectors.joining(" "));
            } else if (this == QueryClause.Numeric_CONTAINING) {
              value = c.stream().map(n -> "@" + field + ":[" + QueryUtils.escape(ObjectUtils.asString(n, converter)) + " " + QueryUtils.escape(ObjectUtils.asString(n, converter)) + "]").collect(Collectors.joining("|"));
            } else if (this == QueryClause.Numeric_CONTAINING_ALL) {
              value = c.stream().map(n -> "@" + field + ":[" + QueryUtils.escape(ObjectUtils.asString(n, converter)) + " " + QueryUtils.escape(ObjectUtils.asString(n, converter)) + "]").collect(Collectors.joining(" "));
            } else if (this == QueryClause.Geo_CONTAINING) {
              value = c.stream().map(n -> {
                Point p = (Point) n;
                return "@" + field + ":[" + p.getX() + " " + p.getY() + " .000001 ft]";
              }).collect(Collectors.joining("|"));
            } else if (this == QueryClause.Geo_CONTAINING_ALL) {
              value = c.stream().map(n -> {
                Point p = (Point) n;
                return "@" + field + ":[" + p.getX() + " " + p.getY() + " .000001 ft]";
              }).collect(Collectors.joining(" "));
            } else {
              value = c.stream().map(n -> QueryUtils.escape(ObjectUtils.asString(n, converter), true)).collect(Collectors.joining("|"));
            }

            prepared = prepared.replace("$param_" + i++, value);
          } else {
            if (value.getIndexType() == FieldType.FullText) {
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
      return Tag_SIMPLE_PROPERTY;
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