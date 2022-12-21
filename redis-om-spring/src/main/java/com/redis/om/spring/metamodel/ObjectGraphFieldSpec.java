package com.redis.om.spring.metamodel;

import com.squareup.javapoet.FieldSpec;

import javax.lang.model.element.Element;
import java.util.List;

public class ObjectGraphFieldSpec {
  private FieldSpec fieldSpec;
  private List<Element> chain;

  public ObjectGraphFieldSpec(FieldSpec fieldSpec, List<Element> chain) {
    this.fieldSpec = fieldSpec;
    this.chain = chain;
  }

  public FieldSpec getFieldSpec() {
    return fieldSpec;
  }

  public List<Element> getChain() {
    return chain;
  }
}
