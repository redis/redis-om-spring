package com.redislabs.spring.annotations.document.fixtures;


import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.redislabs.spring.ops.RedisModulesOperations;
import com.redislabs.spring.ops.search.SearchOperations;

import io.redisearch.Document;
import io.redisearch.Query;
import io.redisearch.SearchResult;

public class MyDocQueriesImpl implements MyDocQueries {
  
  @Autowired
  RedisModulesOperations<String, String> modulesOperations;
  
  private static final Gson gson = new Gson();

  @Override
  public Optional<MyDoc> findByTitle(String title) {
    System.out.println(">>>> In findByTitle... " + title);
    SearchOperations<String> ops = modulesOperations.opsForSearch("MyDocIdx");
    SearchResult result = ops.search(new Query("@title:'" + title + "'"));
    System.out.println(">>>> results ==> " + result.totalResults);
    if (result.totalResults > 0) {
      Document doc = result.docs.get(0);
      return Optional.of(gson.fromJson(doc.toString(), MyDoc.class));
    } else {
      return Optional.empty();
    }
  }

}
