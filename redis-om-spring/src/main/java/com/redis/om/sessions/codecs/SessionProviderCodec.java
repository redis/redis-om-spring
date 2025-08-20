/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.codecs;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import io.lettuce.core.codec.RedisCodec;

public class SessionProviderCodec implements RedisCodec<String, ByteBuffer> {
  private Charset charset = StandardCharsets.UTF_8;

  @Override
  public String decodeKey(ByteBuffer byteBuffer) {
    return charset.decode(byteBuffer).toString();
  }

  @Override
  public ByteBuffer decodeValue(ByteBuffer byteBuffer) {
    return clone(byteBuffer);
  }

  @Override
  public ByteBuffer encodeKey(String s) {
    ByteBuffer buffer = charset.encode(s);
    return buffer;
  }

  @Override
  public ByteBuffer encodeValue(ByteBuffer bytes) {
    return clone(bytes);
  }

  public static ByteBuffer clone(ByteBuffer original) {
    ByteBuffer clone = ByteBuffer.allocate(original.capacity());
    clone.put(original);
    original.rewind();
    clone.flip();
    return clone;
  }
}
