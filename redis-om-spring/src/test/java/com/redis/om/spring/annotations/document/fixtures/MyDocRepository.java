package com.redis.om.spring.annotations.document.fixtures;

import java.util.Set;

import com.redis.om.spring.annotations.Load;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.repository.query.Param;

import com.redis.om.spring.annotations.Aggregation;
import com.redis.om.spring.annotations.Query;
import com.redis.om.spring.repository.RedisDocumentRepository;

import io.redisearch.AggregationResult;
import io.redisearch.SearchResult;

public interface MyDocRepository extends RedisDocumentRepository<MyDoc, String>, MyDocQueries {
  /**
   * <pre>
   * > FT.SEARCH idx * RETURN 3 $.tag[0] AS first_tag 1) (integer) 1 2) "doc1" 3)
   * 1) "first_tag" 
   * 2) "news"
   * </pre>
   */
  @Query(returnFields = { "$.tag[0]", "AS", "first_tag" })
  SearchResult getFirstTag();

  /**
   * <pre>
   * > FT.SEARCH idx '@title:hello @tag:{news}' 
   * 1) (integer) 1 
   * 2) "doc1" 
   * 3) 1) "$"
   *    2) "{\"title\":\"hello world\",\"tag\":[\"news\",\"article\"]}"
   * </pre>
   */
  @Query("@title:$title @tag:{$tags}")
  Iterable<MyDoc> findByTitleAndTags(@Param("title") String title, @Param("tags") Set<String> tags);

  /**
   * <pre>
   * > FT.AGGREGATE idx * LOAD 3 $.tag[1] AS tag2 
   * 1) (integer) 1 
   * 2) 1) "tag2" 
   *    2) "article"
   * </pre>
   */
  @Aggregation(load = { @Load(property = "$.tag[1]", alias = "tag2") })
  AggregationResult getSecondTagWithAggregation();
  
  /**
   * <pre>
   * > FT.SEARCH idx @title:hel* SORTBY title ASC LIMIT 0 2
   * </pre>
   */
  Page<MyDoc> findAllByTitleStartingWith(String title, Pageable pageable);
  
  /**
   * <pre>
   * > FT.SEARCH idx @title:hel* LIMIT 0 2
   * </pre>
   */
  @Query("@title:$prefix*")
  Page<MyDoc> customFindAllByTitleStartingWith(@Param("prefix") String prefix, Pageable pageable);
  
  /**
   * <pre>
   * > FT.SEARCH idx @title:pre* SORTBY title ASC LIMIT 1 12 RETURN 2 title aNumber
   * </pre>
   */
  @Query(value="@title:$prefix*", returnFields={"title", "aNumber"}, limit = 12, offset = 1, sortBy = "title")
  SearchResult customFindAllByTitleStartingWithReturnFieldsAndLimit(@Param("prefix") String prefix);
  
  /**
   * <pre>
   * > FT.TAGVALS idx tags
   * </pre>
   */
  Iterable<String> getAllTag();
  
  Iterable<MyDoc> findByTag(Set<String> tags);

  Iterable<MyDoc> findByLocationNear(Point point, Distance distance); 
  
  Iterable<MyDoc> findByLocation2Near(Point point, Distance distance); 
  
  Iterable<MyDoc> findByaNumber(Integer anotherNumber);
}
