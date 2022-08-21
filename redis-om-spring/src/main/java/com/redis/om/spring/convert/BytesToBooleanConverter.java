package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.util.ObjectUtils;

@ReadingConverter
public class BytesToBooleanConverter implements Converter<byte[], Boolean> {

  @Override
  public Boolean convert(byte[] source) {
    if (ObjectUtils.isEmpty(source)) {
      return null;
    }

    String value = toString(source);
    return ("1".equals(value) || "true".equalsIgnoreCase(value)) ? Boolean.TRUE : Boolean.FALSE;
  }

  String toString(byte[] source) {
    return new String(source, StandardCharsets.UTF_8);
  }

}
