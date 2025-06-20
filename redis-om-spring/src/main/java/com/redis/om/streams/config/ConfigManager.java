package com.redis.om.streams.config;

import java.io.File;
import java.nio.file.Paths;
import java.util.Properties;

import com.redis.om.streams.exception.RedisStreamsException;
import com.redis.om.streams.utils.Util;

import lombok.Getter;

/**
 * Singleton enum for managing Redis Streams configuration properties.
 * This class loads configuration from a properties file specified by the STREAMS_CONFIG_PATH
 * system property, or from a default config.properties file in the current directory.
 */
@Getter
public enum ConfigManager {

  /**
   * The singleton instance of the ConfigManager.
   */
  INSTANCE;

  /**
   * The loaded configuration properties for Redis Streams.
   */
  private final Properties streamsConfig;

  /**
   * Constructor that loads the configuration properties from the specified file.
   * The file path is determined by the STREAMS_CONFIG_PATH system property,
   * or defaults to config.properties in the current directory.
   * 
   * @throws RedisStreamsException if the configuration file does not exist or is not a valid file
   */
  ConfigManager() {
    String path = System.getProperty("STREAMS_CONFIG_PATH", String.valueOf(Paths.get("config.properties")));

    File file = new File(path);
    if (!file.exists() || !file.isFile())
      throw new RedisStreamsException(
          "STREAMS_CONFIG_PATH needs to be passed as a SYSTEM PROPERTY and must be a valid file");

    this.streamsConfig = Util.loadPropertiesFile(path);
  }
}
