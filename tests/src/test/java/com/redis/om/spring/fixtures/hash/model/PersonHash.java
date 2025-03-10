package com.redis.om.spring.fixtures.hash.model;

import com.redis.om.spring.annotations.Indexed;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.Objects;

@RedisHash("persons")
public class PersonHash {
  private @Id String id;
  private @Indexed String firstname;
  private String lastname;
  private @Indexed CityHash hometown;

  public PersonHash() {
  }

  public PersonHash(String firstname, String lastname) {
    this.firstname = firstname;
    this.lastname = lastname;
  }

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getFirstname() {
    return this.firstname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public String getLastname() {
    return this.lastname;
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  public CityHash getHometown() {
    return this.hometown;
  }

  public void setHometown(CityHash hometown) {
    this.hometown = hometown;
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }

    if (!(obj instanceof PersonHash that)) {
      return false;
    }

    return Objects.equals(this.getId(), that.getId()) && Objects.equals(this.getFirstname(),
        that.getFirstname()) && Objects.equals(this.getLastname(), that.getLastname()) && Objects.equals(
        this.getHometown(), that.getHometown());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getFirstname(), getLastname(), getHometown());
  }
}
