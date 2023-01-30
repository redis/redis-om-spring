package com.redis.om.spring.bloom;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import com.redis.om.spring.annotations.Bloom;
import com.redis.om.spring.ops.pds.BloomOperations;

@Aspect
@Component
public class BloomAspect implements Ordered {
  private final BloomOperations<String> ops;
  private static final Log logger = LogFactory.getLog(BloomAspect.class);

  public BloomAspect(BloomOperations<String> ops) {
    this.ops = ops;
  }

  @Pointcut("execution(public * org.springframework.data.repository.CrudRepository+.save(..))")
  public void inCrudRepositorySave() {}

  @Pointcut("execution(public * com.redis.om.spring.repository.RedisDocumentRepository+.save(..))")
  public void inRedisDocumentRepositorySave() {}

  @Pointcut("inCrudRepositorySave() || inRedisDocumentRepositorySave()")
  private void inSaveOperation() {}

  @AfterReturning("inSaveOperation() && args(entity,..)")
  public void addToBloom(JoinPoint jp, Object entity) {
    for (Field field : entity.getClass().getDeclaredFields()) {
      if (field.isAnnotationPresent(Bloom.class)) {
        Bloom bloom = field.getAnnotation(Bloom.class);
        String filterName = !ObjectUtils.isEmpty(bloom.name()) ? bloom.name() : String.format("bf:%s:%s", entity.getClass().getSimpleName(), field.getName());
        try {
          PropertyDescriptor pd = new PropertyDescriptor(field.getName(), entity.getClass());
          ops.add(filterName, pd.getReadMethod().invoke(entity).toString());
        } catch (IllegalArgumentException | IllegalAccessException | IntrospectionException | InvocationTargetException e) {
          logger.error(String.format("Could not add value to Bloom filter %s", filterName), e);
        }
      }
    }
  }

  @Pointcut("execution(public * org.springframework.data.repository.CrudRepository+.saveAll(..))")
  public void inCrudRepositorySaveAll() {}

  @Pointcut("execution(public * com.redis.om.spring.repository.RedisDocumentRepository+.saveAll(..))")
  public void inRedisDocumentRepositorySaveAll() {}

  @Pointcut("inCrudRepositorySaveAll() || inRedisDocumentRepositorySaveAll()")
  private void inSaveAllOperation() {}

  @AfterReturning("inSaveAllOperation() && args(entities,..)")
  public void addAllToBloom(JoinPoint jp, List<Object> entities) {
    for (Object entity : entities) {
      for (Field field : entity.getClass().getDeclaredFields()) {
        if (field.isAnnotationPresent(Bloom.class)) {
          Bloom bloom = field.getAnnotation(Bloom.class);
          String filterName = !ObjectUtils.isEmpty(bloom.name()) ? bloom.name() : String.format("bf:%s:%s", entity.getClass().getSimpleName(), field.getName());
          try {
            PropertyDescriptor pd = new PropertyDescriptor(field.getName(), entity.getClass());
            ops.add(filterName, pd.getReadMethod().invoke(entity).toString());
          } catch (IllegalArgumentException | IllegalAccessException | IntrospectionException | InvocationTargetException e) {
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
