package com.redis.om.spring.repository.query;

import java.util.Set;

public class QueryUtils {
  public static Set<Character> tagEscapeChars = Set.of( //
      ',', '.', '<', '>', '{', '}', '[', //
      ']', '"', '\'', ':', ';', '!', '@', //
      '#', '$', '%', '^', '&', '*', '(', //
      ')', '-', '+', '=', '~', '|', ' ' //
  );

  public static String escapeTagField(String text) {
    var sb = new StringBuilder();
    char[] chars = text.toCharArray();

    for (char c : chars) {
      if (tagEscapeChars.contains(c)) {
        sb.append("\\");
      }
      sb.append(c);
    }

    return sb.toString();
  }
  
  @SuppressWarnings("unchecked")
  public static <T> T escapeTagField(T maybeText) {
    return CharSequence.class.isAssignableFrom(maybeText.getClass()) ? (T) escapeTagField(maybeText.toString()) : maybeText;
  }
}
