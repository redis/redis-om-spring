package com.redis.om.spring.ops.search;

import java.util.List;
import java.util.Map;

import io.redisearch.AggregationResult;
import io.redisearch.Document;
import io.redisearch.Query;
import io.redisearch.Schema;
import io.redisearch.Schema.Field;
import io.redisearch.SearchResult;
import io.redisearch.Suggestion;
import io.redisearch.aggregation.AggregationBuilder;
import io.redisearch.client.AddOptions;
import io.redisearch.client.ConfigOption;
import io.redisearch.client.SuggestionOptions;
import io.redisearch.client.Client.IndexOptions;

public interface SearchOperations<K> {

  boolean createIndex(Schema schema, IndexOptions options);
  SearchResult search(Query q);
  SearchResult[] searchBatch(Query... queries);
  SearchResult search(Query q, boolean decode);
  AggregationResult aggregate(AggregationBuilder q);   
  boolean cursorDelete(long cursorId);
  AggregationResult cursorRead(long cursorId, int count);
  String explain(Query q);
  boolean addDocument(Document doc, AddOptions options);
  boolean addDocument(String docId, double score, Map<String, Object> fields, boolean noSave, boolean replace, byte[] payload);
  boolean addDocument(Document doc);
  boolean[] addDocuments(Document... docs);
  boolean[] addDocuments(AddOptions options, Document... docs);
  boolean addDocument(String docId, double score, Map<String, Object> fields);
  boolean addDocument(String docId, Map<String, Object> fields);
  boolean replaceDocument(String docId, double score, Map<String, Object> fields);
  boolean replaceDocument(String docId, double score, Map<String, Object> fields, String filter);
  boolean updateDocument(String docId, double score, Map<String, Object> fields);
  boolean updateDocument(String docId, double score, Map<String, Object> fields, String filter);
  Map<String, Object> getInfo();
  boolean deleteDocument(String docId);
  boolean[] deleteDocuments(boolean deleteDocuments, String... docIds);
  boolean deleteDocument(String docId, boolean deleteDocument);
  Document getDocument(String docId);   
  Document getDocument(String docId, boolean decode);
  List<Document> getDocuments(String ...docIds);
  List<Document> getDocuments(boolean decode, String ...docIds);
  boolean dropIndex();
  boolean dropIndex(boolean missingOk);
  Long addSuggestion(Suggestion suggestion, boolean increment);
  List<Suggestion> getSuggestion(String prefix, SuggestionOptions suggestionOptions);
  Long deleteSuggestion(String entry);
  Long getSuggestionLength();
  boolean alterIndex(Field ...fields);
  boolean setConfig(ConfigOption option, String value);
  String getConfig(ConfigOption option);
  Map<String, String> getAllConfig();
  boolean addAlias(String name);
  boolean updateAlias(String name);
  boolean deleteAlias(String name);
  boolean updateSynonym(String synonymGroupId, String ...terms);
  Map<String, List<String>> dumpSynonym();
  List<String> tagVals(String value);
}
