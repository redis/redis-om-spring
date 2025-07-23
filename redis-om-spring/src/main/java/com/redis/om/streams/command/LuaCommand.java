package com.redis.om.streams.command;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import lombok.Getter;

/**
 * Enum representing Lua script commands used for Redis Streams operations.
 * Each enum constant corresponds to a Lua script file that is loaded and executed
 * by the {@link LuaCommandRunner} to perform various operations on Redis Streams.
 */
public enum LuaCommand {

  /** Script to calculate the lag (number of unprocessed messages) for a consumer group. */
  LAG("lag.script"),

  /** Script to acknowledge a message has been processed by a consumer group. */
  ACK("xack.script"),

  /** Script to set the last delivered ID for a consumer group. */
  GROUP_SET_ID("xgroup_setid.script"),

  /**
   * Script to retrieve pending entries (messages that were delivered but not yet acknowledged) for a consumer group.
   */
  PENDING("xpending.script"),

  /** Script to read messages from a stream as part of a consumer group. */
  READGROUP("xreadgroup.script"),

  /** Script to read messages from a stream without requiring acknowledgment. */
  NOACK_READGROUP("xreadgroup_noack.script"),

  /** Script to read messages from a stream with single cluster pending entry list. */
  SINGLE_DB_PEL_READGROUP("xreadgroup_singleClusterPel.script"),

  /** Script to get the next stream in an active-active configuration. */
  NEXT_SERIAL_ACTIVE_ACTIVE_STREAM("serial_get_next_stream.script"),

  /** Script to advance a consumer group to the next stream. */
  SERIAL_ADVANCE_CONSUMER_STREAM("serial_advance_consumer.script"),

  /** Script to publish a message to a serial stream. */
  SERIAL_PUBLISH_MESSAGE("serial_publish_message.script"),;

  /** The content of the Lua script loaded from the script file. */
  @Getter
  public final String script;

  /** The filename of the Lua script. */
  @Getter
  public final String scriptFile;

  /**
   * Constructs a LuaCommand with the specified script file.
   *
   * @param scriptFile the name of the script file to load
   */
  LuaCommand(String scriptFile) {
    this.scriptFile = scriptFile;
    this.script = getScriptFromFile(scriptFile);
  }

  /**
   * Constructs the full path to the script file.
   *
   * @param script the name of the script file
   * @return the full path to the script file
   */
  private String getScriptFilename(String script) {
    return Paths.get("scripts", script).toString();
  }

  /**
   * Loads the content of the script file.
   *
   * @param scriptFile the name of the script file to load
   * @return the content of the script file as a string
   * @throws IllegalArgumentException if the script file cannot be read
   */
  private String getScriptFromFile(String scriptFile) {
    String code = null;
    try (InputStream stream = getClass().getClassLoader().getResourceAsStream(getScriptFilename(scriptFile))) {
      if (stream != null) {
        code = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("Error while reading the script file " + scriptFile, e);
    }
    return code;
  }
}
