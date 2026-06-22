package com.redis.om.spring.convert;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

/**
 * Verifies that user-provided converters passed to RedisOMCustomConversions are
 * actually registered and not silently dropped (regression for gh-755).
 */
class RedisOMCustomConversionsTest {

  @WritingConverter
  static class FooToBytesConverter implements Converter<Foo, byte[]> {
    @Override
    public byte[] convert(Foo source) {
      return source.value().getBytes();
    }
  }

  @ReadingConverter
  static class BytesToFooConverter implements Converter<byte[], Foo> {
    @Override
    public Foo convert(byte[] source) {
      return new Foo(new String(source));
    }
  }

  record Foo(String value) {
  }

  @Test
  void emptyConstructorRegistersBuiltInConverters() {
    RedisOMCustomConversions conversions = new RedisOMCustomConversions();
    // Point converter is one of the built-ins
    assertThat(conversions.hasCustomWriteTarget(org.springframework.data.geo.Point.class)).isTrue();
  }

  @Test
  void userProvidedConvertersAreRegistered() {
    RedisOMCustomConversions conversions = new RedisOMCustomConversions(
        List.of(new FooToBytesConverter(), new BytesToFooConverter()));

    Optional<Class<?>> writeTarget = conversions.getCustomWriteTarget(Foo.class);
    assertThat(writeTarget).isPresent();
    assertThat(writeTarget.get()).isEqualTo(byte[].class);
  }

  @Test
  void builtInConvertersStillRegisteredWhenUserConvertersProvided() {
    RedisOMCustomConversions conversions = new RedisOMCustomConversions(
        List.of(new FooToBytesConverter()));

    // built-in should still be present
    assertThat(conversions.hasCustomWriteTarget(org.springframework.data.geo.Point.class)).isTrue();
    // user converter should also be present
    assertThat(conversions.hasCustomWriteTarget(Foo.class)).isTrue();
  }
}
