package com.redis.om.spring.search.stream.predicates.jedis;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import redis.clients.jedis.search.querybuilder.RangeValue;

/**
 * Utility class providing static methods to create RangeValue instances for various
 * Java date and time types in RediSearch queries. This class offers convenient methods
 * for creating range, equality, and comparison operations for Date, LocalDate,
 * LocalDateTime, and Instant types.
 * 
 * <p>The class supports the following operations for each supported date/time type:</p>
 * <ul>
 * <li>Range queries (between)</li>
 * <li>Equality queries (eq)</li>
 * <li>Less than queries (lt)</li>
 * <li>Greater than queries (gt)</li>
 * <li>Less than or equal queries (le)</li>
 * <li>Greater than or equal queries (ge)</li>
 * </ul>
 */
public class JedisValues {
  private JedisValues() {
    throw new InstantiationError("Must not instantiate this class");
  }

  // --------------
  // java.util.Date
  // --------------

  /**
   * Creates a range value for dates between the specified bounds (inclusive).
   *
   * @param from the start date of the range
   * @param to   the end date of the range
   * @return a RangeValue representing the date range
   */
  public static RangeValue between(Date from, Date to) {
    return new DateRangeValue(from, to);
  }

  /**
   * Creates a range value for exact date equality.
   *
   * @param d the date to match exactly
   * @return a RangeValue representing date equality
   */
  public static RangeValue eq(Date d) {
    return new DateRangeValue(d, d);
  }

  /**
   * Creates a range value for dates less than the specified date (exclusive).
   *
   * @param d the upper bound date (exclusive)
   * @return a RangeValue representing dates less than the specified date
   */
  public static RangeValue lt(Date d) {
    return (new DateRangeValue(DateRangeValue.MIN, d)).inclusiveMax(false);
  }

  /**
   * Creates a range value for dates greater than the specified date (exclusive).
   *
   * @param d the lower bound date (exclusive)
   * @return a RangeValue representing dates greater than the specified date
   */
  public static RangeValue gt(Date d) {
    return (new DateRangeValue(d, DateRangeValue.MAX)).inclusiveMin(false);
  }

  /**
   * Creates a range value for dates less than or equal to the specified date.
   *
   * @param d the upper bound date (inclusive)
   * @return a RangeValue representing dates less than or equal to the specified date
   */
  public static RangeValue le(Date d) {
    return lt(d).inclusiveMax(true);
  }

  /**
   * Creates a range value for dates greater than or equal to the specified date.
   *
   * @param d the lower bound date (inclusive)
   * @return a RangeValue representing dates greater than or equal to the specified date
   */
  public static RangeValue ge(Date d) {
    return gt(d).inclusiveMin(true);
  }

  // -------------------
  // java.time.LocalDate
  // -------------------

  /**
   * Creates a range value for LocalDate between the specified bounds (inclusive).
   *
   * @param from the start date of the range
   * @param to   the end date of the range
   * @return a RangeValue representing the LocalDate range
   */
  public static RangeValue between(LocalDate from, LocalDate to) {
    return new LocalDateRangeValue(from, to);
  }

  /**
   * Creates a range value for exact LocalDate equality.
   *
   * @param d the LocalDate to match exactly
   * @return a RangeValue representing LocalDate equality
   */
  public static RangeValue eq(LocalDate d) {
    return new LocalDateRangeValue(d, d);
  }

  /**
   * Creates a range value for LocalDate less than the specified date (exclusive).
   *
   * @param d the upper bound LocalDate (exclusive)
   * @return a RangeValue representing LocalDate less than the specified date
   */
  public static RangeValue lt(LocalDate d) {
    return (new LocalDateRangeValue(LocalDate.MIN, d)).inclusiveMax(false);
  }

  /**
   * Creates a range value for LocalDate greater than the specified date (exclusive).
   *
   * @param d the lower bound LocalDate (exclusive)
   * @return a RangeValue representing LocalDate greater than the specified date
   */
  public static RangeValue gt(LocalDate d) {
    return (new LocalDateRangeValue(d, LocalDate.MAX)).inclusiveMin(false);
  }

  /**
   * Creates a range value for LocalDate less than or equal to the specified date.
   *
   * @param d the upper bound LocalDate (inclusive)
   * @return a RangeValue representing LocalDate less than or equal to the specified date
   */
  public static RangeValue le(LocalDate d) {
    return lt(d).inclusiveMax(true);
  }

  /**
   * Creates a range value for LocalDate greater than or equal to the specified date.
   *
   * @param d the lower bound LocalDate (inclusive)
   * @return a RangeValue representing LocalDate greater than or equal to the specified date
   */
  public static RangeValue ge(LocalDate d) {
    return gt(d).inclusiveMin(true);
  }

  // -----------------------
  // java.time.LocalDateTime
  // -----------------------

  /**
   * Creates a range value for LocalDateTime between the specified bounds (inclusive).
   *
   * @param from the start LocalDateTime of the range
   * @param to   the end LocalDateTime of the range
   * @return a RangeValue representing the LocalDateTime range
   */
  public static RangeValue between(LocalDateTime from, LocalDateTime to) {
    return new LocalDateTimeRangeValue(from, to);
  }

  /**
   * Creates a range value for exact LocalDateTime equality.
   *
   * @param d the LocalDateTime to match exactly
   * @return a RangeValue representing LocalDateTime equality
   */
  public static RangeValue eq(LocalDateTime d) {
    return new LocalDateTimeRangeValue(d, d);
  }

  /**
   * Creates a range value for LocalDateTime less than the specified datetime (exclusive).
   *
   * @param d the upper bound LocalDateTime (exclusive)
   * @return a RangeValue representing LocalDateTime less than the specified datetime
   */
  public static RangeValue lt(LocalDateTime d) {
    return (new LocalDateTimeRangeValue(LocalDateTime.MIN, d)).inclusiveMax(false);
  }

  /**
   * Creates a range value for LocalDateTime greater than the specified datetime (exclusive).
   *
   * @param d the lower bound LocalDateTime (exclusive)
   * @return a RangeValue representing LocalDateTime greater than the specified datetime
   */
  public static RangeValue gt(LocalDateTime d) {
    return (new LocalDateTimeRangeValue(d, LocalDateTime.MAX)).inclusiveMin(false);
  }

  /**
   * Creates a range value for LocalDateTime less than or equal to the specified datetime.
   *
   * @param d the upper bound LocalDateTime (inclusive)
   * @return a RangeValue representing LocalDateTime less than or equal to the specified datetime
   */
  public static RangeValue le(LocalDateTime d) {
    return lt(d).inclusiveMax(true);
  }

  /**
   * Creates a range value for LocalDateTime greater than or equal to the specified datetime.
   *
   * @param d the lower bound LocalDateTime (inclusive)
   * @return a RangeValue representing LocalDateTime greater than or equal to the specified datetime
   */
  public static RangeValue ge(LocalDateTime d) {
    return gt(d).inclusiveMin(true);
  }

  // -----------------
  // java.time.Instant
  // -----------------

  /**
   * Creates a range value for Instant between the specified bounds (inclusive).
   *
   * @param from the start Instant of the range
   * @param to   the end Instant of the range
   * @return a RangeValue representing the Instant range
   */
  public static RangeValue between(Instant from, Instant to) {
    return new InstantRangeValue(from, to);
  }

  /**
   * Creates a range value for exact Instant equality.
   *
   * @param d the Instant to match exactly
   * @return a RangeValue representing Instant equality
   */
  public static RangeValue eq(Instant d) {
    return new InstantRangeValue(d, d);
  }

  /**
   * Creates a range value for Instant less than the specified instant (exclusive).
   *
   * @param d the upper bound Instant (exclusive)
   * @return a RangeValue representing Instant less than the specified instant
   */
  public static RangeValue lt(Instant d) {
    return (new InstantRangeValue(Instant.MIN, d)).inclusiveMax(false);
  }

  /**
   * Creates a range value for Instant greater than the specified instant (exclusive).
   *
   * @param d the lower bound Instant (exclusive)
   * @return a RangeValue representing Instant greater than the specified instant
   */
  public static RangeValue gt(Instant d) {
    return (new InstantRangeValue(d, Instant.MAX)).inclusiveMin(false);
  }

  /**
   * Creates a range value for Instant less than or equal to the specified instant.
   *
   * @param d the upper bound Instant (inclusive)
   * @return a RangeValue representing Instant less than or equal to the specified instant
   */
  public static RangeValue le(Instant d) {
    return lt(d).inclusiveMax(true);
  }

  /**
   * Creates a range value for Instant greater than or equal to the specified instant.
   *
   * @param d the lower bound Instant (inclusive)
   * @return a RangeValue representing Instant greater than or equal to the specified instant
   */
  public static RangeValue ge(Instant d) {
    return gt(d).inclusiveMin(true);
  }
}
