package com.redis.om.spring.repository.query;

import java.lang.reflect.Field;
import java.util.Set;

public class QueryUtils {
  public static final Set<Character> TAG_ESCAPE_CHARS = Set.of( //
      ',', '.', '<', '>', '{', '}', '[', //
      ']', '"', '\'', ':', ';', '!', '@', //
      '#', '$', '%', '^', '&', '*', '(', //
      ')', '-', '+', '=', '~', '|', ' ' //
  );
  
  public static String escape(String text) {
    return escape(text, false, false);
  }
  public static String escape(String text, boolean querying) {
    return escape(text, querying, false);
  }

  public static String escape(String text, boolean querying, boolean isCsv) {
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
  
  public static String unescape(String text) {
    return text.replace("\\", "");
  }
  
  @SuppressWarnings("unchecked")
  public static <T> T escape(T maybeText) {
    return CharSequence.class.isAssignableFrom(maybeText.getClass()) ? (T) escape(maybeText.toString()) : maybeText;
  }
  
  public static String searchIndexFieldAliasFor(Field field, String prefix) {
    String alias = field.getName();
    if (prefix != null && !prefix.isBlank()) {
      alias = prefix.replace(".", "_") + "_" + alias;
    } 
    return alias;
  }
  
  public static String searchIndexFieldAliasFor(Field field) {
    return searchIndexFieldAliasFor(field, null);
  }
  
  private QueryUtils() {}
}
