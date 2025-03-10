package com.redis.om.spring.fixtures.document.model;

import com.redis.om.spring.annotations.Document;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

@Document
public class VersionedEntity {

  private final String name;
  @Id
  private long id;
  @Version
  private long version;

  public VersionedEntity(long id) {
    this(id, 0, null);
  }

  public VersionedEntity(long id, long version, String name) {
    this.id = id;
    this.version = version;
    this.name = name;
  }

  public long getId() {
    return this.id;
  }

  public long getVersion() {
    return this.version;
  }

  public String getName() {
    return this.name;
  }

  public VersionedEntity withId(long id) {
    return new VersionedEntity(id, this.version, this.name);
  }

  public VersionedEntity withVersion(long version) {
    return new VersionedEntity(this.id, version, this.name);
  }

  public VersionedEntity withName(String name) {
    return new VersionedEntity(this.id, this.version, name);
  }
}
