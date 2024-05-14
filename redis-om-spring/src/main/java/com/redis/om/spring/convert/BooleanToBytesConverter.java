package com.redis.om.spring.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@WritingConverter
public class BooleanToBytesConverter implements Converter<Boolean, byte[]> {
  private final byte[] trueAsBytes = fromString("true");
  private final byte[] falseAsBytes = fromString("false");

  byte[] fromString(String source) {
    return source.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public byte[] convert(Boolean source) {
    return Boolean.TRUE.equals(source) ? trueAsBytes : falseAsBytes;
  }
}
