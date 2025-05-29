package com.redis.om.spring.metamodel;

import java.util.List;

import javax.lang.model.element.Element;

import com.squareup.javapoet.FieldSpec;

/**
 * A record representing a field specification within an object graph during metamodel generation.
 * This class encapsulates both the JavaPoet field specification and the chain of elements
 * that represent the path from the root entity to this specific field.
 * 
 * <p>The ObjectGraphFieldSpec is used during annotation processing to track how fields
 * are nested within complex object graphs. The element chain provides the full path
 * context needed to generate proper field accessors and search paths in the metamodel.</p>
 * 
 * <p>For example, if we have {@code Person.address.street}, the chain would contain
 * the elements [Person, address, street], allowing the metamodel generator to create
 * the appropriate field paths for searching and indexing.</p>
 * 
 * @param fieldSpec the JavaPoet field specification containing field metadata and generation info
 * @param chain     the ordered list of elements representing the path from root entity to this field
 * @since 1.0
 * @see MetamodelGenerator
 * @see FieldSpec
 */
public record ObjectGraphFieldSpec(FieldSpec fieldSpec, List<Element> chain) {
}
