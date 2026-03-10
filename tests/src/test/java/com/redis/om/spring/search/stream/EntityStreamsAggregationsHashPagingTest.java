package com.redis.om.spring.search.stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.fixtures.hash.model.Game;
import com.redis.om.spring.fixtures.hash.repository.GameRepository;

import redis.clients.jedis.search.aggr.AggregationResult;

@SuppressWarnings(
  { "SpellCheckingInspection" }
)
class EntityStreamsAggregationsHashPagingTest extends AbstractBaseEnhancedRedisTest {
  @Autowired
  EntityStream entityStream;

  @Autowired
  GameRepository repository;

  @Autowired
  Gson gson;

  @BeforeEach
  void beforeEach() throws IOException {
    // Load Sample Docs
    if (repository.count() == 0) {
      try (Reader reader = Files.newBufferedReader(Paths.get("src/test/resources/data/games.json"))) {
        List<Game> entities = gson.fromJson(reader, new TypeToken<List<Game>>() {
        }.getType());
        repository.saveAll(entities);
      }
    }
  }

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*"
   * "LOAD" "*"
   * "LIMIT" "0" "100"
   * </pre>
   */
  @Test
  void testLoadAllWithEntityReturn() {
    List<Game> result = entityStream.of(Game.class) //
        .loadAll().limit(100).toList(Game.class);

    assertThat(result).hasSize(100);
  }

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*" "WITHCURSOR" "COUNT" "45" "MAXIDLE"
   * "172800000" "LOAD" "*" "LIMIT" "0" "300"
   * "FT.CURSOR" "READ" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "17284697" "45"
   * "FT.CURSOR" "READ" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "17284697" "45"
   * "FT.CURSOR" "READ" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "17284697" "45"
   * "FT.CURSOR" "READ" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "17284697" "45"
   * "FT.CURSOR" "READ" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "17284697" "45"
   * "FT.CURSOR" "READ" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "17284697" "45"
   * </pre>
   */
  @Test
  void testBasicExplicitCursorSession() {
    int pageSize = 45;
    SearchStream<Game> searchStream = entityStream.of(Game.class);

    AggregationResult result = searchStream //
        .cursor(pageSize, Duration.ofSeconds(300)).loadAll().limit(300).aggregate();

    // get the cursor id
    long cursorId = result.getCursorId();

    // read the rest of the results
    Map<Integer, Integer> pageCounts = new HashMap<>();
    AggregationResult ar;
    int index = 0;
    for (; 0 != cursorId; index++) {
      ar = searchStream.getSearchOperations().cursorRead(cursorId, pageSize);
      // collect the page counts
      pageCounts.put(index, ar.getResults().size());
      cursorId = ar.getCursorId();
    }

    // assert the page counts
    assertAll("page counts", () -> assertEquals(6, pageCounts.size()), () -> assertEquals(45, pageCounts.get(0)),
        () -> assertEquals(45, pageCounts.get(1)), () -> assertEquals(45, pageCounts.get(2)), () -> assertEquals(45,
            pageCounts.get(3)), () -> assertEquals(45, pageCounts.get(4)), () -> assertEquals(30, pageCounts.get(5)));
  }

  /**
   * Tests cursor iteration without an explicit .limit() on the aggregation pipeline.
   * Without .limit(), FT.AGGREGATE defaults to returning up to 10,000 results in the
   * pipeline, so the cursor iterates over min(totalDocs, 10000) results.
   * <p>
   * This verifies that cursor IDs are correctly reused by Redis across reads (issue #708)
   * and that all results are eventually returned when the cursor ID becomes 0.
   */
  @Test
  void testCursorSessionWithoutExplicitLimit() {
    int pageSize = 500;
    SearchStream<Game> searchStream = entityStream.of(Game.class);

    AggregationResult result = searchStream //
        .cursor(pageSize, Duration.ofSeconds(300)).loadAll().aggregate();

    long cursorId = result.getCursorId();
    int totalResults = result.getResults().size();

    // Read remaining pages
    while (cursorId != 0) {
      AggregationResult ar = searchStream.getSearchOperations().cursorRead(cursorId, pageSize);
      totalResults += ar.getResults().size();
      cursorId = ar.getCursorId();
    }

    // Without .limit(), we should get all 2265 games from the test dataset
    assertEquals(2265, totalResults);
  }

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*" "WITHCURSOR" "COUNT" "45" "MAXIDLE"
   * "300000" "LOAD" "*" "LIMIT" "0" "300"
   * "FT.CURSOR" "READ" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "17284697" "45"
   * "FT.CURSOR" "READ" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "17284697" "45"
   * "FT.CURSOR" "READ" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "17284697" "45"
   * "FT.CURSOR" "READ" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "17284697" "45"
   * "FT.CURSOR" "READ" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "17284697" "45"
   * "FT.CURSOR" "READ" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "17284697" "45"
   * </pre>
   */
  @Test
  void testManagedCursorSession() {
    int pageSize = 45;
    SearchStream<Game> searchStream = entityStream.of(Game.class);

    Page<Game> page = searchStream //
        .loadAll().limit(300).toList(PageRequest.ofSize(pageSize), Game.class);

    // loop through the slices using the SearchStream.getSlice method passing the next page request
    // obtained from the current page
    Map<Integer, Integer> pageCounts = new HashMap<>();
    while (page.hasContent()) {
      List<Game> contents = page.getContent();
      // collect the page counts
      pageCounts.put(page.getNumber(), contents.size());
      page = page.hasNext() ? searchStream.getPage(page.nextPageable()) : Page.empty();
    }

    // assert the page counts
    assertAll("page counts", () -> assertEquals(pageSize, pageCounts.get(0)), () -> assertEquals(pageSize, pageCounts
        .get(1)), () -> assertEquals(pageSize, pageCounts.get(2)), () -> assertEquals(pageSize, pageCounts.get(3)),
        () -> assertEquals(pageSize, pageCounts.get(4)), () -> assertEquals(pageSize, pageCounts.get(5)),
        () -> assertEquals(30, pageCounts.get(6)));
  }

}
