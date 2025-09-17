/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions;

import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CacheEntry<T extends Session> implements Comparable<CacheEntry<T>> {
  long score;
  T session;

  public CacheEntry(T session, long score) {
    this.score = score;
    this.session = session;
  }

  public long getSize() {
    return this.session.getSize();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int compareTo(CacheEntry<T> o) {
    int scoreComparison = Long.compare(this.score, o.score);
    if (scoreComparison == 0) {
      return this.session.getId().compareTo(o.session.getId());
    }
    return scoreComparison;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null || getClass() != obj.getClass())
      return false;
    CacheEntry<T> that = (CacheEntry<T>) obj;
    return session.getId().equals(that.session.getId());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return Objects.hash(session.getId());
  }
}
