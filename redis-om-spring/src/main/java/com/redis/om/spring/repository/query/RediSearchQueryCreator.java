package com.redis.om.spring.repository.query;

import org.springframework.data.domain.Sort;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;

import java.util.Iterator;

/**
 * Building operations for criteria based queries
 *
 */
public class RediSearchQueryCreator extends AbstractQueryCreator<KeyValueQuery<RediSearchQuery>, RediSearchQuery> {

  public RediSearchQueryCreator(PartTree tree) {
    super(tree);
  }

  public RediSearchQueryCreator(PartTree tree, ParameterAccessor parameters) {
    super(tree, parameters);
  }

  @Override
  protected RediSearchQuery create(Part part, Iterator<Object> iterator) {
    return null;
  }

  @Override
  protected RediSearchQuery and(Part part, RediSearchQuery base, Iterator<Object> iterator) {
    return null;
  }

  @Override
  protected RediSearchQuery or(RediSearchQuery base, RediSearchQuery criteria) {
    return null;
  }

  @Override
  protected KeyValueQuery<RediSearchQuery> complete(RediSearchQuery criteria, Sort sort) {
    return null;
  }

}
