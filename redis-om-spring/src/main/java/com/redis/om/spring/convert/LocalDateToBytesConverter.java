package com.redis.om.spring.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Component
@WritingConverter
public class LocalDateToBytesConverter implements Converter<LocalDate, byte[]> {
  byte[] fromString(String source) {
    return source.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public byte[] convert(LocalDate source) {
    Instant instant = source.atStartOfDay(ZoneId.systemDefault()).toInstant();
    long unixTime = instant.getEpochSecond();
    return fromString(Long.toString(unixTime));
  }
}
