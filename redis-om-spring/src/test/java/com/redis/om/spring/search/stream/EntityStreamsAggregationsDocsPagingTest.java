package com.redis.om.spring.search.stream;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.Game;
import com.redis.om.spring.annotations.document.fixtures.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import redis.clients.jedis.search.aggr.AggregationResult;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({ "SpellCheckingInspection" }) class EntityStreamsAggregationsDocsPagingTest extends AbstractBaseDocumentTest {
  @Autowired EntityStream entityStream;

  @Autowired GameRepository repository;

  @BeforeEach void beforeEach() throws IOException {
    // Load Sample Docs
    if (repository.count() == 0) {
      repository.bulkLoad("src/test/resources/data/games.json");
    }
  }

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*"
   *   "LOAD" "*"
   *   "LIMIT" "0" "100"
   * </pre>
   */
  @Test void testLoadAllWithEntityReturn() {
    List<Game> result = entityStream
        .of(Game.class) //
        .loadAll()
        .limit(100)
        .toList(Game.class);

    assertThat(result).hasSize(100);
  }

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*" "WITHCURSOR" "COUNT" "45" "MAXIDLE" "172800000" "LOAD" "*" "LIMIT" "0" "300"
   * "FT.CURSOR" "READ" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "17284697" "45"
   * "FT.CURSOR" "READ" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "17284697" "45"
   * "FT.CURSOR" "READ" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "17284697" "45"
   * "FT.CURSOR" "READ" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "17284697" "45"
   * "FT.CURSOR" "READ" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "17284697" "45"
   * "FT.CURSOR" "READ" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "17284697" "45"
   * </pre>
   */
  @Test void testBasicExplicitCursorSession() {
    int pageSize = 45;
    SearchStream<Game> searchStream = entityStream.of(Game.class);

    AggregationResult result = searchStream //
        .cursor(pageSize, Duration.ofSeconds(300))
        .loadAll()
        .limit(300)
        .aggregate();

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
    assertAll("page counts",
        () -> assertEquals(6, pageCounts.size()),
        () -> assertEquals(45, pageCounts.get(0)),
        () -> assertEquals(45, pageCounts.get(1)),
        () -> assertEquals(45, pageCounts.get(2)),
        () -> assertEquals(45, pageCounts.get(3)),
        () -> assertEquals(45, pageCounts.get(4)),
        () -> assertEquals(30, pageCounts.get(5))
    );
  }

  /**
   * <pre>
   * "FT.AGGREGATE" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "*" "WITHCURSOR" "COUNT" "45" "MAXIDLE" "172800000" "LOAD" "*" "LIMIT" "0" "300"
   * "FT.CURSOR" "READ" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "17284697" "45"
   * "FT.CURSOR" "READ" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "17284697" "45"
   * "FT.CURSOR" "READ" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "17284697" "45"
   * "FT.CURSOR" "READ" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "17284697" "45"
   * "FT.CURSOR" "READ" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "17284697" "45"
   * "FT.CURSOR" "READ" "com.redis.om.spring.annotations.document.fixtures.GameIdx" "17284697" "45"
   * </pre>
   */
  @Test void testManagedCursorSession() {
    int pageSize = 45;
    SearchStream<Game> searchStream = entityStream.of(Game.class);

    Slice<Game> page = searchStream //
        .loadAll()
        .limit(300)
        .toList(PageRequest.ofSize(pageSize), Game.class);

    // loop through the slices using the SearchStream.getSlice method passing the next page request
    // obtained from the current page
    Map<Integer, Integer> pageCounts = new HashMap<>();
    while (page.hasContent()) {
      List<Game> contents = page.getContent();
      // collect the page counts
      pageCounts.put(page.getNumber(), contents.size());
      page = page.hasNext() ? searchStream.getSlice(page.nextPageable()) : Page.empty();
    }

    // assert the page counts
    assertAll("page counts",
        () -> assertEquals(pageSize, pageCounts.get(0)),
        () -> assertEquals(pageSize, pageCounts.get(1)),
        () -> assertEquals(pageSize, pageCounts.get(2)),
        () -> assertEquals(pageSize, pageCounts.get(3)),
        () -> assertEquals(pageSize, pageCounts.get(4)),
        () -> assertEquals(pageSize, pageCounts.get(5)),
        () -> assertEquals(30, pageCounts.get(6))
    );
  }


}
