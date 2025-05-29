package com.redis.om.spring.mapping;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.core.TimeToLiveAccessor;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration;
import org.springframework.data.redis.core.convert.MappingConfiguration;
import org.springframework.data.redis.core.index.IndexConfiguration;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.redis.core.mapping.RedisPersistentEntity;
import org.springframework.data.util.TypeInformation;

/**
 * Enhanced Redis mapping context that extends Spring Data Redis's {@link RedisMappingContext}
 * to provide advanced mapping capabilities for Redis OM Spring.
 * 
 * <p>This mapping context creates {@link RedisEnhancedPersistentEntity} instances that support:
 * <ul>
 * <li>Composite keys via JPA's {@code @IdClass} annotation</li>
 * <li>Enhanced search indexing for Redis Stack features</li>
 * <li>Backward compatibility with standard Spring Data Redis entities</li>
 * </ul>
 * 
 * <p>The context automatically detects entities that use {@code @IdClass} for composite primary keys
 * and provides appropriate mapping support while maintaining full compatibility with single-field
 * primary keys used in standard Redis entities.
 * 
 * <p>This implementation integrates with Redis OM Spring's indexing and search capabilities,
 * allowing entities to take advantage of RediSearch, RedisJSON, and other Redis Stack features.
 * 
 * @see RedisEnhancedPersistentEntity
 * @see org.springframework.data.redis.core.mapping.RedisMappingContext
 * @see jakarta.persistence.IdClass
 * @since 1.0
 */
public class RedisEnhancedMappingContext extends RedisMappingContext {

  private static final Log logger = LogFactory.getLog(RedisEnhancedMappingContext.class);
  private final MappingConfiguration mappingConfiguration;
  private final TimeToLiveAccessor timeToLiveAccessor;

  /**
   * Creates a new {@code RedisEnhancedMappingContext} with the specified mapping configuration.
   * 
   * <p>This constructor initializes the mapping context with custom index and keyspace configurations,
   * enabling support for Redis Stack features like search indexing and JSON document storage.
   * 
   * @param mappingConfiguration the mapping configuration containing index and keyspace settings;
   *                             must not be {@literal null}
   */
  public RedisEnhancedMappingContext(MappingConfiguration mappingConfiguration) {
    super(mappingConfiguration);
    this.mappingConfiguration = mappingConfiguration;
    this.timeToLiveAccessor = new RedisEnhancedTimeToLiveAccessor(mappingConfiguration.getKeyspaceConfiguration(),
        this);
  }

  /**
   * Creates a new {@code RedisEnhancedMappingContext} with default configuration.
   * 
   * <p>This constructor creates a mapping context with empty index and keyspace configurations,
   * suitable for basic Redis operations while maintaining the enhanced entity support for
   * composite keys and advanced mapping features.
   */
  public RedisEnhancedMappingContext() {
    // Create mapping configuration with empty index and keyspace configs
    this(new MappingConfiguration(new IndexConfiguration(), new KeyspaceConfiguration()));
  }

  @Override
  protected <T> RedisPersistentEntity<T> createPersistentEntity(TypeInformation<T> typeInformation) {
    return new RedisEnhancedPersistentEntity<>(typeInformation, getKeySpaceResolver(), timeToLiveAccessor);
  }
}