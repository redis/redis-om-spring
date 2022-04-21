package com.redis.om.spring.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.core.ResolvableType;
import org.springframework.data.geo.Distance;
import org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit;

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

}
