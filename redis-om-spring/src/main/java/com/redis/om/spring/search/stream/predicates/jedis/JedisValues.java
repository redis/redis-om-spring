package com.redis.om.spring.search.stream.predicates.jedis;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import redis.clients.jedis.search.querybuilder.RangeValue;

public class JedisValues {
  private JedisValues() {
    throw new InstantiationError("Must not instantiate this class");
  }

  // --------------
  // java.util.Date
  // --------------

  public static RangeValue between(Date from, Date to) {
    return new DateRangeValue(from, to);
  }

  public static RangeValue eq(Date d) {
    return new DateRangeValue(d, d);
  }

  public static RangeValue lt(Date d) {
    return (new DateRangeValue(DateRangeValue.MIN, d)).inclusiveMax(false);
  }

  public static RangeValue gt(Date d) {
    return (new DateRangeValue(d, DateRangeValue.MAX)).inclusiveMin(false);
  }

  public static RangeValue le(Date d) {
    return lt(d).inclusiveMax(true);
  }

  public static RangeValue ge(Date d) {
    return gt(d).inclusiveMin(true);
  }

  // -------------------
  // java.time.LocalDate
  // -------------------

  public static RangeValue between(LocalDate from, LocalDate to) {
    return new LocalDateRangeValue(from, to);
  }

  public static RangeValue eq(LocalDate d) {
    return new LocalDateRangeValue(d, d);
  }

  public static RangeValue lt(LocalDate d) {
    return (new LocalDateRangeValue(LocalDate.MIN, d)).inclusiveMax(false);
  }

  public static RangeValue gt(LocalDate d) {
    return (new LocalDateRangeValue(d, LocalDate.MAX)).inclusiveMin(false);
  }

  public static RangeValue le(LocalDate d) {
    return lt(d).inclusiveMax(true);
  }

  public static RangeValue ge(LocalDate d) {
    return gt(d).inclusiveMin(true);
  }

  // -----------------------
  // java.time.LocalDateTime
  // -----------------------

  public static RangeValue between(LocalDateTime from, LocalDateTime to) {
    return new LocalDateTimeRangeValue(from, to);
  }

  public static RangeValue eq(LocalDateTime d) {
    return new LocalDateTimeRangeValue(d, d);
  }

  public static RangeValue lt(LocalDateTime d) {
    return (new LocalDateTimeRangeValue(LocalDateTime.MIN, d)).inclusiveMax(false);
  }

  public static RangeValue gt(LocalDateTime d) {
    return (new LocalDateTimeRangeValue(d, LocalDateTime.MAX)).inclusiveMin(false);
  }

  public static RangeValue le(LocalDateTime d) {
    return lt(d).inclusiveMax(true);
  }

  public static RangeValue ge(LocalDateTime d) {
    return gt(d).inclusiveMin(true);
  }

  // -----------------
  // java.time.Instant
  // -----------------

  public static RangeValue between(Instant from, Instant to) {
    return new InstantRangeValue(from, to);
  }

  public static RangeValue eq(Instant d) {
    return new InstantRangeValue(d, d);
  }

  public static RangeValue lt(Instant d) {
    return (new InstantRangeValue(Instant.MIN, d)).inclusiveMax(false);
  }

  public static RangeValue gt(Instant d) {
    return (new InstantRangeValue(d, Instant.MAX)).inclusiveMin(false);
  }

  public static RangeValue le(Instant d) {
    return lt(d).inclusiveMax(true);
  }

  public static RangeValue ge(Instant d) {
    return gt(d).inclusiveMin(true);
  }
}
