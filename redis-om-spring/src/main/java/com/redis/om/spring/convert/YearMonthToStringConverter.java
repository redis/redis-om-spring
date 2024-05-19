package com.redis.om.spring.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Component
@WritingConverter
public class YearMonthToStringConverter implements Converter<YearMonth, String> {
  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

  @Override
  public String convert(YearMonth source) {
    return source.format(formatter);
  }
}
