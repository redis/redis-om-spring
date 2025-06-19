/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redis.lettucemod.RedisModulesClient;
import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import com.redis.lettucemod.api.async.RedisModulesAsyncCommands;
import com.redis.lettucemod.api.sync.RediSearchCommands;
import com.redis.lettucemod.cluster.RedisModulesClusterClient;
import com.redis.lettucemod.search.*;
import com.redis.om.sessions.filtering.Filter;
import com.redis.om.sessions.indexing.IndexedField;
import com.redis.om.sessions.indexing.RedisIndexConfiguration;

import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.FlushMode;
import io.lettuce.core.KeyValue;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import lombok.Getter;

@Getter
public class RedisSessionProvider implements SessionProvider<RedisSession> {
  private static final Logger logger = LoggerFactory.getLogger(RedisSessionProvider.class);
  private final StatefulRedisModulesConnection<String, String> connection;
  private final StatefulRedisModulesConnection<byte[], byte[]> rawConnection;
  private final Optional<String> appPrefix;
  private final ScheduledExecutorService scheduler;
  private final ConcurrentLinkedQueue<String> sessionsAccessed = new ConcurrentLinkedQueue<>();
  private final ConcurrentLinkedQueue<Double> sessionSizes = new ConcurrentLinkedQueue<>();
  private final ConcurrentLinkedQueue<String> createdSessions = new ConcurrentLinkedQueue<>();
  private final String topKKey;
  private final int topK;
  private final LocalCache<RedisSession> localCache;
  private final RedisIndexConfiguration redisIndexConfiguration;
  private final Serializer serializer;
  private final StatefulRedisPubSubConnection<String, String> pubsub;
  private final RedisSessionProviderConfiguration configuration;

  private RedisSessionProvider(RedisSessionProviderConfiguration configuration,
      StatefulRedisModulesConnection<String, String> connection,
      StatefulRedisModulesConnection<byte[], byte[]> rawConnection,
      StatefulRedisPubSubConnection<String, String> pubsub) {
    this.connection = connection;
    this.rawConnection = rawConnection;
    this.pubsub = pubsub;
    this.serializer = configuration.getSerializer();
    this.appPrefix = configuration.getAppPrefix();
    this.configuration = configuration;

    topK = 1000;
    topKKey = this.appPrefix.isPresent() ?
        String.format("%s:redisSessions:topAccessedSessions", this.appPrefix.get()) :
        "redisSessions:topAccessedSessions";
    scheduler = Executors.newScheduledThreadPool(5);
    int initialDelay = 5;
    int period = 5;
    scheduler.scheduleAtFixedRate(this::writeSessionSizes, initialDelay, period, TimeUnit.SECONDS);
    scheduler.scheduleAtFixedRate(this::writeSessionsAccessed, initialDelay, period, TimeUnit.SECONDS);
    scheduler.scheduleAtFixedRate(this::writeCreatedSessions, initialDelay, period, TimeUnit.SECONDS);
    this.localCache = new LocalCache<>(LocalCacheType.LRU, configuration.getLocalCacheMaxSize(), configuration
        .getMinLocalRecordSize());
    this.redisIndexConfiguration = configuration.getIndexConfiguration();

    this.pubsub.addListener(new LocalCacheInvalidator<>(localCache));
  }

  private void writeCreatedSessions() {
    try {
      String[] sessionsToAppendToHll = new String[this.createdSessions.size()];
      for (int i = 0; i < sessionsToAppendToHll.length; i++) {
        String nextEntry = this.createdSessions.poll();
        if (nextEntry == null) {
          break;
        }

        sessionsToAppendToHll[i] = nextEntry;
      }

      if (sessionsToAppendToHll.length > 0) {
        this.connection.sync().pfadd(uniqueSessionsHll(), sessionsToAppendToHll);
      }

    } catch (Exception e) {
      logger.error("Encountered error while appending new sessions to hll", e);
    }

  }

  private void writeSessionsAccessed() {
    try {
      String[] sessionsAccessed = new String[this.sessionsAccessed.size()];
      for (int i = 0; i < sessionsAccessed.length; i++) {
        String nextEntry = this.sessionsAccessed.poll();
        if (nextEntry == null) {
          break;
        }

        sessionsAccessed[i] = nextEntry;
      }

      if (sessionsAccessed.length > 0) {
        logger.debug("Adding {} sessions", sessionsAccessed.length);
        this.connection.sync().topKAdd(topKKey, sessionsAccessed);
      }
    } catch (Exception e) {
      logger.error("Error encountered adding session accesses", e);
    }
  }

  private void writeSessionSizes() {
    try {
      double[] entries = new double[this.sessionSizes.size()];
      for (int i = 0; i < entries.length; i++) {
        Double nextEntry = sessionSizes.poll();
        if (nextEntry == null) {
          break;
        }
        entries[i] = nextEntry;
      }

      if (entries.length > 0) {
        String result = this.connection.sync().tDigestAdd(tDigestKey(), entries);
        logger.debug("Result from tdigest.add was: {}", result);
      }
    } catch (Exception e) {
      logger.error("encountered error while writing session sizes");
    }
  }

  /**
   * Create a new RedisSessionProvider
   * 
   * @param client        The client that the Provider will use to connect to Redis
   * @param configuration The configuration for the provider
   * @return A new RedisSessionProvider
   */
  public static RedisSessionProvider create(AbstractRedisClient client,
      RedisSessionProviderConfiguration configuration) {
    StatefulRedisModulesConnection<String, String> connection;
    StatefulRedisPubSubConnection<String, String> pubSubConnection;
    StatefulRedisModulesConnection<byte[], byte[]> rawConnection;
    if (client instanceof RedisModulesClusterClient) {
      connection = ((RedisModulesClusterClient) client).connect();
      pubSubConnection = ((RedisModulesClusterClient) client).connectPubSub();
      rawConnection = ((RedisModulesClusterClient) client).connect(new ByteArrayCodec());
    } else {
      connection = ((RedisModulesClient) client).connect();
      pubSubConnection = ((RedisModulesClient) client).connectPubSub();
      rawConnection = ((RedisModulesClient) client).connect(new ByteArrayCodec());
    }

    return new RedisSessionProvider(configuration, connection, rawConnection, pubSubConnection);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RedisSession createSession(String sessionId, Map<String, Object> sessionData) {
    RedisSession session = RedisSession.create(sessionData, sessionId, true, this.connection, this.rawConnection,
        this.appPrefix, this.redisIndexConfiguration, this.serializer, this.configuration);

    Long size = session.save();
    this.createdSessions.add(sessionId);
    boolean locallyCached = this.localCache.addEntry(session);
    if (locallyCached) {
      subscribeToSessionUpdates(sessionId);
    }
    this.sessionSizes.add(size.doubleValue());
    return session;
  }

  private void logDuration(String operationName, long startTime) {
    long endTime = System.nanoTime();
    long duration = (endTime - startTime) / 1000000;
    logger.info("{} took {}ms", operationName, duration);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RedisSession findSessionById(String id) {
    String key = RedisSession.buildKeyName(this.getAppPrefix(), id);
    Optional<RedisSession> localSession = this.localCache.readEntry(id);

    if (localSession.isPresent()) {
      this.sessionsAccessed.add(id);
      this.localCache.addEntry(localSession.get());
      return localSession.get();
    }

    try {
      Map<byte[], byte[]> readResult = this.rawConnection.async().hgetall(key.getBytes(StandardCharsets.UTF_8)).get();
      if (readResult.size() < 2) {
        return null;
      }

      Map<String, Object> sessionData = readResultToMap(readResult);
      if (sessionData.isEmpty()) {
        throw new IllegalArgumentException("Session Data not found.");
      }

      this.sessionsAccessed.add(id);
      RedisSession session = new RedisSession(sessionData, id, false, this.connection, this.rawConnection,
          this.appPrefix, this.redisIndexConfiguration, serializer, this.configuration);
      boolean cached = this.localCache.addEntry(session);
      if (cached) {
        subscribeToSessionUpdates(id);
      }
      return session;
    } catch (Exception e) {
      throw new IllegalArgumentException("Could not look up session.", e);
    }
  }

  private void subscribeToSessionUpdates(String sessionId) {
    this.pubsub.async().subscribe(String.format(Constants.INVALIDATION_CHANNEL_FORMAT, sessionId));
  }

  Map<String, Object> readResultToMap(Map<byte[], byte[]> inputMap) throws Exception {
    Map<String, Object> sessionData = new HashMap<>();
    for (Map.Entry<byte[], byte[]> entry : inputMap.entrySet()) {
      byte[] fieldName = entry.getKey();
      byte[] fieldValueStr = entry.getValue();
      putResultFieldInMap(sessionData, fieldName, fieldValueStr);
    }

    return sessionData;
  }

  private void putResultFieldInMap(Map<String, Object> sessionData, byte[] fieldName, byte[] fieldValueStr)
      throws Exception {
    String fieldNameString = new String(fieldName, StandardCharsets.UTF_8);
    if (Constants.reservedFields.contains(fieldNameString)) {
      sessionData.put(new String(fieldName, StandardCharsets.UTF_8), Long.parseLong(new String(fieldValueStr,
          StandardCharsets.UTF_8)));
    } else if (this.redisIndexConfiguration.getFields().containsKey(fieldNameString)) {
      IndexedField indexedField = this.redisIndexConfiguration.getFields().get(fieldNameString);
      sessionData.put(new String(fieldName, StandardCharsets.UTF_8), indexedField.getConverter().parse(new String(
          fieldValueStr, StandardCharsets.UTF_8)));
    } else {

      sessionData.put(new String(fieldName, StandardCharsets.UTF_8), serializer.Deserialize(fieldValueStr));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteSessionById(String id) {
    this.connection.sync().unlink(RedisSession.buildKeyName(this.appPrefix, id));
    this.localCache.removeEntry(id, true);
    this.publishInvalidationMessage(id);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, RedisSession> findSessionsByExactMatch(String fieldName, String fieldValue) throws Exception {
    RediSearchCommands<byte[], byte[]> commands = this.rawConnection.sync();
    SearchResults<byte[], byte[]> results = commands.ftSearch(indexName().getBytes(StandardCharsets.UTF_8), String
        .format("@%s:{%s}", fieldName, fieldValue).getBytes(StandardCharsets.UTF_8));
    Map<String, RedisSession> sessions = new HashMap<>();
    for (Document<byte[], byte[]> doc : results) {
      RedisSession session = searchDocToSession(doc);
      boolean cached = this.localCache.addEntry(session);
      subscribeToSessionUpdates(session.getId());
      sessions.put(session.getId(), session);
    }

    return sessions;
  }

  RedisSession searchDocToSession(Document<byte[], byte[]> doc) throws Exception {
    Map<String, Object> sessionData = searchDocToSessionData(doc);
    String sessionId = new String(doc.getId(), StandardCharsets.UTF_8).split(keyPrefix())[1];
    return new RedisSession(sessionData, sessionId, false, this.connection, this.rawConnection, this.appPrefix,
        this.redisIndexConfiguration, serializer, this.configuration);
  }

  private Map<String, Object> searchDocToSessionData(Document<byte[], byte[]> doc) throws Exception {
    Map<String, Object> sessionData = new HashMap<>();

    for (Map.Entry<byte[], byte[]> entry : doc.entrySet()) {
      putResultFieldInMap(sessionData, entry.getKey(), entry.getValue());
    }

    return sessionData;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void bootstrap() {
    connection.sync().functionFlush(FlushMode.SYNC);
    String lua = Function.getFunctionFile();
    connection.sync().functionLoad(lua);

    CreateOptions<String, String> createOptions = CreateOptions.<String, String>builder().prefix(keyPrefix()).build();

    List<Field<String>> fields = redisIndexConfiguration.getFields().entrySet().stream().map(f -> f.getValue()
        .toLettuceModField()).collect(Collectors.toList());

    connection.sync().ftCreate(indexName(), createOptions, fields.toArray(Field[]::new));

    if (connection.sync().exists(tDigestKey()) != 1) {
      connection.sync().tDigestCreate(tDigestKey());
    }

    String[] evalKeys = { topKKey };
    connection.sync().fcall(Function.reserve_structs.name(), ScriptOutputType.BOOLEAN, evalKeys, String.valueOf(topK));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dropIndex(boolean dropAssociatedRecords) {
    try {
      if (dropAssociatedRecords) {
        connection.sync().ftDropindexDeleteDocs(indexName());
      } else {
        connection.sync().ftDropindex(indexName());
      }
    } catch (Exception e) {
      if (!e.getMessage().toLowerCase().contains("no such index") && !e.getMessage().toLowerCase().contains(
          "unknown index name")) {
        throw e;
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, Long> largestSessions(int topK) {
    String returnPath = "sessionSize";
    SearchResults<String, String> largestSessions = connection.sync().ftSearch(indexName(), "*", SearchOptions
        .<String, String>builder().sortBy(SearchOptions.SortBy.desc("sessionSize")).limit(0, topK).returnField(
            returnPath).build());
    return largestSessions.stream().filter(d -> d.containsKey(returnPath)).collect(Collectors.toMap(d -> d.getId()
        .split(keyPrefix())[1], d -> Long.parseLong(d.get(returnPath)), (x, y) -> y, LinkedHashMap::new));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<KeyValue<String, Long>> mostAccessedSessions() {
    return this.connection.sync().topKListWithScores(this.topKKey);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, RedisSession> findSessions(Filter filter, int limit) {
    SearchOptions<byte[], byte[]> searchOptions = SearchOptions.<byte[], byte[]>builder().limit(0, limit).build();
    return findSessions(searchOptions, filter, new HashMap<>());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, RedisSession> findSessions(Filter filter, String sortBy, boolean ascending, int limit) {
    SearchOptions.SortBy<byte[], byte[]> orderBy = ascending ?
        SearchOptions.SortBy.asc(sortBy.getBytes(StandardCharsets.UTF_8)) :
        SearchOptions.SortBy.desc(sortBy.getBytes(StandardCharsets.UTF_8));
    SearchOptions<byte[], byte[]> searchOptions = SearchOptions.<byte[], byte[]>builder().limit(0, limit).sortBy(
        orderBy).build();
    return findSessions(searchOptions, filter, new LinkedHashMap<>());
  }

  private Map<String, RedisSession> findSessions(SearchOptions<byte[], byte[]> searchOptions, Filter filter,
      Map<String, RedisSession> resultMap) {
    SearchResults<byte[], byte[]> results = rawConnection.sync().ftSearch(indexName().getBytes(StandardCharsets.UTF_8),
        filter.getQuery().getBytes(StandardCharsets.UTF_8), searchOptions);

    for (Document<byte[], byte[]> doc : results) {
      try {
        RedisSession session = searchDocToSession(doc);
        boolean cached = this.localCache.addEntry(session);
        if (cached) {
          subscribeToSessionUpdates(session.getId());
        }
        resultMap.put(session.getId(), session);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return resultMap;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<String> deleteSessions(Filter filter, int limit) {
    Map<String, RedisSession> sessions = findSessions(filter, limit);
    RedisModulesAsyncCommands<String, String> commands = connection.async();
    for (String session : sessions.keySet()) {
      commands.unlink(session);
    }

    connection.flushCommands();
    for (String session : sessions.keySet()) {
      localCache.removeEntry(session, true);
    }
    return sessions.keySet();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<String> updateSessions(Filter filter, int limit, String field, Object value) {
    Map<String, RedisSession> sessions = findSessions(filter, limit);
    for (Map.Entry<String, RedisSession> entry : sessions.entrySet()) {
      entry.getValue().setAttribute(field, value);
      boolean cached = localCache.addEntry(entry.getValue());
      if (cached) {
        subscribeToSessionUpdates(entry.getValue().getId());
      }

      publishInvalidationMessage(entry.getValue().getId());
    }

    return sessions.keySet();
  }

  private void publishInvalidationMessage(String sessionId) {
    this.pubsub.async().publish(String.format(Constants.INVALIDATION_CHANNEL_FORMAT, sessionId), sessionId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<Double> sessionSizeQuantiles(double[] quantiles) {
    return this.connection.sync().tDigestQuantile(tDigestKey(), quantiles);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Long uniqueSessions() {
    return this.connection.sync().pfcount(uniqueSessionsHll());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addSessionSize(Long sessionSize) {
    this.sessionSizes.add(sessionSize.doubleValue());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public LocalCacheStatistics getLocalCacheStatistics() {
    return localCache.getStats();
  }

  public String tDigestKey() {
    return this.appPrefix.isPresent() ?
        String.format("%s:redisSessions:sessionSizeTd", appPrefix.get()) :
        "redisSessions:sessionSizeTd";
  }

  private String keyPrefix() {
    return this.appPrefix.isPresent() ? String.format("%s:session:", this.appPrefix.get()) : "session:";
  }

  private String indexName() {
    return this.appPrefix.isPresent() ? String.format("%s:sessions-idx", this.appPrefix.get()) : "session-idx";
  }

  public String uniqueSessionsHll() {
    return this.appPrefix.isPresent() ?
        String.format("%s:redisSessions:uniqueSessionsHll", appPrefix.get()) :
        "redisSessions:uniqueSessionsHll";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() {
    this.connection.close();
    this.pubsub.close();
  }
}
