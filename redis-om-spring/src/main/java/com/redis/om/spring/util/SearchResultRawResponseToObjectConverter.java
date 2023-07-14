package com.redis.om.spring.util;

import com.google.gson.Gson;
import org.springframework.data.geo.Point;
import redis.clients.jedis.util.SafeEncoder;

import java.util.Date;
import java.util.StringTokenizer;

import static com.redis.om.spring.util.ObjectUtils.isPrimitiveOfType;

public class SearchResultRawResponseToObjectConverter {
  public static Object process(Object rawValue, Class<?> targetClass, Gson gson) {
    Object value = rawValue != null ? SafeEncoder.encode((byte[]) rawValue) : null;

    Object processValue = null;
    if (value != null) {
      if (targetClass == Date.class) {
        processValue = new Date(Long.parseLong(value.toString()));
      } else if (targetClass == Point.class) {
        StringTokenizer st = new StringTokenizer(value.toString(), ",");
        String lon = st.nextToken();
        String lat = st.nextToken();

        processValue = new Point(Double.parseDouble(lon), Double.parseDouble(lat));
      } else if (targetClass == String.class) {
        processValue = value.toString();
      } else if (targetClass == Boolean.class || isPrimitiveOfType(targetClass, Boolean.class)) {
        processValue = value.toString().equals("1");
      } else {
        processValue = gson.fromJson(value.toString(), targetClass);
      }
    }
    return processValue;
  }
}
