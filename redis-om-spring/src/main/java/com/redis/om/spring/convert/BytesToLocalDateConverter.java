package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class BytesToLocalDateConverter implements Converter<byte[], LocalDate> {

  @Override
  public LocalDate convert(byte[] source) {
    return LocalDate.ofInstant(Instant.ofEpochSecond(Long.parseLong(toString(source))), ZoneId.systemDefault());
  }

  String toString(byte[] source) {
    return new String(source, StandardCharsets.UTF_8);
  }

}
