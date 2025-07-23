
/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.serializers;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.redis.om.sessions.Serializer;

public class JsonSerializer implements Serializer {

  private final ObjectMapper objectMapper;

  public JsonSerializer() {
    this.objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
    this.objectMapper.activateDefaultTyping(this.objectMapper.getPolymorphicTypeValidator(),
        ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.PROPERTY);
  }

  public JsonSerializer(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> byte[] Serialize(T object) throws Exception {
    return this.objectMapper.writeValueAsString(object).getBytes(StandardCharsets.UTF_8);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> T Deserialize(byte[] buffer) throws Exception {
    return (T) this.objectMapper.readValue(new String(buffer, StandardCharsets.UTF_8), Object.class);
  }
}
