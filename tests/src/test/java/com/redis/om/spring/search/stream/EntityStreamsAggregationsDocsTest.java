package com.redis.om.spring.search.stream;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.ReducerFunction;
import com.redis.om.spring.fixtures.document.model.*;
import com.redis.om.spring.fixtures.document.repository.*;
import com.redis.om.spring.metamodel.Alias;
import com.redis.om.spring.tuple.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Order;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({ "unchecked", "SpellCheckingInspection" })
class EntityStreamsAggregationsDocsTest extends AbstractBaseDocumentTest {
  @Autowired
  EntityStream entityStream;

  @Autowired
  GameRepository repository;
  @Autowired
  PersonRepository personRepository;
  @Autowired
  PizzaOrderRepository pizzaOrderRepository;
  @Autowired
  FilmRepository filmRepository;
  @Autowired
  LanguageRepository languageRepository;

  @BeforeEach
  void beforeEach() throws IOException {
    // Load Sample Docs
    if (repository.count() == 0) {
      repository.bulkLoad("src/test/resources/data/games.json");
    }
    // Create Sample Persons
    if (personRepository.count() == 0) {
      var beaker = new Person();
      beaker.setName("Beaker");
      beaker.setAge(23);
      beaker.setSales(500000);
      beaker.setSalesAdjustment(.6);
      beaker.setHeight(15);
      beaker.setDepartmentNumber(3);

      var bunsen = new Person();
      bunsen.setName("Dr Bunsen Honeydew");
      bunsen.setAge(63);
      bunsen.setSales(500000);
      bunsen.setSalesAdjustment(.6);
      bunsen.setHeight(15);
      bunsen.setDepartmentNumber(3);

      var fozzie = new Person();
      fozzie.setName("Fozzie Bear");
      fozzie.setAge(45);
      fozzie.setSales(350000);
      fozzie.setSalesAdjustment(.7);
      fozzie.setHeight(14);
      fozzie.setDepartmentNumber(2);

      var startler = new Person();
      startler.setName("Statler");
      startler.setAge(75);
      startler.setSales(650000);
      startler.setSalesAdjustment(.8);
      startler.setHeight(13);
      startler.setDepartmentNumber(4);

      var waldorf = new Person();
      waldorf.setName("Waldorf");
      waldorf.setAge(78);
      waldorf.setSales(750000);
      waldorf.setSalesAdjustment(.8);
      waldorf.setHeight(13);
      waldorf.setDepartmentNumber(4);

      var kermit = new Person();
      kermit.setName("Kermit the Frog");
      kermit.setAge(52);
      kermit.setSales(1500000);
      kermit.setSalesAdjustment(.8);
      kermit.setHeight(13);
      kermit.setDepartmentNumber(1);

      personRepository.saveAll(List.of(beaker, bunsen, fozzie, startler, waldorf, kermit));
    }
    // Create Sample Orders
    if (pizzaOrderRepository.count() == 0) {
      var order0 = PizzaOrder.of(0, "Pepperoni", "small", 19, 10, Instant.parse("2021-03-13T08:14:30Z"),
          LocalDateTime.parse("2021-03-13T00:00:00.000"));
      var order1 = PizzaOrder.of(1, "Pepperoni", "medium", 20, 20, Instant.parse("2021-03-13T09:13:24Z"),
          LocalDateTime.parse("2021-03-13T00:00:00.000"));
      var order2 = PizzaOrder.of(2, "Pepperoni", "large", 21, 30, Instant.parse("2021-03-17T09:22:12Z"),
          LocalDateTime.parse("2021-03-13T00:00:00.000"));
      var order3 = PizzaOrder.of(3, "Cheese", "small", 12, 15, Instant.parse("2021-03-13T11:21:39.736Z"),
          LocalDateTime.parse("2021-03-13T00:00:00.000"));
      var order4 = PizzaOrder.of(4, "Cheese", "medium", 13, 50, Instant.parse("2022-01-12T21:23:13.331Z"),
          LocalDateTime.parse("2021-03-13T00:00:00.000"));
      var order5 = PizzaOrder.of(5, "Cheese", "large", 14, 10, Instant.parse("2022-01-12T05:08:13Z"),
          LocalDateTime.parse("2021-03-13T00:00:00.000"));
      var order6 = PizzaOrder.of(6, "Vegan", "small", 17, 10, Instant.parse("2021-01-13T05:08:13Z"),
          LocalDateTime.parse("2021-03-13T00:00:00.000"));
      var order7 = PizzaOrder.of(7, "Vegan", "medium", 18, 10, Instant.parse("2021-01-13T05:10:13Z"),
          LocalDateTime.parse("2021-03-13T00:00:00.001"));

      pizzaOrderRepository.saveAll(List.of(order0, order1, order2, order3, order4, order5, order6, order7));
    }

    // Sakila Films/Language
    var en = Language.of(1, "English");
    en.setLastUpdate(LocalDate.of(2022, 1, 1));

    var es = Language.of(2, "Spanish");
    es.setLastUpdate(LocalDate.of(2022, 1, 1));

    languageRepository.saveAll(List.of(en, es));

    var film1 = Film.of(1, "Breakfast with Morty");
    film1.setReleaseYear(LocalDate.of(2020, 1, 1));
    film1.setLanguage(en);

    var film2 = Film.of(2, "Academy Dinosaur");
    film2.setLanguage(es);

    var film3 = Film.of(3, "La Permanente de mi hermana");
    film3.setReleaseYear(LocalDate.of(2020, 1, 1));
    film3.setLanguage(en);

    filmRepository.saveAll(List.of(film1, film2, film3));
  }

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*"
   *   "GROUPBY" "1" "@brand"
   *   "REDUCE" "COUNT" "0" "AS" "count"
   *   "SORTBY" "2" "@count" "DESC"
   *   "LIMIT" "0" "5"
   * </pre>
   */
  @Test
  void testCountAggregation() {
    List<Pair<String, Long>> expectedData = List.of( //
        Tuples.of("", 1498L), Tuples.of("Mad Catz", 43L), Tuples.of("Generic", 40L), Tuples.of("SteelSeries", 37L),
        Tuples.of("Logitech", 35L) //
    );

    List<Pair<String, Long>> countsPerBrand = entityStream.of(Game.class) //
        .groupBy(Game$.BRAND) //
        .reduce(ReducerFunction.COUNT).as("count") //
        .sorted(Order.desc("@count")) //
        .limit(5) //
        .toList(String.class, Long.class);

    assertThat(countsPerBrand).hasSize(5);

    IntStream.range(0, expectedData.size() - 1).forEach(i -> {
      var actual = countsPerBrand.get(i);
      var expected = expectedData.get(i);

      assertEquals(actual.getFirst(), expected.getFirst());
      assertEquals(actual.getSecond(), expected.getSecond());
    });
  }

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "sony"
   *   "GROUPBY" "1" "@brand"
   *   "REDUCE" "COUNT" "0"
   *   "REDUCE" "MIN" "1" "@price" "AS" "minPrice"
   *   "SORTBY" "2" "@minPrice" "DESC"
   * </pre>
   */
  @Test
  void testMinPrice() {
    List<Triple<String, Long, Double>> expectedData = List.of( //
        Tuples.of("Genius", 1L, 88.54), Tuples.of("Logitech", 1L, 78.98), Tuples.of("Monster", 1L, 69.95), //
        Tuples.of("Goliton", 1L, 15.69), Tuples.of("Lenmar", 1L, 15.41), Tuples.of("Oceantree(TM)", 1L, 12.29), //
        Tuples.of("Oceantree", 4L, 11.39), Tuples.of("oooo", 1L, 10.11), Tuples.of("Case Logic", 1L, 9.99), //
        Tuples.of("Neewer", 3L, 9.71) //
    );

    List<Triple<String, Long, Double>> minPrices = entityStream.of(Game.class) //
        .filter("sony") //
        .groupBy(Game$.BRAND) //
        .reduce(ReducerFunction.COUNT) //
        .reduce(ReducerFunction.MIN, Game$.PRICE).as("minPrice") //
        .sorted(Order.desc("@minPrice")) //
        .toList(String.class, Long.class, Double.class);

    assertThat(minPrices).hasSize(27);

    IntStream.range(0, expectedData.size() - 1).forEach(i -> {
      var actual = minPrices.get(i);
      var expected = expectedData.get(i);

      assertEquals(actual.getFirst(), expected.getFirst());
      assertEquals(actual.getSecond(), expected.getSecond());
      assertEquals(actual.getThird(), expected.getThird());
    });
  }

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "sony"
   *   "GROUPBY" "1" "@brand"
   *   "REDUCE" "COUNT" "0" "AS" "count"
   *   "REDUCE" "MAX" "1" "price" "AS" "maxPrice"
   *   "SORTBY" "2" "@maxPrice" "DESC"
   * </pre>
   */
  @Test
  void testMaxPrice() {
    List<Triple<String, Long, Double>> expectedData = List.of( //
        Tuples.of("Sony", 14L, 695.8), Tuples.of("", 119L, 303.59), Tuples.of("Genius", 1L, 88.54), //
        Tuples.of("Logitech", 1L, 78.98), Tuples.of("Monster", 1L, 69.95), Tuples.of("Playstation", 2L, 33.6), //
        Tuples.of("Neewer", 3L, 15.95), Tuples.of("Goliton", 1L, 15.69), Tuples.of("Lenmar", 1L, 15.41), //
        Tuples.of("Oceantree", 4L, 12.45) //
    );

    List<Triple<String, Long, Double>> maxPrices = entityStream.of(Game.class) //
        .filter("sony") //
        .groupBy(Game$.BRAND) //
        .reduce(ReducerFunction.COUNT) //
        .reduce(ReducerFunction.MAX, Game$.PRICE) //
        .as("maxPrice") //
        .sorted(Order.desc("@maxPrice")) //
        .toList(String.class, Long.class, Double.class);

    assertThat(maxPrices).hasSize(27);

    IntStream.range(0, expectedData.size() - 1).forEach(i -> {
      var actual = maxPrices.get(i);
      var expected = expectedData.get(i);

      assertEquals(actual.getFirst(), expected.getFirst());
      assertEquals(actual.getSecond(), expected.getSecond());
      assertEquals(actual.getThird(), expected.getThird());
    });
  }

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*"
   *   "GROUPBY" "1" "@brand"
   *   "REDUCE" "COUNT_DISTINCT" "1" "@title" "AS" "count_distinct(title)"
   *   "REDUCE" "COUNT" "0"
   *   "SORTBY" "2" "@count_distinct(title)" "DESC"
   *   "LIMIT" "0" "5"
   * </pre>
   */
  @Test
  void testCountDistinctByBrandHardcodedLimit() {
    List<Triple<String, Long, Long>> expectedData = List.of( //
        Tuples.of("", 1466L, 1498L), Tuples.of("Generic", 39L, 40L), Tuples.of("SteelSeries", 37L, 37L), //
        Tuples.of("Mad Catz", 36L, 43L), Tuples.of("Logitech", 34L, 35L) //
    );

    List<Triple<String, Long, Long>> distinctCounts = entityStream.of(Game.class) //
        .groupBy(Game$.BRAND) //
        .reduce(ReducerFunction.COUNT_DISTINCT, Game$.TITLE).as("count_distinct(title)") //
        .reduce(ReducerFunction.COUNT) //
        .sorted(Order.desc("@count_distinct(title)")) //
        .limit(5) //
        .toList(String.class, Long.class, Long.class);

    IntStream.range(0, expectedData.size() - 1).forEach(i -> {
      var actual = distinctCounts.get(i);
      var expected = expectedData.get(i);

      assertEquals(actual.getFirst(), expected.getFirst());
      assertEquals(actual.getSecond(), expected.getSecond());
      assertEquals(actual.getThird(), expected.getThird());
    });
  }

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*"
   *   "GROUPBY" "1" "@brand"
   *   "REDUCE" "QUANTILE" "2" "price" "0.5" "AS" "q50"
   *   "REDUCE" "QUANTILE" "2" "price" "0.9" "AS" "q90"
   *   "REDUCE" "QUANTILE" "2" "price" "0.95" "AS" "q95"
   *   "REDUCE" "AVG" "1" "price" "AS" "avg"
   *   "REDUCE" "COUNT" "0" "AS" "rowcount"
   *   "SORTBY" "2" "@rowcount" "DESC" "MAX" "1"
   * </pre>
   */
  @Test
  void testQuantiles() {
    Hextuple<String, Double, Double, Double, Double, Long> expected = Tuples.of("", 19.22, 95.91, 144.96, 29.7105941255,
        1498L);

    List<Hextuple<String, Double, Double, Double, Double, Long>> quantiles = entityStream.of(Game.class) //
        .groupBy(Game$.BRAND) //
        .reduce(ReducerFunction.QUANTILE, Game$.PRICE, "0.50").as("q50") //
        .reduce(ReducerFunction.QUANTILE, Game$.PRICE, "0.90").as("q90") //
        .reduce(ReducerFunction.QUANTILE, Game$.PRICE, "0.95").as("q95") //
        .reduce(ReducerFunction.AVG, Game$.PRICE) //
        .reduce(ReducerFunction.COUNT).as("rowcount") //
        .sorted(1, Order.desc("@rowcount")) //
        .toList(String.class, Double.class, Double.class, Double.class, Double.class, Long.class);

    var actual = quantiles.get(0);

    assertEquals(expected.getFirst(), actual.getFirst());
    assertEquals(expected.getSecond(), actual.getSecond());
    assertEquals(expected.getThird(), actual.getThird());
    assertEquals(expected.getFourth(), actual.getFourth());
    assertEquals(expected.getFifth(), actual.getFifth());
    assertEquals(expected.getSixth(), actual.getSixth());
  }

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*"
   *   "GROUPBY" "1" "@brand"
   *   "REDUCE" "STDDEV" "1" "@price" "AS" "stddev(price)"
   *   "REDUCE" "AVG" "1" "@price" "AS" "avgPrice"
   *   "REDUCE" "QUANTILE" "2" "@price" "0.5" "AS" "q50Price"
   *   "REDUCE" "COUNT" "0" "AS" "rowcount"
   *   "SORTBY" "2" "@rowcount" "DESC"
   *   "LIMIT" "0" "10"
   * </pre>
   */
  @Test
  void testPriceStdDev() {
    List<Quintuple<String, Double, Double, Double, Long>> expectedData = List.of( //
        Tuples.of("", 58.0682859441, 29.7105941255, 19.22, 1498L), //
        Tuples.of("Mad Catz", 63.3626941047, 92.4065116279, 84.99, 43L), //
        Tuples.of("Generic", 13.0528444292, 12.439, 6.69, 40L), //
        Tuples.of("SteelSeries", 44.5684434629, 50.0302702703, 39.69, 37L), //
        Tuples.of("Logitech", 48.016387201, 66.5488571429, 55.0, 35L), //
        Tuples.of("Razer", 49.0284634692, 98.4069230769, 80.49, 26L), //
        Tuples.of("", 11.6611915524, 13.711, 10.0, 20L), //
        Tuples.of("ROCCAT", 71.1336876222, 86.231, 58.72, 20L), //
        Tuples.of("Sony", 195.848045202, 109.536428571, 44.95, 14L), //
        Tuples.of("Nintendo", 71.1987671314, 53.2792307692, 17.99, 13L) //
    );

    List<Quintuple<String, Double, Double, Double, Long>> priceStdDev = entityStream.of(Game.class) //
        .groupBy(Game$.BRAND) //
        .reduce(ReducerFunction.STDDEV, Game$.PRICE).as("stddev(price)") //
        .reduce(ReducerFunction.AVG, Game$.PRICE).as("avgPrice") //
        .reduce(ReducerFunction.QUANTILE, Game$.PRICE, "0.50").as("q50Price") //
        .reduce(ReducerFunction.COUNT).as("rowcount") //
        .sorted(Order.desc("@rowcount")) //
        .limit(10) //
        .toList(String.class, Double.class, Double.class, Double.class, Long.class);

    IntStream.range(0, expectedData.size() - 1).forEach(i -> {
      var actual = priceStdDev.get(i);
      var expected = expectedData.get(i);

      assertEquals(expected.getFirst(), actual.getFirst());
      assertEquals(expected.getSecond(), actual.getSecond());
      assertEquals(expected.getThird(), actual.getThird());
      assertEquals(expected.getFourth(), actual.getFourth());
      assertEquals(expected.getFifth(), actual.getFifth());
    });
  }

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*"
   *   "GROUPBY" "1" "@brand"
   *   "REDUCE" "COUNT" "0" "AS" "count"
   *   "APPLY" "timefmt(1517417144)" "AS" "dt"
   *   "APPLY" "parsetime(@dt, \"%FT%TZ\")" "AS" "parsed_dt"
   *   "LIMIT" "0" "1"
   * </pre>
   */
  @Test
  void testParseTime() {
    Quad<String, Long, String, Long> expected = Tuples.of("", 20L, "2018-01-31T16:45:44Z", 1517417144L);

    List<Quad<String, Long, String, Long>> parseTime = entityStream.of(Game.class) //
        .groupBy(Game$.BRAND) //
        .reduce(ReducerFunction.COUNT).as("count") //
        .apply("timefmt(1517417144)", "dt") //
        .apply("parsetime(@dt, \"%FT%TZ\")", "parsed_dt") //
        .limit(1) //
        .toList(String.class, Long.class, String.class, Long.class);

    var actual = parseTime.get(0);
    assertEquals(expected.getFirst(), actual.getFirst());
    assertEquals(expected.getSecond(), actual.getSecond());
    assertEquals(expected.getThird(), actual.getThird());
    assertEquals(expected.getFourth(), actual.getFourth());
  }

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*"
   *   "GROUPBY" "1" "@brand"
   *   "REDUCE" "COUNT" "0" "AS" "num"
   *   "REDUCE" "RANDOM_SAMPLE" "2" "price" "10" "AS" "sample"
   *   "SORTBY" "2" "@num" "DESC" "MAX" "10"
   * </pre>
   */
  @Test
  void testRandomSample() {
    List<Triple<String, Long, List<Double>>> randomSample = entityStream.of(Game.class) //
        .groupBy(Game$.BRAND) //
        .reduce(ReducerFunction.COUNT).as("num") //
        .reduce(ReducerFunction.RANDOM_SAMPLE, Game$.PRICE, "10").as("sample") //
        .sorted(10, Order.desc("@num")) //
        .limit(10) //
        .toList(String.class, Long.class, List.class);

    randomSample.forEach(row -> assertThat(row.getThird()).hasSize(10));
  }

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*"
   *   "APPLY" "1517417144" "AS" "dt"
   *   "APPLY" "timefmt(@dt)" "AS" "timefmt"
   *   "APPLY" "day(@dt)" "AS" "day"
   *   "APPLY" "hour(@dt)" "AS" "hour"
   *   "APPLY" "minute(@dt)" "AS" "minute"
   *   "APPLY" "month(@dt)" "AS" "month"
   *   "APPLY" "dayofweek(@dt)" "AS" "dayofweek"
   *   "APPLY" "dayofmonth(@dt)" "AS" "dayofmonth"
   *   "APPLY" "dayofyear(@dt)" "AS" "dayofyear"
   *   "APPLY" "year(@dt)" "AS" "year"
   *   "LIMIT" "0" "1"
   * </pre>
   */
  @Test
  void testTimeFunctions() {
    Decuple<Long, String, Long, Long, Long, Long, Long, Long, Long, Long> expected = Tuples.of(1517417144L,
        "2018-01-31T16:45:44Z", 1517356800L, 1517414400L, 1517417100L, 1514764800L, 3L, 31L, 30L, 2018L);

    List<Decuple<Long, String, Long, Long, Long, Long, Long, Long, Long, Long>> parseTime = entityStream.of(
            Game.class) //
        .apply("1517417144", "dt") //
        .apply("timefmt(@dt)", "timefmt") //
        .apply("day(@dt)", "day") //
        .apply("hour(@dt)", "hour") //
        .apply("minute(@dt)", "minute") //
        .apply("month(@dt)", "month") //
        .apply("dayofweek(@dt)", "dayofweek") //
        .apply("dayofmonth(@dt)", "dayofmonth") //
        .apply("dayofyear(@dt)", "dayofyear") //
        .apply("year(@dt)", "year") //
        .limit(1) //
        .toList(Long.class, String.class, Long.class, Long.class, Long.class, Long.class, Long.class, Long.class,
            Long.class, Long.class);

    var actual = parseTime.get(0);
    assertEquals(expected.getFirst(), actual.getFirst());
    assertEquals(expected.getSecond(), actual.getSecond());
    assertEquals(expected.getThird(), actual.getThird());
    assertEquals(expected.getFourth(), actual.getFourth());
    assertEquals(expected.getFifth(), actual.getFifth());
    assertEquals(expected.getSixth(), actual.getSixth());
    assertEquals(expected.getSeventh(), actual.getSeventh());
    assertEquals(expected.getEighth(), actual.getEighth());
    assertEquals(expected.getNinth(), actual.getNinth());
    assertEquals(expected.getTenth(), actual.getTenth());
  }

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*"
   *   "GROUPBY" "1" "@brand"
   *   "REDUCE" "COUNT" "0" "AS" "num"
   *   "REDUCE" "RANDOM_SAMPLE" "2" "@price" "10" "AS" "sample"
   *   "SORTBY" "2" "@num" "DESC" "MAX" "10"
   * </pre>
   */
  @Test
  void testStringFormat() {
    List<String> expectedData = List.of( //
        "Standard Single Gang 4 Port Faceplate, ABS 94V-0, Black, 1/pkg|Hellermann Tyton|Mark|4.95", //
        "250G HDD Hard Disk Drive For Microsoft Xbox 360 E Slim with USB 2.0 AGPtek All-in-One Card Reader|(null)|Mark|51.79",
        "Portable Emergency AA Battery Charger Extender suitable for the Sony PSP - with Gomadic Brand TipExchange Technology|(null)|Mark|19.66",
        "Mad Catz S.T.R.I.K.E.5 Gaming Keyboard for PC|Mad Catz|Mark|193.26", //
        "iConcepts THE SHOCK MASTER For Use With PC|(null)|Mark|9.99", //
        "Saitek CES432110002/06/1 Pro Flight Cessna Trim Wheel|Mad Catz|Mark|47.02", //
        "Noppoo Choc Mini 84 USB NKRO Mechanical Gaming Keyboard Cherry MX Switches (BLUE switch + Black body + POM key cap)|(null)|Mark|34.98",
        "iiMash&reg; Ipega Universal Wireless Bluetooth 3.0 Game Controller Gamepad Joypad for Apple Ios Iphone 5 4 4s Ipad 4 3 2 New Mini Ipod" + " Android Phone HTC One X Samsung Galaxy S3 2 Note 2 N7100 N8000 Tablet Google Nexus 7&quot; 10&quot; Pc|iiMash&reg;|Mark|35.98",
        "16 in 1 Plastic Game Card Case Holder Box For Nintendo 3DS DSi DSi XL DS LITE|Meco|Mark|3.99", //
        "Apocalypse Red Design Protective Decal Skin Sticker (High Gloss Coating) for Nintendo DSi XL Game Device|(null)|Mark|14.99");

    List<Quintuple<String, String, Long, Double, String>> stringFormat = entityStream.of(Game.class) //
        .groupBy(Game$.TITLE, Game$.BRAND) //
        .reduce(ReducerFunction.COUNT) //
        .reduce(ReducerFunction.MAX, Game$.PRICE).as("price") //
        .apply("format(\"%s|%s|%s|%s\", @title, @brand, \"Mark\", @price)", "titleBrand") //
        .limit(10) //
        .toList(String.class, String.class, Long.class, Double.class, String.class);

    IntStream.range(0, expectedData.size() - 1).forEach(i -> {
      var actual = stringFormat.get(i);
      var expected = expectedData.get(i);

      assertEquals(expected, actual.getFifth());
    });
  }

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*"
   *   "GROUPBY" "1" "@brand"
   *   "REDUCE" "COUNT" "0" "AS" "count"
   *   "REDUCE" "SUM" "1" "@price" "AS" "sum(price)"
   *   "SORTBY" "2" "@sum(price)" "DESC"
   *   "LIMIT" "0" "5"
   * </pre>
   */
  @Test
  void testSumPrice() {
    List<Triple<String, Long, Double>> expectedData = List.of( //
        Tuples.of("", 1498L, 44506.47), //
        Tuples.of("Mad Catz", 43L, 3973.48), //
        Tuples.of("Razer", 26L, 2558.58), //
        Tuples.of("Logitech", 35L, 2329.21), //
        Tuples.of("SteelSeries", 37L, 1851.12) //
    );

    List<Triple<String, Long, Double>> sumPrice = entityStream.of(Game.class) //
        .groupBy(Game$.BRAND) //
        .reduce(ReducerFunction.COUNT).as("count") //
        .reduce(ReducerFunction.SUM, Game$.PRICE).as("sum(price)") //
        .sorted(Order.desc("@sum(price)")) //
        .limit(5) //
        .toList(String.class, Long.class, Double.class);

    IntStream.range(0, expectedData.size() - 1).forEach(i -> {
      var actual = sumPrice.get(i);
      var expected = expectedData.get(i);

      assertEquals(expected.getFirst(), actual.getFirst());
      assertEquals(expected.getSecond(), actual.getSecond());
      assertEquals(expected.getThird(), actual.getThird());
    });
  }

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*"
   *   "GROUPBY" "1" "@brand"
   *   "REDUCE" "COUNT" "0" "AS" "count"
   *   "FILTER" "@count < 5"
   *   "FILTER" "@count > 2 && @brand != \"\""
   * </pre>
   */
  @Test
  void testFilters() {
    List<Pair<String, Long>> filtered = entityStream.of(Game.class) //
        .groupBy(Game$.BRAND) //
        .reduce(ReducerFunction.COUNT).as("count") //
        .filter("@count < 5") //
        .filter("@count > 2 && @brand != \"\"").toList(String.class, Long.class);

    assertAll( //
        () -> assertThat(filtered).isNotEmpty(), //
        () -> assertThat(filtered).allSatisfy(e -> assertThat(e.getSecond()).isGreaterThan(2).isLessThan(5)) //
    );
  }

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*"
   *   "GROUPBY" "1" "@brand"
   *   "REDUCE" "COUNT" "0" "AS" "count"
   *   "FILTER" "@count < 5"
   *   "FILTER" "@count > 2 && @brand != \"\""
   * </pre>
   */
  @Test
  void testDynamicFilters() {
    // params
    int countHigh = 5;
    int countLow = 2;
    String brand = "";

    // aggregation with EntityStream
    List<Pair<String, Long>> filtered = entityStream.of(Game.class) //
        .groupBy(Game$.BRAND) //
        .reduce(ReducerFunction.COUNT).as("count") //
        .filter(String.format("@count < %s", countHigh)) //
        .filter(String.format("@count > %s && @brand != \"%s\"", countLow, brand)) //
        .toList(String.class, Long.class);

    assertAll( //
        () -> assertThat(filtered).isNotEmpty(), //
        () -> assertThat(filtered).allSatisfy(e -> assertThat(e.getSecond()).isGreaterThan(2).isLessThan(5)) //
    );
  }

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*"
   *   "GROUPBY" "1" "@brand"
   *   "REDUCE" "COUNT_DISTINCT" "1" "@price" "AS" "count"
   *   "REDUCE" "TOLIST" "1" "@price" "AS" "prices"
   *   "SORTBY" "2" "@count" "DESC"
   *   "LIMIT" "0" "5"
   * </pre>
   */
  @Test
  void testToList() {
    List<Triple<String, Long, List<Double>>> toList = entityStream.of(Game.class) //
        .groupBy(Game$.BRAND) //
        .reduce(ReducerFunction.COUNT_DISTINCT, Game$.PRICE).as("count") //
        .reduce(ReducerFunction.TOLIST, Game$.PRICE).as("prices") //
        .sorted(Order.desc("@count")) //
        .limit(5) //
        .toList(String.class, Long.class, List.class);

    assertThat(toList).isNotEmpty().allSatisfy(e -> assertThat(e.getThird()).hasSize(Math.toIntExact(e.getSecond())));
  }

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*"
   *   "GROUPBY" "1" "@brand"
   *   "REDUCE" "SUM" "1" "price" "AS" "price"
   *   "APPLY" "(@price % 10)" "AS" "price"
   *   "SORTBY" "4" "@price" "ASC" "@brand" "DESC" "MAX" "10"
   * </pre>
   */
  @Test
  void testSortByMany() {
    List<Pair<String, Long>> expectedData = List.of( //
        Tuples.of("yooZoo", 0L), //
        Tuples.of("oooo", 0L), //
        Tuples.of("iWin", 0L), //
        Tuples.of("Zalman", 0L), //
        Tuples.of("ZPS", 0L), //
        Tuples.of("White Label", 0L), //
        Tuples.of("Stinky", 0L), //
        Tuples.of("Polaroid", 0L), //
        Tuples.of("Plantronics", 0L), //
        Tuples.of("Ozone", 0L) //
    );

    List<Pair<String, Long>> sumPrice = entityStream.of(Game.class) //
        .groupBy(Game$.BRAND) //
        .reduce(ReducerFunction.SUM, Game$.PRICE).as("price") //
        .apply("(@price % 10)", "price") //
        .sorted(10, Order.asc("@price"), Order.desc("@brand")) //
        .toList(String.class, Long.class);

    IntStream.range(0, expectedData.size() - 1).forEach(i -> {
      var actual = sumPrice.get(i);
      var expected = expectedData.get(i);

      assertEquals(expected.getFirst(), actual.getFirst());
      assertEquals(expected.getSecond(), actual.getSecond());
    });
  }

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*"
   *   "LOAD" "1" "@title"
   *   "SORTBY" "2" "@price" "DESC"
   *   "LIMIT" "0" "2"
   * </pre>
   */
  @Test
  void testLoadWithSort() {
    List<Pair<String, Double>> expectedData = List.of( //
        Tuples.of("MADCATZ 8241 DVD 2 Wireless Remote for PlayStation 2", 0.01), //
        Tuples.of("INNOVATIONS 7-38012-24010-6 NES AC Adapter (Discontinued by Manufacturer)", 0.01) //
    );

    List<Pair<String, Double>> sumPrice = entityStream.of(Game.class) //
        .load(Game$.TITLE) //
        .sorted(Game$.PRICE.asc()) //
        .limit(2) //
        .toList(String.class, Double.class);

    IntStream.range(0, expectedData.size() - 1).forEach(i -> {
      var actual = sumPrice.get(i);
      var expected = expectedData.get(i);

      assertEquals(expected.getFirst(), actual.getFirst());
      assertEquals(expected.getSecond(), actual.getSecond());
    });
  }

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*"
   *   "LOAD" "3" "@brand" "@price" "@__key"
   *   "SORTBY" "2" "@price" "DESC"
   *   "MAX" "4"
   * </pre>
   */
  @Test
  void testLoadWithDocId() {
    List<Triple<String, Double, String>> expectedData = List.of( //
        Tuples.of("", 759.12, "games:B00006JJIC"), //
        Tuples.of("Sony", 695.8, "games:B000F6W1AG"), //
        Tuples.of("", 599.99, "games:B00002JXBD"), //
        Tuples.of("Matias", 759.12, "games:B00006IZIL") //
    );

    List<Triple<String, Double, String>> loadWithDocId = entityStream.of(Game.class) //
        .load(Game$.BRAND, Game$.PRICE, Game$._KEY) //
        .sorted(4, Game$.PRICE.desc()) //
        .toList(String.class, Double.class, String.class);

    IntStream.range(0, expectedData.size() - 1).forEach(i -> {
      var actual = loadWithDocId.get(i);
      var expected = expectedData.get(i);

      assertEquals(expected.getFirst(), actual.getFirst());
      assertEquals(expected.getSecond(), actual.getSecond());
      assertEquals(expected.getThird(), actual.getThird());
    });
  }

  @Test
  void testEntityStreamMin() {
    // The long way...
    List<Pair<String, Double>> minAggregation = entityStream.of(Game.class) //
        .load(Game$._KEY) //
        .sorted(Game$.PRICE.asc()).limit(1) //
        .toList(String.class, Double.class);

    Pair<String, Double> expected = minAggregation.get(0);

    // The short way...
    Optional<Game> actual = entityStream.of(Game.class).min(Game$.PRICE);

    assertThat(actual) //
        .isPresent() //
        .map(Game::getAsin) //
        .hasValue(expected.getFirst().split(":")[1]);

    assertThat(actual) //
        .map(Game::getPrice).hasValue(expected.getSecond());
  }

  @Test
  void testEntityStreamMax() {
    // The long way...
    List<Pair<String, Double>> maxAggregation = entityStream.of(Game.class) //
        .load(Game$._KEY) //
        .sorted(Game$.PRICE.desc()).limit(1) //
        .toList(String.class, Double.class);

    Pair<String, Double> expected = maxAggregation.get(0);

    // The short way...
    Optional<Game> actual = entityStream.of(Game.class).max(Game$.PRICE);

    assertThat(actual) //
        .isPresent() //
        .map(Game::getAsin) //
        .hasValue(expected.getFirst().split(":")[1]);

    assertThat(actual) //
        .map(Game::getPrice).hasValue(expected.getSecond());
  }

  //
  // Aggregation Tests with apply expressions
  //

  /**
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.PersonIdx" "*"
   * "APPLY" "@sales * @salesAdjustment" "AS" "AdjustedSales"
   * "GROUPBY" "1" "@departmentNumber"
   * "REDUCE" "SUM" "1" "AdjustedSales" "AS" "AdjustedSales_SUM"
   * "SORTBY" "2" "@AdjustedSales_SUM" "DESC"
   */
  @Test
  void testGetDepartmentBySales() {
    var stream = entityStream.of(Person.class);
    List<Pair<Integer, Double>> departments = stream //
        .apply("@sales * @salesAdjustment", "AdjustedSales").groupBy(Person$.DEPARTMENT_NUMBER) //
        .reduce(ReducerFunction.SUM, "AdjustedSales").as("AdjustedSales_SUM") //
        .sorted(Order.desc("@AdjustedSales_SUM")) //
        .toList(Integer.class, Double.class);

    assertAll(() -> assertThat(departments).map(Pair::getFirst).containsExactly(1, 4, 3, 2),
        () -> assertThat(departments).map(Pair::getSecond).containsExactly(1200000.0, 1120000.0, 600000.0, 245000.0));

  }

  /**
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.PersonIdx" "*"
   * "APPLY" "@sales * @salesAdjustment" "AS" "AdjustedSales"
   * "SORTBY" "2" "@AdjustedSales" "DESC"
   */
  @Test
  void testGetHandicappedSales() {
    var stream = entityStream.of(Person.class);
    List<Single<Double>> employees = stream //
        .apply("@sales * @salesAdjustment", "AdjustedSales").sorted(Order.desc("@AdjustedSales")) //
        .toList(Double.class);

    assertThat(employees).map(Single::getFirst).isSortedAccordingTo(Comparator.reverseOrder());
  }

  /**
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.PersonIdx" "*"
   * "APPLY" "@sales * @salesAdjustment" "AS" "AdjustedSales"
   * "GROUPBY" "0"
   * "REDUCE" "STDDEV" "1" "@AdjustedSales" "AS" "stddev"
   */
  @Test
  void testGetAdjustedSalesStandardDeviation() {
    var stream = entityStream.of(Person.class);
    List<Single<Double>> stddev = stream //
        .apply("@sales * @salesAdjustment", "AdjustedSales") //
        .groupBy() //
        .reduce(ReducerFunction.STDDEV, "@AdjustedSales").as("stddev") //
        .toList(Double.class);

    assertThat(stddev).first().extracting("first").isEqualTo(358018.854252);
  }

  /**
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.PersonIdx" "*"
   * "APPLY" "@sales * @salesAdjustment" "AS" "AdjustedSales"
   * "GROUPBY" "0"
   * "REDUCE" "STDDEV" "1" "@AdjustedSales" "AS" "stddev"
   */
  @Test
  void testGetAdjustedSalesStandardDeviationImplicitGroupByZero() {
    var stream = entityStream.of(Person.class);
    List<Single<Double>> stddev = stream //
        .apply("@sales * @salesAdjustment", "AdjustedSales") //
        .reduce(ReducerFunction.STDDEV, "@AdjustedSales").as("stddev") //
        .toList(Double.class);

    assertThat(stddev).first().extracting("first").isEqualTo(358018.854252);
  }

  /**
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.PersonIdx" "*"
   * "APPLY" "@sales * @salesAdjustment" "AS" "AdjustedSales"
   * "GROUPBY" "0"
   * "REDUCE" "AVG" "1" "@AdjustedSales" "AS" "avg"
   */
  @Test
  void testGetAverageAdjustedSales() {
    var stream = entityStream.of(Person.class);
    List<Single<Double>> stddev = stream //
        .apply("@sales * @salesAdjustment", "AdjustedSales") //
        .reduce(ReducerFunction.AVG, "@AdjustedSales").as("avg") //
        .toList(Double.class);

    assertThat(stddev).first().extracting("first").isEqualTo(527500.0);
  }

  //
  // Port of Mongo's Aggregation Examples
  // https://www.mongodb.com/docs/manual/core/aggregation-pipeline/#std-label-aggregation-pipeline-examples
  //

  /**
   * Calculate Total Order Quantity
   * The following aggregation pipeline example contains two stages and returns the total order quantity
   * of medium size pizzas grouped by pizza name:
   * <p>
   * In Mongo:
   * db.orders.aggregate( [
   * // Stage 1: Filter pizza order documents by pizza size
   * { $match: { size: "medium" } },
   * // Stage 2: Group remaining documents by pizza name and calculate total quantity
   * { $group: { _id: "$name", totalQuantity: { $sum: "$quantity" } } }
   * ] )
   */
  @Test
  void testCalculateTotalOrderQuantity() {
    var stream = entityStream.of(PizzaOrder.class);
    List<Pair<String, Integer>> totalOrderQuanties = stream //
        .filter(PizzaOrder$.SIZE.eq("medium")).groupBy(PizzaOrder$.NAME)
        .reduce(ReducerFunction.SUM, PizzaOrder$.QUANTITY).as("sum").toList(String.class, Integer.class);

    assertAll(() -> assertThat(totalOrderQuanties).map(Pair::getFirst).containsExactly("Pepperoni", "Cheese", "Vegan"),
        () -> assertThat(totalOrderQuanties).map(Pair::getSecond).containsExactly(20, 50, 10));
  }

  /**
   * Calculate Total Order Value and Average Order Quantity
   * The following example calculates the total pizza order value and average order quantity between two dates:
   * <p>
   * In Mongo:
   * db.orders.aggregate( [
   * // Stage 1: Filter pizza order documents by date range
   * { $match: { "date": { $gte: new ISODate( "2020-01-30" ), $lt: new ISODate( "2022-01-30" ) } } },
   * // Stage 2: Group remaining documents by date and calculate results
   * { $group: {
   * _id: { $dateToString: { format: "%Y-%m-%d", date: "$date" } },
   * totalOrderValue: { $sum: { $multiply: [ "$price", "$quantity" ] } },
   * averageOrderQuantity: { $avg: "$quantity" } } },
   * // Stage 3: Sort documents by totalOrderValue in descending order
   * { $sort: { totalOrderValue: -1 }}
   * ] )
   * <p>
   * Equivalent RediSearch Aggregation:
   * <p>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.PizzaOrderIdx"
   * "(( @date:[1.5803424E9 inf]) @date:[-inf (1.6435008E9])"
   * "APPLY" "timefmt(@date, '%Y-%m-%d') " "AS" "_id"
   * "APPLY" "@price * @quantity" "AS" "total"
   * "GROUPBY" "1" "@_id"
   * "REDUCE" "SUM" "1" "@total" "AS" "totalOrderValue"
   * "REDUCE" "AVG" "1" "@quantity" "AS" "averageOrderQuantity"
   */
  @Test
  void testCalculateTotalOrderValueandAverageOrderQuantity() {
    var stream = entityStream.of(PizzaOrder.class);
    List<Triple<String, Double, Integer>> totalOrderQuanties = stream //
        .filter(PizzaOrder$.DATE.onOrAfter(Instant.parse("2020-01-30T00:00:00.00Z"))) //
        .filter(PizzaOrder$.DATE.before(Instant.parse("2022-01-30T00:00:00.00Z"))) //
        .apply("timefmt(@date / 1000, '%Y-%m-%d') ", "_id") //
        .apply("@price * @quantity", "total") //
        .groupBy(Alias.of("_id")) //
        .reduce(ReducerFunction.SUM, "@total").as("totalOrderValue") //
        .reduce(ReducerFunction.AVG, "@quantity").as("averageOrderQuantity") //
        .sorted(Order.desc("@totalOrderValue")) //
        .toList(String.class, Double.class, Integer.class);

    assertAll(() -> assertThat(totalOrderQuanties).map(Triple::getFirst)
            .containsExactly("2022-01-12", "2021-03-13", "2021-03-17", "2021-01-13"),
        () -> assertThat(totalOrderQuanties).map(Triple::getSecond).containsExactly(790.0, 770.0, 630.0, 350.0),
        () -> assertThat(totalOrderQuanties).map(Triple::getThird).containsExactly(30, 15, 30, 10));
  }

  /**
   * Find all rows between given dates.
   */
  @Test
  void testSearchBetweenDates() {
    List<Single<Integer>> byCreated = entityStream.of(PizzaOrder.class).filter(
            PizzaOrder$.CREATED.between(LocalDateTime.parse("2021-03-13T00:00:00.000000"),
                LocalDateTime.parse("2021-03-13T00:00:00.000000"))).load(PizzaOrder$.ID).sorted(PizzaOrder$.ID.asc())
        .toList(Integer.class);

    List<Single<Integer>> byDate = entityStream.of(PizzaOrder.class).filter(
            PizzaOrder$.DATE.between(Instant.parse("2021-01-12T05:08:13Z"), Instant.parse("2021-03-30T00:00:00.00Z")))
        .load(PizzaOrder$.ID).sorted(PizzaOrder$.ID.asc()).toList(Integer.class);

    assertAll(() -> assertEquals(7, byCreated.size()),
        () -> assertThat(byCreated).map(Single::getFirst).containsAll(List.of(0, 1, 2, 3, 4, 5, 6)),
        () -> assertEquals(6, byDate.size()),
        () -> assertThat(byDate).map(Single::getFirst).containsAll(List.of(0, 1, 2, 3, 6, 7)));
  }

  @Test
  void testExistPredicate() {
    List<Single<Integer>> releasedFilms = entityStream.of(Film.class) //
        .load(Film$.RELEASE_YEAR) //
        .filter(Film$.RELEASE_YEAR.exists()) //
        .reduce(ReducerFunction.COUNT).as("released") //
        .toList(Integer.class);
    assertThat(releasedFilms).hasSize(1);
    assertThat(releasedFilms.get(0).getFirst()).isEqualTo(2);
  }

  @Test
  void testNotExistPredicate() {
    List<Single<Integer>> releasedFilms = entityStream.of(Film.class) //
        .load(Film$.RELEASE_YEAR) //
        .filter(Film$.RELEASE_YEAR.notExists()) //
        .reduce(ReducerFunction.COUNT).as("released") //
        .toList(Integer.class);
    assertThat(releasedFilms).hasSize(1);
    assertThat(releasedFilms.get(0).getFirst()).isEqualTo(1);
  }

}
