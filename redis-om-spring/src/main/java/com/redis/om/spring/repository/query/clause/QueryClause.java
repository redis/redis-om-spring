package com.redis.om.spring.repository.query.clause;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.Part.Type;

import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.util.ObjectUtils;

import redis.clients.jedis.search.Schema.FieldType;

/**
 * Enumeration of query clauses for building Redis search queries.
 * <p>
 * This enum defines various query clause types that can be used to construct
 * Redis search queries for different field types (TEXT, NUMERIC, GEO, TAG)
 * and different query operations (equality, range, pattern matching, etc.).
 * </p>
 * <p>
 * Each query clause contains a template that defines how the clause should
 * be formatted in the final Redis search query string.
 *
 * @since 1.0.0
 */
public enum QueryClause {
  // FULL TEXT
  /**
   * Text field query clause for full-text search across all fields.
   * Searches for the specified text in any indexed text field.
   */
  TEXT_ALL( //
      QueryClauseTemplate.of(FieldType.TEXT, Part.Type.SIMPLE_PROPERTY, QueryClause.FIRST_PARAM, 1) //
  ),
  /**
   * Text field query clause for simple property matching.
   * Performs exact equality matching on text fields.
   */
  TEXT_SIMPLE_PROPERTY( //
      QueryClauseTemplate.of(FieldType.TEXT, Part.Type.SIMPLE_PROPERTY, QueryClause.FIELD_EQUAL, 1) //
  ),
  /**
   * Text field query clause for negated simple property matching.
   * Performs inequality matching on text fields (not equal to).
   */
  TEXT_NEGATING_SIMPLE_PROPERTY( //
      QueryClauseTemplate.of(FieldType.NUMERIC, Part.Type.SIMPLE_PROPERTY, QueryClause.FIELD_NON_EQUAL_PARAM_0, 1) //
  ),
  /**
   * Text field query clause for prefix matching.
   * Matches text fields that start with the specified string.
   */
  TEXT_STARTING_WITH( //
      QueryClauseTemplate.of(FieldType.TEXT, Part.Type.STARTING_WITH, QueryClause.FIELD_TEXT_STARTING_WITH, 1) //
  ),
  /**
   * Text field query clause for suffix matching.
   * Matches text fields that end with the specified string.
   */
  TEXT_ENDING_WITH( //
      QueryClauseTemplate.of(FieldType.TEXT, Part.Type.ENDING_WITH, QueryClause.FIELD_TEXT_ENDING_WITH, 1) //
  ),
  /**
   * Text field query clause for pattern matching.
   * Supports wildcard and pattern matching using LIKE semantics.
   */
  TEXT_LIKE( //
      QueryClauseTemplate.of(FieldType.TEXT, Part.Type.LIKE, QueryClause.FIELD_LIKE, 1) //
  ),
  /**
   * Text field query clause for negated pattern matching.
   * Supports wildcard and pattern matching using NOT LIKE semantics.
   */
  TEXT_NOT_LIKE( //
      QueryClauseTemplate.of(FieldType.TEXT, Part.Type.NOT_LIKE, QueryClause.FIELD_NOT_LIKE, 1) //
  ),
  /**
   * Text field query clause for substring matching.
   * Matches text fields that contain the specified substring anywhere within the text.
   */
  TEXT_CONTAINING( //
      QueryClauseTemplate.of(FieldType.TEXT, Part.Type.CONTAINING, QueryClause.FIELD_LIKE, 1) //
  ),
  /**
   * Text field query clause for negated substring matching.
   * Matches text fields that do not contain the specified substring.
   */
  TEXT_NOT_CONTAINING( //
      QueryClauseTemplate.of(FieldType.TEXT, Part.Type.NOT_CONTAINING, QueryClause.FIELD_NOT_LIKE, 1) //
  ),
  /**
   * Text field query clause for negated membership testing.
   * Matches text fields that are not equal to any of the specified values.
   */
  TEXT_NOT_IN( //
      QueryClauseTemplate.of(FieldType.TEXT, Part.Type.NOT_IN, QueryClause.FIELD_EQUAL, 1) //
  ),
  /**
   * Text field query clause for membership testing.
   * Matches text fields whose values are in the specified collection of values.
   */
  TEXT_IN( //
      QueryClauseTemplate.of(FieldType.TEXT, Part.Type.IN, QueryClause.FIELD_EQUAL, 1) //
  ),
  /**
   * Text field query clause for lexicographic "greater than" comparisons.
   * Matches text fields whose values are lexicographically greater than the specified value.
   * Only applicable to fields marked with lexicographic=true.
   */
  TEXT_GREATER_THAN( //
      QueryClauseTemplate.of(FieldType.TEXT, Part.Type.GREATER_THAN, QueryClause.FIELD_LEXICOGRAPHIC, 1) //
  ),
  /**
   * Text field query clause for lexicographic "less than" comparisons.
   * Matches text fields whose values are lexicographically less than the specified value.
   * Only applicable to fields marked with lexicographic=true.
   */
  TEXT_LESS_THAN( //
      QueryClauseTemplate.of(FieldType.TEXT, Part.Type.LESS_THAN, QueryClause.FIELD_LEXICOGRAPHIC, 1) //
  ),
  /**
   * Text field query clause for lexicographic "greater than or equal" comparisons.
   * Matches text fields whose values are lexicographically greater than or equal to the specified value.
   * Only applicable to fields marked with lexicographic=true.
   */
  TEXT_GREATER_THAN_EQUAL( //
      QueryClauseTemplate.of(FieldType.TEXT, Part.Type.GREATER_THAN_EQUAL, QueryClause.FIELD_LEXICOGRAPHIC, 1) //
  ),
  /**
   * Text field query clause for lexicographic "less than or equal" comparisons.
   * Matches text fields whose values are lexicographically less than or equal to the specified value.
   * Only applicable to fields marked with lexicographic=true.
   */
  TEXT_LESS_THAN_EQUAL( //
      QueryClauseTemplate.of(FieldType.TEXT, Part.Type.LESS_THAN_EQUAL, QueryClause.FIELD_LEXICOGRAPHIC, 1) //
  ),
  /**
   * Text field query clause for lexicographic range matching.
   * Matches text fields whose values fall lexicographically between the specified minimum and maximum values
   * (inclusive).
   * Only applicable to fields marked with lexicographic=true.
   */
  TEXT_BETWEEN( //
      QueryClauseTemplate.of(FieldType.TEXT, Part.Type.BETWEEN, QueryClause.FIELD_LEXICOGRAPHIC, 2) //
  ),
  // NUMERIC
  /**
   * Numeric field query clause for exact value matching.
   * Matches numeric fields that are equal to the specified value.
   */
  NUMERIC_SIMPLE_PROPERTY( //
      QueryClauseTemplate.of(FieldType.NUMERIC, Part.Type.SIMPLE_PROPERTY, QueryClause.FIELD_NUMERIC_EQUAL_PARAM_0, 1)
  //
  ),
  /**
   * Numeric field query clause for inequality matching.
   * Matches numeric fields that are not equal to the specified value.
   */
  NUMERIC_NEGATING_SIMPLE_PROPERTY( //
      QueryClauseTemplate.of(FieldType.NUMERIC, Part.Type.SIMPLE_PROPERTY, QueryClause.FIELD_NUMERIC_NOT_EQUAL, 1) //
  ),
  /**
   * Numeric field query clause for range matching.
   * Matches numeric fields whose values fall between the specified minimum and maximum values (inclusive).
   */
  NUMERIC_BETWEEN( //
      QueryClauseTemplate.of(FieldType.NUMERIC, Part.Type.BETWEEN, QueryClause.FIELD_NUMERIC_BETWEEN, 2) //
  ),
  /**
   * Numeric field query clause for "less than" comparisons.
   * Matches numeric fields whose values are less than the specified value.
   */
  NUMERIC_LESS_THAN( //
      QueryClauseTemplate.of(FieldType.NUMERIC, Part.Type.LESS_THAN, QueryClause.FIELD_NUMERIC_LESS_THAN, 1) //
  ),
  /**
   * Numeric field query clause for "less than or equal" comparisons.
   * Matches numeric fields whose values are less than or equal to the specified value.
   */
  NUMERIC_LESS_THAN_EQUAL( //
      QueryClauseTemplate.of(FieldType.NUMERIC, Part.Type.LESS_THAN_EQUAL, QueryClause.FIELD_NUMERIC_LESS_THAN_EQUAL, 1)
  //
  ),
  /**
   * Numeric field query clause for "greater than" comparisons.
   * Matches numeric fields whose values are greater than the specified value.
   */
  NUMERIC_GREATER_THAN( //
      QueryClauseTemplate.of(FieldType.NUMERIC, Part.Type.GREATER_THAN, QueryClause.FIELD_NUMERIC_GREATER_THAN, 1) //
  ),
  /**
   * Numeric field query clause for "greater than or equal" comparisons.
   * Matches numeric fields whose values are greater than or equal to the specified value.
   */
  NUMERIC_GREATER_THAN_EQUAL( //
      QueryClauseTemplate.of(FieldType.NUMERIC, Part.Type.GREATER_THAN_EQUAL,
          QueryClause.FIELD_NUMERIC_GREATER_THAN_EQUAL, 1) //
  ),
  /**
   * Numeric field query clause for "before" temporal comparisons.
   * Matches values that are before (less than) the specified parameter.
   */
  NUMERIC_BEFORE( //
      QueryClauseTemplate.of(FieldType.NUMERIC, Part.Type.BEFORE, QueryClause.FIELD_NUMERIC_BEFORE, 1) //
  ),
  /**
   * Numeric field query clause for "after" temporal comparisons.
   * Matches values that are after (greater than) the specified parameter.
   */
  NUMERIC_AFTER( //
      QueryClauseTemplate.of(FieldType.NUMERIC, Part.Type.AFTER, QueryClause.FIELD_NUMERIC_AFTER, 1) //
  ),
  /**
   * Numeric field query clause for collection membership testing.
   * Matches numeric fields whose values are contained in the specified collection (OR logic).
   */
  NUMERIC_CONTAINING( //
      QueryClauseTemplate.of(FieldType.NUMERIC, Part.Type.CONTAINING, QueryClause.FIRST_PARAM, 1) //
  ),
  /**
   * Numeric field query clause for collection membership testing (all values).
   * Matches documents that contain all of the specified numeric values (AND logic).
   */
  NUMERIC_CONTAINING_ALL( //
      QueryClauseTemplate.of(FieldType.NUMERIC, Part.Type.CONTAINING, QueryClause.FIRST_PARAM, 1) //
  ),
  // GEO
  /**
   * Geo field query clause for proximity searches.
   * Matches geographic points that are within a specified distance from a given point.
   */
  GEO_NEAR( //
      QueryClauseTemplate.of(FieldType.GEO, Part.Type.NEAR, QueryClause.FIELD_GEO_NEAR, 2) //
  ),
  /**
   * Geo field query clause for containment checks.
   * Matches geographic fields that contain any of the specified points.
   */
  GEO_CONTAINING( //
      QueryClauseTemplate.of(FieldType.GEO, Part.Type.CONTAINING, QueryClause.FIRST_PARAM, 1) //
  ),
  /**
   * Geo field query clause for containment checks (all items).
   * Matches geographic fields that contain all of the specified points.
   */
  GEO_CONTAINING_ALL( //
      QueryClauseTemplate.of(FieldType.GEO, Part.Type.CONTAINING, QueryClause.FIRST_PARAM, 1) //
  ),
  // TAG
  /**
   * Tag field query clause for exact value matching.
   * Matches tag fields that are equal to the specified tag value.
   */
  TAG_SIMPLE_PROPERTY( //
      QueryClauseTemplate.of(FieldType.TAG, Part.Type.SIMPLE_PROPERTY, QueryClause.FIELD_TAG_EQUAL, 1) //
  ),
  /**
   * Tag field query clause for lexicographic "greater than" comparisons.
   * Matches tag fields whose values are lexicographically greater than the specified value.
   * Only applicable to fields marked with lexicographic=true.
   */
  TAG_GREATER_THAN( //
      QueryClauseTemplate.of(FieldType.TAG, Part.Type.GREATER_THAN, QueryClause.FIELD_LEXICOGRAPHIC, 1) //
  ),
  /**
   * Tag field query clause for lexicographic "less than" comparisons.
   * Matches tag fields whose values are lexicographically less than the specified value.
   * Only applicable to fields marked with lexicographic=true.
   */
  TAG_LESS_THAN( //
      QueryClauseTemplate.of(FieldType.TAG, Part.Type.LESS_THAN, QueryClause.FIELD_LEXICOGRAPHIC, 1) //
  ),
  /**
   * Tag field query clause for lexicographic "greater than or equal" comparisons.
   * Matches tag fields whose values are lexicographically greater than or equal to the specified value.
   * Only applicable to fields marked with lexicographic=true.
   */
  TAG_GREATER_THAN_EQUAL( //
      QueryClauseTemplate.of(FieldType.TAG, Part.Type.GREATER_THAN_EQUAL, QueryClause.FIELD_LEXICOGRAPHIC, 1) //
  ),
  /**
   * Tag field query clause for lexicographic "less than or equal" comparisons.
   * Matches tag fields whose values are lexicographically less than or equal to the specified value.
   * Only applicable to fields marked with lexicographic=true.
   */
  TAG_LESS_THAN_EQUAL( //
      QueryClauseTemplate.of(FieldType.TAG, Part.Type.LESS_THAN_EQUAL, QueryClause.FIELD_LEXICOGRAPHIC, 1) //
  ),
  /**
   * Tag field query clause for lexicographic range matching.
   * Matches tag fields whose values fall lexicographically between the specified minimum and maximum values
   * (inclusive).
   * Only applicable to fields marked with lexicographic=true.
   */
  TAG_BETWEEN( //
      QueryClauseTemplate.of(FieldType.TAG, Part.Type.BETWEEN, QueryClause.FIELD_LEXICOGRAPHIC, 2) //
  ),
  /**
   * Tag field query clause for exclusion testing.
   * Matches tag fields whose values are not in the specified collection of values.
   */
  TAG_NOT_IN( //
      QueryClauseTemplate.of(FieldType.TAG, Part.Type.NOT_IN, QueryClause.FIELD_TAG_EQUAL, 1) //
  ),
  /**
   * Tag field query clause for membership testing.
   * Matches tag fields whose values are in the specified collection of values.
   */
  TAG_IN( //
      QueryClauseTemplate.of(FieldType.TAG, Part.Type.IN, QueryClause.FIELD_TAG_EQUAL, 1) //
  ),
  /**
   * Tag field query clause for collection membership testing.
   * Matches tag fields that contain any of the specified tag values (OR logic).
   */
  TAG_CONTAINING( //
      QueryClauseTemplate.of(FieldType.TAG, Part.Type.CONTAINING, QueryClause.FIELD_EQUAL, 1) //
  ),
  /**
   * Tag field query clause for collection membership testing (all values).
   * Matches documents that contain all of the specified tag values (AND logic).
   */
  TAG_CONTAINING_ALL( //
      QueryClauseTemplate.of(FieldType.TAG, Part.Type.CONTAINING, QueryClause.FIRST_PARAM, 1) //
  ),
  /**
   * Tag field query clause for prefix matching.
   * Matches tag fields that start with the specified prefix string.
   */
  TAG_STARTING_WITH( //
      QueryClauseTemplate.of(FieldType.TAG, Part.Type.STARTING_WITH, QueryClause.FIELD_TAG_STARTING_WITH, 1) //
  ),
  /**
   * Tag field query clause for suffix matching.
   * Matches tag fields that end with the specified suffix string.
   */
  TAG_ENDING_WITH( //
      QueryClauseTemplate.of(FieldType.TAG, Part.Type.ENDING_WITH, QueryClause.FIELD_TAG_ENDING_WITH, 1) //
  ),
  // ALL FIELDS
  /**
   * Query clause for null value checks.
   * Matches fields that do not exist or have null values.
   */
  IS_NULL( //
      QueryClauseTemplate.of(FieldType.TAG, Part.Type.IS_NULL, QueryClause.FIELD_IS_NULL, 0) //
  ),
  /**
   * Query clause for non-null value checks.
   * Matches fields that exist and have non-null values.
   */
  IS_NOT_NULL( //
      QueryClauseTemplate.of(FieldType.TAG, Type.IS_NOT_NULL, QueryClause.FIELD_IS_NOT_NULL, 0) //
  );

  /**
   * Map for translating "ContainingAll" method names to their "Containing" equivalents.
   * Used for method name normalization in query parsing.
   */
  public static final Map<String, String> methodNameMap = Map.of("IsContainingAll", "IsContaining", "ContainingAll",
      "Containing", "ContainsAll", "Contains");
  /**
   * Pattern for matching method names that indicate "ContainingAll" logic.
   * Used to distinguish between OR logic (Containing) and AND logic (ContainingAll) operations.
   */
  public static final Pattern CONTAINING_ALL_PATTERN = Pattern.compile("(IsContainingAll|ContainingAll|ContainsAll)");
  private static final String PARAM_PREFIX = "$param_";
  private static final String FIRST_PARAM = "$param_0";
  private static final String FIELD_EQUAL = "@$field:$param_0";
  private static final String FIELD_NON_EQUAL_PARAM_0 = "-@$field:$param_0";
  private static final String FIELD_TAG_EQUAL = "@$field:{$param_0}";
  private static final String FIELD_LIKE = "@$field:%%%$param_0%%%";
  private static final String FIELD_NOT_LIKE = "-@$field:%%%$param_0%%%";
  private static final String FIELD_NUMERIC_EQUAL_PARAM_0 = "@$field:[$param_0 $param_0]";
  private static final String FIELD_TEXT_STARTING_WITH = "@$field:$param_0*";
  private static final String FIELD_TAG_STARTING_WITH = "@$field:{$param_0*}";
  private static final String FIELD_TEXT_ENDING_WITH = "@$field:*$param_0";
  private static final String FIELD_TAG_ENDING_WITH = "@$field:{*$param_0}";
  private static final String FIELD_NUMERIC_NOT_EQUAL = "@$field:-[$param_0 $param_0]";
  private static final String FIELD_NUMERIC_BETWEEN = "@$field:[$param_0 $param_1]";
  private static final String FIELD_NUMERIC_LESS_THAN = "@$field:[-inf ($param_0]";
  private static final String FIELD_NUMERIC_LESS_THAN_EQUAL = "@$field:[-inf $param_0]";
  private static final String FIELD_NUMERIC_GREATER_THAN = "@$field:[($param_0 inf]";
  private static final String FIELD_NUMERIC_GREATER_THAN_EQUAL = "@$field:[$param_0 inf]";
  private static final String FIELD_NUMERIC_BEFORE = "@$field:[-inf ($param_0]";
  private static final String FIELD_NUMERIC_AFTER = "@$field:[($param_0 inf]";
  private static final String FIELD_GEO_NEAR = "@$field:[$param_0 $param_1 $param_2]";
  private static final String FIELD_IS_NULL = "!exists(@$field)";
  private static final String FIELD_IS_NOT_NULL = "exists(@$field)";
  private static final String FIELD_LEXICOGRAPHIC = "__LEXICOGRAPHIC__";
  private final QueryClauseTemplate clauseTemplate;
  private final MappingRedisOMConverter converter = new MappingRedisOMConverter();

  QueryClause(QueryClauseTemplate value) {
    this.clauseTemplate = value;
  }

  /**
   * Retrieves the appropriate QueryClause for the given field type and part type combination.
   * <p>
   * This method constructs the enum name by concatenating the field type and part type
   * with an underscore, then attempts to find the corresponding QueryClause enum value.
   * </p>
   *
   * @param fieldType the Redis field type (TEXT, NUMERIC, GEO, TAG)
   * @param partType  the Spring Data query part type (SIMPLE_PROPERTY, LIKE, BETWEEN, etc.)
   * @return the matching QueryClause, or TAG_SIMPLE_PROPERTY as a fallback if no match is found
   */
  public static QueryClause get(FieldType fieldType, Part.Type partType) {
    try {
      return QueryClause.valueOf(fieldType.toString() + "_" + partType.name());
    } catch (IllegalArgumentException | NullPointerException e) {
      return TAG_SIMPLE_PROPERTY;
    }
  }

  /**
   * Checks if the given method name contains a "containing all" clause pattern.
   * <p>
   * This method searches for patterns like "IsContainingAll", "ContainingAll", or "ContainsAll"
   * in the method name to determine if it represents a query that should match all items
   * in a collection rather than any item.
   * </p>
   *
   * @param methodName the Spring Data repository method name to check
   * @return true if the method name contains a "containing all" pattern, false otherwise
   */
  public static boolean hasContainingAllClause(String methodName) {
    return CONTAINING_ALL_PATTERN.matcher(methodName).find();
  }

  /**
   * Post-processes a method name by replacing "containing all" patterns with their simpler equivalents.
   * <p>
   * This method transforms method names containing "IsContainingAll", "ContainingAll", or "ContainsAll"
   * patterns into their simpler "containing" equivalents. This is used during query processing to
   * normalize method names while preserving the semantic meaning that the query should match all items.
   * </p>
   *
   * @param methodName the original Spring Data repository method name
   * @return the processed method name with "containing all" patterns replaced, or the original name if no patterns are
   *         found
   */
  public static String getPostProcessMethodName(String methodName) {
    if (hasContainingAllClause(methodName)) {
      Optional<String> maybeMatchSubstring = CONTAINING_ALL_PATTERN.matcher(methodName).results().map(mr -> mr.group(1))
          .findFirst();
      if (maybeMatchSubstring.isPresent()) {
        String matchSubstring = maybeMatchSubstring.get();
        return methodName.replace(matchSubstring, methodNameMap.get(matchSubstring));
      } else {
        return methodName;
      }

    }
    return methodName;
  }

  /**
   * Returns the query clause template associated with this QueryClause.
   * <p>
   * The template contains the field type, part type, query segment template,
   * and parameter count information needed to construct the actual Redis search query.
   * </p>
   *
   * @return the QueryClauseTemplate for this clause
   */
  public QueryClauseTemplate getClauseTemplate() {
    return clauseTemplate;
  }

  /**
   * Prepares a Redis search query string by substituting field names and parameters into the clause template.
   * <p>
   * This method takes the template associated with this QueryClause and replaces placeholders
   * with actual field names and parameter values. It handles various parameter types including:
   * <ul>
   * <li>Geographic points and distances for geo queries</li>
   * <li>Collections for "in" and "containing" operations</li>
   * <li>Scalar values for equality and range operations</li>
   * </ul>
   * Special handling is provided for different field types (TEXT, NUMERIC, TAG, GEO) and
   * different query operations.
   *
   * @param field  the field name to search on, or "__ALL__" for full-text search across all fields
   * @param params the parameter values to substitute into the query template
   * @return the prepared Redis search query string with all placeholders replaced
   */
  public String prepareQuery(String field, Object... params) {
    String prepared = field.equalsIgnoreCase("__ALL__") ?
        clauseTemplate.getQuerySegmentTemplate() :
        clauseTemplate.getQuerySegmentTemplate().replace("$field", field);

    Iterator<Object> iter = Arrays.asList(params).iterator();

    int i = 0;
    while (iter.hasNext()) {
      Object param = iter.next();
      String paramClass = param.getClass().getName();
      switch (paramClass) {
        case "org.springframework.data.geo.Point":
          Point point = (Point) param;
          prepared = prepared.replace(PARAM_PREFIX + i++, Double.toString(point.getX()));
          prepared = prepared.replace(PARAM_PREFIX + i++, Double.toString(point.getY()));
          break;
        case "org.springframework.data.geo.Distance":
          Distance distance = (Distance) param;
          prepared = prepared.replace(PARAM_PREFIX + i++, ObjectUtils.getDistanceAsRedisString(distance));
          break;
        default:
          // unfold collections
          if (param instanceof Collection<?> c) {
            String value;
            if (this == QueryClause.TAG_CONTAINING_ALL) {
              value = c.stream().map(n -> "@" + field + ":{" + QueryUtils.escape(ObjectUtils.asString(n,
                  converter)) + "}").collect(Collectors.joining(" "));
            } else if (this == QueryClause.NUMERIC_CONTAINING) {
              value = c.stream().map(n -> "@" + field + ":[" + QueryUtils.escape(ObjectUtils.asString(n,
                  converter)) + " " + QueryUtils.escape(ObjectUtils.asString(n, converter)) + "]").collect(Collectors
                      .joining("|"));
            } else if (this == QueryClause.NUMERIC_CONTAINING_ALL) {
              value = c.stream().map(n -> "@" + field + ":[" + QueryUtils.escape(ObjectUtils.asString(n,
                  converter)) + " " + QueryUtils.escape(ObjectUtils.asString(n, converter)) + "]").collect(Collectors
                      .joining(" "));
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
              value = c.stream()//
                  .map(n -> QueryUtils.escape(ObjectUtils.asString(n, converter), false)).collect(Collectors.joining(
                      "|"));
            }

            prepared = prepared.replace(PARAM_PREFIX + i++, value);
          } else {
            if (clauseTemplate.getIndexType() == FieldType.TEXT) {
              prepared = prepared.replace(PARAM_PREFIX + i++, param.toString());
            } else if (clauseTemplate.getIndexType() == FieldType.NUMERIC && !paramClass.equalsIgnoreCase(
                "java.time.LocalDateTime") && !paramClass.equalsIgnoreCase("java.time.LocalDate")) {
              prepared = prepared.replace(PARAM_PREFIX + i++, param.toString());
            } else {
              prepared = prepared.replace(PARAM_PREFIX + i++, QueryUtils.escape(ObjectUtils.asString(param,
                  converter)));
            }
          }
          break;
      }
    }

    return prepared;
  }
}