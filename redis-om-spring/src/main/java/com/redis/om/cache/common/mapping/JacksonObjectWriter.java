package com.redis.om.cache.common.mapping;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Functional interface for writing Java objects to JSON data using Jackson.
 * Provides a consistent way to serialize Java objects with different Jackson configurations.
 */
@FunctionalInterface
public interface JacksonObjectWriter {

  /**
   * Write the object graph with the given root {@code source} as byte array.
   *
   * @param mapper the object mapper to use.
   * @param source the root of the object graph to marshal.
   * @return a byte array containing the serialized object graph.
   * @throws IOException if an I/O error or JSON serialization error occurs.
   */
  byte[] write(ObjectMapper mapper, Object source) throws IOException;

  /**
   * Create a default {@link JacksonObjectWriter} delegating to
   * {@link ObjectMapper#writeValueAsBytes(Object)}.
   *
   * @return the default {@link JacksonObjectWriter}.
   */
  static JacksonObjectWriter create() {
    return ObjectMapper::writeValueAsBytes;
  }

}
