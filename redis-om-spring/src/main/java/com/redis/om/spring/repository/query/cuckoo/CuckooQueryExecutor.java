package com.redis.om.spring.repository.query.cuckoo;

import com.redis.om.spring.annotations.Cuckoo;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.pds.CuckooFilterOperations;
import com.redis.om.spring.util.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

public class CuckooQueryExecutor {

  public static final String EXISTS_BY_PREFIX = "existsBy";
  private static final Log logger = LogFactory.getLog(CuckooQueryExecutor.class);
  final RepositoryQuery query;
  final RedisModulesOperations<String> modulesOperations;

  public CuckooQueryExecutor(RepositoryQuery query, RedisModulesOperations<String> modulesOperations) {
    this.query = query;
    this.modulesOperations = modulesOperations;
  }

  public Optional<String> getCuckooFilter() {
    String methodName = query.getQueryMethod().getName();
    boolean hasExistByPrefix = methodName.startsWith(EXISTS_BY_PREFIX);
    if (hasExistByPrefix && boolean.class.isAssignableFrom(query.getQueryMethod().getReturnedObjectType())) {
      String targetProperty = ObjectUtils.firstToLowercase(methodName.substring(EXISTS_BY_PREFIX.length()));
      logger.debug(String.format("Target Property : %s", targetProperty));
      Class<?> entityClass = query.getQueryMethod().getEntityInformation().getJavaType();

      try {
        Field field = ReflectionUtils.findField(entityClass, targetProperty);
        if (field == null) {
          return Optional.empty();
        }
        if (field.isAnnotationPresent(Cuckoo.class)) {
          Cuckoo cuckoo = field.getAnnotation(Cuckoo.class);
          return Optional.of(!org.apache.commons.lang3.ObjectUtils.isEmpty(cuckoo.name()) ?
              cuckoo.name() :
              String.format("cf:%s:%s", entityClass.getSimpleName(), field.getName()));
        }
      } catch (SecurityException e) {
        // NO-OP
      }
    }
    return Optional.empty();
  }

  public Object executeCuckooQuery(Object[] parameters, String cuckooFilter) {
    logger.debug(String.format("filter:%s, params:%s", cuckooFilter, Arrays.toString(parameters)));
    CuckooFilterOperations<String> ops = modulesOperations.opsForCuckoFilter();
    return ops.exists(cuckooFilter, parameters[0].toString());
  }
}
