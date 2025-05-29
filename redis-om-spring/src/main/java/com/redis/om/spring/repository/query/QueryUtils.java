package com.redis.om.spring.repository.query;

import java.lang.reflect.Field;
import java.util.Set;

/**
 * Utility class for handling Redis search query operations and text escaping.
 * <p>
 * This class provides static methods for escaping and unescaping text in Redis search queries,
 * handling special characters that need to be escaped in TAG fields, and generating field aliases
 * for search indexes. It is essential for proper query construction and avoiding syntax errors
 * in Redis search operations.
 * </p>
 * <p>
 * The escape functionality is particularly important when dealing with user input or dynamic
 * values that may contain special characters reserved by Redis search syntax.
 * </p>
 *
 * @see com.redis.om.spring.repository.query.clause.QueryClause
 * @see com.redis.om.spring.repository.query.clause.QueryClauseTemplate
 * @since 0.1.0
 */
public class QueryUtils {
  /**
   * Set of characters that need to be escaped in Redis TAG field queries.
   * These characters have special meaning in Redis search syntax and must be
   * escaped with backslashes when they appear in search values.
   */
  public static final Set<Character> TAG_ESCAPE_CHARS = Set.of( //
      ',', '.', '<', '>', '{', '}', '[', //
      ']', '"', '\'', ':', ';', '!', '@', //
      '#', '$', '%', '^', '&', '*', '(', //
      ')', '-', '+', '=', '~', '|', '/' //
  );

  private QueryUtils() {
  }

  /**
   * Escapes special characters in text for Redis search queries.
   * This is a convenience method that calls {@link #escape(String, boolean)} with querying=false.
   *
   * @param text the text to escape
   * @return the escaped text, or null if input was null
   */
  public static String escape(String text) {
    return escape(text, false);
  }

  /**
   * Escapes special characters in text for Redis search queries.
   * <p>
   * Escapes characters that have special meaning in Redis search syntax by prefixing
   * them with backslashes. When querying is true, also escapes spaces which are
   * treated specially in query contexts.
   * </p>
   *
   * @param text     the text to escape
   * @param querying whether this text will be used in a query context (affects space escaping)
   * @return the escaped text with special characters prefixed with backslashes, or null if input was null
   */
  public static String escape(String text, boolean querying) {
    if (text == null) {
      return null;
    }
    var sb = new StringBuilder();
    char[] chars = text.toCharArray();

    for (char c : chars) {
      if (TAG_ESCAPE_CHARS.contains(c)) {
        sb.append("\\");
      }
      if (querying && c == ' ') {
        sb.append("\\");
      }
      sb.append(c);
    }

    return sb.toString();
  }

  /**
   * Removes escape characters from text that was previously escaped.
   * This simply removes all backslash characters from the input string.
   *
   * @param text the escaped text to unescape
   * @return the unescaped text with backslashes removed
   */
  public static String unescape(String text) {
    return text.replace("\\", "");
  }

  /**
   * Generic escape method that applies escaping only to CharSequence types.
   * <p>
   * If the input object is a CharSequence (String, StringBuilder, etc.), it will be
   * escaped using {@link #escape(String)}. For other types, the object is returned unchanged.
   * </p>
   *
   * @param <T>       the type of the input object
   * @param maybeText the object that may need escaping
   * @return the escaped text if input was a CharSequence, otherwise the original object
   */
  @SuppressWarnings(
    "unchecked"
  )
  public static <T> T escape(T maybeText) {
    return CharSequence.class.isAssignableFrom(maybeText.getClass()) ? (T) escape(maybeText.toString()) : maybeText;
  }

  /**
   * Generates a search index field alias for a given field and prefix.
   * <p>
   * Creates an alias name by combining a prefix with the field name. The prefix is
   * sanitized by replacing dots with underscores to ensure valid Redis field names.
   * If no prefix is provided, returns just the field name.
   * </p>
   *
   * @param field  the field to generate an alias for
   * @param prefix the prefix to prepend to the field name (can be null or blank)
   * @return the generated field alias with prefix and field name separated by underscore
   */
  public static String searchIndexFieldAliasFor(Field field, String prefix) {
    String alias = field.getName();
    if (prefix != null && !prefix.isBlank()) {
      alias = prefix.replace(".", "_") + "_" + alias;
    }
    return alias;
  }
}
