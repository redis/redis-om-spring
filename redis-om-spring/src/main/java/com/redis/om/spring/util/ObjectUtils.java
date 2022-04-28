package com.redis.om.spring.util;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.core.ResolvableType;
import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Distance;
import org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit;
import org.springframework.util.ReflectionUtils;

import io.redisearch.querybuilder.GeoValue;
import io.redisearch.querybuilder.GeoValue.Unit;

public class ObjectUtils {
  public static String getDistanceAsRedisString(Distance distance) {
    return String.format("%s %s", Double.toString(distance.getValue()), distance.getUnit());
  }

  public static List<Field> getFieldsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
    return Arrays //
        .stream(clazz.getDeclaredFields()) //
        .filter(f -> f.isAnnotationPresent(annotationClass)) //
        .collect(Collectors.toList());
  }
  
  public static Class<?> getNumericClassFor(String str) {
    try {
      Double.parseDouble(str);
      return Double.class;
    } catch (NumberFormatException nfe) {}
    return Integer.class;
  }
  
  public static Unit getDistanceUnit(Distance distance) {
    if (distance.getUnit().equals(DistanceUnit.MILES.getAbbreviation())) {
      return GeoValue.Unit.MILES;
    } else if (distance.getUnit().equals(DistanceUnit.FEET.getAbbreviation())) {
      return GeoValue.Unit.FEET;
    } else if (distance.getUnit().equals(DistanceUnit.KILOMETERS.getAbbreviation())) {
      return GeoValue.Unit.KILOMETERS;
    } else {
      return GeoValue.Unit.METERS;
    }
  }
  
  public static String getTargetClassName(String fullTypeClassName) {
    String[] splitted = fullTypeClassName.split(" ");
    String cls = splitted[splitted.length-1];
    if (cls.contains("<")) {
      cls = cls.substring(0, cls.indexOf("<"));
    }
    return cls;
  }
  
  public static String firstToLowercase(String string) {
    char c[] = string.toCharArray();
    c[0] = Character.toLowerCase(c[0]);
    return new String(c);
  }
  
  public static Optional<Class<?>> getCollectionElementType(Field field) {
    if (Collection.class.isAssignableFrom(field.getType())) {
      ResolvableType collectionType = ResolvableType.forField(field);
      Class<?> elementType = collectionType.getGeneric(0).getRawClass();
      return Optional.of(elementType);
    }

    return Optional.empty();
  }
  
  public static Optional<Field> getIdFieldForEntityClass(Class<?> cl) {
    return Arrays.stream(cl.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Id.class)).findFirst();
  }
  
  public static Optional<?> getIdFieldForEntity(Object entity) {
    Optional<Field> maybeIdField = getIdFieldForEntityClass(entity.getClass());
    if (maybeIdField.isPresent()) { 
      Field idField = maybeIdField.get();
      
      String getterName = "get" + ObjectUtils.ucfirst(idField.getName());
      Method getter = ReflectionUtils.findMethod(entity.getClass(), getterName);
      Object id = ReflectionUtils.invokeMethod(getter, entity);
      
      return Optional.of(id);
    } else {
      return Optional.empty();
    }
  }
  
  public static Method getGetterForField(Class<?> cls, Field field) {
    String getterName = "get" + ucfirst(field.getName());
    Method getter = ReflectionUtils.findMethod(cls, getterName);
    return getter;
  }
  
  public static Method getSetterForField(Class<?> cls, Field field) {
    String setterName = "set" + ucfirst(field.getName());
    Method setter = ReflectionUtils.findMethod(cls, setterName, field.getType());
    return setter;
  }

  /**
   * Returns the specified text but with the first character uppercase.
   *
   * @param input The text.
   * @return The resulting text.
   */
  public static String ucfirst(String input) {
    return withFirst(input, first -> String.valueOf(Character.toUpperCase(first)));
  }

  /**
   * Does something with the first character in the specified String.
   *
   * @param input    The String.
   * @param callback The something.
   * @return The new String.
   */
  public static String withFirst(String input, Function<Character, String> callback) {
    if (input == null) {
      return null;
    } else if (input.length() == 0) {
      return "";
    } else {
      return String.join("", callback.apply(input.charAt(0)), input.subSequence(1, input.length()));
    }
  }

  public static boolean isFirstLowerCase(String string) {
    String first = string.substring(0, 1);
    return first.toLowerCase().equals(first);
  }

  /**
   * Returns the specified text but with the first character lowercase.
   *
   * @param input The text.
   * @return The resulting text.
   */
  public static String lcfirst(String input) {
    return withFirst(input, first -> String.valueOf(Character.toLowerCase(first)));
  }

  /**
   * Returns the string but with any leading and trailing quotation marks trimmed.
   *
   * @param s the string to unquote
   * @return the string without surrounding quotation marks
   */
  public static String unQuote(final String s) {
    requireNonNull(s);
    if (s.startsWith("\"") && s.endsWith("\"")) {
      // Un-quote the name
      return s.substring(1, s.length() - 1);
    }
    return s;
  }

  /**
   * Turns the specified string into an underscore-separated string.
   *
   * @param javaName the string to parse
   * @return as underscore separated
   */
  public static String toUnderscoreSeparated(final String javaName) {
    requireNonNull(javaName);
    final StringBuilder result = new StringBuilder();
    final String input = unQuote(javaName.trim());
    for (int i = 0; i < input.length(); i++) {
      final char c = input.charAt(i);
      if (result.length() == 0) {
        result.append(Character.toLowerCase(c));
      } else if (Character.isUpperCase(c)) {
        result.append("_").append(Character.toLowerCase(c));
      } else {
        result.append(c);
      }
    }
    return result.toString();
  }

  /**
   * Returns the 'name' part of a long name. This is everything after the last dot
   * for non-parameterized types. For parameterized types the rule applies to the
   * part proceeding the bracket enclosed parameters e.g.
   * {@code long name java.util.Map<String, java.util.Date>} returns
   * {@code Map<String, java.util.Date>}.
   *
   * @param longName The long name.
   * @return The name part.
   */
  public static String shortName(String longName) {
    String temp = longName.replace('$', '.');
    final int openBrPos = temp.indexOf('<');
    String parameters = "";
    if (openBrPos > 0) {
      parameters = temp.substring(openBrPos);
      temp = temp.substring(0, openBrPos);
    }
    if (temp.contains(".")) {
      temp = temp.substring(temp.lastIndexOf('.') + 1);
    }
    return temp + parameters;
  }

}
