package com.redis.om.spring.mapping;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.redis.core.convert.KeyspaceConfiguration;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration.KeyspaceSettings;

/**
 * A resolver that handles KeyspaceSettings lookups across different class loaders.
 * <p>
 * This class addresses the issue where spring-boot-devtools uses a different class loader
 * (RestartClassLoader) which causes entity classes to not be recognized by the
 * KeyspaceConfiguration when comparing Class instances directly.
 * <p>
 * By storing and retrieving settings based on fully qualified class names instead of
 * Class instances, this resolver ensures that TTL and other keyspace configurations
 * work correctly regardless of the class loader being used.
 *
 * @since 1.0.0
 */
public class ClassLoaderAwareKeyspaceResolver {

  private final KeyspaceConfiguration keyspaceConfiguration;
  private final Map<String, KeyspaceSettings> settingsByClassName = new ConcurrentHashMap<>();

  /**
   * Creates a new resolver wrapping the given KeyspaceConfiguration.
   *
   * @param keyspaceConfiguration the keyspace configuration to wrap
   */
  public ClassLoaderAwareKeyspaceResolver(KeyspaceConfiguration keyspaceConfiguration) {
    this.keyspaceConfiguration = keyspaceConfiguration;
  }

  /**
   * Registers keyspace settings for a class.
   * <p>
   * This method stores settings both by the Class instance (for normal operation)
   * and by the fully qualified class name (for cross-class-loader compatibility).
   *
   * @param entityClass the entity class
   * @param settings    the keyspace settings for the class
   */
  public void addKeyspaceSettings(Class<?> entityClass, KeyspaceSettings settings) {
    // Store by class name for cross-class-loader lookup
    settingsByClassName.put(entityClass.getName(), settings);
    // Also add to the original configuration for backward compatibility
    keyspaceConfiguration.addKeyspaceSettings(settings);
  }

  /**
   * Checks if settings exist for the given class.
   * <p>
   * This method first checks the original KeyspaceConfiguration, then falls back
   * to checking by class name if not found. This handles the case where the class
   * was loaded by a different class loader.
   *
   * @param entityClass the entity class to check
   * @return true if settings exist for the class, false otherwise
   */
  public boolean hasSettingsFor(Class<?> entityClass) {
    // First try the normal lookup
    if (keyspaceConfiguration.hasSettingsFor(entityClass)) {
      return true;
    }
    // Fall back to class name lookup for cross-class-loader compatibility
    return settingsByClassName.containsKey(entityClass.getName());
  }

  /**
   * Gets the keyspace settings for the given class.
   * <p>
   * This method first attempts to get settings from the original KeyspaceConfiguration,
   * then falls back to retrieving by class name if not found. This handles the case
   * where the class was loaded by a different class loader.
   *
   * @param entityClass the entity class
   * @return the keyspace settings, or null if not found
   */
  public KeyspaceSettings getKeyspaceSettings(Class<?> entityClass) {
    // First try the normal lookup
    if (keyspaceConfiguration.hasSettingsFor(entityClass)) {
      return keyspaceConfiguration.getKeyspaceSettings(entityClass);
    }
    // Fall back to class name lookup for cross-class-loader compatibility
    return settingsByClassName.get(entityClass.getName());
  }

  /**
   * Gets the underlying KeyspaceConfiguration.
   *
   * @return the wrapped keyspace configuration
   */
  public KeyspaceConfiguration getKeyspaceConfiguration() {
    return keyspaceConfiguration;
  }
}