package com.redis.om.cache.common.convert;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * {@link KeyspaceConfiguration} allows programmatic setup of keyspaces and time
 * to live options for certain types. This is suitable for cases where there is
 * no option to use the equivalent RedisHash annotations.
 *
 */
public class KeyspaceConfiguration {

  private Map<Class<?>, KeyspaceSettings> settingsMap;

  /**
   * Creates a new {@link KeyspaceConfiguration} with initial settings from {@link #initialConfiguration()}.
   */
  public KeyspaceConfiguration() {

    this.settingsMap = new ConcurrentHashMap<>();
    for (KeyspaceSettings initial : initialConfiguration()) {
      settingsMap.put(initial.type, initial);
    }
  }

  /**
   * Check if specific {@link KeyspaceSettings} are available for given type.
   *
   * @param type must not be {@literal null}.
   * @return true if settings exist.
   */
  public boolean hasSettingsFor(Class<?> type) {

    Assert.notNull(type, "Type to lookup must not be null");

    if (settingsMap.containsKey(type)) {

      if (settingsMap.get(type) instanceof DefaultKeyspaceSetting) {
        return false;
      }

      return true;
    }

    for (KeyspaceSettings assignment : settingsMap.values()) {
      if (assignment.inherit) {
        if (ClassUtils.isAssignable(assignment.type, type)) {
          settingsMap.put(type, assignment.cloneFor(type));
          return true;
        }
      }
    }

    settingsMap.put(type, new DefaultKeyspaceSetting(type));
    return false;
  }

  /**
   * Get the {@link KeyspaceSettings} for given type.
   *
   * @param type must not be {@literal null}
   * @return {@literal null} if no settings configured.
   */
  public KeyspaceSettings getKeyspaceSettings(Class<?> type) {

    if (!hasSettingsFor(type)) {
      return null;
    }

    KeyspaceSettings settings = settingsMap.get(type);
    if (settings == null || settings instanceof DefaultKeyspaceSetting) {
      return null;
    }

    return settings;
  }

  /**
   * Customization hook.
   *
   * @return must not return {@literal null}.
   */
  protected Iterable<KeyspaceSettings> initialConfiguration() {
    return Collections.emptySet();
  }

  /**
   * Add {@link KeyspaceSettings} for type.
   *
   * @param keyspaceSettings must not be {@literal null}.
   */
  public void addKeyspaceSettings(KeyspaceSettings keyspaceSettings) {

    Assert.notNull(keyspaceSettings, "KeyspaceSettings must not be null");
    this.settingsMap.put(keyspaceSettings.getType(), keyspaceSettings);
  }

  /**
   * Settings class that holds keyspace configuration for a specific type.
   */
  public static class KeyspaceSettings {

    private final String keyspace;
    private final Class<?> type;
    private final boolean inherit;

    /**
     * Creates a new {@link KeyspaceSettings} for the given type and keyspace with inheritance enabled.
     *
     * @param type     the type to configure the keyspace for
     * @param keyspace the keyspace to use
     */
    public KeyspaceSettings(Class<?> type, String keyspace) {
      this(type, keyspace, true);
    }

    /**
     * Creates a new {@link KeyspaceSettings} for the given type and keyspace.
     *
     * @param type     the type to configure the keyspace for
     * @param keyspace the keyspace to use
     * @param inherit  whether the settings should be inherited by subtypes
     */
    public KeyspaceSettings(Class<?> type, String keyspace, boolean inherit) {

      this.type = type;
      this.keyspace = keyspace;
      this.inherit = inherit;
    }

    /**
     * Creates a clone of this {@link KeyspaceSettings} for the given type with inheritance disabled.
     *
     * @param type the type to create the clone for
     * @return a new {@link KeyspaceSettings} instance
     */
    KeyspaceSettings cloneFor(Class<?> type) {
      return new KeyspaceSettings(type, this.keyspace, false);
    }

    /**
     * Returns the configured keyspace.
     *
     * @return the keyspace
     */
    public String getKeyspace() {
      return keyspace;
    }

    /**
     * Returns the type these settings are for.
     *
     * @return the type
     */
    public Class<?> getType() {
      return type;
    }

  }

  /**
   * Marker class indicating no settings defined.
   */
  private static class DefaultKeyspaceSetting extends KeyspaceSettings {

    public DefaultKeyspaceSetting(Class<?> type) {
      super(type, "#default#", false);
    }
  }
}
