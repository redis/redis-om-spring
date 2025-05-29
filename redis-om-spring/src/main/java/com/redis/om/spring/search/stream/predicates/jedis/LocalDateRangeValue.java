package com.redis.om.spring.search.stream.predicates.jedis;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import com.google.gson.JsonPrimitive;

import redis.clients.jedis.search.querybuilder.RangeValue;

/**
 * A range value implementation for LocalDate objects that converts LocalDate values
 * to Unix timestamps for use in Redis search range queries. This class handles
 * special cases for minimum and maximum date values by using Redis infinity values.
 */
public class LocalDateRangeValue extends RangeValue {
  private final LocalDate from;
  private final LocalDate to;

  /**
   * Constructs a new LocalDateRangeValue with the specified date range.
   *
   * @param from the start date of the range (inclusive)
   * @param to   the end date of the range (inclusive)
   */
  public LocalDateRangeValue(LocalDate from, LocalDate to) {
    this.from = from;
    this.to = to;
  }

  /**
   * Appends a LocalDate value to the query string builder, converting it to Unix timestamp.
   * Handles special cases for minimum and maximum date values using Redis infinity notation.
   *
   * @param sb        the StringBuilder to append to
   * @param localDate the LocalDate to convert and append
   * @param inclusive whether the range boundary is inclusive
   */
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

  /**
   * Appends the "from" date value to the query string.
   *
   * @param sb        the StringBuilder to append to
   * @param inclusive whether the range boundary is inclusive
   */
  @Override
  protected void appendFrom(StringBuilder sb, boolean inclusive) {
    appendLocalDate(sb, this.from, inclusive);
  }

  /**
   * Appends the "to" date value to the query string.
   *
   * @param sb        the StringBuilder to append to
   * @param inclusive whether the range boundary is inclusive
   */
  @Override
  protected void appendTo(StringBuilder sb, boolean inclusive) {
    appendLocalDate(sb, this.to, inclusive);
  }
}
