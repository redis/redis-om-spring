package com.redis.om.cache.common.mapping;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.serializer.support.DeserializingConverter;
import org.springframework.core.serializer.support.SerializingConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.redis.om.cache.common.RedisStringMapper;
import com.redis.om.cache.common.SerializationException;

/**
 * Implementation of {@link RedisStringMapper} that uses JDK serialization to convert
 * objects to and from byte arrays for storage in Redis.
 */
public class JdkSerializationStringMapper implements RedisStringMapper {

  /**
   * Converter used to serialize objects to byte arrays.
   */
  private final Converter<Object, byte[]> serializer;

  /**
   * Converter used to deserialize byte arrays back to objects.
   */
  private final Converter<byte[], Object> deserializer;

  /**
   * Creates a new {@link JdkSerializationStringMapper} using the default
   * {@link ClassLoader}.
   */
  public JdkSerializationStringMapper() {
    this(new SerializingConverter(), new DeserializingConverter());
  }

  /**
   * Creates a new {@link JdkSerializationStringMapper} with the given
   * {@link ClassLoader} used to resolve {@link Class types} during
   * deserialization.
   *
   * @param classLoader {@link ClassLoader} used to resolve {@link Class types}
   *                    for deserialization; can be {@literal null}.
   * @since 1.7
   */
  public JdkSerializationStringMapper(@Nullable ClassLoader classLoader) {
    this(new SerializingConverter(), new DeserializingConverter(classLoader));
  }

  /**
   * Creates a new {@link JdkSerializationStringMapper} using {@link Converter
   * converters} to serialize and deserialize {@link Object objects}.
   *
   * @param serializer   {@link Converter} used to serialize an {@link Object} to
   *                     a byte array; must not be {@literal null}.
   * @param deserializer {@link Converter} used to deserialize and convert a byte
   *                     arra into an {@link Object}; must not be {@literal null}
   * @throws IllegalArgumentException if either the given {@code serializer} or
   *                                  {@code deserializer} are {@literal null}.
   * @since 1.7
   */
  public JdkSerializationStringMapper(Converter<Object, byte[]> serializer, Converter<byte[], Object> deserializer) {

    Assert.notNull(serializer, "Serializer must not be null");
    Assert.notNull(deserializer, "Deserializer must not be null");
    this.serializer = serializer;
    this.deserializer = deserializer;
  }

  @Override
  public byte[] toString(@Nullable Object value) {

    if (value == null) {
      return SerializationUtils.EMPTY_ARRAY;
    }

    try {
      return serializer.convert(value);
    } catch (Exception ex) {
      throw new SerializationException("Cannot serialize", ex);
    }
  }

  @Override
  public Object fromString(@Nullable byte[] bytes) {

    if (SerializationUtils.isEmpty(bytes)) {
      return null;
    }

    try {
      return deserializer.convert(bytes);
    } catch (Exception ex) {
      throw new SerializationException("Cannot deserialize", ex);
    }
  }
}
