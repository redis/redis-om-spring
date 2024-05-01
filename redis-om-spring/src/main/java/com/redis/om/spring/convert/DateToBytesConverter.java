package com.redis.om.spring.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@WritingConverter
public class DateToBytesConverter implements Converter<Date, byte[]> {
  byte[] fromString(String source) {
    return source.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public byte[] convert(Date source) {
    return fromString(Long.toString(source.getTime()));
  }
}