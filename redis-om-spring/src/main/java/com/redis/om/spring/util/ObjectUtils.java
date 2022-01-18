package com.redis.om.spring.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.geo.Distance;

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

}
