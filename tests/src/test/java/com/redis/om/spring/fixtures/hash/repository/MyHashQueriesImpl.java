package com.redis.om.spring.fixtures.hash.repository;

import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.fixtures.hash.model.MyHash;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import com.redis.om.spring.util.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;

import java.util.Optional;

@SuppressWarnings({ "unused", "SpringJavaAutowiredMembersInspection" })
public class MyHashQueriesImpl implements MyHashQueries {

  private final MappingRedisOMConverter converter = new MappingRedisOMConverter();
  @Autowired
  RedisModulesOperations<String> modulesOperations;

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
