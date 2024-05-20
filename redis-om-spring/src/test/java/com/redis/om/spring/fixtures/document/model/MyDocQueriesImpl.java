package com.redis.om.spring.fixtures.document.model;

import com.google.gson.Gson;
import com.redis.om.spring.fixtures.document.repository.MyDocQueries;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.util.SafeEncoder;

import java.util.Optional;

@SuppressWarnings({ "unused", "SpringJavaAutowiredMembersInspection" })
public class MyDocQueriesImpl implements MyDocQueries {

  @Autowired
  RedisModulesOperations<String> modulesOperations;

  @Autowired
  Gson gson;

  @Override
  public Optional<MyDoc> findByTitle(String title) {
    SearchOperations<String> ops = modulesOperations.opsForSearch(
      "com.redis.om.spring.annotations.document.fixtures.MyDocIdx");
    SearchResult result = ops.search(new Query("@title:'" + title + "'"));
    if (result.getTotalResults() > 0) {
      Document doc = result.getDocuments().get(0);
      return Optional.of(gson.fromJson(SafeEncoder.encode((byte[]) doc.get("$")), MyDoc.class));
    } else {
      return Optional.empty();
    }
  }

}
