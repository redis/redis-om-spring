package com.redis.om.cache.common.mapping;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.redis.om.cache.common.RedisStringMapper;

/**
 * Implementation of {@link RedisStringMapper} that converts between String objects and byte arrays
 * using a specified character set encoding.
 */
public class StringMapper implements RedisStringMapper {

  private final Charset charset;

  /**
   * {@link StringMapper} to use 7 bit ASCII, a.k.a. ISO646-US, a.k.a. the Basic
   * Latin block of the Unicode character set.
   *
   * @see StandardCharsets#US_ASCII
   * @since 2.1
   */
  public static final StringMapper US_ASCII = new StringMapper(StandardCharsets.US_ASCII);

  /**
   * {@link StringMapper} to use ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1.
   *
   * @see StandardCharsets#ISO_8859_1
   * @since 2.1
   */
  public static final StringMapper ISO_8859_1 = new StringMapper(StandardCharsets.ISO_8859_1);

  /**
   * {@link StringMapper} to use 8 bit UCS Transformation Format.
   *
   * @see StandardCharsets#UTF_8
   * @since 2.1
   */
  public static final StringMapper UTF_8 = new StringMapper(StandardCharsets.UTF_8);

  /**
   * Creates a new {@link StringMapper} using {@link StandardCharsets#UTF_8
   * UTF-8}.
   */
  public StringMapper() {
    this(StandardCharsets.UTF_8);
  }

  /**
   * Creates a new {@link StringMapper} using the given {@link Charset} to encode
   * and decode strings.
   *
   * @param charset must not be {@literal null}.
   */
  public StringMapper(Charset charset) {

    Assert.notNull(charset, "Charset must not be null");
    this.charset = charset;
  }

  @Override
  public byte[] toString(@Nullable Object value) {
    return (value == null ? null : ((String) value).getBytes(charset));
  }

  @Override
  public String fromString(@Nullable byte[] bytes) {
    return (bytes == null ? null : new String(bytes, charset));
  }

}
