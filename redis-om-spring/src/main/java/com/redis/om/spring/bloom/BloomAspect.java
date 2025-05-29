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

/**
 * Aspect for automatically maintaining Bloom filters on entity fields annotated with {@link Bloom}.
 * <p>
 * This aspect intercepts save operations on repositories and automatically adds field values
 * to their corresponding Bloom filters. It provides transparent Bloom filter maintenance
 * without requiring explicit calls from application code.
 * </p>
 * <p>
 * The aspect operates on two levels:
 * <ul>
 * <li>Single entity saves - {@link #addToBloom(JoinPoint, Object)}</li>
 * <li>Batch entity saves - {@link #addAllToBloom(JoinPoint, List)}</li>
 * </ul>
 * <p>
 * For each field annotated with {@link Bloom}, the aspect:
 * <ol>
 * <li>Determines the Bloom filter name (explicit or generated)</li>
 * <li>Extracts the field value using reflection</li>
 * <li>Adds the value to the corresponding Bloom filter</li>
 * </ol>
 * <p>
 * The aspect is automatically configured when Redis OM Spring detects Bloom-annotated
 * fields and is ordered to execute after the primary save operation completes.
 * </p>
 *
 * @see Bloom
 * @see BloomOperations
 * @since 0.1.0
 */
@Aspect
@Component
public class BloomAspect implements Ordered {
  private static final Log logger = LogFactory.getLog(BloomAspect.class);
  private final BloomOperations<String> ops;

  /**
   * Creates a new BloomAspect with the specified Bloom operations.
   *
   * @param ops the Bloom operations for managing filters
   */
  public BloomAspect(BloomOperations<String> ops) {
    this.ops = ops;
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
   * Advice that executes after successful save operations to add field values to Bloom filters.
   * <p>
   * This method processes all fields in the saved entity that are annotated with {@link Bloom}
   * and adds their values to the corresponding Bloom filters. Filter names are either
   * explicitly specified in the annotation or automatically generated.
   * </p>
   *
   * @param jp     the join point representing the save operation
   * @param entity the entity that was saved
   */
  @AfterReturning(
    "inSaveOperation() && args(entity,..)"
  )
  public void addToBloom(JoinPoint jp, Object entity) {
    for (Field field : com.redis.om.spring.util.ObjectUtils.getDeclaredFieldsTransitively(entity.getClass())) {
      if (field.isAnnotationPresent(Bloom.class)) {
        Bloom bloom = field.getAnnotation(Bloom.class);
        String filterName = !ObjectUtils.isEmpty(bloom.name()) ?
            bloom.name() :
            String.format("bf:%s:%s", entity.getClass().getSimpleName(), field.getName());
        try {
          PropertyDescriptor pd = new PropertyDescriptor(field.getName(), entity.getClass());
          ops.add(filterName, pd.getReadMethod().invoke(entity).toString());
        } catch (IllegalArgumentException | IllegalAccessException | IntrospectionException |
                 InvocationTargetException e) {
          logger.error(String.format("Could not add value to Bloom filter %s", filterName), e);
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
   * Advice that executes after successful saveAll operations to add field values to Bloom filters.
   * <p>
   * This method processes all fields in each saved entity that are annotated with {@link Bloom}
   * and adds their values to the corresponding Bloom filters. This provides efficient batch
   * processing for multiple entity saves.
   * </p>
   *
   * @param jp       the join point representing the saveAll operation
   * @param entities the list of entities that were saved
   */
  @AfterReturning(
    "inSaveAllOperation() && args(entities,..)"
  )
  public void addAllToBloom(JoinPoint jp, List<Object> entities) {
    for (Object entity : entities) {
      for (Field field : com.redis.om.spring.util.ObjectUtils.getDeclaredFieldsTransitively(entity.getClass())) {
        if (field.isAnnotationPresent(Bloom.class)) {
          Bloom bloom = field.getAnnotation(Bloom.class);
          String filterName = !ObjectUtils.isEmpty(bloom.name()) ?
              bloom.name() :
              String.format("bf:%s:%s", entity.getClass().getSimpleName(), field.getName());
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
