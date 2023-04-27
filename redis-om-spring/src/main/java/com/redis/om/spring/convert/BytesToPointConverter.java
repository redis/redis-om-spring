package com.redis.om.spring.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.geo.Point;

import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

@ReadingConverter
public class BytesToPointConverter implements Converter<byte[], Point> {

  @Override
  public Point convert(byte[] source) {
    String latlon = toString(source);
    StringTokenizer st = new StringTokenizer(latlon, ",");
    String lon = st.nextToken();
    String lat = st.nextToken();

    return new Point(Double.parseDouble(lon), Double.parseDouble(lat));
  }

  String toString(byte[] source) {
    return new String(source, StandardCharsets.UTF_8);
  }

}
