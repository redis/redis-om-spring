package com.redis.spring.repository.query;

import java.util.Iterator;

import org.springframework.data.domain.Sort;
import org.springframework.data.keyvalue.core.query.KeyValueQuery;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;

/**
 * Building operations for criteria based queries
 *
 */
public class RediSearchQueryCreator extends AbstractQueryCreator<KeyValueQuery<RediSearchQuery>, RediSearchQuery> {
  
  public RediSearchQueryCreator(PartTree tree) {
    super(tree);
    System.out.println(">>>> IN RediSearchQueryCreator: tree " + tree);
  }

  public RediSearchQueryCreator(PartTree tree, ParameterAccessor parameters) {
    super(tree, parameters);
    System.out.println(">>>> IN RediSearchQueryCreator: tree " + tree + ", parameters: " + parameters);
  }

  @Override
  protected RediSearchQuery create(Part part, Iterator<Object> iterator) {
    System.out.println(">>>> IN RediSearchQueryCreator#create: part: " + part + ", iterator: " + iterator);
    return null;
  }

  @Override
  protected RediSearchQuery and(Part part, RediSearchQuery base, Iterator<Object> iterator) {
    System.out.println(">>>> IN RediSearchQueryCreator#and: part: " + part + ", base: " + base + ", iterator: " + iterator);
    return null;
  }

  @Override
  protected RediSearchQuery or(RediSearchQuery base, RediSearchQuery criteria) {
    System.out.println(">>>> IN RediSearchQueryCreator#or: base: " + base + ", criteria: " + criteria);
    return null;
  }

  @Override
  protected KeyValueQuery<RediSearchQuery> complete(RediSearchQuery criteria, Sort sort) {
    System.out.println(">>>> IN RediSearchQueryCreator#complete: criteria: " + criteria + ", sort: " + sort);
    return null;
  }

}
