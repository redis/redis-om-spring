package com.redis.om.spring.indexing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.GeoIndexed;
import com.redis.om.spring.annotations.NumericIndexed;
import com.redis.om.spring.annotations.TagIndexed;
import com.redis.om.spring.annotations.TextIndexed;
import com.redis.om.spring.ops.RedisModulesOperations;
import com.redis.om.spring.ops.search.SearchOperations;

import io.redisearch.FieldName;
import io.redisearch.Schema;
import io.redisearch.Schema.Field;
import io.redisearch.Schema.FieldType;
import io.redisearch.Schema.TagField;
import io.redisearch.Schema.TextField;
import io.redisearch.client.Client;
import io.redisearch.client.IndexDefinition;

public class IndexBuilder {

  private static final Log logger = LogFactory.getLog(IndexBuilder.class);

  private IndexBuilder() {}

  public static IndexBuilder INSTANCE = new IndexBuilder();

  public void scanForIndexDefinitions(String basePackage, RedisModulesOperations<String, String> rmo) {
    logger.info("Creating Indexes......");

    ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
    provider.addIncludeFilter(new AnnotationTypeFilter(Document.class));
    for (BeanDefinition beanDef : provider.findCandidateComponents(basePackage)) {
      try {
        Class<?> cl = Class.forName(beanDef.getBeanClassName());
        System.out.printf(">>>> Found @Document annotated class: %s\n", cl.getSimpleName());

        List<Field> fields = new ArrayList<Field>();
        for (java.lang.reflect.Field field : cl.getDeclaredFields()) {
          System.out.println(">>>> Inspecting field " + field.getName());
          // Text
          if (field.isAnnotationPresent(TextIndexed.class)) {
            System.out.println(">>>>>> FOUND TextIndexed on " + field.getName());
            Optional<TextField> maybeTextField = getRediSearchTextFieldFor(field);

            if (maybeTextField.isPresent()) {
              fields.add(maybeTextField.get());
            }
          }
          // Tag
          if (field.isAnnotationPresent(TagIndexed.class)) {
            System.out.println(">>>>>> FOUND TagIndexed on " + field.getName());
            Optional<TagField> maybeTagField = getRediSearchTagFieldFor(field);

            if (maybeTagField.isPresent()) {
              fields.add(maybeTagField.get());
            }
          }
          // Geo
          if (field.isAnnotationPresent(GeoIndexed.class)) {
            System.out.println(">>>>>> FOUND GeoIndexed on " + field.getName());
            Optional<Field> maybeGeoField = getRediSearchGeoFieldFor(field);

            if (maybeGeoField.isPresent()) {
              fields.add(maybeGeoField.get());
            }
          }
          // Numeric
          if (field.isAnnotationPresent(NumericIndexed.class)) {
            System.out.println(">>>>>> FOUND NumericIndexed on " + field.getName());
            Optional<Field> maybeNumbericField = getRediSearchNumericFieldFor(field);

            if (maybeNumbericField.isPresent()) {
              fields.add(maybeNumbericField.get());
            }
          }
        }

        if (!fields.isEmpty()) {
          Schema schema = new Schema();
          SearchOperations<String> opsForSearch = rmo.opsForSearch(cl.getSimpleName() + "Idx");
          for (Field field : fields) {
            schema.addField(field);
          }
          IndexDefinition def = new IndexDefinition(IndexDefinition.Type.JSON);
          opsForSearch.createIndex(schema, Client.IndexOptions.defaultOptions().setDefinition(def));
        }
      } catch (Exception e) {
        System.err.println(
            String.format("In scanForIndexDefinitions: Exception: %s ==> %s", e.getClass().getName(), e.getMessage()));
        e.printStackTrace();
      }
    }

  }

  /**
   * Allows full-text search queries against the value in this field.
   *
   * @param field
   * @return
   */
  private Optional<TextField> getRediSearchTextFieldFor(java.lang.reflect.Field field) {
    TextIndexed ti = field.getAnnotation(TextIndexed.class);
    if (ti != null) {
      FieldName fieldName = FieldName.of("$." + field.getName());
      if (!ObjectUtils.isEmpty(ti.alias())) {
        fieldName = fieldName.as(ti.alias());
      }
      String phonetic = ObjectUtils.isEmpty(ti.phonetic()) ? null : ti.phonetic();
      TextField tf = new TextField(fieldName, ti.weight(), ti.sortable(), ti.nostem(), ti.noindex(), phonetic);

      return Optional.of(tf);
    } else {
      return Optional.empty();
    }
  }

  /**
   * Allows exact-match queries, such as categories or primary keys, against the
   * value in this field
   *
   * @param field
   * @return
   */
  private Optional<TagField> getRediSearchTagFieldFor(java.lang.reflect.Field field) {
    TagIndexed ti = field.getAnnotation(TagIndexed.class);
    if (ti != null) {
      FieldName fieldName = FieldName.of("$." + field.getName());
      if (!ObjectUtils.isEmpty(ti.alias())) {
        fieldName = fieldName.as(ti.alias());
      }

      TagField tf = new TagField(fieldName, ti.separator(), false);

      return Optional.of(tf);
    } else {
      return Optional.empty();
    }
  }

  /**
   * Allows geographic range queries against the value in this field.
   *
   * @param field
   * @return
   */
  private Optional<Field> getRediSearchGeoFieldFor(java.lang.reflect.Field field) {
    GeoIndexed gi = field.getAnnotation(GeoIndexed.class);
    if (gi != null) {
      FieldName fieldName = FieldName.of("$." + field.getName() + "[*]");
      if (!ObjectUtils.isEmpty(gi.alias())) {
        fieldName = fieldName.as(gi.alias());
      }

      Field gf = new Field(fieldName, FieldType.Geo, false, gi.noindex());

      return Optional.of(gf);
    } else {
      return Optional.empty();
    }
  }

  /**
   * Allows numeric range queries against the value in this field
   *
   * @param field
   * @return
   */
  private Optional<Field> getRediSearchNumericFieldFor(java.lang.reflect.Field field) {
    NumericIndexed ni = field.getAnnotation(NumericIndexed.class);
    if (ni != null) {
      FieldName fieldName = FieldName.of("$." + field.getName() + "[*]");
      if (!ObjectUtils.isEmpty(ni.alias())) {
        fieldName = fieldName.as(ni.alias());
      }
      Field nf = new Field(fieldName, FieldType.Numeric, ni.sortable(), ni.noindex());

      return Optional.of(nf);
    } else {
      return Optional.empty();
    }
  }
}
