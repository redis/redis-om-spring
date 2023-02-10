package com.redis.om.spring.search.stream.predicates.vector;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import com.redis.om.spring.util.ObjectUtils;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import redis.clients.jedis.search.querybuilder.GeoValue;
import redis.clients.jedis.search.querybuilder.Node;
import redis.clients.jedis.search.querybuilder.QueryBuilders;

public class KNNPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private final int k;
  private final byte[] blobAttribute;

  public KNNPredicate(SearchFieldAccessor field, int k, byte[] blobAttribute) {
    super(field);
    this.k = k;
    this.blobAttribute = blobAttribute;
  }

  public int getK() {
    return k;
  }

  public byte[] getBlobAttribute() {
    return blobAttribute;
  }

  public String getBlobAttributeName() {
    return String.format("%s_blob", getSearchAlias());
  }

  @Override
  public Node apply(Node root) {
    String query = String.format("(%s)=>[KNN $K @%s $%s]", root.toString().isBlank() ? "*" : root.toString(), getSearchAlias(), getBlobAttributeName());

    return new Node() {
      @Override
      public String toString() {
        return query;
      }

      @Override
      public String toString(Parenthesize mode) {
        return query;
      }
    };
  }

}
