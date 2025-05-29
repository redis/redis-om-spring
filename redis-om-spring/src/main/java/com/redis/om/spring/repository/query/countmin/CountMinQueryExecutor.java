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

/**
 * Query executor for Count-Min Sketch based repository queries.
 * <p>
 * This executor handles repository methods that follow the "countBy" naming convention
 * and are targeting fields annotated with {@link CountMin}. It provides approximate
 * frequency counting capabilities using Redis Count-Min Sketches, enabling efficient
 * counting operations without scanning the entire dataset.
 * </p>
 * <p>
 * Supported query patterns:
 * </p>
 * <ul>
 * <li>{@code countByFieldName(value)} - returns the frequency count of a single value</li>
 * <li>{@code countByFieldName(Collection<values>)} - returns frequency counts for multiple values</li>
 * </ul>
 * <p>
 * Example repository methods:
 * <pre>{@code
 * public interface EventRepository extends RedisDocumentRepository<Event, String> {
 *     // Count occurrences of a specific event type
 *     long countByEventType(String eventType);
 *     
 *     // Count occurrences of multiple event types
 *     List<Long> countByEventType(List<String> eventTypes);
 * }
 * }</pre>
 * <p>
 * The executor automatically detects if a method targets a Count-Min annotated field
 * and routes the query to the appropriate sketch operations instead of performing
 * a full entity scan.
 * </p>
 *
 * @see CountMin
 * @see CountMinSketchOperations
 * @see com.redis.om.spring.countmin.CountMinAspect
 * @since 0.1.0
 */
public class CountMinQueryExecutor {

  /** Prefix for count-based repository query methods. */
  public static final String COUNT_BY_PREFIX = "countBy";
  private static final Log logger = LogFactory.getLog(CountMinQueryExecutor.class);
  final RepositoryQuery query;
  final RedisModulesOperations<String> modulesOperations;

  /**
   * Creates a new CountMinQueryExecutor for the specified query and operations.
   *
   * @param query             the repository query to execute
   * @param modulesOperations the Redis modules operations for accessing Count-Min Sketches
   */
  public CountMinQueryExecutor(RepositoryQuery query, RedisModulesOperations<String> modulesOperations) {
    this.query = query;
    this.modulesOperations = modulesOperations;
  }

  /**
   * Determines the Count-Min Sketch name for the query method.
   * <p>
   * This method analyzes the repository query method to determine if it targets
   * a field annotated with {@link CountMin}. If the method follows the "countBy"
   * naming convention and returns a numeric type, it extracts the target field name
   * and returns the corresponding sketch name.
   * </p>
   *
   * @return Optional containing the sketch name if applicable, empty otherwise
   */
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

  /**
   * Executes a Count-Min Sketch query with the specified parameters.
   * <p>
   * This method handles both single value queries and batch queries:
   * <ul>
   * <li>Single value: returns a single count as Long</li>
   * <li>Iterable of values: returns a list of counts</li>
   * </ul>
   *
   * @param parameters     the query parameters (values to count)
   * @param countMinSketch the name of the Count-Min Sketch to query
   * @return the frequency count(s) for the specified value(s)
   */
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
