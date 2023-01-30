package com.redis.om.spring.annotations.hash.fixtures;

import com.redis.om.spring.annotations.Aggregation;
import com.redis.om.spring.annotations.Apply;
import com.redis.om.spring.annotations.Load;
import com.redis.om.spring.annotations.Query;
import com.redis.om.spring.autocomplete.Suggestion;
import com.redis.om.spring.repository.RedisEnhancedRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.search.aggr.AggregationResult;

import java.util.List;
import java.util.Set;

@SuppressWarnings({ "unused", "SpellCheckingInspection", "SpringDataMethodInconsistencyInspection" }) @Repository
public interface PersonRepository extends RedisEnhancedRepository<Person, String>, EmailTaken {
  boolean existsByEmail(String email);

  boolean existsByNickname(String nickname);

  List<Suggestion> autoCompleteEmail(String string);

  // find by tag field, using RediSearch "native" annotation
  @Query("@roles:{$roles}")
  Iterable<Person> withRoles(@Param("roles") Set<String> roles);

  Iterable<Person> findByRoles(Set<String> roles);

  Iterable<Person> findByRolesContainingAll(Set<String> roles);

  // FT.AGGREGATE
  // com.redis.om.spring.annotations.hash.fixtures.PersonIdx "*"
  // LOAD 1 name
  // APPLY upper(@name) AS upcasedName
  @Aggregation(load = { @Load(property = "name") }, apply = { @Apply(expression = "upper(@name)", alias = "upcasedName") })
  AggregationResult allNamesInUppercase();
}
