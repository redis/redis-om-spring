package com.redis.om.spring.search.stream.predicates.jedis;

import java.util.Date;

import com.google.gson.JsonPrimitive;

import redis.clients.jedis.search.querybuilder.RangeValue;

public class DateRangeValue extends RangeValue {
  public static final Date MIN = new Date(Long.MIN_VALUE);
  public static final Date MAX = new Date(Long.MAX_VALUE);
  private final Date from;
  private final Date to;

  public DateRangeValue(Date from, Date to) {
    this.from = from;
    this.to = to;
  }

  private static void appendDate(StringBuilder sb, Date date, boolean inclusive) {
    if (!inclusive) {
      sb.append("(");
    }

    if (date == MIN) {
      sb.append("-inf");
    } else if (date == MAX) {
      sb.append("inf");
    } else {
      sb.append(new JsonPrimitive(date.getTime()));
    }

  }

  @Override
  protected void appendFrom(StringBuilder sb, boolean inclusive) {
    appendDate(sb, this.from, inclusive);
  }

  @Override
  protected void appendTo(StringBuilder sb, boolean inclusive) {
    appendDate(sb, this.to, inclusive);
  }
}
