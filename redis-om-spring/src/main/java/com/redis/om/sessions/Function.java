/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public enum Function {
  touch_key,
  read_key,
  read_locally_cached_entry,
  reserve_structs;

  private static final String LIBRARY_FILE = "redisSessions.lua";

  private static String getLibraryFileLocation() {
    return Paths.get("functions", LIBRARY_FILE).toString();
  }

  public static String getFunctionFile() {
    try (InputStream stream = Function.class.getClassLoader().getResourceAsStream(getLibraryFileLocation())) {
      if (stream == null) {
        throw new IllegalArgumentException(String.format("Could not load %s from disk", getLibraryFileLocation()));
      }

      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalArgumentException(String.format("Error while reading the function file %s",
          getLibraryFileLocation()));
    }
  }
}
