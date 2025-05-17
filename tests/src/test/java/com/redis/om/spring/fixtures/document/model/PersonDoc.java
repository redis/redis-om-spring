package com.redis.om.spring.fixtures.document.model;

import java.util.Objects;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

@Document(
  "dpersons"
)
public class PersonDoc {
  private @Id String id;
  private @Indexed String firstname;
  private String lastname;
  private @Indexed CityDoc hometown;

  public PersonDoc() {
  }

  public PersonDoc(String firstname, String lastname) {
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

  public CityDoc getHometown() {
    return this.hometown;
  }

  public void setHometown(CityDoc hometown) {
    this.hometown = hometown;
  }

  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }

    if (!(obj instanceof PersonDoc that)) {
      return false;
    }

    return Objects.equals(this.getId(), that.getId()) && Objects.equals(this.getFirstname(), that
        .getFirstname()) && Objects.equals(this.getLastname(), that.getLastname()) && Objects.equals(this.getHometown(),
            that.getHometown());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getFirstname(), getLastname(), getHometown());
  }
}
