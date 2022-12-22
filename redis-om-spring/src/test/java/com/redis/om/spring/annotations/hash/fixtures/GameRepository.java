package com.redis.om.spring.annotations.hash.fixtures;

import com.redis.om.spring.annotations.*;
import com.redis.om.spring.repository.RedisEnhancedRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import redis.clients.jedis.search.aggr.AggregationResult;

import java.util.Map;

@SuppressWarnings({ "unused", "SpellCheckingInspection", "SpringDataRepositoryMethodReturnTypeInspection" }) public interface GameRepository extends RedisEnhancedRepository<Game, String> {
  /**
   * <pre>
   * FT.AGGREGATE "com.redis.om.spring.annotations.document.fixtures.GameIdx" '*'
   *   'GROUPBY' '1' '@brand'
   *   'REDUCE' 'count' '0' 'AS' 'count'
   *   'SORTBY' 2 '@count' 'desc'
   *   'LIMIT' '0' '5'
   * </pre>
   */
  @Aggregation( //
      groupBy = { //
          @GroupBy( //
              properties = "@brand", //
              reduce = { @Reducer(func = ReducerFunction.COUNT, alias = "count") } //
          ) //
      }, //
      sortBy = { //
          @SortBy(field = "@count", direction = Direction.DESC), //
      }
  ) //
  Page<Map<String, String>> countByBrand(Pageable pageable);

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "sony"
   *   "GROUPBY" "1" "@brand"
   *   "REDUCE" "COUNT" "0"
   *   "REDUCE" "MIN" "1" "@price" "AS" "minPrice"
   *   "SORTBY" "2" "@minPrice" "DESC"
   * </pre>
   */
  @Aggregation( //
      value = "sony", //
      groupBy = { //
          @GroupBy( //
              properties = "@brand", //
              reduce = { //
                  @Reducer(func = ReducerFunction.COUNT), //
                  @Reducer(func = ReducerFunction.MIN, args={"@price"}, alias="minPrice")
              } //
          ) //
      }, //
      sortBy = { //
          @SortBy(field = "@minPrice", direction = Direction.DESC), //
      }
  ) //
  AggregationResult minPricesContainingSony();

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "sony"
   *   "GROUPBY" "1" "@brand"
   *   "REDUCE" "COUNT" "0"
   *   "REDUCE" "MAX" "1" "@price" "AS" "maxPrice"
   *   "SORTBY" "2" "@maxPrice" "DESC"
   * </pre>
   */
  @Aggregation( //
      value = "sony", //
      groupBy = { //
          @GroupBy( //
              properties = "@brand", //
              reduce = { //
                  @Reducer(func = ReducerFunction.COUNT), //
                  @Reducer(func = ReducerFunction.MAX, args={"@price"}, alias="maxPrice")
              } //
          ) //
      }, //
      sortBy = { //
          @SortBy(field = "@maxPrice", direction = Direction.DESC), //
      }
  ) //
  AggregationResult maxPricesContainingSony();

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
  @Aggregation( //
      groupBy = { //
          @GroupBy( //
              properties = "@brand", //
              reduce = { //
                  @Reducer(func = ReducerFunction.COUNT_DISTINCT, args={"@title"}, alias="count_distinct(title)"), //
                  @Reducer(func = ReducerFunction.COUNT) //
              } //
          ) //
      }, //
      sortBy = { //
          @SortBy(field = "@count_distinct(title)", direction = Direction.DESC), //
      },
      limit = 5
  ) //
  AggregationResult top5countDistinctByBrand();

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*"
   *   "GROUPBY" "1" "@brand"
   *   "REDUCE" "QUANTILE" "2" "@price" "0.5" "AS" "q50"
   *   "REDUCE" "QUANTILE" "2" "@price" "0.9" "AS" "q90"
   *   "REDUCE" "QUANTILE" "2" "@price" "0.95" "AS" "q95"
   *   "REDUCE" "AVG" "1" "@price"
   *   "REDUCE" "COUNT" "0" "AS" "rowcount"
   *   "SORTBY" "2" "@rowcount" "DESC" "MAX" "1"
   * </pre>
   */
  @Aggregation( //
      groupBy = { //
          @GroupBy( //
              properties = "@brand", //
              reduce = { //
                  @Reducer(func = ReducerFunction.QUANTILE, args={"@price", "0.50"}, alias="q50"), //
                  @Reducer(func = ReducerFunction.QUANTILE, args={"@price", "0.90"}, alias="q90"), //
                  @Reducer(func = ReducerFunction.QUANTILE, args={"@price", "0.95"}, alias="q95"), //
                  @Reducer(func = ReducerFunction.AVG, args={"@price"}), //
                  @Reducer(func = ReducerFunction.COUNT, alias = "rowcount") //
              } //
          ) //
      }, //
      sortBy = { //
          @SortBy(field = "@rowcount", direction = Direction.DESC), //
      },
      sortByMax = 1 //
  ) //
  AggregationResult priceQuantiles();

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
  @Aggregation( //
      groupBy = { //
          @GroupBy( //
              properties = "@brand", //
              reduce = { //
                  @Reducer(func = ReducerFunction.STDDEV, args={"@price"}, alias="stddev(price)"), //
                  @Reducer(func = ReducerFunction.AVG, args={"@price"}, alias = "avgPrice"), //
                  @Reducer(func = ReducerFunction.QUANTILE, args={"@price", "0.50"}, alias="q50Price"), //
                  @Reducer(func = ReducerFunction.COUNT, alias = "rowcount") //
              } //
          ) //
      }, //
      sortBy = { //
          @SortBy(field = "@rowcount", direction = Direction.DESC), //
      },
      limit = 10 //
  ) //
  AggregationResult priceStdDev();

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*"
   *   "GROUPBY" "1" "@brand"
   *   "REDUCE" "COUNT" "0" "AS" "count"
   *   "LIMIT" "0" "1"
   *   "APPLY" "timefmt(1517417144)" "AS" "dt"
   *   "APPLY" "parsetime(@dt, \"%FT%TZ\")" "AS" "parsed_dt"
   * </pre>
   */
  @Aggregation( //
      groupBy = { //
          @GroupBy( //
              properties = "@brand", //
              reduce = { @Reducer(func = ReducerFunction.COUNT, alias = "count") } //
          ) //
      }, //
      apply = { //
          @Apply(expression = "timefmt(1517417144)", alias = "dt"), //
          @Apply(expression = "parsetime(@dt, \"%FT%TZ\")", alias = "parsed_dt"), //
      }, //
      limit = 1 //
  ) //
  AggregationResult parseTime();

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*"
   *   "GROUPBY" "1" "@brand"
   *   "REDUCE" "COUNT" "0" "AS" "num"
   *   "REDUCE" "RANDOM_SAMPLE" "2" "@price" "10" "AS" "sample"
   *   "SORTBY" "2" "@num" "DESC" "MAX" "10"
   * </pre>
   */
  @Aggregation( //
      groupBy = { //
          @GroupBy( //
              properties = "@brand", //
              reduce = { //
                  @Reducer(func = ReducerFunction.COUNT, alias = "num"), //
                  @Reducer(func = ReducerFunction.RANDOM_SAMPLE, args={"@price", "10"}, alias = "sample") //
              } //
          ) //
      }, //
      sortBy = { //
          @SortBy(field = "@num", direction = Direction.DESC), //
      },
      sortByMax = 10
  ) //
  AggregationResult randomSample();

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
  @Aggregation( //
      apply = {
          @Apply(expression = "1517417144", alias = "dt"), //
          @Apply(expression = "timefmt(@dt)", alias = "timefmt"), //
          @Apply(expression = "day(@dt)", alias = "day"), //
          @Apply(expression = "hour(@dt)", alias = "hour"), //
          @Apply(expression = "minute(@dt)", alias = "minute"), //
          @Apply(expression = "month(@dt)", alias = "month"), //
          @Apply(expression = "dayofweek(@dt)", alias = "dayofweek"), //
          @Apply(expression = "dayofmonth(@dt)", alias = "dayofmonth"), //
          @Apply(expression = "dayofyear(@dt)", alias = "dayofyear"), //
          @Apply(expression = "year(@dt)", alias = "year"), //
      },
      limit = 1
  ) //
  AggregationResult timeFunctions();

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*"
   *   "GROUPBY" "1" "@brand"
   *   "REDUCE" "COUNT" "0" "AS" "num"
   *   "REDUCE" "RANDOM_SAMPLE" "2" "@price" "10" "AS" "sample"
   *   "SORTBY" "2" "@num" "DESC" "MAX" "10"
   * </pre>
   */
  @Aggregation( //
      groupBy = { //
          @GroupBy( //
              properties = {"@title", "@brand"}, //
              reduce = { //
                  @Reducer(func = ReducerFunction.COUNT), //
                  @Reducer(func = ReducerFunction.MAX, args={"@price"}, alias = "price") //
              } //
          ) //
      }, //
      apply = {
          @Apply(expression = "format(\"%s|%s|%s|%s\", @title, @brand, \"Mark\", @price)", alias = "titleBrand"), //
      },
      limit = 10
  ) //
  AggregationResult stringFormat();

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
  @Aggregation( //
      groupBy = { //
          @GroupBy( //
              properties = {"@brand"}, //
              reduce = { //
                  @Reducer(func = ReducerFunction.COUNT, alias="count"), //
                  @Reducer(func = ReducerFunction.SUM, args={"@price"}, alias = "sum(price)") //
              } //
          ) //
      }, //
      sortBy = { //
          @SortBy(field = "@sum(price)", direction = Direction.DESC), //
      },
      limit = 5 //
  ) //
  AggregationResult sumPrice();

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*"
   *   "FILTER" "@count < 5"
   *   "FILTER" "@count > 2 && @brand != \"\""
   *   "GROUPBY" "1" "@brand"
   *   "REDUCE" "COUNT" "0" "AS" "count"
   *   'ft.aggregate', 'games', '*',
   *                'GROUPBY', '1', '@brand',
   *                'REDUCE', 'count', '0', 'AS', 'count',
   *                'FILTER', '@count < 5',
   *                'FILTER', '@count > 2 && @brand != ""'
   * </pre>
   */
  @Aggregation( //
      groupBy = { //
          @GroupBy( //
              properties = {"@brand"}, //
              reduce = { @Reducer(func = ReducerFunction.COUNT, alias="count") } //
          ) //
      }, //
      filter = { "@count < 5", "@count > 2 && @brand != \"\"" }
  ) //
  AggregationResult filters();

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
  @Aggregation( //
      groupBy = { //
          @GroupBy( //
              properties = {"@brand"}, //
              reduce = { //
                  @Reducer(func = ReducerFunction.COUNT_DISTINCT, args="@price", alias="count"), //
                  @Reducer(func = ReducerFunction.TOLIST, args="@price", alias="prices") //
              } //
          ) //
      }, //
      sortBy = { //
          @SortBy(field = "@count", direction = Direction.DESC), //
      },
      limit = 5 //
  ) //
  AggregationResult toList();

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*"
   *   "GROUPBY" "1" "@brand"
   *   "REDUCE" "SUM" "1" "@price" "AS" "price"
   *   "SORTBY" "4" "@price" "ASC" "@brand" "DESC"
   *   "MAX" "10"
   *   "APPLY" "(@price % 10)" "AS" "price"
   * </pre>
   */
  @Aggregation( //
      groupBy = { //
          @GroupBy( //
              properties = {"@brand"}, //
              reduce = { @Reducer(func = ReducerFunction.SUM, args="@price", alias="price") } //
          ) //
      }, //
      apply = { @Apply(expression = "(@price % 10)", alias = "price") }, //
      sortBy = { //
          @SortBy(field = "@price", direction = Direction.ASC), //
          @SortBy(field = "@brand", direction = Direction.DESC), //
      },
      sortByMax = 10
  ) //
  AggregationResult sortByMany();

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*"
   *   "LOAD" "1" "@title"
   *   "SORTBY" "2" "@price" "DESC"
   *   "LIMIT" "0" "2"
   * </pre>
   */
  @Aggregation( //
      load = @Load(property = "@title"),
      sortBy = @SortBy(field = "@price", direction = Direction.DESC),
      limit = 2
  ) //
  AggregationResult loadWithSort();

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*"
   *   "LOAD" "1" "@brand"
   *   "LOAD" "1" "@price"
   *   "LOAD" "1" "@__key"
   *   "SORTBY" "2" "@price" "DESC"
   *   "MAX" "4"
   * </pre>
   */
  @Aggregation( //
      load = { @Load(property = "@brand"), @Load(property = "@price"), @Load(property = "@__key") },
      sortBy = @SortBy(field = "@price", direction = Direction.DESC),
      sortByMax = 4
  ) //
  AggregationResult loadWithDocId();
}
