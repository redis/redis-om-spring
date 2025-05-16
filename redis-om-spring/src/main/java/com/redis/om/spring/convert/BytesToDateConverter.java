package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class BytesToDateConverter implements Converter<byte[], Date> {

  @Override
  public Date convert(byte[] source) {
    long milliseconds = Long.parseLong(toString(source));
    return new Date(milliseconds);
  }

  String toString(byte[] source) {
    return new String(source, StandardCharsets.UTF_8);
  }

}