package com.redis.om.spring.convert;

import java.nio.charset.StandardCharsets;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Component;

@Component
@WritingConverter
public class PointToBytesConverter implements Converter<Point, byte[]> {
  byte[] fromString(String source) {
    return source.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public byte[] convert(Point source) {
    return fromString(source.getX() + "," + source.getY());
  }
}
