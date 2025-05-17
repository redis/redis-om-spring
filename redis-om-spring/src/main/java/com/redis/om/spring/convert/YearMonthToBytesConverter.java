package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

@Component
@WritingConverter
public class YearMonthToBytesConverter implements Converter<YearMonth, byte[]> {
  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

  byte[] fromString(String source) {
    return source.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public byte[] convert(YearMonth source) {
    return fromString(source.format(formatter));
  }
}
