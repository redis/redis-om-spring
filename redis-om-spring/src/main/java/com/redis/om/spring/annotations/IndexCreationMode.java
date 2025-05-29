package com.redis.om.spring.annotations;

/**
 * Enumeration of index creation modes for controlling how Redis search indexes are managed.
 */
public enum IndexCreationMode {
  /** Skip index creation if the index already exists */
  SKIP_IF_EXIST,
  /** Always skip index creation */
  SKIP_ALWAYS,
  /** Drop existing index and recreate it */
  DROP_AND_RECREATE
}
