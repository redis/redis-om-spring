package com.redis.om.spring.util;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import com.redis.om.spring.annotations.EnableRedisEnhancedRepositories;
import com.redis.om.spring.convert.MappingRedisOMConverter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Distance;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit;
import org.springframework.data.redis.core.convert.Bucket;
import org.springframework.data.redis.core.convert.RedisData;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ReflectionUtils;
import redis.clients.jedis.args.GeoUnit;
import redis.clients.jedis.search.Document;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.springframework.util.ClassUtils.resolvePrimitiveIfNecessary;

public class ObjectUtils {

  // Refactored getDistanceUnit using polymorphism
  public static GeoUnit getDistanceUnit(Distance distance) {
    DistanceUnitConverter converter = getConverter(DistanceUnit.valueOf (distance.getUnit()));
    return converter.getGeoUnit();
  }

  // Helper method to get the appropriate converter
  private static DistanceUnitConverter getConverter(DistanceUnit unit) {
    Map<DistanceUnit, DistanceUnitConverter> converters = new HashMap<>();
    converters.put(DistanceUnit.MILES, new MilesConverter());
    converters.put(DistanceUnit.FEET, new FeetConverter());
    converters.put(DistanceUnit.KILOMETERS, new KilometersConverter());
    converters.put(DistanceUnit.METERS, new MetersConverter());

    return converters.get(unit);
  }
  public static List<Field> getDeclaredFieldsTransitively(Class<?> clazz) {
    List<Field> fields = new ArrayList<>();
    while (clazz != null) {
      fields.addAll(Arrays.stream(clazz.getDeclaredFields()).toList());
      clazz = clazz.getSuperclass();
    }
    return fields;
  }


  public static String getTargetClassName(String fullTypeClassName) {
    String[] splitted = fullTypeClassName.split(" ");
    String cls = splitted[splitted.length - 1];
    if (cls.contains("<")) {
      cls = cls.substring(0, cls.indexOf("<"));
    }
    return cls;
  }

  public static String getCollectionTargetClassName(String fullTypeClassName) {
    String[] splitted = fullTypeClassName.split(" ");
    String cls = splitted[splitted.length - 1];
    if (cls.contains("<")) {
      cls = cls.substring(cls.indexOf("<") + 1, cls.indexOf(">"));
    }
    return cls;
  }

  public static String firstToLowercase(String string) {
    char[] c = string.toCharArray();
    c[0] = Character.toLowerCase(c[0]);
    return new String(c);
  }



  // Interface for DistanceUnit converters
  private interface DistanceUnitConverter {
    GeoUnit getGeoUnit();
  }

  // Concrete converter for miles
  private static class MilesConverter implements DistanceUnitConverter {
    @Override
    public GeoUnit getGeoUnit() {
      return GeoUnit.MI;
    }
  }

  // Concrete converter for feet
  private static class FeetConverter implements DistanceUnitConverter {
    @Override
    public GeoUnit getGeoUnit() {
      return GeoUnit.FT;
    }
  }

  // Concrete converter for kilometers
  private static class KilometersConverter implements DistanceUnitConverter {
    @Override
    public GeoUnit getGeoUnit() {
      return GeoUnit.KM;
    }
  }

  // Concrete converter for meters
  private static class MetersConverter implements DistanceUnitConverter {
    @Override
    public GeoUnit getGeoUnit() {
      return GeoUnit.M;
    }
  }
}
