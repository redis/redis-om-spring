package com.redis.om.spring.metamodel;

import java.util.List;

import javax.lang.model.element.Element;

import com.squareup.javapoet.FieldSpec;

public record ObjectGraphFieldSpec(FieldSpec fieldSpec, List<Element> chain) {
}
