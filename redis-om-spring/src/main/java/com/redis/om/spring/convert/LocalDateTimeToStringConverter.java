package com.redis.om.spring.convert;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

@Component
@WritingConverter
public class LocalDateTimeToStringConverter implements Converter<LocalDateTime, String> {
  @Override
  public String convert(LocalDateTime source) {
    Instant instant = ZonedDateTime.of(source, ZoneId.systemDefault()).toInstant();
    long timeInMillis = instant.toEpochMilli();
    return Long.toString(timeInMillis);
  }
}