/*
 * Copyright (c) 2024. Redis Ltd.
 */

package com.redis.om.sessions;

import java.io.*;

public class RedisHashSerializer {
  private static byte[] serialize(Object object) throws Exception {
    if (!(object instanceof Serializable)) {
      throw new IllegalArgumentException("Object must be serializable to serialize");
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
    ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
    objectOutputStream.writeObject(object);
    objectOutputStream.flush();
    byte[] bytes = out.toByteArray();
    return bytes;
  }

  private static Object Deserialize(byte[] obj) throws Exception {
    ByteArrayInputStream in = new ByteArrayInputStream(obj);
    ObjectInputStream objectInputStream = new ObjectInputStream(in);
    return objectInputStream.readObject();
  }
}
