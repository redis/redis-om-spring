/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions.serializers;

import java.io.*;

import com.redis.om.sessions.Serializer;

public class JdkSerializer implements Serializer {

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> byte[] Serialize(T object) throws Exception {
    if (!(object instanceof Serializable)) {
      throw new IllegalArgumentException("Object must be serializable to serialize");
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
    ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
    objectOutputStream.writeObject(object);
    objectOutputStream.flush();
    return out.toByteArray();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> T Deserialize(byte[] redisObj) throws Exception {
    ByteArrayInputStream in = new ByteArrayInputStream(redisObj);
    ObjectInputStream objectInputStream = new ObjectInputStream(in);
    return (T) objectInputStream.readObject();
  }
}
