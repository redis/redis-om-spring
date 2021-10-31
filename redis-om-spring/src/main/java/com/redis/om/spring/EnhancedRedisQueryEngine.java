package com.redis.om.spring;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.keyvalue.core.CriteriaAccessor;
import org.springframework.data.keyvalue.core.QueryEngine;
import org.springframework.data.keyvalue.core.SortAccessor;
import org.springframework.data.keyvalue.core.SpelSortAccessor;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.repository.query.RedisOperationChain;
import org.springframework.expression.spel.standard.SpelExpressionParser;

public class EnhancedRedisQueryEngine extends QueryEngine<RedisKeyValueAdapter, RedisOperationChain, Comparator<?>> {
  
  public EnhancedRedisQueryEngine() {
    this(new RedisCriteriaAccessor(), new SpelSortAccessor(new SpelExpressionParser()));
    System.out.println(">>>> EnhancedRedisQueryEngine#new... 1");
  }

  public EnhancedRedisQueryEngine(CriteriaAccessor<RedisOperationChain> criteriaAccessor,
      SortAccessor<Comparator<?>> sortAccessor) {
    super(criteriaAccessor, sortAccessor);
    System.out.println(">>>> EnhancedRedisQueryEngine#new... 2");
  }
  
  /*
   * (non-Javadoc)
   * @see org.springframework.data.keyvalue.core.QueryEngine#execute(java.lang.Object, java.lang.Object, int, int, java.lang.String, java.lang.Class)
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T> Collection<T> execute(RedisOperationChain criteria, Comparator<?> sort, long offset, int rows,
      String keyspace, Class<T> type) {
    System.out.println(">>>> EnhancedRedisQueryEngine#execute ==> " + criteria);
    List<T> result = List.of();
        //doFind(criteria, offset, rows, keyspace, type);

    if (sort != null) {
      result.sort((Comparator<? super T>) sort);
    }

    return result;
  }

  @Override
  public Collection<?> execute(RedisOperationChain criteria, Comparator<?> sort, long offset, int rows,
      String keyspace) {
    return execute(criteria, sort, offset, rows, keyspace, Object.class);
  }

  @Override
  public long count(RedisOperationChain criteria, String keyspace) {
    if (criteria == null || criteria.isEmpty()) {
      return this.getAdapter().count(keyspace);
    }
    
    return 0;
  }
  
  static class RedisCriteriaAccessor implements CriteriaAccessor<RedisOperationChain> {

    @Override
    public RedisOperationChain resolve(KeyValueQuery<?> query) {
      return (RedisOperationChain) query.getCriteria();
    }
  }

}
