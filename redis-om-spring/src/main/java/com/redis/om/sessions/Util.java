/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Util {
  static final Charset charset = StandardCharsets.UTF_8;

  static ByteBuffer s2BB(String s) {
    return ByteBuffer.wrap(s.getBytes(charset));
  }

  static String bB2S(ByteBuffer byteBuffer) {
    byte[] arr = new byte[byteBuffer.remaining()];
    byteBuffer.get(arr);
    return new String(arr, charset);
  }
}
