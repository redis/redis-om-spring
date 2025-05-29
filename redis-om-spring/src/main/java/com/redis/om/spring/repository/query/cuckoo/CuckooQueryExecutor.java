package com.redis.om.spring.repository.query.cuckoo;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.util.ReflectionUtils;

import com.redis.om.spring.annotations.Cuckoo;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.pds.CuckooFilterOperations;
import com.redis.om.spring.util.ObjectUtils;

/**
 * Query executor for Cuckoo Filter based repository queries.
 * <p>
 * This executor handles repository methods that follow the "existsBy" naming convention
 * and are targeting fields annotated with {@link Cuckoo}. It provides approximate
 * membership testing capabilities using Redis Cuckoo Filters, enabling efficient
 * existence checks without scanning the entire dataset.
 * </p>
 * <p>
 * Cuckoo filters are probabilistic data structures that support:
 * <ul>
 * <li>Fast membership queries with bounded false positive rates</li>
 * <li>Item deletion (unlike Bloom filters)</li>
 * <li>Space-efficient storage at higher capacities</li>
 * <li>No false negatives</li>
 * </ul>
 * <p>
 * Supported query patterns:
 * </p>
 * <ul>
 * <li>{@code existsByFieldName(value)} - returns true if the value might exist</li>
 * </ul>
 * <p>
 * Example repository methods:
 * <pre>{@code
 * public interface SessionRepository extends RedisDocumentRepository<Session, String> {
 *     // Check if a session token exists (with possible false positives)
 *     boolean existsBySessionToken(String token);
 * }
 * }</pre>
 * <p>
 * The executor automatically detects if a method targets a Cuckoo-annotated field
 * and routes the query to the appropriate filter operations instead of performing
 * a full entity scan.
 * </p>
 *
 * @see Cuckoo
 * @see CuckooFilterOperations
 * @see com.redis.om.spring.cuckoo.CuckooAspect
 * @since 0.1.0
 */
public class CuckooQueryExecutor {

  /** Prefix for existence-based repository query methods. */
  public static final String EXISTS_BY_PREFIX = "existsBy";
  private static final Log logger = LogFactory.getLog(CuckooQueryExecutor.class);
  final RepositoryQuery query;
  final RedisModulesOperations<String> modulesOperations;

  /**
   * Creates a new CuckooQueryExecutor for the specified query and operations.
   *
   * @param query             the repository query to execute
   * @param modulesOperations the Redis modules operations for accessing Cuckoo filters
   */
  public CuckooQueryExecutor(RepositoryQuery query, RedisModulesOperations<String> modulesOperations) {
    this.query = query;
    this.modulesOperations = modulesOperations;
  }

  /**
   * Determines the Cuckoo filter name for the query method.
   * <p>
   * This method analyzes the repository query method to determine if it targets
   * a field annotated with {@link Cuckoo}. If the method follows the "existsBy"
   * naming convention and returns a boolean type, it extracts the target field name
   * and returns the corresponding filter name.
   * </p>
   *
   * @return Optional containing the filter name if applicable, empty otherwise
   */
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

  /**
   * Executes a Cuckoo filter membership query with the specified parameters.
   * <p>
   * This method performs an existence check using the Cuckoo filter, which may
   * return false positives but never false negatives. A true result means the
   * value might exist, while a false result means it definitely does not exist.
   * </p>
   *
   * @param parameters   the query parameters (value to check for existence)
   * @param cuckooFilter the name of the Cuckoo filter to query
   * @return true if the value might exist, false if it definitely does not exist
   */
  public Object executeCuckooQuery(Object[] parameters, String cuckooFilter) {
    logger.debug(String.format("filter:%s, params:%s", cuckooFilter, Arrays.toString(parameters)));
    CuckooFilterOperations<String> ops = modulesOperations.opsForCuckoFilter();
    return ops.exists(cuckooFilter, parameters[0].toString());
  }
}
