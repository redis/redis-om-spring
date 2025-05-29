package com.redis.om.spring.search.stream.predicates.jedis;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.google.gson.JsonPrimitive;

import redis.clients.jedis.search.querybuilder.RangeValue;

/**
 * A range value implementation for LocalDateTime objects that converts LocalDateTime values
 * to Unix timestamps (in milliseconds) for use in Redis search range queries. This class handles
 * special cases for minimum and maximum datetime values by using Redis infinity values.
 */
public class LocalDateTimeRangeValue extends RangeValue {
  private final LocalDateTime from;
  private final LocalDateTime to;

  /**
   * Constructs a new LocalDateTimeRangeValue with the specified datetime range.
   *
   * @param from the start datetime of the range (inclusive)
   * @param to   the end datetime of the range (inclusive)
   */
  public LocalDateTimeRangeValue(LocalDateTime from, LocalDateTime to) {
    this.from = from;
    this.to = to;
  }

  /**
   * Appends a LocalDateTime value to the query string builder, converting it to Unix timestamp in milliseconds.
   * Handles special cases for minimum and maximum datetime values using Redis infinity notation.
   *
   * @param sb            the StringBuilder to append to
   * @param localDateTime the LocalDateTime to convert and append
   * @param inclusive     whether the range boundary is inclusive
   */
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

  /**
   * Appends the "from" datetime value to the query string.
   *
   * @param sb        the StringBuilder to append to
   * @param inclusive whether the range boundary is inclusive
   */
  @Override
  protected void appendFrom(StringBuilder sb, boolean inclusive) {
    appendLocalDateTime(sb, this.from, inclusive);
  }

  /**
   * Appends the "to" datetime value to the query string.
   *
   * @param sb        the StringBuilder to append to
   * @param inclusive whether the range boundary is inclusive
   */
  @Override
  protected void appendTo(StringBuilder sb, boolean inclusive) {
    appendLocalDateTime(sb, this.to, inclusive);
  }
}
