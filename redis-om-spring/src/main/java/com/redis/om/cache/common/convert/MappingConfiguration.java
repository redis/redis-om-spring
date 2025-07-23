package com.redis.om.cache.common.convert;

/**
 * {@link MappingConfiguration} is used for programmatic configuration of key
 * prefixes.
 *
 */
public class MappingConfiguration {

  private final KeyspaceConfiguration keyspaceConfiguration;

  /**
   * Creates new {@link MappingConfiguration}.
   *
   * @param keyspaceConfiguration must not be {@literal null}.
   */
  public MappingConfiguration(KeyspaceConfiguration keyspaceConfiguration) {

    this.keyspaceConfiguration = keyspaceConfiguration;
  }

  /**
   * @return never {@literal null}.
   */
  public KeyspaceConfiguration getKeyspaceConfiguration() {
    return keyspaceConfiguration;
  }
}
