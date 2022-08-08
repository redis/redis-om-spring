package com.redis.om.spring.annotations.hash.fixtures;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.repository.query.Param;

import com.redis.om.spring.annotations.Query;
import com.redis.om.spring.repository.RedisEnhancedRepository;

public interface MyHashRepository extends RedisEnhancedRepository<MyHash, String>, MyHashQueries {
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
  Iterable<MyHash> findByTitleAndTags(@Param("title") String title, @Param("tags") Set<String> tags);
  
  /**
   * <pre>
   * > FT.SEARCH idx @title:hel* SORTBY title ASC LIMIT 0 2
   * </pre>
   */
  Page<MyHash> findAllByTitleStartingWith(String title, Pageable pageable);
  
  /**
   * <pre>
   * > FT.SEARCH idx @title:hel* LIMIT 0 2
   * </pre>
   */
  @Query("@title:$prefix*")
  Page<MyHash> customFindAllByTitleStartingWith(@Param("prefix") String prefix, Pageable pageable);
  
  /**
   * <pre>
   * > FT.TAGVALS idx tags
   * </pre>
   */
  Iterable<String> getAllTag();
  
  Iterable<MyHash> findByTag(Set<String> tags);

  Iterable<MyHash> findByLocationNear(Point point, Distance distance); 
  
  Iterable<MyHash> findByLocation2Near(Point point, Distance distance); 
  
  Iterable<MyHash> findByaNumber(Integer anotherNumber);
}
