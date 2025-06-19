package com.redis.om.cache.common.convert;

import org.springframework.data.keyvalue.annotation.KeySpace;
import org.springframework.data.keyvalue.core.mapping.KeySpaceResolver;
import org.springframework.data.keyvalue.core.mapping.context.KeyValueMappingContext;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.util.TypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Redis specific {@link MappingContext}.
 *
 */
public class RedisMappingContext extends KeyValueMappingContext<RedisPersistentEntity<?>, RedisPersistentProperty> {

  private static final SimpleTypeHolder SIMPLE_TYPE_HOLDER = new RedisCustomConversions().getSimpleTypeHolder();

  private final MappingConfiguration mappingConfiguration;

  /**
   * Creates new {@link RedisMappingContext} with empty {@link MappingConfiguration}.
   */
  public RedisMappingContext() {
    this(new MappingConfiguration(new KeyspaceConfiguration()));
  }

  /**
   * Creates new {@link RedisMappingContext}.
   *
   * @param mappingConfiguration can be {@literal null}.
   */
  public RedisMappingContext(@Nullable MappingConfiguration mappingConfiguration) {

    this.mappingConfiguration = mappingConfiguration != null ?
        mappingConfiguration :
        new MappingConfiguration(new KeyspaceConfiguration());

    setKeySpaceResolver(new ConfigAwareKeySpaceResolver(this.mappingConfiguration.getKeyspaceConfiguration()));
    this.setSimpleTypeHolder(SIMPLE_TYPE_HOLDER);
  }

  @Override
  protected <T> RedisPersistentEntity<T> createPersistentEntity(TypeInformation<T> typeInformation) {
    return new BasicRedisPersistentEntity<>(typeInformation, getKeySpaceResolver());
  }

  @Override
  protected RedisPersistentProperty createPersistentProperty(Property property, RedisPersistentEntity<?> owner,
      SimpleTypeHolder simpleTypeHolder) {
    return new RedisPersistentProperty(property, owner, simpleTypeHolder);
  }

  /**
   * Get the {@link MappingConfiguration} used.
   *
   * @return never {@literal null}.
   */
  public MappingConfiguration getMappingConfiguration() {
    return mappingConfiguration;
  }

  /**
   * {@link KeySpaceResolver} implementation considering {@link KeySpace} and {@link KeyspaceConfiguration}.
   *
   */
  static class ConfigAwareKeySpaceResolver implements KeySpaceResolver {

    private final KeyspaceConfiguration keyspaceConfig;

    public ConfigAwareKeySpaceResolver(KeyspaceConfiguration keyspaceConfig) {

      this.keyspaceConfig = keyspaceConfig;
    }

    @Override
    public String resolveKeySpace(Class<?> type) {

      Assert.notNull(type, "Type must not be null");
      if (keyspaceConfig.hasSettingsFor(type)) {

        String value = keyspaceConfig.getKeyspaceSettings(type).getKeyspace();
        if (StringUtils.hasText(value)) {
          return value;
        }
      }

      return null;
    }
  }

  /**
   * {@link KeySpaceResolver} implementation considering {@link KeySpace}.
   *
   */
  enum ClassNameKeySpaceResolver implements KeySpaceResolver {

    INSTANCE;

    @Override
    public String resolveKeySpace(Class<?> type) {

      Assert.notNull(type, "Type must not be null");
      return ClassUtils.getUserClass(type).getName();
    }
  }

}
