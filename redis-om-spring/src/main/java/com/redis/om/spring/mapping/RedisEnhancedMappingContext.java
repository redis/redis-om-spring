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
 * Custom mapping context that creates RedisEnhancedPersistentEntity instances
 * to support composite keys via @IdClass while maintaining backwards compatibility
 * for single ID cases.
 */
public class RedisEnhancedMappingContext extends RedisMappingContext {

  private static final Log logger = LogFactory.getLog(RedisEnhancedMappingContext.class);
  private final MappingConfiguration mappingConfiguration;
  private final TimeToLiveAccessor timeToLiveAccessor;

  public RedisEnhancedMappingContext(MappingConfiguration mappingConfiguration) {
    super(mappingConfiguration);
    this.mappingConfiguration = mappingConfiguration;
    this.timeToLiveAccessor = new RedisEnhancedTimeToLiveAccessor(
        mappingConfiguration.getKeyspaceConfiguration(),
        this
    );
  }

  public RedisEnhancedMappingContext() {
    // Create mapping configuration with empty index and keyspace configs
    this(new MappingConfiguration(
        new IndexConfiguration(),
        new KeyspaceConfiguration()
    ));
  }

  @Override
  protected <T> RedisPersistentEntity<T> createPersistentEntity(TypeInformation<T> typeInformation) {
    return new RedisEnhancedPersistentEntity<>(typeInformation, getKeySpaceResolver(), timeToLiveAccessor);
  }
}