package com.redis.om.spring.indexing;

import static com.redis.om.spring.util.ObjectUtils.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.geo.Point;
import org.springframework.util.ClassUtils;

import com.github.f4b6a3.ulid.Ulid;
import com.google.gson.GsonBuilder;
import com.redis.om.spring.annotations.*;
import com.redis.om.spring.id.IdFilter;
import com.redis.om.spring.id.IdentifierFilter;
import com.redis.om.spring.repository.query.QueryUtils;
import com.redis.om.spring.serialization.gson.EnumTypeAdapter;
import com.redis.om.spring.tuple.Pair;
import com.redis.om.spring.tuple.Tuples;

import redis.clients.jedis.search.FieldName;
import redis.clients.jedis.search.schemafields.*;

/**
 * Assembles the full list of {@link SearchField}s needed to define a RediSearch index for a
 * given entity class.
 *
 * <p>This class handles the field-routing logic: inspecting each entity field's annotations,
 * recursing into nested/reference types, and delegating to {@link SchemaFieldFactory} for
 * the low-level annotation → Jedis {@link SchemaField} conversion.
 *
 * <p>Package-private — used exclusively by {@link RediSearchIndexer}.
 */
class IndexDefinitionBuilder {

  private static final Log logger = LogFactory.getLog(IndexDefinitionBuilder.class);

  private final SchemaFieldFactory factory;
  private final GsonBuilder gsonBuilder;
  private final Consumer<Pair<Class<?>, IdentifierFilter<?>>> identifierFilterRegistry;
  private final BiConsumer<Class<?>, String> lexicographicFieldRegistry;

  IndexDefinitionBuilder(SchemaFieldFactory factory, GsonBuilder gsonBuilder,
      Consumer<Pair<Class<?>, IdentifierFilter<?>>> identifierFilterRegistry,
      BiConsumer<Class<?>, String> lexicographicFieldRegistry) {
    this.factory = factory;
    this.gsonBuilder = gsonBuilder;
    this.identifierFilterRegistry = identifierFilterRegistry;
    this.lexicographicFieldRegistry = lexicographicFieldRegistry;
  }

  /**
   * Builds the complete list of {@link SearchField}s for an entity class, including fields
   * derived from the entity's {@code @Id} field(s). As a side effect, any {@link IdFilter}
   * found on an ID field is registered via the {@code identifierFilterRegistry} supplied at
   * construction time.
   */
  List<SearchField> buildSearchFields(Class<?> entityClass, boolean isDocument,
      List<java.lang.reflect.Field> allClassFields) {
    List<SearchField> searchFields = processIndexedFields(allClassFields, isDocument);
    createIndexedFieldsForIdFields(entityClass, searchFields.stream().map(SearchField::getSchemaField).toList(),
        isDocument).forEach(searchFields::add);
    return searchFields;
  }

  Optional<String> getDocumentScoreField(List<java.lang.reflect.Field> allClassFields, boolean isDocument) {
    return allClassFields.stream().filter(field -> field.isAnnotationPresent(DocumentScore.class)).findFirst().map(
        field -> (isDocument ? "$." : "") + field.getName());
  }

  // ---------------------------------------------------------------------------
  // Field processing pipeline
  // ---------------------------------------------------------------------------

  private List<SearchField> processIndexedFields(List<java.lang.reflect.Field> allClassFields, boolean isDocument) {
    List<SearchField> fields = new ArrayList<>();
    for (java.lang.reflect.Field field : allClassFields) {
      fields.addAll(findIndexFields(field, null, isDocument));
    }
    return fields;
  }

  List<SearchField> findIndexFields(java.lang.reflect.Field field, String prefix, boolean isDocument) {
    List<SearchField> fields = new ArrayList<>();

    if (field.isAnnotationPresent(Indexed.class)) {
      logger.info(String.format("Found @Indexed annotation on field of type: %s", field.getType()));

      Indexed indexed = field.getAnnotation(Indexed.class);

      if (indexed.lexicographic()) {
        Class<?> entityClass = field.getDeclaringClass();
        lexicographicFieldRegistry.accept(entityClass, field.getName());
        logger.info(String.format("Tracked lexicographic field %s on class %s", field.getName(), entityClass
            .getName()));
      }

      Class<?> fieldType = ClassUtils.resolvePrimitiveIfNecessary(field.getType());

      if (field.isAnnotationPresent(Reference.class)) {
        logger.debug("🪲Found @Reference field " + field.getName() + " in " + field.getDeclaringClass()
            .getSimpleName());
        createIndexedFieldForReferenceIdField(field, isDocument).ifPresent(fields::add);
        createIndexedFieldsForReferencedEntity(field, isDocument, prefix).forEach(fields::add);
      } else if (indexed.schemaFieldType() == SchemaFieldType.AUTODETECT) {
        if (CharSequence.class.isAssignableFrom(fieldType) || //
            (fieldType == Boolean.class) || (fieldType == UUID.class) || (fieldType == Ulid.class)) {
          fields.add(SearchField.of(field, factory.indexAsTagFieldFor(field, isDocument, prefix, indexed.sortable(),
              indexed.separator(), indexed.arrayIndex(), indexed.alias(), indexed.indexMissing(), indexed.indexEmpty(),
              indexed.withSuffixTrie())));
        } else if (fieldType.isEnum()) {
          if (Objects.requireNonNull(indexed.serializationHint()) == SerializationHint.ORDINAL) {
            fields.add(SearchField.of(field, factory.indexAsNumericFieldFor(field, isDocument, prefix, indexed
                .sortable(), indexed.noindex(), indexed.alias(), indexed.indexMissing(), indexed.indexEmpty())));
            gsonBuilder.registerTypeAdapter(fieldType, EnumTypeAdapter.of(fieldType));
          } else {
            fields.add(SearchField.of(field, factory.indexAsTagFieldFor(field, isDocument, prefix, indexed.sortable(),
                indexed.separator(), indexed.arrayIndex(), indexed.alias(), indexed.indexMissing(), indexed
                    .indexEmpty(), indexed.withSuffixTrie())));
          }
        } else if ( //
        Number.class.isAssignableFrom(fieldType) || //
            (fieldType == LocalDateTime.class) || //
            (field.getType() == LocalDate.class) || //
            (field.getType() == Date.class) || //
            (field.getType() == Instant.class) || //
            (field.getType() == OffsetDateTime.class) //
        ) {
          fields.add(SearchField.of(field, factory.indexAsNumericFieldFor(field, isDocument, prefix, indexed.sortable(),
              indexed.noindex(), indexed.alias(), indexed.indexMissing(), indexed.indexEmpty())));
        } else if (Set.class.isAssignableFrom(fieldType) || List.class.isAssignableFrom(fieldType)) {
          Optional<Class<?>> maybeCollectionType = getCollectionElementClass(field);

          if (maybeCollectionType.isPresent()) {
            Class<?> collectionType = maybeCollectionType.get();

            if (CharSequence.class.isAssignableFrom(collectionType) || (collectionType == Boolean.class)) {
              fields.add(SearchField.of(field, factory.indexAsTagFieldFor(field, isDocument, prefix, indexed.sortable(),
                  indexed.separator(), indexed.arrayIndex(), indexed.alias(), indexed.indexMissing(), indexed
                      .indexEmpty(), indexed.withSuffixTrie())));
            } else if (isDocument) {
              if (Number.class.isAssignableFrom(collectionType)) {
                fields.add(SearchField.of(field, factory.indexAsNumericFieldFor(field, true, prefix, indexed.sortable(),
                    indexed.noindex(), indexed.alias(), indexed.indexMissing(), indexed.indexEmpty())));
              } else if (collectionType == Point.class) {
                fields.add(SearchField.of(field, factory.indexAsGeoFieldFor(field, true, prefix, indexed.alias())));
              } else if (collectionType == UUID.class || collectionType == Ulid.class) {
                fields.add(SearchField.of(field, factory.indexAsTagFieldFor(field, true, prefix, indexed.sortable(),
                    indexed.separator(), 0, indexed.alias(), indexed.indexMissing(), indexed.indexEmpty(), indexed
                        .withSuffixTrie())));
              } else {
                logger.debug(String.format("Found nested field on field of type: %s", field.getType()));
                fields.addAll(indexAsNestedFieldFor(field, prefix));
              }
            }
          } else {
            logger.debug(String.format("Could not determine the type of elements in the collection %s in entity %s",
                field.getName(), field.getDeclaringClass().getSimpleName()));
          }
        } else if (Map.class.isAssignableFrom(fieldType) && isDocument) {
          logger.info(String.format("Processing Map field: %s of type %s", field.getName(), fieldType));
          Optional<Class<?>> maybeValueType = getMapValueClass(field);
          if (maybeValueType.isPresent()) {
            Class<?> valueType = maybeValueType.get();
            logger.info(String.format("Map field %s has value type: %s", field.getName(), valueType));

            String mapFieldNameForIndex = (indexed.alias() != null && !indexed.alias().isEmpty()) ?
                indexed.alias() :
                field.getName();

            String mapJsonPath = (prefix == null || prefix.isBlank()) ?
                "$." + field.getName() + ".*" :
                "$." + prefix + "." + field.getName() + ".*";
            String mapFieldAlias = mapFieldNameForIndex + "_values";

            if (CharSequence.class.isAssignableFrom(
                valueType) || valueType == UUID.class || valueType == Ulid.class || valueType.isEnum()) {
              TagField tagField = TagField.of(FieldName.of(mapJsonPath).as(mapFieldAlias));
              if (indexed.sortable())
                tagField.sortable();
              if (indexed.indexMissing())
                tagField.indexMissing();
              if (indexed.indexEmpty())
                tagField.indexEmpty();
              if (indexed.withSuffixTrie())
                tagField.withSuffixTrie();
              if (!indexed.separator().isEmpty()) {
                tagField.separator(indexed.separator().charAt(0));
              }
              fields.add(SearchField.of(field, tagField));
              logger.info(String.format("Added TAG field for Map: %s as %s", field.getName(), mapFieldAlias));
            } else if (Number.class.isAssignableFrom(
                valueType) || valueType == Boolean.class || valueType == LocalDateTime.class || valueType == LocalDate.class || valueType == Date.class || valueType == Instant.class || valueType == OffsetDateTime.class) {
              NumericField numField = NumericField.of(FieldName.of(mapJsonPath).as(mapFieldAlias));
              if (indexed.sortable())
                numField.sortable();
              if (indexed.noindex())
                numField.noIndex();
              if (indexed.indexMissing())
                numField.indexMissing();
              fields.add(SearchField.of(field, numField));
              logger.info(String.format("Added NUMERIC field for Map: %s as %s", field.getName(), mapFieldAlias));
            } else if (valueType == Point.class) {
              GeoField geoField = GeoField.of(FieldName.of(mapJsonPath).as(mapFieldAlias));
              fields.add(SearchField.of(field, geoField));
              logger.info(String.format("Added GEO field for Map: %s as %s", field.getName(), mapFieldAlias));
            } else {
              logger.info(String.format("Processing complex object Map field: %s with value type %s", field.getName(),
                  valueType.getName()));

              for (java.lang.reflect.Field subfield : getDeclaredFieldsTransitively(valueType)) {
                if (subfield.isAnnotationPresent(Indexed.class)) {
                  Indexed subfieldIndexed = subfield.getAnnotation(Indexed.class);
                  String jsonFieldName = factory.getJsonFieldName(subfield);
                  String nestedJsonPath = (prefix == null || prefix.isBlank()) ?
                      "$." + field.getName() + ".*." + jsonFieldName :
                      "$." + prefix + "." + field.getName() + ".*." + jsonFieldName;
                  String subfieldAlias = (subfieldIndexed.alias() != null && !subfieldIndexed.alias().isEmpty()) ?
                      subfieldIndexed.alias() :
                      subfield.getName();
                  String nestedFieldAlias = mapFieldNameForIndex + "_" + subfieldAlias;

                  logger.info(String.format("Processing nested field %s in Map value type, path: %s, alias: %s",
                      subfield.getName(), nestedJsonPath, nestedFieldAlias));

                  Class<?> subfieldType = subfield.getType();

                  if (CharSequence.class.isAssignableFrom(
                      subfieldType) || subfieldType == UUID.class || subfieldType == Ulid.class || subfieldType
                          .isEnum()) {
                    TagField tagField = TagField.of(FieldName.of(nestedJsonPath).as(nestedFieldAlias));
                    if (subfieldIndexed.sortable())
                      tagField.sortable();
                    if (subfieldIndexed.indexMissing())
                      tagField.indexMissing();
                    if (subfieldIndexed.indexEmpty())
                      tagField.indexEmpty();
                    if (subfieldIndexed.withSuffixTrie())
                      tagField.withSuffixTrie();
                    if (!subfieldIndexed.separator().isEmpty()) {
                      tagField.separator(subfieldIndexed.separator().charAt(0));
                    }
                    fields.add(SearchField.of(subfield, tagField));
                    logger.info(String.format("Added nested TAG field for Map value: %s", nestedFieldAlias));
                  } else if (Number.class.isAssignableFrom(
                      subfieldType) || subfieldType == Boolean.class || subfieldType == LocalDateTime.class || subfieldType == LocalDate.class || subfieldType == Date.class || subfieldType == Instant.class || subfieldType == OffsetDateTime.class) {
                    NumericField numField = NumericField.of(FieldName.of(nestedJsonPath).as(nestedFieldAlias));
                    if (subfieldIndexed.sortable())
                      numField.sortable();
                    if (subfieldIndexed.noindex())
                      numField.noIndex();
                    if (subfieldIndexed.indexMissing())
                      numField.indexMissing();
                    fields.add(SearchField.of(subfield, numField));
                    logger.info(String.format("Added nested NUMERIC field for Map value: %s", nestedFieldAlias));
                  } else if (subfieldType == Point.class) {
                    GeoField geoField = GeoField.of(FieldName.of(nestedJsonPath).as(nestedFieldAlias));
                    fields.add(SearchField.of(subfield, geoField));
                    logger.info(String.format("Added nested GEO field for Map value: %s", nestedFieldAlias));
                  }
                }
              }
            }
          }
        } else if (fieldType == Point.class) {
          fields.add(SearchField.of(field, factory.indexAsGeoFieldFor(field, isDocument, prefix, indexed.alias())));
        } else {
          for (java.lang.reflect.Field subfield : getDeclaredFieldsTransitively(field.getType())) {
            String subfieldPrefix = (prefix == null || prefix.isBlank()) ?
                field.getName() :
                String.join(".", prefix, field.getName());
            fields.addAll(findIndexFields(subfield, subfieldPrefix, isDocument));
          }
        }
      } else { // Schema field type hardcoded/set in @Indexed
        switch (indexed.schemaFieldType()) {
          case TAG -> fields.add(SearchField.of(field, factory.indexAsTagFieldFor(field, isDocument, prefix, indexed
              .sortable(), indexed.separator(), indexed.arrayIndex(), indexed.alias(), indexed.indexMissing(), indexed
                  .indexEmpty(), indexed.withSuffixTrie())));
          case NUMERIC -> fields.add(SearchField.of(field, factory.indexAsNumericFieldFor(field, isDocument, prefix,
              indexed.sortable(), indexed.noindex(), indexed.alias(), indexed.indexMissing(), indexed.indexEmpty())));
          case GEO -> fields.add(SearchField.of(field, factory.indexAsGeoFieldFor(field, true, prefix, indexed
              .alias())));
          case VECTOR -> fields.add(SearchField.of(field, factory.indexAsVectorFieldFor(field, isDocument, prefix,
              indexed)));
          case NESTED -> {
            Class<?> nestedType = field.getType();

            if (List.class.isAssignableFrom(nestedType) || Set.class.isAssignableFrom(nestedType)) {
              Optional<Class<?>> maybeCollectionType = getCollectionElementClass(field);
              if (maybeCollectionType.isPresent()) {
                nestedType = maybeCollectionType.get();
                logger.info(String.format("Processing nested array field %s with element type %s", field.getName(),
                    nestedType.getSimpleName()));
              } else {
                logger.warn(String.format("Could not determine element type for nested field %s", field.getName()));
                break;
              }
            }

            for (java.lang.reflect.Field subfield : com.redis.om.spring.util.ObjectUtils.getDeclaredFieldsTransitively(
                nestedType)) {
              String subfieldPrefix = (prefix == null || prefix.isBlank()) ?
                  field.getName() :
                  String.join(".", prefix, field.getName());
              fields.addAll(createNestedIndexFields(field, subfield, subfieldPrefix, isDocument));
            }
          }
          default -> {
          }
        }
      }
    } else if (field.isAnnotationPresent(Searchable.class)) {
      logger.info(String.format("Found @Searchable annotation on field of type: %s", field.getType()));
      Searchable searchable = field.getAnnotation(Searchable.class);
      if (searchable.lexicographic()) {
        Class<?> entityClass = field.getDeclaringClass();
        lexicographicFieldRegistry.accept(entityClass, field.getName());
        logger.info(String.format("Tracked lexicographic field %s on class %s", field.getName(), entityClass
            .getName()));
      }
      fields.add(SearchField.of(field, factory.indexAsTextFieldFor(field, isDocument, prefix, searchable)));
    } else if (field.isAnnotationPresent(TextIndexed.class)) {
      TextIndexed ti = field.getAnnotation(TextIndexed.class);
      fields.add(SearchField.of(field, factory.indexAsTextFieldFor(field, isDocument, prefix, ti)));
    } else if (field.isAnnotationPresent(TagIndexed.class)) {
      TagIndexed ti = field.getAnnotation(TagIndexed.class);
      fields.add(SearchField.of(field, factory.indexAsTagFieldFor(field, isDocument, prefix, ti)));
    } else if (field.isAnnotationPresent(GeoIndexed.class)) {
      GeoIndexed gi = field.getAnnotation(GeoIndexed.class);
      fields.add(SearchField.of(field, factory.indexAsGeoFieldFor(field, isDocument, prefix, gi)));
    } else if (field.isAnnotationPresent(NumericIndexed.class)) {
      NumericIndexed ni = field.getAnnotation(NumericIndexed.class);
      fields.add(SearchField.of(field, factory.indexAsNumericFieldFor(field, isDocument, prefix, ni)));
    } else if (field.isAnnotationPresent(VectorIndexed.class)) {
      VectorIndexed vi = field.getAnnotation(VectorIndexed.class);
      fields.add(SearchField.of(field, factory.indexAsVectorFieldFor(field, isDocument, prefix, vi)));
    }

    return fields;
  }

  // ---------------------------------------------------------------------------
  // ID field indexing
  // ---------------------------------------------------------------------------

  private List<SearchField> createIndexedFieldsForIdFields(Class<?> cl, List<SchemaField> fields, boolean isDocument) {
    List<SearchField> results = new ArrayList<>();
    List<java.lang.reflect.Field> idFields = getIdFieldsForEntityClass(cl);
    for (java.lang.reflect.Field idField : idFields) {
      if (isAnnotationPreset(idField, fields)) {
        Class<?> idClass = idField.getType();
        if (idField.getType().isPrimitive()) {
          String cls = com.redis.om.spring.util.ObjectUtils.getTargetClassName(idClass.getName());
          Class<?> primitive = ClassUtils.resolvePrimitiveClassName(cls);
          if (primitive != null) {
            idClass = ClassUtils.resolvePrimitiveIfNecessary(primitive);
          }
        }

        if (Number.class.isAssignableFrom(idClass)) {
          results.add(SearchField.of(idField, factory.indexAsNumericFieldFor(idField, isDocument, "", true, false,
              null)));
        } else {
          results.add(SearchField.of(idField, factory.indexAsTagFieldFor(idField, isDocument, "", false, "|",
              Integer.MIN_VALUE, null)));
        }
      }
    }

    java.lang.reflect.Field idField = (!idFields.isEmpty()) ? idFields.get(0) : null;

    if (idField != null && idField.isAnnotationPresent(IdFilter.class)) {
      IdFilter idFilter = idField.getAnnotation(IdFilter.class);
      var identifierFilterClass = idFilter.value();
      try {
        var identifierFilter = (IdentifierFilter<?>) identifierFilterClass.getDeclaredConstructor().newInstance();
        identifierFilterRegistry.accept(Tuples.of(cl, identifierFilter));
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
               NoSuchMethodException idFilterInstantiationException) {
        logger.error(String.format("Could not instantiate IdFilter of type %s applied to class %s",
            identifierFilterClass.getSimpleName(), cl), idFilterInstantiationException);
      }
    }

    return results;
  }

  private boolean isAnnotationPreset(java.lang.reflect.Field idField, List<SchemaField> fields) {
    return (!idField.isAnnotationPresent(Indexed.class) && !idField.isAnnotationPresent(Searchable.class) && !idField
        .isAnnotationPresent(TagIndexed.class) && !idField.isAnnotationPresent(TextIndexed.class) && !idField
            .isAnnotationPresent(NumericIndexed.class) && (fields.stream().noneMatch(f -> f.getName().equals(idField
                .getName()))));
  }

  // ---------------------------------------------------------------------------
  // Reference field indexing
  // ---------------------------------------------------------------------------

  private Optional<SearchField> createIndexedFieldForReferenceIdField(java.lang.reflect.Field referenceIdField,
      boolean isDocument) {
    com.google.gson.annotations.SerializedName serializedName = referenceIdField.getAnnotation(
        com.google.gson.annotations.SerializedName.class);
    String fname = (serializedName != null) ? serializedName.value() : referenceIdField.getName();

    String fieldPrefix = factory.getFieldPrefix("", isDocument);
    FieldName fieldName = FieldName.of(fieldPrefix + fname);
    String alias = QueryUtils.searchIndexFieldAliasFor(referenceIdField, "");
    fieldName = fieldName.as(alias);

    return Optional.of(SearchField.of(referenceIdField, isDocument ?
        TagField.of(fieldName).separator('|') :
        TagField.of(fieldName).separator('|').sortable()));
  }

  private List<SearchField> createIndexedFieldsForReferencedEntity(java.lang.reflect.Field referenceField,
      boolean isDocument, String prefix) {
    List<SearchField> fields = new ArrayList<>();
    Class<?> referencedType = referenceField.getType();
    String referenceFieldName = referenceField.getName();

    logger.debug(
        "Processing indexed subfields for @Reference field " + referenceFieldName + " of type " + referencedType
            .getSimpleName());

    List<java.lang.reflect.Field> referencedFields = new ArrayList<>();
    referencedFields.addAll(com.redis.om.spring.util.ObjectUtils.getFieldsWithAnnotation(referencedType,
        Indexed.class));
    referencedFields.addAll(com.redis.om.spring.util.ObjectUtils.getFieldsWithAnnotation(referencedType,
        Searchable.class));
    referencedFields.addAll(com.redis.om.spring.util.ObjectUtils.getFieldsWithAnnotation(referencedType,
        TagIndexed.class));
    referencedFields.addAll(com.redis.om.spring.util.ObjectUtils.getFieldsWithAnnotation(referencedType,
        TextIndexed.class));
    referencedFields.addAll(com.redis.om.spring.util.ObjectUtils.getFieldsWithAnnotation(referencedType,
        NumericIndexed.class));
    referencedFields.addAll(com.redis.om.spring.util.ObjectUtils.getFieldsWithAnnotation(referencedType,
        GeoIndexed.class));
    referencedFields.addAll(com.redis.om.spring.util.ObjectUtils.getFieldsWithAnnotation(referencedType,
        VectorIndexed.class));
    referencedFields = referencedFields.stream().distinct().toList();

    for (java.lang.reflect.Field subField : referencedFields) {
      if (subField.isAnnotationPresent(Id.class)) {
        continue;
      }
      if (subField.isAnnotationPresent(Reference.class)) {
        continue;
      }

      Class<?> subFieldType = ClassUtils.resolvePrimitiveIfNecessary(subField.getType());
      String subFieldName = subField.getName();

      String fieldPath = isDocument ?
          factory.getFieldPrefix(prefix, true) + referenceFieldName + "." + subFieldName :
          referenceFieldName + "_" + subFieldName;

      String alias = referenceFieldName + "_" + subFieldName;
      FieldName fieldName = FieldName.of(fieldPath).as(alias);

      logger.debug(
          "Creating index field for " + referenceFieldName + "." + subFieldName + " with path " + fieldPath + " and alias " + alias);

      Searchable searchable = subField.getAnnotation(Searchable.class);
      if (searchable != null) {
        TextField textField = TextField.of(fieldName);
        if (searchable.weight() != 1.0)
          textField.weight(searchable.weight());
        if (searchable.sortable())
          textField.sortable();
        if (searchable.nostem())
          textField.noStem();
        if (searchable.noindex())
          textField.noIndex();
        String phonetic = searchable.phonetic();
        if (phonetic != null && !phonetic.isEmpty())
          textField.phonetic(phonetic);
        if (searchable.indexMissing())
          textField.indexMissing();
        if (searchable.indexEmpty())
          textField.indexEmpty();
        if (searchable.withSuffixTrie())
          textField.withSuffixTrie();
        fields.add(SearchField.of(subField, textField));
        continue;
      }

      TextIndexed textIndexed = subField.getAnnotation(TextIndexed.class);
      if (textIndexed != null) {
        TextField textField = TextField.of(fieldName);
        if (textIndexed.weight() != 1.0)
          textField.weight(textIndexed.weight());
        if (textIndexed.sortable())
          textField.sortable();
        if (textIndexed.nostem())
          textField.noStem();
        if (textIndexed.noindex())
          textField.noIndex();
        String phonetic = textIndexed.phonetic();
        if (phonetic != null && !phonetic.isEmpty())
          textField.phonetic(phonetic);
        if (textIndexed.indexMissing())
          textField.indexMissing();
        if (textIndexed.indexEmpty())
          textField.indexEmpty();
        if (textIndexed.withSuffixTrie())
          textField.withSuffixTrie();
        fields.add(SearchField.of(subField, textField));
        continue;
      }

      Indexed indexed = subField.getAnnotation(Indexed.class);
      TagIndexed tagIndexed = subField.getAnnotation(TagIndexed.class);
      NumericIndexed numericIndexed = subField.getAnnotation(NumericIndexed.class);
      GeoIndexed geoIndexed = subField.getAnnotation(GeoIndexed.class);

      if (tagIndexed != null || (indexed != null && (CharSequence.class.isAssignableFrom(
          subFieldType) || subFieldType == java.util.UUID.class || subFieldType == com.github.f4b6a3.ulid.Ulid.class))) {
        String separatorStr = tagIndexed != null ?
            tagIndexed.separator() :
            (indexed != null ? indexed.separator() : "|");
        char separator = separatorStr != null && !separatorStr.isEmpty() ? separatorStr.charAt(0) : '|';
        TagField tagField = TagField.of(fieldName).separator(separator);
        if (indexed != null && indexed.sortable())
          tagField.sortable();
        if (tagIndexed != null && tagIndexed.indexMissing()) {
          tagField.indexMissing();
        } else if (indexed != null && indexed.indexMissing()) {
          tagField.indexMissing();
        }
        if (tagIndexed != null && tagIndexed.indexEmpty()) {
          tagField.indexEmpty();
        } else if (indexed != null && indexed.indexEmpty()) {
          tagField.indexEmpty();
        }
        if ((tagIndexed != null && tagIndexed.withSuffixTrie()) || (indexed != null && indexed.withSuffixTrie()))
          tagField.withSuffixTrie();
        fields.add(SearchField.of(subField, tagField));
      } else if (numericIndexed != null || (indexed != null && (Number.class.isAssignableFrom(
          subFieldType) || subFieldType == java.time.LocalDateTime.class || subFieldType == java.time.LocalDate.class || subFieldType == java.util.Date.class || subFieldType == java.time.Instant.class || subFieldType == java.time.OffsetDateTime.class))) {
        NumericField numField = NumericField.of(fieldName);
        if ((numericIndexed != null && numericIndexed.sortable()) || (indexed != null && indexed.sortable()))
          numField.sortable();
        if ((numericIndexed != null && numericIndexed.noindex()) || (indexed != null && indexed.noindex()))
          numField.noIndex();
        if (indexed != null && indexed.indexMissing())
          numField.indexMissing();
        fields.add(SearchField.of(subField, numField));
      } else if (geoIndexed != null || (indexed != null && Point.class.isAssignableFrom(subFieldType))) {
        fields.add(SearchField.of(subField, GeoField.of(fieldName)));
      } else if (indexed != null && subFieldType.isEnum()) {
        String separatorStr = indexed.separator();
        char separator = separatorStr != null && !separatorStr.isEmpty() ? separatorStr.charAt(0) : '|';
        TagField tagField = TagField.of(fieldName).separator(separator);
        if (indexed.sortable())
          tagField.sortable();
        if (indexed.indexMissing())
          tagField.indexMissing();
        if (indexed.indexEmpty())
          tagField.indexEmpty();
        if (indexed.withSuffixTrie())
          tagField.withSuffixTrie();
        fields.add(SearchField.of(subField, tagField));
      } else if (indexed != null && (subFieldType == Boolean.class || subFieldType == boolean.class)) {
        TagField tagField = TagField.of(fieldName);
        if (indexed.sortable())
          tagField.sortable();
        if (indexed.indexMissing())
          tagField.indexMissing();
        if (indexed.indexEmpty())
          tagField.indexEmpty();
        if (indexed.withSuffixTrie())
          tagField.withSuffixTrie();
        fields.add(SearchField.of(subField, tagField));
      }

      VectorIndexed vectorIndexed = subField.getAnnotation(VectorIndexed.class);
      if (vectorIndexed != null) {
        VectorField.VectorAlgorithm algorithm = vectorIndexed.algorithm();
        VectorType vectorType = vectorIndexed.type();
        int dimension = vectorIndexed.dimension();
        DistanceMetric distanceMetric = vectorIndexed.distanceMetric();
        int initialCap = vectorIndexed.initialCapacity();

        Map<String, Object> vectorAttrs = new HashMap<>();
        vectorAttrs.put("TYPE", vectorType.toString());
        vectorAttrs.put("DIM", dimension);
        vectorAttrs.put("DISTANCE_METRIC", distanceMetric.toString());
        if (initialCap > 0)
          vectorAttrs.put("INITIAL_CAP", initialCap);

        if (algorithm == VectorField.VectorAlgorithm.HNSW) {
          int m = vectorIndexed.m();
          int efConstruction = vectorIndexed.efConstruction();
          int efRuntime = vectorIndexed.efRuntime();
          double epsilon = vectorIndexed.epsilon();
          if (m > 0)
            vectorAttrs.put("M", m);
          if (efConstruction > 0)
            vectorAttrs.put("EF_CONSTRUCTION", efConstruction);
          if (efRuntime > 0)
            vectorAttrs.put("EF_RUNTIME", efRuntime);
          if (epsilon > 0)
            vectorAttrs.put("EPSILON", epsilon);
        } else if (algorithm == VectorField.VectorAlgorithm.FLAT) {
          int blockSize = vectorIndexed.blockSize();
          if (blockSize > 0)
            vectorAttrs.put("BLOCK_SIZE", blockSize);
        }

        fields.add(SearchField.of(subField, VectorField.builder().fieldName(fieldName).algorithm(algorithm).attributes(
            vectorAttrs).build()));
      }
    }

    return fields;
  }

  // ---------------------------------------------------------------------------
  // Nested field helpers
  // ---------------------------------------------------------------------------

  private List<SearchField> indexAsNestedFieldFor(java.lang.reflect.Field field, String prefix) {
    String fieldPrefix = factory.getFieldPrefix(prefix, true);
    return getNestedField(fieldPrefix, field, prefix, null);
  }

  private List<SearchField> getNestedField(String fieldPrefix, java.lang.reflect.Field field, String prefix,
      List<SearchField> fieldList) {
    if (fieldList == null) {
      fieldList = new ArrayList<>();
    }
    Type genericType = field.getGenericType();
    if (genericType instanceof ParameterizedType pt) {
      Class<?> actualTypeArgument = (Class<?>) pt.getActualTypeArguments()[0];
      List<java.lang.reflect.Field> subDeclaredFields = com.redis.om.spring.util.ObjectUtils
          .getDeclaredFieldsTransitively(actualTypeArgument);
      String tempPrefix = "";
      if (prefix == null) {
        prefix = field.getName();
      } else {
        prefix += "." + field.getName();
      }
      for (java.lang.reflect.Field subField : subDeclaredFields) {
        Optional<Class<?>> maybeCollectionType = getCollectionElementClass(subField);

        String suffix = (maybeCollectionType.isPresent() && (CharSequence.class.isAssignableFrom(maybeCollectionType
            .get()) || (maybeCollectionType.get() == Boolean.class))) ? "[*]" : "";

        if (subField.isAnnotationPresent(TagIndexed.class)) {
          TagIndexed ti = subField.getAnnotation(TagIndexed.class);
          tempPrefix = field.getName() + "[0:].";

          FieldName fieldName = FieldName.of(fieldPrefix + tempPrefix + subField.getName() + suffix);
          fieldName = fieldName.as(QueryUtils.searchIndexFieldAliasFor(subField, prefix));

          logger.info(String.format("Creating nested relationships: %s -> %s", field.getName(), subField.getName()));
          fieldList.add(SearchField.of(field, factory.getTagField(fieldName, ti.separator(), false, false, false, ti
              .withSuffixTrie())));
          continue;
        } else if (subField.isAnnotationPresent(Indexed.class)) {
          boolean subFieldIsTagField = (subField.isAnnotationPresent(Indexed.class) && ( //
          CharSequence.class.isAssignableFrom(subField.getType()) || //
              (subField.getType() == Boolean.class) || (subField.getType() == UUID.class) || //
              ( //
              maybeCollectionType.isPresent() && //
                  ( //
                  CharSequence.class.isAssignableFrom(maybeCollectionType.get()) || //
                      (maybeCollectionType.get() == Boolean.class) //
                  ) //
              ) //
          ) //
          );
          if (subFieldIsTagField) {
            Indexed indexed = subField.getAnnotation(Indexed.class);
            tempPrefix = field.getName() + "[0:].";

            FieldName fieldName = FieldName.of(fieldPrefix + tempPrefix + subField.getName() + suffix);
            String alias = QueryUtils.searchIndexFieldAliasFor(subField, prefix);
            fieldName = fieldName.as(alias);

            logger.info(String.format("Creating nested relationships: %s -> %s", field.getName(), subField.getName()));
            fieldList.add(SearchField.of(field, factory.getTagField(fieldName, indexed.separator(), false, false, false,
                indexed.withSuffixTrie())));
            continue;
          } else if (Number.class.isAssignableFrom(subField.getType()) || (subField
              .getType() == LocalDateTime.class) || (subField.getType() == LocalDate.class) || (subField
                  .getType() == Date.class)) {

            FieldName fieldName = FieldName.of(fieldPrefix + tempPrefix + subField.getName() + suffix);
            String alias = QueryUtils.searchIndexFieldAliasFor(subField, prefix);
            fieldName = fieldName.as(alias);

            logger.info(String.format("Creating nested relationships: %s -> %s", field.getName(), subField.getName()));
            fieldList.add(SearchField.of(field, NumericField.of(fieldName)));
          }
        } else if (subField.isAnnotationPresent(Searchable.class)) {
          Searchable searchable = subField.getAnnotation(Searchable.class);
          tempPrefix = field.getName() + "[0:].";

          FieldName fieldName = FieldName.of(fieldPrefix + tempPrefix + subField.getName() + suffix);
          String alias = QueryUtils.searchIndexFieldAliasFor(subField, prefix);
          fieldName = fieldName.as(alias);

          logger.info(String.format("Creating TEXT nested relationships: %s -> %s", field.getName(), subField
              .getName()));

          String phonetic = org.apache.commons.lang3.ObjectUtils.isEmpty(searchable.phonetic()) ?
              null :
              searchable.phonetic();

          fieldList.add(SearchField.of(field, factory.getTextField(fieldName, searchable.weight(), searchable
              .sortable(), searchable.nostem(), searchable.noindex(), phonetic, searchable.indexMissing(), searchable
                  .indexEmpty(), searchable.withSuffixTrie())));

          continue;
        } else if (subField.isAnnotationPresent(TextIndexed.class)) {
          TextIndexed textIndexed = subField.getAnnotation(TextIndexed.class);
          tempPrefix = field.getName() + "[0:].";

          FieldName fieldName = FieldName.of(fieldPrefix + tempPrefix + subField.getName() + suffix);
          String alias = QueryUtils.searchIndexFieldAliasFor(subField, prefix);
          fieldName = fieldName.as(alias);

          logger.info(String.format("Creating TEXT nested relationships: %s -> %s", field.getName(), subField
              .getName()));

          String phonetic = org.apache.commons.lang3.ObjectUtils.isEmpty(textIndexed.phonetic()) ?
              null :
              textIndexed.phonetic();

          fieldList.add(SearchField.of(field, factory.getTextField(fieldName, textIndexed.weight(), textIndexed
              .sortable(), textIndexed.nostem(), textIndexed.noindex(), phonetic, textIndexed.indexMissing(),
              textIndexed.indexEmpty(), textIndexed.withSuffixTrie())));

          continue;
        }
        if (subField.isAnnotationPresent(Indexed.class)) {
          getNestedField(fieldPrefix + tempPrefix, subField, prefix, fieldList);
        }
      }
    }
    return fieldList;
  }

  private List<SearchField> createNestedIndexFields(java.lang.reflect.Field arrayField,
      java.lang.reflect.Field nestedField, String prefix, boolean isDocument) {
    List<SearchField> fields = new ArrayList<>();

    Class<?> nestedFieldType = ClassUtils.resolvePrimitiveIfNecessary(nestedField.getType());

    String fullFieldPath = isDocument ?
        "$." + arrayField.getName() + "[*]." + nestedField.getName() :
        arrayField.getName() + "[*]." + nestedField.getName();

    logger.info(String.format("Creating automatic nested field index: %s -> %s", arrayField.getName(), fullFieldPath));

    FieldName fieldName = nestedFieldName(fullFieldPath, nestedField, prefix);

    Searchable searchable = nestedField.getAnnotation(Searchable.class);
    if (searchable != null) {
      String phonetic = org.apache.commons.lang3.ObjectUtils.isEmpty(searchable.phonetic()) ?
          null :
          searchable.phonetic();
      fields.add(SearchField.of(arrayField, factory.getTextField(fieldName, searchable.weight(), searchable.sortable(),
          searchable.nostem(), searchable.noindex(), phonetic, searchable.indexMissing(), searchable.indexEmpty(),
          searchable.withSuffixTrie())));
      return fields;
    }

    TextIndexed textIndexed = nestedField.getAnnotation(TextIndexed.class);
    if (textIndexed != null) {
      String phonetic = org.apache.commons.lang3.ObjectUtils.isEmpty(textIndexed.phonetic()) ?
          null :
          textIndexed.phonetic();
      fields.add(SearchField.of(arrayField, factory.getTextField(fieldName, textIndexed.weight(), textIndexed
          .sortable(), textIndexed.nostem(), textIndexed.noindex(), phonetic, textIndexed.indexMissing(), textIndexed
              .indexEmpty(), textIndexed.withSuffixTrie())));
      return fields;
    }

    FieldTypeMapper fieldTypeMapper = FieldTypeMapper.getFieldType(nestedFieldType);
    switch (fieldTypeMapper) {
      case TAG -> {
        Indexed indexed = nestedField.getAnnotation(Indexed.class);
        TagIndexed tagIndexed = nestedField.getAnnotation(TagIndexed.class);
        boolean withSuffixTrie = (indexed != null && indexed.withSuffixTrie()) || (tagIndexed != null && tagIndexed
            .withSuffixTrie());
        fields.add(SearchField.of(arrayField, factory.getTagField(fieldName, "|", false, false, false,
            withSuffixTrie)));
      }
      case NUMERIC -> fields.add(SearchField.of(arrayField, NumericField.of(fieldName)));
      case GEO -> fields.add(SearchField.of(arrayField, GeoField.of(fieldName)));
      case UNSUPPORTED -> logger.debug(String.format("Skipping nested field %s of unsupported type %s", nestedField
          .getName(), nestedFieldType.getSimpleName()));
    }

    return fields;
  }

  private FieldName nestedFieldName(String fullFieldPath, java.lang.reflect.Field nestedField, String prefix) {
    FieldName fieldName = FieldName.of(fullFieldPath);
    String alias = QueryUtils.searchIndexFieldAliasFor(nestedField, prefix);
    if (alias != null && !alias.isEmpty()) {
      fieldName = fieldName.as(alias);
    }
    return fieldName;
  }

  // ---------------------------------------------------------------------------
  // Inner helpers
  // ---------------------------------------------------------------------------

  private enum FieldTypeMapper {
    TAG,
    NUMERIC,
    GEO,
    UNSUPPORTED;

    static FieldTypeMapper getFieldType(Class<?> fieldType) {
      if (CharSequence.class.isAssignableFrom(
          fieldType) || fieldType == Boolean.class || fieldType == UUID.class || fieldType == Ulid.class || fieldType
              .isEnum()) {
        return TAG;
      } else if (Number.class.isAssignableFrom(
          fieldType) || fieldType == LocalDateTime.class || fieldType == LocalDate.class || fieldType == Date.class || fieldType == Instant.class || fieldType == OffsetDateTime.class) {
        return NUMERIC;
      } else if (fieldType == Point.class) {
        return GEO;
      } else {
        return UNSUPPORTED;
      }
    }
  }
}
