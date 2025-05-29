package com.redis.om.spring.search.stream.predicates.jedis;

import java.util.Date;

import com.google.gson.JsonPrimitive;

import redis.clients.jedis.search.querybuilder.RangeValue;

/**
 * Utility class providing range value constants for Date-based queries.
 * This class extends {@link RangeValue} to provide convenient constants
 * for representing the minimum and maximum possible date values in range queries.
 * 
 * <p>These constants are useful when constructing unbounded range queries
 * where you need to specify "from beginning of time" or "until end of time".</p>
 * 
 * @since 1.0
 * @see RangeValue
 * @see Date
 */
public class DateRangeValue extends RangeValue {
  /** The minimum possible Date value (Long.MIN_VALUE) */
  public static final Date MIN = new Date(Long.MIN_VALUE);

  /** The maximum possible Date value (Long.MAX_VALUE) */
  public static final Date MAX = new Date(Long.MAX_VALUE);
  private final Date from;
  private final Date to;

  /**
   * Creates a new DateRangeValue with the specified from and to dates.
   *
   * @param from the starting date of the range
   * @param to   the ending date of the range
   */
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
