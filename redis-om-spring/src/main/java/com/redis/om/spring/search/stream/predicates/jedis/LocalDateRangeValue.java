package com.redis.om.spring.search.stream.predicates.jedis;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import com.google.gson.JsonPrimitive;

import redis.clients.jedis.search.querybuilder.RangeValue;

public class LocalDateRangeValue extends RangeValue {
  private final LocalDate from;
  private final LocalDate to;

  public LocalDateRangeValue(LocalDate from, LocalDate to) {
    this.from = from;
    this.to = to;
  }

  private static void appendLocalDate(StringBuilder sb, LocalDate localDate, boolean inclusive) {
    if (!inclusive) {
      sb.append("(");
    }

    if (localDate == LocalDate.MIN) {
      sb.append("-inf");
    } else if (localDate == LocalDate.MAX) {
      sb.append("inf");
    } else {
      Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
      long unixTime = instant.getEpochSecond();
      sb.append(new JsonPrimitive(unixTime));
    }

  }

  @Override
  protected void appendFrom(StringBuilder sb, boolean inclusive) {
    appendLocalDate(sb, this.from, inclusive);
  }

  @Override
  protected void appendTo(StringBuilder sb, boolean inclusive) {
    appendLocalDate(sb, this.to, inclusive);
  }
}
