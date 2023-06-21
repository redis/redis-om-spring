package com.redis.om.spring.search.stream.predicates.jedis;

import com.google.gson.JsonPrimitive;
import redis.clients.jedis.search.querybuilder.RangeValue;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class LocalDateTimeRangeValue extends RangeValue {
  private final LocalDateTime from;
  private final LocalDateTime to;

  public LocalDateTimeRangeValue(LocalDateTime from, LocalDateTime to) {
    this.from = from;
    this.to = to;
  }

  private static void appendLocalDateTime(StringBuilder sb, LocalDateTime localDateTime, boolean inclusive) {
    if (!inclusive) {
      sb.append("(");
    }

    if (localDateTime == LocalDateTime.MIN) {
      sb.append("-inf");
    } else if (localDateTime == LocalDateTime.MAX) {
      sb.append("inf");
    } else {
      Instant instant = ZonedDateTime.of(localDateTime, ZoneId.systemDefault()).toInstant();
      long timeInMillis = instant.toEpochMilli();
      sb.append(new JsonPrimitive(timeInMillis));
    }
  }

  @Override
  protected void appendFrom(StringBuilder sb, boolean inclusive) {
    appendLocalDateTime(sb, this.from, inclusive);
  }

  @Override
  protected void appendTo(StringBuilder sb, boolean inclusive) {
    appendLocalDateTime(sb, this.to, inclusive);
  }
}
