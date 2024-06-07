package com.redis.om.spring.id;

public interface IdentifierFilter<ID> {
  String filter(ID id);
}
