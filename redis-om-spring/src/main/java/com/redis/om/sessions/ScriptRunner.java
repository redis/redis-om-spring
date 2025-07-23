/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions;

import java.util.HashMap;
import java.util.Map;

import com.redis.lettucemod.api.StatefulRedisModulesConnection;

import io.lettuce.core.ScriptOutputType;

public class ScriptRunner {
  private final static String MISSING_SCRIPT_ERROR = "NOSCRIPT No matching script";
  public static final ScriptRunner INSTANCE = new ScriptRunner();
  public Map<Script, String> hashMapping;

  private ScriptRunner() {
    hashMapping = new HashMap<>();
  }

  <T, K, V> T run(StatefulRedisModulesConnection<K, V> connection, Script script, ScriptOutputType outputType, K[] keys,
      V... values) {
    if (!hashMapping.containsKey(script)) {
      return loadAndRun(connection, script, outputType, keys, values);
    }

    try {
      return connection.sync().evalsha(hashMapping.get(script), outputType, keys, values);
    } catch (Exception e) {
      if (e.getMessage().contains(MISSING_SCRIPT_ERROR)) {
        return loadAndRun(connection, script, outputType, keys, values);
      }

      throw e;
    }
  }

  private <T, K, V> T loadAndRun(StatefulRedisModulesConnection<K, V> connection, Script script,
      ScriptOutputType outputType, K[] keys, V... values) {
    hashMapping.put(script, connection.sync().scriptLoad(script.code));
    return connection.sync().evalsha(hashMapping.get(script), outputType, keys, values);
  }
}
