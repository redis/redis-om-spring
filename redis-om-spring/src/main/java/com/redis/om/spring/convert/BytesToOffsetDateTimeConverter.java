package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class BytesToOffsetDateTimeConverter implements Converter<byte[], OffsetDateTime> {

  @Override
  public OffsetDateTime convert(byte[] source) {
    return OffsetDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(toString(source))), ZoneId.systemDefault());
  }

  String toString(byte[] source) {
    return new String(source, StandardCharsets.UTF_8);
  }

}
