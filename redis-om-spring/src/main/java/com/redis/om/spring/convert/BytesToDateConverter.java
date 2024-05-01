package com.redis.om.spring.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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