package com.redis.om.spring.search.stream.predicates.jedis;

import java.time.Instant;

import com.google.gson.JsonPrimitive;

import redis.clients.jedis.search.querybuilder.RangeValue;

/**
 * A specialized RangeValue implementation for Instant-based range queries.
 * This class converts Instant values to epoch milliseconds for use in RediSearch
 * numeric range queries, supporting both inclusive and exclusive range boundaries.
 */
public class InstantRangeValue extends RangeValue {
  private final Instant from;
  private final Instant to;

  /**
   * Constructs a new InstantRangeValue with the specified range boundaries.
   *
   * @param from the start instant of the range
   * @param to   the end instant of the range
   */
  public InstantRangeValue(Instant from, Instant to) {
    this.from = from;
    this.to = to;
  }

  private static void appendInstant(StringBuilder sb, Instant instant, boolean inclusive) {
    if (!inclusive) {
      sb.append("(");
    }

    if (instant == Instant.MIN) {
      sb.append("-inf");
    } else if (instant == Instant.MAX) {
      sb.append("inf");
    } else {
      long timeInMillis = instant.toEpochMilli();
      sb.append(new JsonPrimitive(timeInMillis));
    }

  }

  @Override
  protected void appendFrom(StringBuilder sb, boolean inclusive) {
    appendInstant(sb, this.from, inclusive);
  }

  @Override
  protected void appendTo(StringBuilder sb, boolean inclusive) {
    appendInstant(sb, this.to, inclusive);
  }
}
