package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import com.github.f4b6a3.ulid.Ulid;

@ReadingConverter
public class BytesToUlidConverter implements Converter<byte[], Ulid> {

  @Override
  public Ulid convert(byte[] source) {
    return Ulid.from(toString(source));
  }

  String toString(byte[] source) {
    return new String(source, StandardCharsets.UTF_8);
  }

}
