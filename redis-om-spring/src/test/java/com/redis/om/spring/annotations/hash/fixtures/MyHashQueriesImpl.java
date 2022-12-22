package com.redis.om.spring.annotations.hash.fixtures;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.util.ObjectUtils;

import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;

public class MyHashQueriesImpl implements MyHashQueries {

  @Autowired
  RedisModulesOperations<String> modulesOperations;

  private MappingRedisOMConverter converter = new MappingRedisOMConverter();

  @Override
  public Optional<MyHash> findByTitle(String title) {
    SearchOperations<String> ops = modulesOperations.opsForSearch(MyHash.class.getName() + "Idx");
    SearchResult result = ops.search(new Query("@title:'" + title + "'"));
    if (result.getTotalResults() > 0) {
      Document doc = result.getDocuments().get(0);
      return Optional.of(ObjectUtils.documentToEntity(doc, MyHash.class, converter));
    } else {
      return Optional.empty();
    }
  }

}
