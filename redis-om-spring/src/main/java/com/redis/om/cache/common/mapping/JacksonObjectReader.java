package com.redis.om.cache.common.mapping;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Functional interface for reading JSON data into Java objects using Jackson.
 * Provides a consistent way to deserialize JSON data with different Jackson configurations.
 */
@FunctionalInterface
public interface JacksonObjectReader {

  /**
   * Read an object graph from the given root JSON into a Java object considering
   * the {@link JavaType}.
   *
   * @param mapper the object mapper to use.
   * @param source the JSON to deserialize.
   * @param type   the Java target type
   * @return the deserialized Java object.
   * @throws IOException if an I/O error or JSON deserialization error occurs.
   */
  Object read(ObjectMapper mapper, byte[] source, JavaType type) throws IOException;

  /**
   * Create a default {@link JacksonObjectReader} delegating to
   * {@link ObjectMapper#readValue(InputStream, JavaType)}.
   *
   * @return the default {@link JacksonObjectReader}.
   */
  static JacksonObjectReader create() {
    return (mapper, source, type) -> mapper.readValue(source, 0, source.length, type);
  }

}
