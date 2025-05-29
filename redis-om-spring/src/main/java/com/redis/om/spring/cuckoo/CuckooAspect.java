package com.redis.om.spring.cuckoo;

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

import com.redis.om.spring.annotations.Cuckoo;
import com.redis.om.spring.ops.pds.CuckooFilterOperations;

/**
 * Aspect for automatically maintaining Cuckoo Filters on entity fields annotated with {@link Cuckoo}.
 * <p>
 * This aspect intercepts save operations on repositories and automatically adds field values
 * to their corresponding Cuckoo filters. Cuckoo filters are space-efficient probabilistic
 * data structures that support approximate membership testing with the additional capability
 * of item deletion, unlike Bloom filters.
 * </p>
 * <p>
 * The aspect operates on two levels:
 * <ul>
 * <li>Single entity saves - {@link #addToCuckoo(JoinPoint, Object)}</li>
 * <li>Batch entity saves - {@link #addAllToCuckoo(JoinPoint, List)}</li>
 * </ul>
 * <p>
 * For each field annotated with {@link Cuckoo}, the aspect:
 * <ol>
 * <li>Determines the Cuckoo filter name (explicit or generated)</li>
 * <li>Extracts the field value using reflection</li>
 * <li>Adds the value to the corresponding Cuckoo filter</li>
 * </ol>
 * <p>
 * Cuckoo filters provide several advantages over Bloom filters:
 * <ul>
 * <li>Support for item deletion</li>
 * <li>Better space efficiency at higher capacities</li>
 * <li>Bounded false positive rates</li>
 * </ul>
 *
 * @see Cuckoo
 * @see CuckooFilterOperations
 * @since 0.1.0
 */
@Aspect
@Component
public class CuckooAspect implements Ordered {
  private static final Log logger = LogFactory.getLog(CuckooAspect.class);
  private final CuckooFilterOperations<String> ops;

  /**
   * Creates a new CuckooAspect with the specified Cuckoo filter operations.
   *
   * @param ops the Cuckoo filter operations for managing filters
   */
  public CuckooAspect(CuckooFilterOperations<String> ops) {
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
   * Advice that executes after successful save operations to add field values to Cuckoo filters.
   * <p>
   * This method processes all fields in the saved entity that are annotated with {@link Cuckoo}
   * and adds their values to the corresponding Cuckoo filters. Filter names are either
   * explicitly specified in the annotation or automatically generated.
   * </p>
   *
   * @param jp     the join point representing the save operation
   * @param entity the entity that was saved
   */
  @AfterReturning(
    "inSaveOperation() && args(entity,..)"
  )
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
   * Advice that executes after successful saveAll operations to add field values to Cuckoo filters.
   * <p>
   * This method processes all fields in each saved entity that are annotated with {@link Cuckoo}
   * and adds their values to the corresponding Cuckoo filters. This provides efficient batch
   * processing for multiple entity saves.
   * </p>
   *
   * @param jp       the join point representing the saveAll operation
   * @param entities the list of entities that were saved
   */
  @AfterReturning(
    "inSaveAllOperation() && args(entities,..)"
  )
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
