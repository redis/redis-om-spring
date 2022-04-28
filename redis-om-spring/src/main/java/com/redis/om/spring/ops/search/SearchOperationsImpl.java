package com.redis.om.spring.ops.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.redis.om.spring.client.RedisModulesClient;
import com.redis.om.spring.ops.Command;

import io.redisearch.AggregationResult;
import io.redisearch.Client;
import io.redisearch.Document;
import io.redisearch.Query;
import io.redisearch.Schema;
import io.redisearch.Schema.Field;
import io.redisearch.SearchResult;
import io.redisearch.Suggestion;
import io.redisearch.aggregation.AggregationBuilder;
import io.redisearch.client.AddOptions;
import io.redisearch.client.Client.IndexOptions;
import io.redisearch.client.ConfigOption;
import io.redisearch.client.SuggestionOptions;
import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.SafeEncoder;

public class SearchOperationsImpl<K> implements SearchOperations<K> {

  Client client;
  K index;

  public SearchOperationsImpl(K index, RedisModulesClient client) {
    this.index = index;
    this.client = client.clientForSearch(index.toString());
  }

  @Override
  public boolean createIndex(Schema schema, IndexOptions options) {
    return client.createIndex(schema, options);
  }

  @Override
  public SearchResult search(Query q) {
    return client.search(q);
  }

  @Override
  public SearchResult[] searchBatch(Query... queries) {
    return client.searchBatch(queries);
  }

  @Override
  public SearchResult search(Query q, boolean decode) {
    return client.search(q, decode);
  }

  @Override
  public AggregationResult aggregate(AggregationBuilder q) {
    return client.aggregate(q);
  }

  @Override
  public boolean cursorDelete(long cursorId) {
    return client.cursorDelete(cursorId);
  }

  @Override
  public AggregationResult cursorRead(long cursorId, int count) {
    return client.cursorRead(cursorId, count);
  }

  @Override
  public String explain(Query q) {
    return client.explain(q);
  }

  @Override
  public boolean addDocument(Document doc, AddOptions options) {
    return client.addDocument(doc, options);
  }

  @Override
  public boolean addDocument(String docId, double score, Map<String, Object> fields, boolean noSave, boolean replace,
      byte[] payload) {
    return client.addDocument(docId, score, fields, noSave, replace, payload);
  }

  @Override
  public boolean addDocument(Document doc) {
    return client.addDocument(doc);
  }

  @Override
  public boolean[] addDocuments(Document... docs) {
    return client.addDocuments(docs);
  }

  @Override
  public boolean[] addDocuments(AddOptions options, Document... docs) {
    return client.addDocuments(options, docs);
  }

  @Override
  public boolean addDocument(String docId, double score, Map<String, Object> fields) {
    return client.addDocument(docId, score, fields);
  }

  @Override
  public boolean addDocument(String docId, Map<String, Object> fields) {
    return client.addDocument(docId, fields);
  }

  @Override
  public boolean replaceDocument(String docId, double score, Map<String, Object> fields) {
    return client.replaceDocument(docId, score, fields);
  }

  @Override
  public boolean replaceDocument(String docId, double score, Map<String, Object> fields, String filter) {
    return client.replaceDocument(docId, score, fields, filter);
  }

  @Override
  public boolean updateDocument(String docId, double score, Map<String, Object> fields) {
    return client.updateDocument(docId, score, fields);
  }

  @Override
  public boolean updateDocument(String docId, double score, Map<String, Object> fields, String filter) {
    return client.updateDocument(docId, score, fields, filter);
  }

  @Override
  public Map<String, Object> getInfo() {
    return client.getInfo();
  }

  @Override
  public boolean deleteDocument(String docId) {
    return client.deleteDocument(docId);
  }

  @Override
  public boolean[] deleteDocuments(boolean deleteDocuments, String... docIds) {
    return client.deleteDocuments(deleteDocuments, docIds);
  }

  @Override
  public boolean deleteDocument(String docId, boolean deleteDocument) {
    return client.deleteDocument(docId, deleteDocument);
  }

  @Override
  public Document getDocument(String docId) {
    return client.getDocument(docId);
  }

  @Override
  public Document getDocument(String docId, boolean decode) {
    return client.getDocument(docId, decode);
  }

  @Override
  public List<Document> getDocuments(String... docIds) {
    return client.getDocuments(docIds);
  }

  @Override
  public List<Document> getDocuments(boolean decode, String... docIds) {
    return client.getDocuments(decode, docIds);
  }

  @Override
  public boolean dropIndex() {
    return client.dropIndex();
  }

  @Override
  public boolean dropIndex(boolean missingOk) {
    return client.dropIndex(missingOk);
  }

  @Override
  public Long addSuggestion(Suggestion suggestion, boolean increment) {
    return client.addSuggestion(suggestion, increment);
  }

  @Override
  public List<Suggestion> getSuggestion(String prefix, SuggestionOptions suggestionOptions) {
    return client.getSuggestion(prefix, suggestionOptions);
  }

  @Override
  public Long deleteSuggestion(String entry) {
    return client.deleteSuggestion(entry);
  }

  @Override
  public Long getSuggestionLength() {
    return client.getSuggestionLength();
  }

  @Override
  public boolean alterIndex(Field... fields) {
    return client.alterIndex(fields);
  }

  @Override
  public boolean setConfig(ConfigOption option, String value) {
    return client.setConfig(option, value);
  }

  @Override
  public String getConfig(ConfigOption option) {
    return client.getConfig(option);
  }

  @Override
  public Map<String, String> getAllConfig() {
    return client.getAllConfig();
  }

  @Override
  public boolean addAlias(String name) {
    return client.addAlias(name);
  }

  @Override
  public boolean updateAlias(String name) {
    return client.updateAlias(name);
  }

  @Override
  public boolean deleteAlias(String name) {
    return client.deleteAlias(name);
  }

  @Override
  public boolean updateSynonym(String synonymGroupId, String... terms) {
    return client.updateSynonym(synonymGroupId, terms);
  }

  @Override
  public Map<String, List<String>> dumpSynonym() {
    return client.dumpSynonym();
  }

  @Override
  public List<String> tagVals(String field) {
    ArrayList<byte[]> args = new ArrayList<>();
    args.add(SafeEncoder.encode(index.toString()));
    args.add(SafeEncoder.encode(field));

    List<String> result = List.of();

    try (Jedis conn = client.connection()) {
      BinaryClient bc = conn.getClient();
      bc.sendCommand(Command.FT_TAGVALS, args.toArray(new byte[args.size()][]));
      List<Object> resp = bc.getObjectMultiBulkReply();

      result = resp.stream() //
          .map(x -> x instanceof Long ? String.valueOf(x) : SafeEncoder.encode((byte[]) x)) //
          .collect(Collectors.toList());
    }

    return result;

  }

}
