package com.redis.om.spring.indexing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations;

import com.redis.om.spring.annotations.Indexed;

@ExtendWith(
  MockitoExtension.class
)
class LexicographicIndexerTest {
  @Mock
  RedisTemplate<String, String> redisTemplate;

  @Mock
  RediSearchIndexer rediSearchIndexer;

  @Mock
  ZSetOperations<String, String> zSetOperations;

  @Mock
  Cursor<ZSetOperations.TypedTuple<String>> cursor;

  @Mock
  ZSetOperations.TypedTuple<String> tuple;

  private LexicographicIndexer lexicographicIndexer;

  @BeforeEach
  void setUp() {
    when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    lexicographicIndexer = new LexicographicIndexer(redisTemplate, rediSearchIndexer);
  }

  @Test
  void processEntityRemovesLexicographicIdByExactMemberWithoutFullRangeScan() {
    when(rediSearchIndexer.getLexicographicFields(LexicographicIdEntity.class)).thenReturn(Set.of("id"));

    LexicographicIdEntity entity = new LexicographicIdEntity("session-42");

    lexicographicIndexer.processEntity(entity, "session-42", false, "sessions:");

    verify(zSetOperations).remove("sessions:id:lex", "session-42#session-42");
    verify(zSetOperations).add("sessions:id:lex", "session-42#session-42", 0.0);
    verify(zSetOperations, never()).range(anyString(), anyLong(), anyLong());
    verify(zSetOperations, never()).scan(anyString(), any(ScanOptions.class));
  }

  @Test
  void processEntityUsesCursorScanForMutableLexicographicFieldWithoutFullRangeScan() {
    when(rediSearchIndexer.getLexicographicFields(MutableLexicographicEntity.class)).thenReturn(Set.of("sku"));
    when(zSetOperations.scan(eq("products:sku:lex"), any(ScanOptions.class))).thenReturn(cursor);
    when(cursor.hasNext()).thenReturn(true, false);
    when(cursor.next()).thenReturn(tuple);
    when(tuple.getValue()).thenReturn("old-sku#product-1");

    MutableLexicographicEntity entity = new MutableLexicographicEntity("product-1", "new-sku");

    lexicographicIndexer.processEntity(entity, "product-1", false, "products:");

    verify(zSetOperations).scan(eq("products:sku:lex"), any(ScanOptions.class));
    verify(zSetOperations).remove("products:sku:lex", "old-sku#product-1");
    verify(zSetOperations).add("products:sku:lex", "new-sku#product-1", 0.0);
    verify(zSetOperations, never()).range(anyString(), anyLong(), anyLong());
    verify(cursor).close();
  }

  @Test
  void processEntityDeletionRemovesLexicographicIdByExactMemberWithoutFullRangeScan() {
    when(rediSearchIndexer.getLexicographicFields(LexicographicIdEntity.class)).thenReturn(Set.of("id"));

    LexicographicIdEntity entity = new LexicographicIdEntity("session-42");

    lexicographicIndexer.processEntityDeletion(entity, "session-42", "sessions:");

    verify(zSetOperations).remove("sessions:id:lex", "session-42#session-42");
    verify(zSetOperations, never()).range(anyString(), anyLong(), anyLong());
    verify(zSetOperations, never()).scan(anyString(), any(ScanOptions.class));
  }

  private record LexicographicIdEntity(@Id @Indexed(
      lexicographic = true
  ) String id) {
  }

  private record MutableLexicographicEntity(@Id String id, @Indexed(
      lexicographic = true
  ) String sku) {
  }
}
