package com.redis.om.spring.indexing;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.core.TypeInformation;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.redis.om.spring.annotations.*;
import com.redis.om.spring.repository.query.QueryUtils;

import redis.clients.jedis.search.FieldName;
import redis.clients.jedis.search.schemafields.*;

/**
 * Stateless factory that converts annotated {@link java.lang.reflect.Field} descriptors into
 * the Jedis {@link SchemaField} subtypes required by RediSearch index definitions.
 *
 * <p>All methods are package-private; callers within the {@code indexing} package
 * (primarily {@link IndexDefinitionBuilder} and {@link RediSearchIndexer}) use this class
 * for the low-level annotation → field-type mapping, keeping that concern separate from
 * the higher-level schema assembly logic.
 */
class SchemaFieldFactory {

  SchemaFieldFactory() {
  }

  TagField indexAsTagFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix, TagIndexed ti) {
    FieldName fieldName = buildFieldName(field, prefix, isDocument, Optional.ofNullable(ti.alias()), Optional.empty());
    return getTagField(fieldName, ti.separator(), false, false, false, ti.withSuffixTrie());
  }

  VectorField indexAsVectorFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix, Indexed indexed) {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("TYPE", indexed.type().toString());
    attributes.put("DIM", indexed.dimension());
    attributes.put("DISTANCE_METRIC", indexed.distanceMetric());

    if (indexed.initialCapacity() > 0) {
      attributes.put("INITIAL_CAP", indexed.initialCapacity());
    }

    if (indexed.algorithm().equals(VectorField.VectorAlgorithm.FLAT) && (indexed.blockSize() > 0)) {
      attributes.put("BLOCK_SIZE", indexed.blockSize());
    }

    if (indexed.algorithm().equals(VectorField.VectorAlgorithm.HNSW)) {
      attributes.put("M", indexed.m());
      attributes.put("EF_CONSTRUCTION", indexed.efConstruction());
      if (indexed.efRuntime() != 10) {
        attributes.put("EF_RUNTIME", indexed.efRuntime());
      }
      if (indexed.epsilon() != 0.01) {
        attributes.put("EPSILON", indexed.epsilon());
      }
    }

    FieldName fieldName = buildFieldName(field, prefix, isDocument, Optional.ofNullable(indexed.alias()), Optional
        .empty());
    return new VectorField(fieldName, indexed.algorithm(), attributes);
  }

  VectorField indexAsVectorFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix,
      VectorIndexed vi) {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("TYPE", vi.type().toString());
    attributes.put("DIM", vi.dimension());
    attributes.put("DISTANCE_METRIC", vi.distanceMetric());

    if (vi.initialCapacity() > 0) {
      attributes.put("INITIAL_CAP", vi.initialCapacity());
    }

    if (vi.algorithm().equals(VectorField.VectorAlgorithm.FLAT) && (vi.blockSize() > 0)) {
      attributes.put("BLOCK_SIZE", vi.blockSize());
    }

    if (vi.algorithm().equals(VectorField.VectorAlgorithm.HNSW)) {
      attributes.put("M", vi.m());
      attributes.put("EF_CONSTRUCTION", vi.efConstruction());
      if (vi.efRuntime() != 10) {
        attributes.put("EF_RUNTIME", vi.efRuntime());
      }
      if (vi.epsilon() != 0.01) {
        attributes.put("EPSILON", vi.epsilon());
      }
    }

    FieldName fieldName = buildFieldName(field, prefix, isDocument, Optional.ofNullable(vi.alias()), Optional.empty());
    return new VectorField(fieldName, vi.algorithm(), attributes);
  }

  SchemaField indexAsTagFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix, boolean sortable,
      String separator, int arrayIndex, String annotationAlias) {
    return indexAsTagFieldFor(field, isDocument, prefix, sortable, separator, arrayIndex, annotationAlias, false,
        false, false);
  }

  SchemaField indexAsTagFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix, boolean sortable,
      String separator, int arrayIndex, String annotationAlias, boolean indexMissing, boolean indexEmpty) {
    return indexAsTagFieldFor(field, isDocument, prefix, sortable, separator, arrayIndex, annotationAlias, indexMissing,
        indexEmpty, false);
  }

  SchemaField indexAsTagFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix, boolean sortable,
      String separator, int arrayIndex, String annotationAlias, boolean indexMissing, boolean indexEmpty,
      boolean withSuffixTrie) {
    FieldName fieldName = buildFieldName(field, prefix, isDocument, Optional.ofNullable(annotationAlias), Optional.of(
        arrayIndex));
    return getTagField(fieldName, separator, sortable, indexMissing, indexEmpty, withSuffixTrie);
  }

  TextField indexAsTextFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix, TextIndexed ti) {
    var fieldName = buildFieldName(field, prefix, isDocument, Optional.ofNullable(ti.alias()), Optional.empty());
    String phonetic = ObjectUtils.isEmpty(ti.phonetic()) ? null : ti.phonetic();
    return getTextField(fieldName, ti.weight(), ti.sortable(), ti.nostem(), ti.noindex(), phonetic, ti.indexMissing(),
        ti.indexEmpty(), ti.withSuffixTrie());
  }

  TextField indexAsTextFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix, Searchable ti) {
    var fieldName = buildFieldName(field, prefix, isDocument, Optional.ofNullable(ti.alias()), Optional.empty());
    String phonetic = ObjectUtils.isEmpty(ti.phonetic()) ? null : ti.phonetic();
    return getTextField(fieldName, ti.weight(), ti.sortable(), ti.nostem(), ti.noindex(), phonetic, ti.indexMissing(),
        ti.indexEmpty(), ti.withSuffixTrie());
  }

  GeoField indexAsGeoFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix, GeoIndexed gi) {
    var fieldName = buildFieldName(field, prefix, isDocument, Optional.ofNullable(gi.alias()), Optional.empty());
    return GeoField.of(fieldName);
  }

  NumericField indexAsNumericFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix,
      NumericIndexed ni) {
    var fieldName = buildFieldName(field, prefix, isDocument, Optional.ofNullable(ni.alias()), Optional.empty());
    return NumericField.of(fieldName);
  }

  NumericField indexAsNumericFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix,
      boolean sortable, boolean noIndex, String annotationAlias) {
    return indexAsNumericFieldFor(field, isDocument, prefix, sortable, noIndex, annotationAlias, false, false);
  }

  NumericField indexAsNumericFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix,
      boolean sortable, boolean noIndex, String annotationAlias, boolean indexMissing, boolean indexEmpty) {
    var fieldName = buildFieldName(field, prefix, isDocument, Optional.ofNullable(annotationAlias), Optional.empty());
    NumericField num = NumericField.of(fieldName);
    if (sortable)
      num.sortable();
    if (noIndex)
      num.noIndex();
    if (indexMissing)
      num.indexMissing();
    // Note: NumericField doesn't support indexEmpty() in current Jedis version
    return num;
  }

  GeoField indexAsGeoFieldFor(java.lang.reflect.Field field, boolean isDocument, String prefix,
      String annotationAlias) {
    var fieldName = buildFieldName(field, prefix, isDocument, Optional.ofNullable(annotationAlias), Optional.empty());
    return GeoField.of(fieldName);
  }

  TagField getTagField(FieldName fieldName, String separator, boolean sortable) {
    return getTagField(fieldName, separator, sortable, false, false);
  }

  TagField getTagField(FieldName fieldName, String separator, boolean sortable, boolean indexMissing,
      boolean indexEmpty) {
    return getTagField(fieldName, separator, sortable, indexMissing, indexEmpty, false);
  }

  TagField getTagField(FieldName fieldName, String separator, boolean sortable, boolean indexMissing,
      boolean indexEmpty, boolean withSuffixTrie) {
    TagField tag = TagField.of(fieldName);
    if (separator != null) {
      if (separator.length() != 1) {
        throw new IllegalArgumentException("Separator '" + separator + "' is not of length 1.");
      }
      tag.separator(separator.charAt(0));
    }
    if (sortable)
      tag.sortable();
    if (indexMissing)
      tag.indexMissing();
    if (indexEmpty)
      tag.indexEmpty();
    if (withSuffixTrie)
      tag.withSuffixTrie();
    return tag;
  }

  TextField getTextField(FieldName fieldName, double weight, boolean sortable, boolean noStem, boolean noIndex,
      String phonetic, boolean indexMissing, boolean indexEmpty) {
    return getTextField(fieldName, weight, sortable, noStem, noIndex, phonetic, indexMissing, indexEmpty, false);
  }

  TextField getTextField(FieldName fieldName, double weight, boolean sortable, boolean noStem, boolean noIndex,
      String phonetic, boolean indexMissing, boolean indexEmpty, boolean withSuffixTrie) {
    TextField text = TextField.of(fieldName);
    text.weight(weight);
    if (sortable)
      text.sortable();
    if (noStem)
      text.noStem();
    if (noIndex)
      text.noIndex();
    if (phonetic != null)
      text.phonetic(phonetic);
    if (indexMissing)
      text.indexMissing();
    if (indexEmpty)
      text.indexEmpty();
    if (withSuffixTrie)
      text.withSuffixTrie();
    return text;
  }

  String getFieldPrefix(String prefix, boolean isDocument) {
    String chain = (prefix == null || prefix.isBlank()) ? "" : prefix + ".";
    return isDocument ? "$." + chain : chain;
  }

  String getJsonFieldName(java.lang.reflect.Field field) {
    if (field.isAnnotationPresent(com.fasterxml.jackson.annotation.JsonProperty.class)) {
      com.fasterxml.jackson.annotation.JsonProperty jsonProperty = field.getAnnotation(
          com.fasterxml.jackson.annotation.JsonProperty.class);
      if (jsonProperty.value() != null && !jsonProperty.value().isEmpty()) {
        return jsonProperty.value();
      }
    }
    if (field.isAnnotationPresent(com.google.gson.annotations.SerializedName.class)) {
      com.google.gson.annotations.SerializedName serializedName = field.getAnnotation(
          com.google.gson.annotations.SerializedName.class);
      if (serializedName.value() != null && !serializedName.value().isEmpty()) {
        return serializedName.value();
      }
    }
    return field.getName();
  }

  FieldName buildFieldName(java.lang.reflect.Field field, String prefix, boolean isDocument,
      Optional<String> maybeAlias, Optional<Integer> maybeArrayIndex) {
    SerializedName serializedName = field.getAnnotation(SerializedName.class);
    Indexed indexed = field.getAnnotation(Indexed.class);
    String fname = (serializedName != null) ? serializedName.value() : field.getName();

    TypeInformation<?> typeInfo = TypeInformation.of(field.getType());
    String fieldPrefix = getFieldPrefix(prefix, isDocument);

    String index = maybeArrayIndex.isPresent() && (maybeArrayIndex.get() != Integer.MIN_VALUE) ?
        ".[" + maybeArrayIndex.get() + "]" :
        "[*]";

    boolean needsPostfix = (isDocument && typeInfo.isCollectionLike() && !field.isAnnotationPresent(
        JsonAdapter.class) && (indexed != null && !indexed.schemaFieldType().equals(SchemaFieldType.VECTOR)));
    String fieldPostfix = needsPostfix ? index : "";

    String name = fieldPrefix + fname + fieldPostfix;

    String alias = maybeAlias.isEmpty() || maybeAlias.get().isBlank() ?
        QueryUtils.searchIndexFieldAliasFor(field, prefix) :
        maybeAlias.get();

    return FieldName.of(name).as(alias);
  }
}
