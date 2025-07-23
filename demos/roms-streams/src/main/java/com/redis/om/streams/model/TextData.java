package com.redis.om.streams.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(
    staticName = "of"
)
public class TextData {
  private int id;
  private String name;
  private String description;
}
