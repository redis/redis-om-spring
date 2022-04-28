package com.redis.om.spring.ops;

import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.util.SafeEncoder;

public enum Command implements ProtocolCommand {
  FT_TAGVALS("FT.TAGVALS"),
  JSON_NUMINCRBY("JSON.NUMINCRBY");

  private final byte[] raw;

  Command(String alt) {
      raw = SafeEncoder.encode(alt);
  }

  @Override
  public byte[] getRaw() {
      return raw;
  }
}
