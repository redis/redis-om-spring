package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

@Component
@WritingConverter
public class BooleanToBytesConverter implements Converter<Boolean, byte[]> {
  byte[] fromString(String source) {
    return source.getBytes(StandardCharsets.UTF_8);
  }

  private final byte[] trueAsBytes = fromString("true");
  private final byte[] falseAsBytes = fromString("false");

  @Override
  public byte[] convert(Boolean source) {
    return source.booleanValue() ? trueAsBytes : falseAsBytes;
  }
}
