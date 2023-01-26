package com.redis.om.spring.annotations.document.fixtures;

import com.google.gson.Gson;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;

import java.util.Optional;

@SuppressWarnings({ "unused", "SpringJavaAutowiredMembersInspection" })
public class MyDocQueriesImpl implements MyDocQueries {

  @Autowired
  RedisModulesOperations<String> modulesOperations;

  @Autowired
  Gson gson;

  @Override
  public Optional<MyDoc> findByTitle(String title) {
    SearchOperations<String> ops = modulesOperations
        .opsForSearch("com.redis.om.spring.annotations.document.fixtures.MyDocIdx");
    SearchResult result = ops.search(new Query("@title:'" + title + "'"));
    if (result.getTotalResults() > 0) {
      Document doc = result.getDocuments().get(0);
      return Optional.of(gson.fromJson(doc.get("$").toString(), MyDoc.class));
    } else {
      return Optional.empty();
    }
  }

}
