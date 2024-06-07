package com.redis.om.spring.util;

import com.google.gson.Gson;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.data.geo.Point;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class SearchResultRawResponseToObjectConverterTest {
  private final Gson gson = new Gson();

  @Test
  void shouldReturnNullWhenRawValueIsNull() {
    assertThat(SearchResultRawResponseToObjectConverter.process(null, String.class, gson)).isNull();
  }

  @Test
  void shouldProcessStringWhenTargetClassIsString() {
    assertThat(SearchResultRawResponseToObjectConverter.process("hello".getBytes(), String.class, gson)).isEqualTo(
        "hello");
  }

  @Test
  void shouldProcessDateWhenTargetClassIsDate() {
    Date date = new Date();
    assertThat(SearchResultRawResponseToObjectConverter.process(String.valueOf(date.getTime()).getBytes(), Date.class,
        gson)).isEqualTo(date);
  }

  @Test
  void shouldProcessPointWhenTargetClassIsPoint() {
    Point point = new Point(12.34, 56.78);
    assertThat(SearchResultRawResponseToObjectConverter.process("12.34,56.78".getBytes(), Point.class, gson)).isEqualTo(
        point);
  }

  @Test
  void shouldProcessBooleanWhenTargetClassIsBoolean() {
    assertThat(SearchResultRawResponseToObjectConverter.process("1".getBytes(), Boolean.class, gson)).isEqualTo(true);
    assertThat(SearchResultRawResponseToObjectConverter.process("0".getBytes(), Boolean.class, gson)).isEqualTo(false);
  }

  @Test
  void shouldProcessOtherObjectWhenTargetClassIsNotSpecial() {
    MyClass target = new Gson().fromJson("{\"name\": \"Morgan\"}", MyClass.class);
    assertThat(SearchResultRawResponseToObjectConverter.process("{\"name\": \"Morgan\"}".getBytes(), MyClass.class,
        gson)).isEqualTo(target);
  }

  @Data
  static class MyClass {
    private String name;
  }
}
