package com.redis.om.spring.annotations.document.fixtures;

import java.math.BigInteger;

import org.springframework.data.annotation.Id;

import com.redis.om.spring.annotations.Document;

import lombok.Data;

@Data
@Document
public class BadDoc {
  @Id
  private BigInteger id;
}
