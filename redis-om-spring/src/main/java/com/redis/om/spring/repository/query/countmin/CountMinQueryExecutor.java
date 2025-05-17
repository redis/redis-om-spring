package com.redis.om.spring.repository.query.countmin;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.util.ReflectionUtils;

import com.redis.om.spring.annotations.CountMin;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.pds.CountMinSketchOperations;
import com.redis.om.spring.util.ObjectUtils;

public class CountMinQueryExecutor {

  public static final String COUNT_BY_PREFIX = "countBy";
  private static final Log logger = LogFactory.getLog(CountMinQueryExecutor.class);
  final RepositoryQuery query;
  final RedisModulesOperations<String> modulesOperations;

  public CountMinQueryExecutor(RepositoryQuery query, RedisModulesOperations<String> modulesOperations) {
    this.query = query;
    this.modulesOperations = modulesOperations;
  }

  public Optional<String> getCountMinSketch() {
    String methodName = query.getQueryMethod().getName();
    boolean hasCountByPrefix = methodName.startsWith(COUNT_BY_PREFIX);

    Class<?> returnType = query.getQueryMethod().getReturnedObjectType();
    boolean isLong = long.class.isAssignableFrom(returnType) || Long.class.isAssignableFrom(returnType);

    if (hasCountByPrefix && isLong) {
      String targetProperty = ObjectUtils.firstToLowercase(methodName.substring(COUNT_BY_PREFIX.length()));
      logger.debug(String.format("Target Property : %s", targetProperty));
      Class<?> entityClass = query.getQueryMethod().getEntityInformation().getJavaType();

      try {
        Field field = ReflectionUtils.findField(entityClass, targetProperty);
        if (field == null) {
          return Optional.empty();
        }
        if (field.isAnnotationPresent(CountMin.class)) {
          CountMin countMin = field.getAnnotation(CountMin.class);
          return Optional.of(!org.apache.commons.lang3.ObjectUtils.isEmpty(countMin.name()) ?
              countMin.name() :
              String.format("cms:%s:%s", entityClass.getSimpleName(), field.getName()));
        }
      } catch (SecurityException e) {
        // NO-OP
      }
    }
    return Optional.empty();
  }

  public Object executeCountMinQuery(Object[] parameters, String countMinSketch) {
    logger.debug(String.format("filter:%s, params:%s", countMinSketch, Arrays.toString(parameters)));
    CountMinSketchOperations<String> ops = modulesOperations.opsForCountMinSketch();
    if (parameters[0] instanceof Iterable<?> iterable) {
      String[] array = StreamSupport.stream(iterable.spliterator(), false).map(Object::toString).toArray(String[]::new);
      return ops.cmsQuery(countMinSketch, array);
    }
    return ops.cmsQuery(countMinSketch, parameters[0].toString()).get(0);
  }
}
