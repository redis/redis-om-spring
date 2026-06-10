package com.redis.om.documents.repro;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.redis.om.documents.domain.LexicographicIdRecord;
import com.redis.om.documents.domain.LexicographicIdRecord$;
import com.redis.om.documents.repositories.LexicographicIdRecordRepository;
import com.redis.om.spring.search.stream.EntityStream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "demo.lexicographic-repro.enabled", havingValue = "true"
)
public class LexicographicIdReproRunner implements CommandLineRunner {
  private static final String LEX_SET_KEY = "lex-repro:id:lex";

  private final LexicographicIdRecordRepository repository;
  private final EntityStream entityStream;
  private final RedisTemplate<String, String> redisTemplate;

  @Value(
      "${demo.lexicographic-repro.count:50000}"
  )
  private int count;

  @Value(
      "${demo.lexicographic-repro.batch-size:1000}"
  )
  private int batchSize;

  @Value(
      "${demo.lexicographic-repro.threshold:lex000000000}"
  )
  private String threshold;

  @Value(
      "${demo.lexicographic-repro.update-count:100}"
  )
  private int updateCount;

  @Override
  public void run(String... args) {
    log.info("LEXICOGRAPHIC REPRO: preparing {} records in batches of {}", count, batchSize);

    log.info("LEXICOGRAPHIC REPRO: clearing existing records and sorted set {}", LEX_SET_KEY);
    long cleanupStartNanos = System.nanoTime();
    redisTemplate.delete(LEX_SET_KEY);
    repository.deleteAll();
    log.info("LEXICOGRAPHIC REPRO: cleanup completed in {} ms", elapsedMs(cleanupStartNanos));

    long seedStartNanos = System.nanoTime();
    seedRecords();
    long seedElapsedMs = elapsedMs(seedStartNanos);

    Long lexSetSize = redisTemplate.opsForZSet().size(LEX_SET_KEY);
    long lexMemberCount = lexSetSize == null ? 0 : lexSetSize;
    log.info("LEXICOGRAPHIC REPRO: seeded {} records in {} ms", count, seedElapsedMs);
    log.info("LEXICOGRAPHIC REPRO: sorted set {} contains {} members", LEX_SET_KEY, lexMemberCount);

    demonstrateWriteSideCleanup(lexMemberCount);
    demonstrateMissingQueryFix(lexMemberCount);
  }

  private void demonstrateWriteSideCleanup(long lexMemberCount) {
    if (count <= 0 || updateCount <= 0) {
      log.info("WRITE-SIDE CLEANUP DEMO: skipped because count={} and update-count={}", count, updateCount);
      return;
    }

    int actualUpdateCount = Math.min(updateCount, count);
    long oldBranchMemberReads = lexMemberCount * actualUpdateCount;
    log.info("WRITE-SIDE CLEANUP DEMO: updating {} existing records whose lexicographic field is the @Id",
        actualUpdateCount);
    log.info(
        "WRITE-SIDE CLEANUP DEMO: older cleanup implementations used ZRANGE 0 -1 on {} once per update, "
            + "materializing about {} sorted-set members",
        LEX_SET_KEY, oldBranchMemberReads);

    long beforeMiB = usedHeapMiB();
    long startNanos = System.nanoTime();
    int step = Math.max(1, count / actualUpdateCount);
    for (int i = 0; i < actualUpdateCount; i++) {
      int recordIndex = Math.min(count - 1, i * step);
      repository.save(new LexicographicIdRecord(recordId(recordIndex), "updated-%02d".formatted(i % 10)));
    }
    long elapsedMs = elapsedMs(startNanos);
    long afterMiB = usedHeapMiB();
    Long afterSize = redisTemplate.opsForZSet().size(LEX_SET_KEY);

    log.info("WRITE-SIDE CLEANUP DEMO: update loop completed in {} ms", elapsedMs);
    log.info("WRITE-SIDE CLEANUP DEMO: heap before={} MiB after={} MiB delta={} MiB", beforeMiB, afterMiB, afterMiB
        - beforeMiB);
    log.info("WRITE-SIDE CLEANUP DEMO: sorted set size after updates is {}", afterSize);
  }

  private void demonstrateMissingQueryFix(long lexMemberCount) {
    long lexMatches = countLexMatchesGreaterThan(threshold);
    log.info("MISSING QUERY FIX DEMO: ID.gt({}) has {} lexicographic matches in a {} member sorted set", threshold,
        lexMatches, lexMemberCount);
    log.info(
        "MISSING QUERY FIX DEMO: the current stream predicate still calls rangeByLex(..., Limit.unlimited()) "
            + "before limit(1) is applied");

    long beforeMiB = usedHeapMiB();
    long startNanos = System.nanoTime();
    List<LexicographicIdRecord> firstMatch = entityStream.of(LexicographicIdRecord.class)
        .filter(LexicographicIdRecord$.ID.gt(threshold)) //
        .limit(1) //
        .collect(Collectors.toList());
    long elapsedMs = elapsedMs(startNanos);
    long afterMiB = usedHeapMiB();

    log.info("MISSING QUERY FIX DEMO: ID.gt({}) with limit(1) returned {} record(s) in {} ms", threshold, firstMatch
        .size(), elapsedMs);
    log.info("MISSING QUERY FIX DEMO: heap before={} MiB after={} MiB delta={} MiB", beforeMiB, afterMiB, afterMiB
        - beforeMiB);
  }

  private void seedRecords() {
    List<LexicographicIdRecord> batch = new ArrayList<>(batchSize);
    for (int i = 0; i < count; i++) {
      batch.add(new LexicographicIdRecord(recordId(i), "bucket-%02d".formatted(i % 10)));
      if (batch.size() == batchSize) {
        repository.saveAll(batch);
        batch.clear();
      }
    }
    if (!batch.isEmpty()) {
      repository.saveAll(batch);
    }
  }

  private String recordId(int index) {
    return "lex%09d".formatted(index);
  }

  private long countLexMatchesGreaterThan(String value) {
    Object result = redisTemplate.execute((RedisCallback<Object>) connection -> connection.execute("ZLEXCOUNT",
        LEX_SET_KEY.getBytes(UTF_8), ("(" + value + "\uffff").getBytes(UTF_8), "+".getBytes(UTF_8)));

    if (result instanceof Number number) {
      return number.longValue();
    }
    if (result instanceof byte[] bytes) {
      return Long.parseLong(new String(bytes, UTF_8));
    }
    return -1;
  }

  private long elapsedMs(long startNanos) {
    return (System.nanoTime() - startNanos) / 1_000_000;
  }

  private long usedHeapMiB() {
    Runtime runtime = Runtime.getRuntime();
    return (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
  }
}
