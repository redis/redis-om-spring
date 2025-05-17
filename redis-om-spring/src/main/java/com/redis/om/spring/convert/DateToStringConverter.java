package com.redis.om.spring.convert;

import java.util.Date;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

@Component
@WritingConverter
public class DateToStringConverter implements Converter<Date, String> {
  @Override
  public String convert(Date source) {
    return Long.toString(source.getTime());
  }
}