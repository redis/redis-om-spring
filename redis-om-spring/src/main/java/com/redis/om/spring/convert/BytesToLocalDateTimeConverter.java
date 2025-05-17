package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class BytesToLocalDateTimeConverter implements Converter<byte[], LocalDateTime> {

  @Override
  public LocalDateTime convert(byte[] source) {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(toString(source))), ZoneId.systemDefault());
  }

  String toString(byte[] source) {
    return new String(source, StandardCharsets.UTF_8);
  }

}
