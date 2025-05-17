package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class BytesToBooleanConverter implements Converter<byte[], Boolean> {

  @Override
  public Boolean convert(byte[] source) {
    String value = toString(source);
    return ("1".equals(value) || "true".equalsIgnoreCase(value)) ? Boolean.TRUE : Boolean.FALSE;
  }

  String toString(byte[] source) {
    return new String(source, StandardCharsets.UTF_8);
  }

}
