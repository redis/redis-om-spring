package com.redis.om.spring.repository.query.bloom;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.util.ReflectionUtils;

import com.redis.om.spring.annotations.Bloom;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.pds.BloomOperations;
import com.redis.om.spring.util.ObjectUtils;

/**
 * Query executor for Redis Bloom filter operations in repository methods.
 * This class analyzes repository query methods to determine if they are Bloom filter
 * existence checks and executes them against Redis Bloom filters.
 * 
 * <p>BloomQueryExecutor identifies methods that start with "existsBy"
 * and validates that the target field is annotated with {@link Bloom}.
 * It then executes the existence check using Bloom filter operations for
 * efficient membership testing.</p>
 * 
 * <p>Example usage in repository:</p>
 * <pre>{@code
 * public interface UserRepository extends RedisDocumentRepository<User, String> {
 *     boolean existsByEmail(String email);
 *     boolean existsByUsername(String username);
 * }
 * }</pre>
 * 
 * @since 1.0
 * @see Bloom
 * @see BloomOperations
 */
public class BloomQueryExecutor {

  /** The prefix used to identify Bloom filter existence methods */
  public static final String EXISTS_BY_PREFIX = "existsBy";

  private static final Log logger = LogFactory.getLog(BloomQueryExecutor.class);

  /** The repository query being executed */
  final RepositoryQuery query;

  /** Redis modules operations for executing Bloom filter commands */
  final RedisModulesOperations<String> modulesOperations;

  /**
   * Constructs a new BloomQueryExecutor for the specified query.
   * 
   * @param query             the repository query to execute
   * @param modulesOperations the Redis modules operations for executing commands
   */
  public BloomQueryExecutor(RepositoryQuery query, RedisModulesOperations<String> modulesOperations) {
    this.query = query;
    this.modulesOperations = modulesOperations;
  }

  /**
   * Determines the Bloom filter key for the query method.
   * This method analyzes the method name to extract the target property
   * and validates that it has the {@link Bloom} annotation.
   * 
   * @return an Optional containing the filter key if this is a valid Bloom existence method,
   *         or empty if the method is not a Bloom operation
   */
  public Optional<String> getBloomFilter() {
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
        if (field.isAnnotationPresent(Bloom.class)) {
          Bloom bloom = field.getAnnotation(Bloom.class);
          return Optional.of(!org.apache.commons.lang3.ObjectUtils.isEmpty(bloom.name()) ?
              bloom.name() :
              String.format("bf:%s:%s", entityClass.getSimpleName(), field.getName()));
        }
      } catch (SecurityException e) {
        // NO-OP
      }
    }
    return Optional.empty();
  }

  /**
   * Executes the Bloom filter existence query with the given parameters.
   * This method checks if the specified value exists in the Bloom filter.
   * 
   * @param parameters  the query parameters (the value to check for existence)
   * @param bloomFilter the Redis key for the Bloom filter
   * @return true if the value might exist (allowing for false positives),
   *         false if the value definitely does not exist
   */
  public Object executeBloomQuery(Object[] parameters, String bloomFilter) {
    logger.debug(String.format("filter:%s, params:%s", bloomFilter, Arrays.toString(parameters)));
    BloomOperations<String> ops = modulesOperations.opsForBloom();
    return ops.exists(bloomFilter, parameters[0].toString());
  }
}
