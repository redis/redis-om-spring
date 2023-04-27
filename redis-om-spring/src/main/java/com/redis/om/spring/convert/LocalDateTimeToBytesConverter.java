package com.redis.om.spring.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@WritingConverter
public class LocalDateTimeToBytesConverter implements Converter<LocalDateTime, byte[]> {
  byte[] fromString(String source) {
    return source.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public byte[] convert(LocalDateTime source) {
    Instant instant = source.atZone(ZoneId.systemDefault()).toInstant();
    long timeInMillis = instant.toEpochMilli();

    return fromString(Long.toString(timeInMillis));
  }
}
