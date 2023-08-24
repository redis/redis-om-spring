package com.redis.om.spring.search.stream.predicates.vector;

import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.search.stream.predicates.BaseAbstractPredicate;
import redis.clients.jedis.search.querybuilder.Node;

public class KNNPredicate<E, T> extends BaseAbstractPredicate<E, T> {

  private final int k;
  private final byte[] blob;
  private final float[] floats;

  public KNNPredicate(SearchFieldAccessor field, int k, byte[] blob) {
    super(field);
    this.k = k;
    this.blob = blob;
    this.floats = null;
  }

  public KNNPredicate(SearchFieldAccessor field, int k, float[] floats) {
    super(field);
    this.k = k;
    this.blob = null;
    this.floats = floats;
  }

  public int getK() {
    return k;
  }

  public byte[] getBlobAttribute() {
    return blob;
  }

  public float[] getDoublesAttribute() {
    return floats;
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
