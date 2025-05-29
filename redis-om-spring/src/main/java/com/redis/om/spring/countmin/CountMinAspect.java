
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

/**
 * Aspect for automatically maintaining Count-Min Sketches on entity fields annotated with {@link CountMin}.
 * <p>
 * This aspect intercepts save and delete operations on repositories and automatically maintains
 * Count-Min Sketches that track frequency counts of field values. Count-Min Sketches are
 * probabilistic data structures that provide approximate frequency counting with sub-linear
 * space requirements.
 * </p>
 * <p>
 * The aspect operates on three levels:
 * <ul>
 * <li>Single entity saves - {@link #addToCountMin(JoinPoint, Object)}</li>
 * <li>Batch entity saves - {@link #addAllToCountMin(JoinPoint, List)}</li>
 * <li>Repository deleteAll operations - {@link #deleteCMSOnDeleteAll(JoinPoint)}</li>
 * </ul>
 * <p>
 * For each field annotated with {@link CountMin}, the aspect:
 * <ol>
 * <li>Initializes the Count-Min Sketch if it doesn't exist</li>
 * <li>Extracts the field value using reflection</li>
 * <li>Increments the count for that value in the sketch</li>
 * <li>Handles different value types (single values, pairs, collections)</li>
 * </ol>
 * <p>
 * The aspect supports flexible value types:
 * <ul>
 * <li>Single values - incremented by 1</li>
 * <li>Pair&lt;String, Number&gt; - incremented by the number value</li>
 * <li>Collections of values or pairs - each item processed individually</li>
 * </ul>
 *
 * @see CountMin
 * @see CountMinSketchOperations
 * @since 0.1.0
 */
@Aspect
@Component
public class CountMinAspect implements Ordered {
  private static final Log logger = LogFactory.getLog(CountMinAspect.class);
  private final CountMinSketchOperations<String> ops;
  private final StringRedisTemplate stringRedisTemplate;

  /**
   * Creates a new CountMinAspect with the specified operations.
   *
   * @param ops                 the Count-Min Sketch operations for managing sketches
   * @param stringRedisTemplate the Redis template for direct sketch deletion
   */
  public CountMinAspect(CountMinSketchOperations<String> ops, StringRedisTemplate stringRedisTemplate) {
    this.ops = ops;
    this.stringRedisTemplate = stringRedisTemplate;
  }

  /**
   * Pointcut matching save operations on Spring Data CrudRepository implementations.
   */
  @Pointcut(
    "execution(public * org.springframework.data.repository.CrudRepository+.save(..))"
  )
  public void inCrudRepositorySave() {
  }

  /**
   * Pointcut matching save operations on RedisDocumentRepository implementations.
   */
  @Pointcut(
    "execution(public * com.redis.om.spring.repository.RedisDocumentRepository+.save(..))"
  )
  public void inRedisDocumentRepositorySave() {
  }

  /**
   * Composite pointcut matching all save operations.
   */
  @Pointcut(
    "inCrudRepositorySave() || inRedisDocumentRepositorySave()"
  )
  private void inSaveOperation() {
  }

  /**
   * Advice that executes after successful save operations to update Count-Min Sketches.
   * <p>
   * This method processes all fields in the saved entity that are annotated with {@link CountMin}
   * and increments their counts in the corresponding sketches. The method handles various value types:
   * <ul>
   * <li>Single values: incremented by 1</li>
   * <li>Pair&lt;String, Number&gt;: incremented by the number value</li>
   * <li>Collections: each item processed individually</li>
   * </ul>
   *
   * @param jp     the join point representing the save operation
   * @param entity the entity that was saved
   */
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

  /**
   * Pointcut matching saveAll operations on Spring Data CrudRepository implementations.
   */
  @Pointcut(
    "execution(public * org.springframework.data.repository.CrudRepository+.saveAll(..))"
  )
  public void inCrudRepositorySaveAll() {
  }

  /**
   * Pointcut matching saveAll operations on RedisDocumentRepository implementations.
   */
  @Pointcut(
    "execution(public * com.redis.om.spring.repository.RedisDocumentRepository+.saveAll(..))"
  )
  public void inRedisDocumentRepositorySaveAll() {
  }

  /**
   * Composite pointcut matching all saveAll operations.
   */
  @Pointcut(
    "inCrudRepositorySaveAll() || inRedisDocumentRepositorySaveAll()"
  )
  private void inSaveAllOperation() {
  }

  /**
   * Pointcut matching deleteAll() operations on Spring Data CrudRepository implementations.
   */
  @Pointcut(
    "execution(public * org.springframework.data.repository.CrudRepository+.deleteAll())"
  )
  public void inCrudRepositoryDeleteAllNoArgs() {
  }

  /**
   * Pointcut matching deleteAll() operations on RedisDocumentRepository implementations.
   */
  @Pointcut(
    "execution(public * com.redis.om.spring.repository.RedisDocumentRepository+.deleteAll())"
  )
  public void inRedisDocumentRepositoryDeleteAllNoArgs() {
  }

  /**
   * Composite pointcut matching all deleteAll() operations without arguments.
   */
  @Pointcut(
    "inCrudRepositoryDeleteAllNoArgs() || inRedisDocumentRepositoryDeleteAllNoArgs()"
  )
  private void inDeleteAllNoArgsOperation() {
  }

  /**
   * Advice that executes after successful saveAll operations to update Count-Min Sketches.
   * <p>
   * This method processes all fields in each saved entity that are annotated with {@link CountMin}
   * and increments their counts in the corresponding sketches. This provides efficient batch
   * processing for multiple entity saves.
   * </p>
   *
   * @param jp       the join point representing the saveAll operation
   * @param entities the list of entities that were saved
   */
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

  /**
   * Initializes a Count-Min Sketch if it doesn't already exist.
   * <p>
   * This method checks if the specified sketch exists by querying its information.
   * If the sketch doesn't exist, it creates a new one using either dimension-based
   * or probability-based initialization depending on the CountMin annotation configuration.
   * </p>
   *
   * @param sketchName the name of the sketch to initialize
   * @param countMin   the CountMin annotation containing initialization parameters
   */
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

  /**
   * Advice that executes after successful deleteAll operations to clean up Count-Min Sketches.
   * <p>
   * When all entities of a type are deleted, this method also deletes the corresponding
   * Count-Min Sketches to maintain consistency. It infers the entity class from the
   * repository's generic type parameters.
   * </p>
   *
   * @param jp the join point representing the deleteAll operation
   */
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

  /**
   * Checks if the given class is or extends the KeyValueRepository interface.
   * <p>
   * This method recursively checks the class hierarchy to determine if the given
   * class implements or extends KeyValueRepository, which is used to identify
   * repository types that should be managed by this aspect.
   * </p>
   *
   * @param clazz the class to check
   * @return true if the class is a KeyValueRepository interface or implementation
   */
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

  /**
   * Resolves the entity class from a repository class by examining its generic type parameters.
   * <p>
   * This method inspects the repository's generic interface declarations to extract
   * the entity type that the repository manages. This is essential for determining
   * which Count-Min Sketches to clean up during deleteAll operations.
   * </p>
   *
   * @param repoClass the repository class to analyze
   * @return the entity class managed by the repository, or null if it cannot be determined
   */
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

  /**
   * Returns the order in which this aspect should be applied.
   * <p>
   * This aspect has an order of 1, ensuring it runs early in the aspect chain
   * but after core Spring Data operations have completed successfully.
   * </p>
   *
   * @return the aspect order (1)
   */
  @Override
  public int getOrder() {
    return 1;
  }
}