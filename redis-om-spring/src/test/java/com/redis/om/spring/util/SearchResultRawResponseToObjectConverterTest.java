package com.redis.om.spring.util;

import com.google.gson.Gson;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class SearchResultRawResponseToObjectConverterTest {
  @Autowired
  private Gson gson;

  @Test
  void shouldReturnNullWhenRawValueIsNull() {
    assertThat(SearchResultRawResponseToObjectConverter.process(null, String.class, new Gson())).isNull();
  }

  @Test
  void shouldProcessStringWhenTargetClassIsString() {
    assertThat(SearchResultRawResponseToObjectConverter.process("hello".getBytes(), String.class, new Gson())).isEqualTo("hello");
  }

  @Test
  void shouldProcessDateWhenTargetClassIsDate() {
    Date date = new Date();
    assertThat(SearchResultRawResponseToObjectConverter.process(String.valueOf(date.getTime()).getBytes(), Date.class, new Gson())).isEqualTo(date);
  }

  @Test
  void shouldProcessPointWhenTargetClassIsPoint() {
    Point point = new Point(12.34, 56.78);
    assertThat(SearchResultRawResponseToObjectConverter.process("12.34,56.78".getBytes(), Point.class, new Gson())).isEqualTo(point);
  }

  @Test
  void shouldProcessBooleanWhenTargetClassIsBoolean() {
    assertThat(SearchResultRawResponseToObjectConverter.process("1".getBytes(), Boolean.class, new Gson())).isEqualTo(true);
    assertThat(SearchResultRawResponseToObjectConverter.process("0".getBytes(), Boolean.class, new Gson())).isEqualTo(false);
  }

  @Data
  static class MyClass {
    private String name;
  }

  @Test
  void shouldProcessOtherObjectWhenTargetClassIsNotSpecial() {
    MyClass target = new Gson().fromJson("{\"name\": \"Morgan\"}", MyClass.class);
    assertThat(SearchResultRawResponseToObjectConverter.process("{\"name\": \"Morgan\"}".getBytes(), MyClass.class, new Gson())).isEqualTo(target);
  }
}
