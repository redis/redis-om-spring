/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.session.*;

public class RedisSessionRepository implements SessionRepository<RedisSessionRepository.RedisSession>,
    FindByIndexNameSessionRepository<RedisSessionRepository.RedisSession> {

  public static final String DEFAULT_KEY_NAMESPACE = "spring:";
  private final SessionIdGenerator sessionIdGenerator = UuidSessionIdGenerator.getInstance();

  private final RedisSessionProvider sessionProvider;

  private final Duration maxInactiveInterval = Duration.ofSeconds(MapSession.DEFAULT_MAX_INACTIVE_INTERVAL_SECONDS);

  public RedisSessionRepository(RedisSessionProvider sessionProvider) {
    this.sessionProvider = sessionProvider;
  }

  @Override
  public RedisSession createSession() {
    MapSession mapSession = new MapSession(this.sessionIdGenerator);
    String sessionId = this.sessionIdGenerator.generate();
    mapSession.setMaxInactiveInterval(this.maxInactiveInterval);
    RedisSession session = new RedisSession(new HashMap<>(), sessionId, sessionProvider);
    session.save();
    return session;
  }

  @Override
  public void save(RedisSession session) {
    session.save();
  }

  @Override
  public RedisSession findById(String s) {
    com.redis.om.sessions.RedisSession session = this.sessionProvider.findSessionById(s);
    if (session == null) {
      return null;
    }

    return new RedisSession(session, sessionProvider);
  }

  @Override
  public void deleteById(String s) {
    this.sessionProvider.deleteSessionById(s);
  }

  @Override
  public Map<String, RedisSession> findByIndexNameAndIndexValue(String indexName, String indexValue) {
    Map<String, com.redis.om.sessions.RedisSession> sessions;
    try {
      sessions = this.sessionProvider.findSessionsByExactMatch(indexName, indexValue);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return sessions.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new RedisSession(e.getValue(),
        sessionProvider)));
  }

  @Override
  public Map<String, RedisSession> findByPrincipalName(String principalName) {
    return FindByIndexNameSessionRepository.super.findByPrincipalName(principalName);
  }

  public static final class RedisSession implements org.springframework.session.Session {
    private final com.redis.om.sessions.RedisSession internalSession;
    private final RedisSessionProvider provider;

    private RedisSession(Map<String, Object> sessionData, String sessionId, RedisSessionProvider provider) {
      this.provider = provider;
      this.internalSession = provider.createSession(sessionId, sessionData);
    }

    private RedisSession(com.redis.om.sessions.RedisSession internalSession, RedisSessionProvider provider) {
      this.provider = provider;
      this.internalSession = internalSession;
    }

    private void save() {
      this.internalSession.save();
    }

    @Override
    public String getId() {
      return this.internalSession.getId();
    }

    @Override
    public String changeSessionId() {
      return this.internalSession.changeSessionId();
    }

    @Override
    public <T> T getAttribute(String s) {
      Optional<T> opt = this.internalSession.getAttribute(s);
      return opt.orElse(null);
    }

    @Override
    public Set<String> getAttributeNames() {
      return this.internalSession.getAttributeNames();
    }

    @Override
    public void setAttribute(String s, Object o) {
      Long newSize = this.internalSession.setAttribute(s, o);
      provider.addSessionSize(newSize);

    }

    @Override
    public void removeAttribute(String s) {
      this.internalSession.removeAttribute(s);
    }

    @Override
    public Instant getCreationTime() {
      return this.internalSession.getCreationTime();
    }

    @Override
    public void setLastAccessedTime(Instant instant) {
      this.internalSession.setLastAccessedTime(instant);
    }

    @Override
    public Instant getLastAccessedTime() {
      return this.internalSession.getLastAccessedTime();
    }

    @Override
    public void setMaxInactiveInterval(Duration duration) {
      this.internalSession.setMaxInactiveInterval(duration);
    }

    @Override
    public Duration getMaxInactiveInterval() {
      return this.internalSession.getMaxInactiveInterval().orElse(null);
    }

    @Override
    public boolean isExpired() {
      return this.internalSession.isExpired();
    }
  }
}
