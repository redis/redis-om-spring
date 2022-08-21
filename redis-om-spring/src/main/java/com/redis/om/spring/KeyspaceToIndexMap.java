package com.redis.om.spring;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class KeyspaceToIndexMap {
  private Map<String, Class<?>> keyspaceToEntityClass = new ConcurrentHashMap<>();
  private Map<Class<?>, String> entityClassToKeySpace = new ConcurrentHashMap<>();
  private List<Class<?>> indexedEntityClasses = new ArrayList<>();

  public Optional<String> getIndexName(String keyspace) {
    Class<?> entityClass = keyspaceToEntityClass.get(getKey(keyspace));
    if (entityClass != null) {
      return Optional.of(entityClass.getName() + "Idx");
    } else {
      return Optional.empty();
    }
  }
  
  public Optional<String> getIndexName(Class<?> entityClass) {
    if (entityClassToKeySpace.containsKey(entityClass)) {
      return Optional.of(entityClass.getName() + "Idx");
    } else {
      return Optional.empty();
    }
  }

  public void addKeySpaceMapping(String keyspace, Class<?> entityClass, boolean isIndexed) {
    String key = getKey(keyspace);
    keyspaceToEntityClass.put(key, entityClass);
    entityClassToKeySpace.put(entityClass, key);
    if (isIndexed) indexedEntityClasses.add(entityClass);
  }

  public Class<?> getEntityClassForKeyspace(String keyspace) {
    return keyspaceToEntityClass.get(getKey(keyspace));
  }

  public String getKeyspaceForEntityClass(Class<?> entityClass) {
    return entityClassToKeySpace.get(entityClass);
  }
  
  public boolean indexExistsFor(Class<?> entityClass) {
    return indexedEntityClasses.contains(entityClass);
  }

  private String getKey(String keyspace) {
    return keyspace.endsWith(":") ? keyspace : keyspace + ":";
  }
}
