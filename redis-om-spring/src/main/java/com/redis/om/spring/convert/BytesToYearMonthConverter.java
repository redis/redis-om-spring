package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class BytesToYearMonthConverter implements Converter<byte[], YearMonth> {
  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

  @Override
  public YearMonth convert(byte[] source) {
    return formatter.parse(toString(source), YearMonth::from);
  }

  String toString(byte[] source) {
    return new String(source, StandardCharsets.UTF_8);
  }

}