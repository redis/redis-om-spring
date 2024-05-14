package com.redis.om.spring.cuckoo;

import com.redis.om.spring.annotations.Cuckoo;
import com.redis.om.spring.ops.pds.CuckooFilterOperations;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Aspect
@Component
public class CuckooAspect implements Ordered {
  private static final Log logger = LogFactory.getLog(CuckooAspect.class);
  private final CuckooFilterOperations<String> ops;

  public CuckooAspect(CuckooFilterOperations<String> ops) {
    this.ops = ops;
  }

  @Pointcut("execution(public * org.springframework.data.repository.CrudRepository+.save(..))")
  public void inCrudRepositorySave() {
  }

  @Pointcut("execution(public * com.redis.om.spring.repository.RedisDocumentRepository+.save(..))")
  public void inRedisDocumentRepositorySave() {
  }

  @Pointcut("inCrudRepositorySave() || inRedisDocumentRepositorySave()")
  private void inSaveOperation() {
  }

  @AfterReturning("inSaveOperation() && args(entity,..)")
  public void addToCuckoo(JoinPoint jp, Object entity) {
    for (Field field : com.redis.om.spring.util.ObjectUtils.getDeclaredFieldsTransitively(entity.getClass())) {
      if (field.isAnnotationPresent(Cuckoo.class)) {
        Cuckoo cuckoo = field.getAnnotation(Cuckoo.class);
        String filterName = !ObjectUtils.isEmpty(cuckoo.name()) ?
          cuckoo.name() :
          String.format("cf:%s:%s", entity.getClass().getSimpleName(), field.getName());
        try {
          PropertyDescriptor pd = new PropertyDescriptor(field.getName(), entity.getClass());
          ops.add(filterName, pd.getReadMethod().invoke(entity).toString());
        } catch (IllegalArgumentException | IllegalAccessException | IntrospectionException |
                 InvocationTargetException e) {
          logger.error(String.format("Could not add value to Cuckoo filter %s", filterName), e);
        }
      }
    }
  }

  @Pointcut("execution(public * org.springframework.data.repository.CrudRepository+.saveAll(..))")
  public void inCrudRepositorySaveAll() {
  }

  @Pointcut("execution(public * com.redis.om.spring.repository.RedisDocumentRepository+.saveAll(..))")
  public void inRedisDocumentRepositorySaveAll() {
  }

  @Pointcut("inCrudRepositorySaveAll() || inRedisDocumentRepositorySaveAll()")
  private void inSaveAllOperation() {
  }

  @AfterReturning("inSaveAllOperation() && args(entities,..)")
  public void addAllToCuckoo(JoinPoint jp, List<Object> entities) {
    for (Object entity : entities) {
      for (Field field : com.redis.om.spring.util.ObjectUtils.getDeclaredFieldsTransitively(entity.getClass())) {
        if (field.isAnnotationPresent(Cuckoo.class)) {
          Cuckoo cuckoo = field.getAnnotation(Cuckoo.class);
          String filterName = !ObjectUtils.isEmpty(cuckoo.name()) ?
            cuckoo.name() :
            String.format("cf:%s:%s", entity.getClass().getSimpleName(), field.getName());
          try {
            PropertyDescriptor pd = new PropertyDescriptor(field.getName(), entity.getClass());
            ops.add(filterName, pd.getReadMethod().invoke(entity).toString());
          } catch (IllegalArgumentException | IllegalAccessException | IntrospectionException |
                   InvocationTargetException e) {
            logger.error(String.format("Could not add values to Bloom filter %s", filterName), e);
          }
        }
      }
    }
  }

  @Override
  public int getOrder() {
    return 1;
  }
}
