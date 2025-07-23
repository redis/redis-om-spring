/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import lombok.Getter;

public enum Script {
  readKey("readKey.lua"),
  reserveStructs("reserveStructs.lua");

  @Getter
  public final String scriptFile;
  @Getter
  public final String code;

  private Script(String scriptFile) {
    this.scriptFile = scriptFile;
    this.code = getScriptFromFile();
  }

  private static String getScriptName(String script) {
    return Paths.get("scripts", script).toString();
  }

  private String getScriptFromFile() {

    try (InputStream stream = getClass().getClassLoader().getResourceAsStream(getScriptName(scriptFile))) {
      if (stream == null) {
        throw new IllegalArgumentException(String.format("Could not load %s from disk", getScriptName(scriptFile)));
      }

      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new IllegalArgumentException(String.format("Error while reading the script file %s", getScriptName(
          scriptFile)), e);
    }
  }
}
