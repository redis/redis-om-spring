package com.redis.om.spring.search.stream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.ops.json.JSONOperations;
import com.redis.om.spring.tuple.Pair;
import com.redis.om.spring.tuple.Tuples;
import com.redis.om.spring.util.ObjectUtils;

import redis.clients.jedis.util.SafeEncoder;

/**
 * Package-private helper that converts raw Redis search/aggregate documents into entity
 * instances for {@link SearchStreamImpl}. All mutable query-state lives on
 * {@link SearchStreamImpl}; this class is stateless aside from its constructor
 * dependencies.
 *
 * @param <E> the entity type
 */
class SearchStreamDocumentMapper<E> {

  private static final Log logger = LogFactory.getLog(SearchStreamDocumentMapper.class);

  private final boolean isDocument;
  private final Class<E> entityClass;
  private final JSONOperations<String> json;
  private final GsonBuilder gsonBuilder;
  private final MappingRedisOMConverter mappingConverter;

  // Lazily initialised Gson (mirrors SearchStreamImpl.getGson())
  private Gson gson;

  SearchStreamDocumentMapper(boolean isDocument, Class<E> entityClass, JSONOperations<String> json,
      GsonBuilder gsonBuilder, MappingRedisOMConverter mappingConverter) {
    this.isDocument = isDocument;
    this.entityClass = entityClass;
    this.json = json;
    this.gsonBuilder = gsonBuilder;
    this.mappingConverter = mappingConverter;
  }

  /**
   * Converts a single {@link redis.clients.jedis.search.Document} to an entity instance,
   * handling both JSON document and Hash structures. Includes a null-safe JSON.GET fallback
   * for the case where the "$" projection is absent from the FT.SEARCH result.
   */
  @SuppressWarnings(
    "unchecked"
  )
  E documentToEntity(redis.clients.jedis.search.Document d) {
    E entity;
    if (isDocument) {
      Object rawJson = d.get("$");
      if (rawJson == null) {
        // "$" is absent from the FT.SEARCH result — this can happen on some Redis
        // versions / query shapes when the root JSON projection is not returned inline.
        // Fall back to a direct JSON.GET so the entity is never silently dropped.
        logger.debug("Document '" + d.getId() + "' has no '$' field in FT.SEARCH result; falling back to JSON.GET.");
        entity = json.get(d.getId(), entityClass);
        if (entity == null) {
          logger.warn("Document '" + d.getId() + "' not found via JSON.GET; skipping entity mapping for " + entityClass
              .getSimpleName() + ". The key may have been deleted or is not a JSON document.");
          return null;
        }
        return ObjectUtils.populateRedisKey(entity, d.getId());
      }
      String jsonStr = (rawJson instanceof byte[]) ? SafeEncoder.encode((byte[]) rawJson) : rawJson.toString();
      entity = getGson().fromJson(jsonStr, entityClass);
    } else {
      entity = (E) ObjectUtils.documentToObject(d, entityClass, mappingConverter);
    }
    return ObjectUtils.populateRedisKey(entity, d.getId());
  }

  /**
   * Converts a list of documents to entities, filtering out nulls.
   */
  List<E> documentsToEntities(List<redis.clients.jedis.search.Document> documents) {
    return documents.stream().map(this::documentToEntity).filter(Objects::nonNull).toList();
  }

  /**
   * Converts a list of documents to entity-score pairs, filtering out nulls.
   */
  List<Pair<E, Double>> documentsToEntityScorePairs(List<redis.clients.jedis.search.Document> documents) {
    return documents.stream().map(d -> {
      E entity = documentToEntity(d);
      return entity != null ? Tuples.of(entity, d.getScore()) : null;
    }).filter(Objects::nonNull).toList();
  }

  /**
   * Converts FT.HYBRID {@link redis.clients.jedis.search.Document} objects to entity instances.
   * FT.HYBRID documents contain String values (via ENCODED_OBJECT_MAP) rather than byte[]
   * like FT.SEARCH documents, so we extract properties as a Map and use mapToObject.
   */
  @SuppressWarnings(
    "unchecked"
  )
  List<E> hybridDocumentsToEntities(List<redis.clients.jedis.search.Document> documents) {
    if (isDocument) {
      return documents.stream().map(this::documentToEntity).filter(Objects::nonNull).toList();
    } else {
      return (List<E>) documents.stream().map(d -> {
        Map<String, Object> props = new HashMap<>();
        d.getProperties().forEach(p -> {
          if (p.getValue() != null) {
            props.put(p.getKey(), p.getValue());
          }
        });
        Object entity = ObjectUtils.mapToObject(props, entityClass, mappingConverter);
        return ObjectUtils.populateRedisKey(entity, d.getId());
      }).collect(java.util.stream.Collectors.toList());
    }
  }

  Gson getGson() {
    if (gson == null) {
      gson = gsonBuilder.create();
    }
    return gson;
  }
}
