package com.redis.om.spring.metamodel;

import com.squareup.javapoet.FieldSpec;

import javax.lang.model.element.Element;
import java.util.List;

public record ObjectGraphFieldSpec(FieldSpec fieldSpec, List<Element> chain) {
}
