package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

import com.github.f4b6a3.ulid.Ulid;

@Component
@WritingConverter
public class UlidToBytesConverter implements Converter<Ulid, byte[]> {
  byte[] fromString(String source) {
    return source.getBytes(StandardCharsets.UTF_8);
  }
  
  @Override
  public byte[] convert(Ulid source) {
    return fromString(source.toString());
  }
}
