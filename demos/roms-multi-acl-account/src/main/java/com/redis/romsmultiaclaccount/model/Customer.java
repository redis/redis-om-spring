package com.redis.romsmultiaclaccount.model;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Searchable;

@Document
public class Customer {
  @Id
  private String id;

  @Searchable
  private String name;

  @Searchable
  private String email;

  public Customer() {
  }

  public Customer(String id, String name, String email) {
    this.id = id;
    this.name = name;
    this.email = email;
  }

  public Customer(String name, String email) {
    this.name = name;
    this.email = email;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}