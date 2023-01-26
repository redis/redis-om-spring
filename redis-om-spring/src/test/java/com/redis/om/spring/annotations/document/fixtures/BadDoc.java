package com.redis.om.spring.annotations.document.fixtures;

import com.redis.om.spring.annotations.Document;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigInteger;

@Data
@Document
public class BadDoc {
  @Id
  private BigInteger id;
}
