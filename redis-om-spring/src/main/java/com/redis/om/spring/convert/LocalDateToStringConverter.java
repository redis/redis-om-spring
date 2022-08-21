package com.redis.om.spring.convert;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

@Component
@WritingConverter
public class LocalDateToStringConverter implements Converter<LocalDate, String> {
  @Override
  public String convert(LocalDate source) {
    Instant instant = source.atStartOfDay(ZoneId.systemDefault()).toInstant();
    long unixTime = instant.getEpochSecond();
    return Long.toString(unixTime);
  }
}
