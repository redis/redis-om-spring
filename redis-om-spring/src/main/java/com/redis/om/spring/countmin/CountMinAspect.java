package com.redis.om.spring.countmin;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.Ordered;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.redis.om.spring.annotations.CountMin;
import com.redis.om.spring.ops.pds.CountMinSketchOperations;
import com.redis.om.spring.tuple.Pair;

@Aspect
@Component
public class CountMinAspect implements Ordered {
  private static final Log logger = LogFactory.getLog(CountMinAspect.class);
  private final CountMinSketchOperations<String> ops;
  private final StringRedisTemplate stringRedisTemplate;

  public CountMinAspect(CountMinSketchOperations<String> ops, StringRedisTemplate stringRedisTemplate) {
    this.ops = ops;
    this.stringRedisTemplate = stringRedisTemplate;
  }

  @Pointcut(
    "execution(public * org.springframework.data.repository.CrudRepository+.save(..))"
  )
  public void inCrudRepositorySave() {
  }

  @Pointcut(
    "execution(public * com.redis.om.spring.repository.RedisDocumentRepository+.save(..))"
  )
  public void inRedisDocumentRepositorySave() {
  }

  @Pointcut(
    "inCrudRepositorySave() || inRedisDocumentRepositorySave()"
  )
  private void inSaveOperation() {
  }

  @AfterReturning(
    "inSaveOperation() && args(entity,..)"
  )
  public void addToCountMin(JoinPoint jp, Object entity) {
    for (Field field : com.redis.om.spring.util.ObjectUtils.getDeclaredFieldsTransitively(entity.getClass())) {
      if (field.isAnnotationPresent(CountMin.class)) {
        CountMin countMin = field.getAnnotation(CountMin.class);
        String sketchName = !ObjectUtils.isEmpty(countMin.name()) ?
            countMin.name() :
            String.format("cms:%s:%s", entity.getClass().getSimpleName(), field.getName());

        try {
          PropertyDescriptor pd = new PropertyDescriptor(field.getName(), entity.getClass());
          Object fieldValue = pd.getReadMethod().invoke(entity);

          if (fieldValue != null) {
            // Initialize the Count-min Sketch if it doesn't exist
            initializeCountMinSketch(sketchName, countMin);

            // Increment the count for the field value
            if (fieldValue instanceof Pair<?, ?> pair && pair.getFirst() instanceof String && pair
                .getSecond() instanceof Number) {
              ops.cmsIncrBy(sketchName, (String) pair.getFirst(), ((Number) pair.getSecond()).longValue());
            } else if (fieldValue instanceof Iterable<?> iterable) {
              for (Object item : iterable) {
                if (item instanceof Pair<?, ?> p && p.getFirst() instanceof String && p.getSecond() instanceof Number) {
                  ops.cmsIncrBy(sketchName, (String) p.getFirst(), ((Number) p.getSecond()).longValue());
                } else {
                  ops.cmsIncrBy(sketchName, item.toString(), 1);
                }
              }
            } else {
              ops.cmsIncrBy(sketchName, fieldValue.toString(), 1);
            }
          }
        } catch (IllegalArgumentException | IllegalAccessException | IntrospectionException |
                 InvocationTargetException e) {
          logger.error(String.format("Could not add value to Count-min Sketch %s", sketchName), e);
        }
      }
    }
  }

  @Pointcut(
    "execution(public * org.springframework.data.repository.CrudRepository+.saveAll(..))"
  )
  public void inCrudRepositorySaveAll() {
  }

  @Pointcut(
    "execution(public * com.redis.om.spring.repository.RedisDocumentRepository+.saveAll(..))"
  )
  public void inRedisDocumentRepositorySaveAll() {
  }

  @Pointcut(
    "inCrudRepositorySaveAll() || inRedisDocumentRepositorySaveAll()"
  )
  private void inSaveAllOperation() {
  }

  @Pointcut(
    "execution(public * org.springframework.data.repository.CrudRepository+.deleteAll())"
  )
  public void inCrudRepositoryDeleteAllNoArgs() {
  }

  @Pointcut(
    "execution(public * com.redis.om.spring.repository.RedisDocumentRepository+.deleteAll())"
  )
  public void inRedisDocumentRepositoryDeleteAllNoArgs() {
  }

  @Pointcut(
    "inCrudRepositoryDeleteAllNoArgs() || inRedisDocumentRepositoryDeleteAllNoArgs()"
  )
  private void inDeleteAllNoArgsOperation() {
  }

  @AfterReturning(
    "inSaveAllOperation() && args(entities,..)"
  )
  public void addAllToCountMin(JoinPoint jp, List<Object> entities) {
    for (Object entity : entities) {
      for (Field field : com.redis.om.spring.util.ObjectUtils.getDeclaredFieldsTransitively(entity.getClass())) {
        if (field.isAnnotationPresent(CountMin.class)) {
          CountMin countMin = field.getAnnotation(CountMin.class);
          String sketchName = !ObjectUtils.isEmpty(countMin.name()) ?
              countMin.name() :
              String.format("cms:%s:%s", entity.getClass().getSimpleName(), field.getName());

          try {
            PropertyDescriptor pd = new PropertyDescriptor(field.getName(), entity.getClass());
            Object fieldValue = pd.getReadMethod().invoke(entity);

            if (fieldValue != null) {
              // Initialize the Count-min Sketch if it doesn't exist
              initializeCountMinSketch(sketchName, countMin);

              // Increment the count for the field value
              if (fieldValue instanceof Pair<?, ?> pair && pair.getFirst() instanceof String && pair
                  .getSecond() instanceof Number) {
                ops.cmsIncrBy(sketchName, (String) pair.getFirst(), ((Number) pair.getSecond()).longValue());
              } else if (fieldValue instanceof Iterable<?> iterable) {
                for (Object item : iterable) {
                  if (item instanceof Pair<?, ?> p && p.getFirst() instanceof String && p
                      .getSecond() instanceof Number) {
                    ops.cmsIncrBy(sketchName, (String) p.getFirst(), ((Number) p.getSecond()).longValue());
                  } else {
                    ops.cmsIncrBy(sketchName, item.toString(), 1);
                  }
                }
              } else {
                ops.cmsIncrBy(sketchName, fieldValue.toString(), 1);
              }
            }
          } catch (IllegalArgumentException | IllegalAccessException | IntrospectionException |
                   InvocationTargetException e) {
            logger.error(String.format("Could not add value to Count-min Sketch %s", sketchName), e);
          }
        }
      }
    }
  }

  private void initializeCountMinSketch(String sketchName, CountMin countMin) {
    try {
      // Check if the sketch already exists by querying its info
      ops.cmsInfo(sketchName);
    } catch (Exception e) {
      // Sketch doesn't exist, initialize it based on the specified mode
      if (countMin.initMode() == CountMin.InitMode.DIMENSIONS) {
        if (countMin.width() > 0 && countMin.depth() > 0) {
          ops.cmsInitByDim(sketchName, countMin.width(), countMin.depth());
        } else {
          logger.error(String.format("Invalid dimensions for Count-min Sketch %s: width=%d, depth=%d", sketchName,
              countMin.width(), countMin.depth()));
        }
      } else {
        // Initialize by probability
        ops.cmsInitByProb(sketchName, countMin.errorRate(), countMin.probability());
      }
    }
  }

  @AfterReturning(
    "inDeleteAllNoArgsOperation()"
  )
  public void deleteCMSOnDeleteAll(JoinPoint jp) {
    // Try to infer entity class from repository generics
    Object target = jp.getTarget();

    Class<?> entityClass = resolveEntityClassFromRepository(target.getClass());
    if (entityClass == null) {
      logger.warn("Could not determine entity class for repository: " + target.getClass());
      return;
    }

    for (Field field : com.redis.om.spring.util.ObjectUtils.getDeclaredFieldsTransitively(entityClass)) {
      if (field.isAnnotationPresent(CountMin.class)) {
        CountMin countMin = field.getAnnotation(CountMin.class);
        String sketchName = !ObjectUtils.isEmpty(countMin.name()) ?
            countMin.name() :
            String.format("cms:%s:%s", entityClass.getSimpleName(), field.getName());

        try {
          stringRedisTemplate.delete(sketchName);
        } catch (Exception e) {
          logger.warn(String.format("Failed to delete Count-Min Sketch %s", sketchName), e);
        }
      }
    }
  }

  private boolean isKeyValueRepositoryInterface(Class<?> clazz) {
    if (clazz == null)
      return false;
    if (clazz == KeyValueRepository.class)
      return true;
    for (Class<?> iface : clazz.getInterfaces()) {
      if (isKeyValueRepositoryInterface(iface))
        return true;
    }
    return false;
  }

  private Class<?> resolveEntityClassFromRepository(Class<?> repoClass) {
    for (Type genericInterface : repoClass.getGenericInterfaces()) {
      if (genericInterface instanceof ParameterizedType pType) {
        Type raw = pType.getRawType();
        if (raw instanceof Class<?> rawClass && isKeyValueRepositoryInterface(rawClass)) {
          Type[] typeArgs = pType.getActualTypeArguments();
          if (typeArgs.length > 0 && typeArgs[0] instanceof Class<?> entityClass) {
            return entityClass;
          }
        }
      } else if (genericInterface instanceof Class<?> rawInterface) {
        // Look recursively
        Class<?> found = resolveEntityClassFromRepository(rawInterface);
        if (found != null)
          return found;
      }
    }

    Class<?> superClass = repoClass.getSuperclass();
    if (superClass != null && superClass != Object.class) {
      return resolveEntityClassFromRepository(superClass);
    }

    return null;
  }

  @Override
  public int getOrder() {
    return 1;
  }
}