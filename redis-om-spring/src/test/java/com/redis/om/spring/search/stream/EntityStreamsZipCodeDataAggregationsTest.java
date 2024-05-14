package com.redis.om.spring.search.stream;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.ReducerFunction;
import com.redis.om.spring.annotations.document.fixtures.ZipCode;
import com.redis.om.spring.annotations.document.fixtures.ZipCode$;
import com.redis.om.spring.annotations.document.fixtures.ZipCodeRepository;
import com.redis.om.spring.metamodel.Alias;
import com.redis.om.spring.tuple.Pair;
import com.redis.om.spring.tuple.Quintuple;
import com.redis.om.spring.tuple.Tuples;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SuppressWarnings({ "unchecked", "SpellCheckingInspection" })
class EntityStreamsZipCodeDataAggregationsTest extends AbstractBaseDocumentTest {
  @Autowired
  EntityStream entityStream;

  @Autowired
  ZipCodeRepository repository;

  @BeforeEach
  void beforeEach() throws IOException {
    // Load Sample Docs
    if (repository.count() == 0) {
      repository.bulkLoad("src/test/resources/data/zips.json");
    }
  }

  /**
   * In Mongo:
   * <pre>
   * db.zipcodes.aggregate( [
   *    { $group: { _id: "$state", totalPop: { $sum: "$pop" } } },
   *    { $match: { totalPop: { $gte: 10*1000*1000 } } }
   * ] )
   * </pre>
   */
  @Test
  void testReturnStatesWithPopulationsAbove10Million() {
    var stream = entityStream.of(ZipCode.class);
    List<Pair<String, Long>> statePopulation = stream //
      .apply("@state", "_id") //
      .groupBy(Alias.of("_id")) //
      .reduce(ReducerFunction.SUM, ZipCode$.POP).as("totalPop") //
      .filter("@totalPop >= 10*1000*1000").toList(String.class, Long.class);

    assertAll(
      () -> assertThat(statePopulation).map(Pair::getFirst).containsExactly("NY", "IL", "PA", "CA", "OH", "FL", "TX"),
      () -> assertThat(statePopulation).map(Pair::getSecond)
        .containsExactly(17990402L, 11427576L, 11881643L, 29754890L, 10846517L, 12686644L, 16984601L));
  }

  /**
   * In Mongo:
   * <pre>
   * db.zipcodes.aggregate( [
   *    { $group: { _id: { state: "$state", city: "$city" }, pop: { $sum: "$pop" } } },
   *    { $group: { _id: "$_id.state", avgCityPop: { $avg: "$pop" } } }
   * ] )
   * </pre>
   */
  @Test
  void testReturnAverageCityPopulationByState() {
    var stream = entityStream.of(ZipCode.class);
    List<Pair<String, Long>> statePopulation = stream //
      .apply("@state", "_id") //
      .groupBy(Alias.of("_id")) //
      .reduce(ReducerFunction.SUM, ZipCode$.POP).as("totalPop") //
      .filter("@totalPop >= 10*1000*1000").toList(String.class, Long.class);

    assertAll(
      () -> assertThat(statePopulation).map(Pair::getFirst).containsExactly("NY", "IL", "PA", "CA", "OH", "FL", "TX"),
      () -> assertThat(statePopulation).map(Pair::getSecond)
        .containsExactly(17990402L, 11427576L, 11881643L, 29754890L, 10846517L, 12686644L, 16984601L));
  }

  /**
   * In Mongo:
   * <pre>
   * db.zipcodes.aggregate( [
   *    { $group:
   *       {
   *         _id: { state: "$state", city: "$city" },
   *         pop: { $sum: "$pop" }
   *       }
   *    },
   *    { $sort: { pop: 1 } },
   *    { $group:
   *       {
   *         _id : "$_id.state",
   *         biggestCity:  { $last: "$_id.city" },
   *         biggestPop:   { $last: "$pop" },
   *         smallestCity: { $first: "$_id.city" },
   *         smallestPop:  { $first: "$pop" }
   *       }
   *    },
   *    // the following $project is optional, and
   *    // modifies the output format.
   *    { $project:
   *      { _id: 0,
   *        state: "$_id",
   *        biggestCity:  { name: "$biggestCity",  pop: "$biggestPop" },
   *        smallestCity: { name: "$smallestCity", pop: "$smallestPop" }
   *      }
   *    }
   *  ])
   * </pre>
   * <p>
   * The RediSearch way:
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.ZipCodeIdx" "( @pop:[(0 inf])"
   *   "GROUPBY" "2" "@state" "@city"
   *     "REDUCE" "SUM" "1" "pop" "AS" "total_pop"
   *   "GROUPBY" "1" "@state"
   *     "REDUCE" "FIRST_VALUE" "4" "city" "BY" "@total_pop" "DESC" "AS" "biggestCity"
   *     "REDUCE" "FIRST_VALUE" "4" "total_pop" "BY" "@total_pop" "DESC" "AS" "biggestPop"
   *     "REDUCE" "FIRST_VALUE" "4" "city" "BY" "@total_pop" "ASC" "AS" "smallestCity"
   *     "REDUCE" "FIRST_VALUE" "4" "total_pop" "BY" "@total_pop" "ASC" "AS" "smallestPop"
   *   "LIMIT" "0" "10000"
   * </pre>
   */
  @Test
  void testReturnLargestAndSmallestCitiesByState() {
    // expected results
    Quintuple<String, String, Long, String, Long>[] expected = List.of( //
      Tuples.of("AK", "ANCHORAGE", 183987L, "CROOKED CREEK", 1L), //
      Tuples.of("AL", "BIRMINGHAM", 242606L, "MORVIN", 24L), //
      Tuples.of("AR", "LITTLE ROCK", 192895L, "ARKANSAS CITY", 7L), //
      Tuples.of("AZ", "PHOENIX", 890853L, "HUALAPAI", 2L), //
      Tuples.of("CA", "LOS ANGELES", 2102295L, "MAD RIVER", 6L), //
      Tuples.of("CO", "DENVER", 451182L, "ELK SPRINGS", 10L), //
      Tuples.of("CT", "BRIDGEPORT", 141638L, "EAST KILLINGLY", 25L), //
      Tuples.of("DC", "WASHINGTON", 606879L, "PENTAGON", 21L), //
      Tuples.of("DE", "NEWARK", 111674L, "BETHEL", 108L), //
      Tuples.of("FL", "MIAMI", 825232L, "KENNEDY SPACE CE", 1L), //
      Tuples.of("GA", "ATLANTA", 609591L, "MARBLE HILL", 98L), //
      Tuples.of("HI", "HONOLULU", 396643L, "HAWAII NATIONAL", 91L), //
      Tuples.of("IA", "DES MOINES", 148155L, "DOUDS", 15L), //
      Tuples.of("ID", "BOISE", 165522L, "DARLINGTON", 12L), //
      Tuples.of("IL", "CHICAGO", 2452177L, "ANCONA", 38L), //
      Tuples.of("IN", "INDIANAPOLIS", 348868L, "WESTPOINT", 145L), //
      Tuples.of("KS", "WICHITA", 295115L, "NEW ALMELO", 2L), //
      Tuples.of("KY", "LOUISVILLE", 288058L, "WOODBINE", 10L), //
      Tuples.of("LA", "NEW ORLEANS", 496937L, "LOTTIE", 9L), //
      Tuples.of("MA", "WORCESTER", 169856L, "BUCKLAND", 16L), //
      Tuples.of("MD", "BALTIMORE", 733081L, "ANNAPOLIS JUNCTI", 32L), //
      Tuples.of("ME", "PORTLAND", 63268L, "SQUIRREL ISLAND", 3L), //
      Tuples.of("MI", "DETROIT", 963243L, "COOKS", 2L), //
      Tuples.of("MN", "MINNEAPOLIS", 344719L, "JOHNSON", 12L), //
      Tuples.of("MO", "SAINT LOUIS", 397802L, "BENDAVIS", 44L), //
      Tuples.of("MS", "JACKSON", 204788L, "CHUNKY", 79L), //
      Tuples.of("MT", "BILLINGS", 78805L, "HOMESTEAD", 7L), //
      Tuples.of("NC", "CHARLOTTE", 465833L, "ROARING GAP", 21L), //
      Tuples.of("ND", "GRAND FORKS", 59527L, "TROTTERS", 12L), //
      Tuples.of("NE", "OMAHA", 358930L, "LAKESIDE", 5L), //
      Tuples.of("NH", "MANCHESTER", 106452L, "WEST NOTTINGHAM", 27L), //
      Tuples.of("NJ", "NEWARK", 275572L, "IMLAYSTOWN", 17L), //
      Tuples.of("NM", "ALBUQUERQUE", 449584L, "JEMEZ SPRINGS", 1L), //
      Tuples.of("NV", "LAS VEGAS", 597557L, "TUSCARORA", 1L), //
      Tuples.of("NY", "BROOKLYN", 2300504L, "NEW HYDE PARK", 1L), //
      Tuples.of("OH", "CLEVELAND", 536759L, "ISLE SAINT GEORG", 38L), //
      Tuples.of("OK", "TULSA", 389072L, "SOUTHARD", 8L), //
      Tuples.of("OR", "PORTLAND", 518543L, "SUMMER LAKE", 1L), //
      Tuples.of("PA", "PHILADELPHIA", 1610956L, "OLIVEBURG", 8L), //
      Tuples.of("RI", "CRANSTON", 176404L, "CLAYVILLE", 45L), //
      Tuples.of("SC", "COLUMBIA", 269521L, "GARNETT", 61L), //
      Tuples.of("SD", "SIOUX FALLS", 102046L, "ZEONA", 8L), //
      Tuples.of("TN", "MEMPHIS", 632837L, "ALLRED", 2L), //
      Tuples.of("TX", "HOUSTON", 2095918L, "BEND", 1L), //
      Tuples.of("UT", "SALT LAKE CITY", 186346L, "MODENA", 9L), //
      Tuples.of("VA", "VIRGINIA BEACH", 385080L, "HOWARDSVILLE", 21L), //
      Tuples.of("VT", "BURLINGTON", 39127L, "AVERILL", 7L), //
      Tuples.of("WA", "SEATTLE", 520096L, "BENGE", 2L), //
      Tuples.of("WI", "MILWAUKEE", 597324L, "CLAM LAKE", 2L), //
      Tuples.of("WV", "HUNTINGTON", 75343L, "FISHER", 1L), //
      Tuples.of("WY", "CHEYENNE", 70185L, "LOST SPRINGS", 6L) //
    ).toArray(new Quintuple[0]);

    var stream = entityStream.of(ZipCode.class);
    List<Quintuple<String, String, Long, String, Long>> largestAndSmallestCitiesByState = stream //
      .filter(ZipCode$.POP.gt(0)) //
      .groupBy(ZipCode$.STATE, ZipCode$.CITY) //
      .reduce(ReducerFunction.SUM, ZipCode$.POP).as("total_pop") //
      .groupBy(ZipCode$.STATE) //
      .reduce(ReducerFunction.FIRST_VALUE, ZipCode$.CITY, Alias.of("total_pop").desc()).as("biggestCity") //
      .reduce(ReducerFunction.FIRST_VALUE, Alias.of("total_pop"), Alias.of("total_pop").desc()).as("biggestPop") //
      .reduce(ReducerFunction.FIRST_VALUE, ZipCode$.CITY, Alias.of("total_pop").asc()).as("smallestCity") //
      .reduce(ReducerFunction.FIRST_VALUE, Alias.of("total_pop"), Alias.of("total_pop").asc()).as("smallestPop") //
      .toList(String.class, String.class, Long.class, String.class, Long.class);

    assertThat(largestAndSmallestCitiesByState).containsExactlyInAnyOrder(expected);
  }
}
