package com.redis.om.spring.metamodel;

import com.github.f4b6a3.ulid.Ulid;
import com.google.auto.service.AutoService;
import com.redis.om.spring.annotations.*;
import com.redis.om.spring.metamodel.indexed.*;
import com.redis.om.spring.metamodel.nonindexed.*;
import com.redis.om.spring.tuple.Pair;
import com.redis.om.spring.tuple.Triple;
import com.redis.om.spring.tuple.Tuples;
import com.redis.om.spring.util.ObjectUtils;
import com.squareup.javapoet.*;
import com.squareup.javapoet.CodeBlock.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.util.ClassUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.redis.om.spring.util.ObjectUtils.isInnerClassWithEnclosing;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@SupportedAnnotationTypes(
    value = { "com.redis.om.spring.annotations.Document", "org.springframework.data.redis.core.RedisHash" }
)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public final class MetamodelGenerator extends AbstractProcessor {

  static final String GET_PREFIX = "get";
  static final String IS_PREFIX = "is";
  private static final Set<String> DISALLOWED_ACCESS_LEVELS = Stream.of("PROTECTED", "PRIVATE", "NONE")
      .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
  private final Map<String, Integer> depthMap = new HashMap<>();
  private ProcessingEnvironment processingEnvironment;
  private Messager messager;
  private TypeElement objectTypeElement;

  public MetamodelGenerator() {
  }

  private static TypeSpec getTypeSpecForMetamodelClass(String genEntityName, List<FieldSpec> interceptors,
      List<ObjectGraphFieldSpec> fields, List<FieldSpec> nestedFieldsConstants, CodeBlock staticBlock) {
    return TypeSpec.classBuilder(genEntityName) //
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL) //
        .addFields(fields.stream().map(ObjectGraphFieldSpec::fieldSpec).toList()) //
        .addFields(nestedFieldsConstants) //
        .addStaticBlock(staticBlock) //
        .addFields(interceptors) //
        .build();
  }

  @Override
  public synchronized void init(ProcessingEnvironment env) {
    super.init(env);

    this.processingEnvironment = env;
    processingEnvironment.getElementUtils();
    processingEnvironment.getTypeUtils();

    messager = processingEnvironment.getMessager();
    messager.printMessage(Diagnostic.Kind.NOTE, "🍃 Redis OM Spring Entity Metamodel Generator");

    this.objectTypeElement = processingEnvironment.getElementUtils().getTypeElement("java.lang.Object");
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    if (annotations.isEmpty() || roundEnv.processingOver()) {
      // Allow other processors to run
      return false;
    }

    Set<? extends Element> documentEntities = roundEnv.getElementsAnnotatedWith(Document.class);
    Set<? extends Element> hashEntities = roundEnv.getElementsAnnotatedWith(RedisHash.class);
    Set<? extends Element> metamodelCandidates = Stream.of(documentEntities, hashEntities) //
        .flatMap(Collection::stream).collect(Collectors.toSet());

    metamodelCandidates.stream().filter(ae -> ae.getKind() == ElementKind.CLASS).forEach(ae -> {
      try {
        generateMetaModelClass(ae);
      } catch (IOException ioe) {
        messager.printMessage(Diagnostic.Kind.ERROR,
            "Cannot generate metamodel class for " + ae.getClass().getName() + " because " + ioe.getMessage());
      }
    });

    return true;
  }

  void generateMetaModelClass(final Element annotatedElement) throws IOException {
    Pair<Boolean, String> innerClassInfo = isInnerClassWithEnclosing(annotatedElement);
    boolean isInnerClass = innerClassInfo.getFirst();
    String enclosingClassName = innerClassInfo.getSecond();

    String qualifiedName = annotatedElement.asType().toString();

    final String entityName;
    final String packageName;
    final String genEntityName;

    PackageElement packageElement = processingEnvironment.getElementUtils().getPackageOf(annotatedElement);
    if (packageElement.isUnnamed()) {
      messager.printMessage(Diagnostic.Kind.WARNING,
          "Class " + annotatedElement.getSimpleName() + " has an unnamed package.");
      packageName = "";
      entityName = qualifiedName; // Use the full name as the entity name for packageless classes
    } else {
      packageName = packageElement.getQualifiedName().toString();
      entityName = isInnerClass ? annotatedElement.getSimpleName().toString() : ObjectUtils.shortName(qualifiedName);
    }

    genEntityName = entityName + "$";

    String qualifiedGenEntityName = (packageName.isEmpty() ? "" : packageName + ".") + genEntityName;
    TypeName entity = TypeName.get(annotatedElement.asType());

    messager.printMessage(Diagnostic.Kind.NOTE, "Generating Entity Metamodel: " + qualifiedGenEntityName);

    Map<? extends Element, String> enclosedFields = getInstanceFields(annotatedElement);

    List<FieldSpec> interceptors = new ArrayList<>();
    List<ObjectGraphFieldSpec> fields = new ArrayList<>();
    List<CodeBlock> initCodeBlocks = new ArrayList<>();
    List<FieldSpec> nestedFieldsConstants = new ArrayList<>();

    enclosedFields.forEach((field, getter) -> {
      List<Triple<ObjectGraphFieldSpec, FieldSpec, CodeBlock>> fieldMetamodels = processFieldMetamodel(entity,
          entityName, List.of(field));
      extractFieldMetamodels(entity, interceptors, fields, initCodeBlocks, fieldMetamodels);
    });

    Pair<FieldSpec, CodeBlock> keyAccessor = generateUnboundMetamodelField(entity, "_KEY", "__key", String.class);
    interceptors.add(keyAccessor.getFirst());
    initCodeBlocks.add(keyAccessor.getSecond());

    Pair<FieldSpec, CodeBlock> thisAccessor = generateThisMetamodelField(entity);
    interceptors.add(thisAccessor.getFirst());
    initCodeBlocks.add(thisAccessor.getSecond());

    CodeBlock.Builder blockBuilder = CodeBlock.builder();

    boolean hasFields = !fields.isEmpty();
    if (hasFields)
      blockBuilder.beginControlFlow("try");
    addStatement(entity, fields, initCodeBlocks, blockBuilder);

    if (hasFields) {
      blockBuilder.nextControlFlow("catch($T | $T e)", NoSuchFieldException.class, SecurityException.class);
      blockBuilder.addStatement("System.err.println(e.getMessage())");
      blockBuilder.endControlFlow();
    }

    CodeBlock staticBlock = blockBuilder.build();

    TypeSpec metaClass = getTypeSpecForMetamodelClass(genEntityName, interceptors, fields, nestedFieldsConstants,
        staticBlock);

    JavaFile javaFile = JavaFile //
        .builder(packageName, metaClass) //
        .build();

    JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(qualifiedGenEntityName);
    Writer writer = builderFile.openWriter();
    javaFile.writeTo(writer);

    writer.close();
  }

  private void addStatement(TypeName entity, List<ObjectGraphFieldSpec> fields, List<CodeBlock> initCodeBlocks,
      Builder blockBuilder) {
    for (ObjectGraphFieldSpec ogfs : fields) {
      StringBuilder sb = new StringBuilder("$T.class");
      for (int i = 0; i < ogfs.chain().size(); i++) {
        Element element = ogfs.chain().get(i);
        if (i != 0) {
          sb.append(".getType()");
        }
        String formattedString = String.format(
            "com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(%s, \"%s\")", sb,
            element.getSimpleName());
        sb.setLength(0); // clear the builder
        sb.append(formattedString);
      }
      FieldSpec fieldSpec = ogfs.fieldSpec();
      blockBuilder.addStatement("$L = " + sb, fieldSpec.name, entity);
    }

    for (CodeBlock initCodeBlock : initCodeBlocks) {
      blockBuilder.add(initCodeBlock);
    }
  }

  private List<Triple<ObjectGraphFieldSpec, FieldSpec, CodeBlock>> processFieldMetamodel(TypeName entity,
      String entityName, List<Element> chain) {
    return processFieldMetamodel(entity, entityName, chain, null);
  }

  private List<Triple<ObjectGraphFieldSpec, FieldSpec, CodeBlock>> processFieldMetamodel(TypeName entity,
      String entityName, List<Element> chain, String collectionPrefix) {
    List<Triple<ObjectGraphFieldSpec, FieldSpec, CodeBlock>> fieldMetamodelSpec = new ArrayList<>();

    Element field = chain.get(chain.size() - 1);

    var indexed = field.getAnnotation(Indexed.class);
    var searchable = field.getAnnotation(Searchable.class);
    var textIndexed = field.getAnnotation(TextIndexed.class);
    var tagIndexed = field.getAnnotation(TagIndexed.class);
    var numericIndexed = field.getAnnotation(NumericIndexed.class);
    var geoIndexed = field.getAnnotation(GeoIndexed.class);
    var vectorIndexed = field.getAnnotation(VectorIndexed.class);

    var id = field.getAnnotation(Id.class);
    var reference = field.getAnnotation(Reference.class);

    boolean fieldIsIndexed = (searchable != null) || (indexed != null) || (textIndexed != null) || (tagIndexed != null) || (numericIndexed != null) || (geoIndexed != null) || (vectorIndexed != null) || (id != null);

    String chainedFieldName = chain.stream().map(Element::getSimpleName).collect(Collectors.joining("_"));
    messager.printMessage(Diagnostic.Kind.NOTE, "Processing " + chainedFieldName);
    TypeName entityField = TypeName.get(field.asType());

    TypeMirror fieldType = field.asType();
    String fullTypeClassName = fieldType.toString();
    String cls = ObjectUtils.getTargetClassName(fullTypeClassName);

    if (field.asType().getKind().isPrimitive()) {
      Class<?> primitive = ClassUtils.resolvePrimitiveClassName(cls);
      if (primitive == null)
        return Collections.emptyList();
      Class<?> primitiveWrapper = ClassUtils.resolvePrimitiveIfNecessary(primitive);
      entityField = TypeName.get(primitiveWrapper);
      fullTypeClassName = entityField.toString();
      cls = ObjectUtils.getTargetClassName(fullTypeClassName);
    }

    Class<?> targetInterceptor = null;
    Class<?> targetCls = null;

    String searchSchemaAlias = null;

    if (indexed != null && reference != null) {
      //
      // @Reference: Field is a reference to another entity
      //
      targetInterceptor = ReferenceField.class;
      searchSchemaAlias = indexed.alias();
    } else if (searchable != null || textIndexed != null) {
      //
      // @Searchable/@TextIndexed: Field is a full-text field
      //
      targetInterceptor = TextField.class;
      searchSchemaAlias = (searchable != null) ? searchable.alias() : textIndexed.alias();
    } else if (fieldIsIndexed) {
      //
      //
      //
      try {
        targetCls = ClassUtils.forName(cls, MetamodelGenerator.class.getClassLoader());
      } catch (ClassNotFoundException cnfe) {
        messager.printMessage(Diagnostic.Kind.WARNING,
            "Processing class " + entityName + " could not resolve " + cls + " while checking for nested @Indexed");
        fieldMetamodelSpec.addAll(processNestedIndexableFields(entity, chain));
      }

      if (tagIndexed != null) {
        targetInterceptor = TextTagField.class;
        searchSchemaAlias = tagIndexed.alias();
      } else if (numericIndexed != null) {
        targetInterceptor = NumericField.class;
        searchSchemaAlias = numericIndexed.alias();
      } else if (geoIndexed != null) {
        targetInterceptor = GeoField.class;
        searchSchemaAlias = geoIndexed.alias();
      } else if (vectorIndexed != null) {
        targetInterceptor = VectorField.class;
        searchSchemaAlias = vectorIndexed.alias();
      } else if (indexed != null && indexed.schemaFieldType() != SchemaFieldType.AUTODETECT) {
        searchSchemaAlias = indexed.alias();
        // here we do the non autodetect annotated fields
        switch (indexed.schemaFieldType()) {
          case TAG -> targetInterceptor = TextTagField.class;
          case NUMERIC -> targetInterceptor = NumericField.class;
          case GEO -> targetInterceptor = GeoField.class;
          case VECTOR -> targetInterceptor = VectorField.class;
          default -> {
          } // NOOP
        }
      } else if (indexed != null && targetCls == null && isEnum(processingEnv, fieldType)) {
        targetInterceptor = TextTagField.class;
        searchSchemaAlias = indexed.alias();
      } else if (targetCls != null) {
        //
        // Any Character class -> Tag Search Field
        //
        if (CharSequence.class.isAssignableFrom(targetCls) || (UUID.class.isAssignableFrom(
            targetCls)) || (targetCls == Ulid.class)) {
          targetInterceptor = TextTagField.class;
        }
        //
        // Any Numeric class -> Numeric Search Field
        //
        else if (Number.class.isAssignableFrom(targetCls)) {
          targetInterceptor = NumericField.class;
        }
        //
        // Any Date/Time Types
        //
        else if ((targetCls == LocalDateTime.class) || (targetCls == LocalDate.class) //
            || (targetCls == Date.class) || (targetCls == Instant.class) || (targetCls == OffsetDateTime.class)) {
          targetInterceptor = DateField.class;
        }
        //
        // Set / List
        //
        else if (Set.class.isAssignableFrom(targetCls) || List.class.isAssignableFrom(targetCls)) {
          String collectionElementName = ObjectUtils.getCollectionTargetClassName(fullTypeClassName);
          targetInterceptor = TagField.class;
          try {
            ClassUtils.forName(collectionElementName, MetamodelGenerator.class.getClassLoader());
          } catch (ClassNotFoundException cnfe) {
            Triple<ObjectGraphFieldSpec, FieldSpec, CodeBlock> collectionFieldMetamodel = null;
            try {
              collectionFieldMetamodel = generateCollectionFieldMetamodel(entity, chain, chainedFieldName,
                  collectionElementName);
            } catch (IOException e) {
              messager.printMessage(Diagnostic.Kind.WARNING,
                  "Processing class " + entityName + " could create collection field metamodel element for " + collectionElementName);
            }
            if (collectionFieldMetamodel != null) {
              fieldMetamodelSpec.add(collectionFieldMetamodel);
              targetInterceptor = null;
            }
          }
        }
        //
        // Point
        //
        else if (targetCls == Point.class) {
          targetInterceptor = GeoField.class;
        }
        //
        // Boolean
        //
        else if (targetCls == Boolean.class) {
          targetInterceptor = BooleanField.class;
        }
      }
      if (indexed != null) {
        searchSchemaAlias = indexed.alias();
      }
    } else {
      var metamodel = field.getAnnotation(Metamodel.class);
      try {
        targetCls = ClassUtils.forName(cls, MetamodelGenerator.class.getClassLoader());

        //
        // Any Character class
        //
        if (CharSequence.class.isAssignableFrom(targetCls) || (targetCls == Ulid.class)) {
          targetInterceptor = NonIndexedTextField.class;
        }
        //
        // Non-indexed Boolean
        //
        else if (targetCls == Boolean.class) {
          targetInterceptor = NonIndexedBooleanField.class;
        }
        //
        // Numeric class AND Any Date/Time Types
        //
        else if (Number.class.isAssignableFrom(
            targetCls) || (targetCls == LocalDateTime.class) || (targetCls == LocalDate.class) || (targetCls == Date.class) || (targetCls == Instant.class) || (targetCls == OffsetDateTime.class)) {
          targetInterceptor = NonIndexedNumericField.class;
        }
        //
        // Set / List
        //
        else if (Set.class.isAssignableFrom(targetCls) || List.class.isAssignableFrom(targetCls)) {
          targetInterceptor = NonIndexedTagField.class;
        }
        //
        // Point
        //
        else if (targetCls == Point.class) {
          targetInterceptor = NonIndexedGeoField.class;
        }
      } catch (ClassNotFoundException cnfe) {
        if (metamodel != null) {
          messager.printMessage(Kind.NOTE,
              "Processing class " + entityName + ", generating nested class " + cls + " metamodel (@Metamodel)");
          fieldMetamodelSpec.addAll(processNestedIndexableFields(entity, chain));
        }
      }
    }

    if (targetInterceptor != null) {
      fieldMetamodelSpec.add(
          generateFieldMetamodel(entity, chain, chainedFieldName, entityField, targetInterceptor, fieldIsIndexed,
              collectionPrefix, searchSchemaAlias));
    }
    return fieldMetamodelSpec;
  }

  private Triple<ObjectGraphFieldSpec, FieldSpec, CodeBlock> generateCollectionFieldMetamodel( //
      TypeName parentEntity, //
      List<Element> chain, //
      String chainedFieldName, //
      String collectionElementName //
  ) throws IOException {
    Element entity1 = chain.get(chain.size() - 1).getEnclosingElement();
    String qualifiedGenEntityName = parentEntity.toString() + "_" + chainedFieldName + "$";
    final String genEntityName = qualifiedGenEntityName.substring(qualifiedGenEntityName.lastIndexOf('.') + 1);
    final String entityName = collectionElementName;
    TypeName entity = ClassName.bestGuess(entityName);
    Map<? extends Element, String> enclosedFields = getInstanceFields(entity);

    final PackageElement packageElement = processingEnvironment.getElementUtils().getPackageOf(entity1);
    String packageName;
    if (packageElement.isUnnamed()) {
      messager.printMessage(Diagnostic.Kind.WARNING, "Class " + entity1.getSimpleName() + " has an unnamed package.");
      packageName = "";
    } else {
      packageName = packageElement.getQualifiedName().toString();
    }

    List<FieldSpec> interceptors = new ArrayList<>();
    List<ObjectGraphFieldSpec> fields = new ArrayList<>();
    List<CodeBlock> initCodeBlocks = new ArrayList<>();
    List<FieldSpec> nestedFieldsConstants = new ArrayList<>();

    enclosedFields.forEach((field, getter) -> {
      List<Triple<ObjectGraphFieldSpec, FieldSpec, CodeBlock>> fieldMetamodels = processFieldMetamodel(entity,
          entityName, List.of(field), chainedFieldName);
      extractFieldMetamodels(entity, interceptors, fields, initCodeBlocks, fieldMetamodels);
    });

    CodeBlock.Builder blockBuilder = CodeBlock.builder();

    blockBuilder.beginControlFlow("try");
    addStatement(entity, fields, initCodeBlocks, blockBuilder);

    blockBuilder.nextControlFlow("catch($T | $T e)", NoSuchFieldException.class, SecurityException.class);
    blockBuilder.addStatement("System.err.println(e.getMessage())");
    blockBuilder.endControlFlow();

    TypeSpec metaClass = getTypeSpecForFieldMetamodel(genEntityName, interceptors, fields, nestedFieldsConstants,
        blockBuilder);

    JavaFile javaFile = JavaFile //
        .builder(packageName, metaClass) //
        .build();

    JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(qualifiedGenEntityName);
    Writer writer = builderFile.openWriter();
    javaFile.writeTo(writer);

    writer.close();

    TypeName generatedTypeName = ClassName.bestGuess(qualifiedGenEntityName);

    return generateFieldMetamodel(chain, chainedFieldName, generatedTypeName);
  }

  private void extractFieldMetamodels(TypeName entity, List<FieldSpec> interceptors, List<ObjectGraphFieldSpec> fields,
      List<CodeBlock> initCodeBlocks, List<Triple<ObjectGraphFieldSpec, FieldSpec, CodeBlock>> fieldMetamodels) {
    for (Triple<ObjectGraphFieldSpec, FieldSpec, CodeBlock> fieldMetamodel : fieldMetamodels) {
      FieldSpec fieldSpec = fieldMetamodel.getSecond();
      fields.add(fieldMetamodel.getFirst());
      interceptors.add(fieldMetamodel.getSecond());
      initCodeBlocks.add(fieldMetamodel.getThird());

      // Add _SCORE field to Vector
      if (fieldSpec.type.toString().startsWith(VectorField.class.getName())) {
        String fieldName = fieldMetamodel.getFirst().fieldSpec().name;
        Pair<FieldSpec, CodeBlock> vectorFieldScore = generateUnboundMetamodelField(entity,
            "_" + fieldSpec.name + "_SCORE", "__" + fieldName + "_score", Double.class);
        interceptors.add(vectorFieldScore.getFirst());
        initCodeBlocks.add(vectorFieldScore.getSecond());
      }
    }
  }

  private TypeSpec getTypeSpecForFieldMetamodel(String genEntityName, List<FieldSpec> interceptors,
      List<ObjectGraphFieldSpec> fields, List<FieldSpec> nestedFieldsConstants, Builder blockBuilder) {
    CodeBlock staticBlock = blockBuilder.build();

    return TypeSpec.classBuilder(genEntityName) //
        .superclass(CollectionField.class) //
        .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
            .addParameter(SearchFieldAccessor.class, "searchFieldAccessor").addParameter(boolean.class, "indexed")
            .addStatement("super(searchFieldAccessor, indexed)").build())
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL) //
        .addFields(fields.stream().map(ObjectGraphFieldSpec::fieldSpec).toList()) //
        .addFields(nestedFieldsConstants) //
        .addStaticBlock(staticBlock) //
        .addFields(interceptors) //
        .build();
  }

  private List<Triple<ObjectGraphFieldSpec, FieldSpec, CodeBlock>> processNestedIndexableFields(TypeName entity,
      List<Element> chain) {
    Element fieldElement = chain.get(chain.size() - 1);
    TypeMirror typeMirror = fieldElement.asType();
    DeclaredType asDeclaredType = (DeclaredType) typeMirror;
    Element entityField = asDeclaredType.asElement();

    Indexed annotation = fieldElement.getAnnotation(Indexed.class);
    if (entity.toString().equals(entityField.toString()) && annotation != null) {
      Integer integer = depthMap.get(entity.toString());
      if (integer == null) {
        depthMap.put(entity.toString(), 1);
      } else {
        if (++integer > annotation.depth()) {
          return new ArrayList<>();
        }
        depthMap.put(entity.toString(), integer);
      }
    }

    List<Triple<ObjectGraphFieldSpec, FieldSpec, CodeBlock>> fieldMetamodels = new ArrayList<>();

    messager.printMessage(Diagnostic.Kind.NOTE, "Processing constants for " + fieldElement + " of type " + entityField);

    final String entityFieldName = fieldElement.toString();
    messager.printMessage(Diagnostic.Kind.NOTE, "entityFieldName => " + entityFieldName);

    Map<? extends Element, String> enclosedFields = getInstanceFields(entityField);

    messager.printMessage(Diagnostic.Kind.NOTE, "Enclosed subfield size() ==> " + enclosedFields.size());

    enclosedFields.forEach((field, getter) -> {
      boolean fieldIsIndexed = (field.getAnnotation(Indexed.class) != null) || (field.getAnnotation(
          Searchable.class) != null);
      boolean generateMetamodel = field.getAnnotation(Metamodel.class) != null;

      if (fieldIsIndexed || generateMetamodel) {
        List<Element> newChain = new ArrayList<>(chain);
        newChain.add(field);
        fieldMetamodels.addAll(processFieldMetamodel(entity, entityFieldName, newChain));
      }
    });

    return fieldMetamodels;
  }

  private Map<? extends Element, String> getInstanceFields(Element element) {
    if (objectTypeElement.equals(element)) {
      return Collections.emptyMap();
    }

    final Map<String, Element> getters = element.getEnclosedElements().stream()
        .filter(ee -> ee.getKind() == ElementKind.METHOD)
        // Only consider methods with no parameters
        .filter(ee -> ee.getEnclosedElements().stream().noneMatch(eee -> eee.getKind() == ElementKind.PARAMETER))
        // Todo: Filter out methods that returns void or Void
        .collect(Collectors.toMap(e -> e.getSimpleName().toString(), Function.identity()));

    final Set<String> isGetters = getters.values().stream()
        // todo: Filter out methods only returning boolean or Boolean
        .map(Element::getSimpleName).map(Object::toString).filter(n -> n.startsWith(IS_PREFIX)).map(n -> n.substring(2))
        .map(ObjectUtils::toLowercaseFirstCharacter).collect(Collectors.toSet());

    // Retrieve all declared non-final instance fields of the annotated class
    Map<Element, String> results = element.getEnclosedElements().stream()
        .filter(ee -> ee.getKind().isField() && !ee.getModifiers().contains(Modifier.STATIC) // Ignore static
            // fields
            && !ee.getModifiers().contains(Modifier.FINAL)) // Ignore final fields
        .collect(Collectors.toMap(Function.identity(),
            ee -> findGetter(ee, getters, isGetters, element.toString(), lombokGetterAvailable(element, ee))));

    Types types = processingEnvironment.getTypeUtils();
    List<? extends TypeMirror> superTypes = types.directSupertypes(element.asType());
    superTypes.stream().map(types::asElement).filter(superElement -> superElement.getKind().isClass()).findFirst()
        .ifPresent(superElement -> results.putAll(getInstanceFields(superElement)));

    return results;
  }

  private Map<? extends Element, String> getInstanceFields(TypeName entity) {
    Element element = getElementFromTypeName(entity);
    return getInstanceFields(element);
  }

  private Element getElementFromTypeName(TypeName typeName) {
    if (typeName instanceof ParameterizedTypeName parameterizedTypeName) {
      return getElementFromTypeName(parameterizedTypeName.rawType);
    } else if (typeName instanceof ClassName className) {
      return processingEnvironment.getElementUtils().getTypeElement(className.reflectionName());
    } else {
      throw new IllegalArgumentException("Unknown type name: " + typeName);
    }
  }

  private boolean lombokGetterAvailable(Element classElement, Element fieldElement) {
    final boolean globalEnable = isLombokAnnotated(classElement, "Data") || isLombokAnnotated(classElement, "Getter");
    final boolean localEnable = isLombokAnnotated(fieldElement, "Getter");
    final boolean disallowedAccessLevel = DISALLOWED_ACCESS_LEVELS.contains(
        getterAccessLevel(fieldElement).orElse("No access level defined"));
    return !disallowedAccessLevel && (globalEnable || localEnable);
  }

  private boolean isLombokAnnotated(final Element annotatedElement, final String lombokSimpleClassName) {
    try {
      final String className = "lombok." + lombokSimpleClassName;
      @SuppressWarnings(
          "unchecked"
      ) final java.lang.Class<java.lang.annotation.Annotation> clazz = (java.lang.Class<java.lang.annotation.Annotation>) java.lang.Class.forName(
          className);
      return annotatedElement.getAnnotation(clazz) != null;
    } catch (ClassNotFoundException ignored) {
      // ignore
    }
    return false;
  }

  private Optional<String> getterAccessLevel(final Element fieldElement) {

    final List<? extends AnnotationMirror> mirrors = fieldElement.getAnnotationMirrors();

    Map<? extends ExecutableElement, ? extends AnnotationValue> map = mirrors.stream()
        .filter(am -> "lombok.Getter".equals(am.getAnnotationType().toString())).findFirst()
        .map(AnnotationMirror::getElementValues).orElse(Collections.emptyMap());

    return map.values().stream() //
        .map(AnnotationValue::toString).map(v -> v.substring(v.lastIndexOf('.') + 1)) // Format as simple name
        .filter(this::isAccessLevel).findFirst();
  }

  private boolean isAccessLevel(String s) {
    Set<String> validAccessLevels = Stream.of("PACKAGE", "NONE", "PRIVATE", "MODULE", "PROTECTED", "PUBLIC")
        .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
    return validAccessLevels.contains(s);
  }

  private String findGetter(final Element field, final Map<String, Element> getters, final Set<String> isGetters,
      final String entityName, boolean lombokGetterAvailable) {

    final String fieldName = field.getSimpleName().toString();
    final String getterPrefix = isGetters.contains(fieldName) ? IS_PREFIX : GET_PREFIX;

    final String standardJavaName = ObjectUtils.javaNameFromExternal(fieldName);

    final String standardGetterName = getterPrefix + standardJavaName;

    final Element standardGetter = getters.get(standardGetterName);

    if (standardGetter != null || lombokGetterAvailable) {
      // We got lucky because the user elected to conform
      // to the standard JavaBean notation.
      return entityName + "::" + standardGetterName;
    }

    final String lambdaName = ObjectUtils.toLowercaseFirstCharacter(entityName);

    if (!field.getModifiers().contains(Modifier.PROTECTED) && !field.getModifiers().contains(Modifier.PRIVATE)) {
      // We can use a lambda. Great escape hatch!
      return lambdaName + " -> " + lambdaName + "." + fieldName;
    }

    // default to thrower
    messager.printMessage(Diagnostic.Kind.ERROR,
        "Class " + entityName + " is not a proper JavaBean because " + field.getSimpleName()
            .toString() + " has no standard getter.");
    return lambdaName + " -> {throw new " + IllegalJavaBeanException.class.getSimpleName() + "(" + entityName + ".class, \"" + fieldName + "\");}";
  }

  private Triple<ObjectGraphFieldSpec, FieldSpec, CodeBlock> generateFieldMetamodel( //
      TypeName entity, //
      List<Element> chain, //
      String chainFieldName, //
      TypeName entityField, //
      Class<?> interceptorClass, //
      boolean fieldIsIndexed, //
      String collectionPrefix, //
      String searchSchemaAlias //
  ) {
    String fieldAccessor = ObjectUtils.staticField(chainFieldName);

    FieldSpec objectField = FieldSpec //
        .builder(Field.class, chainFieldName).addModifiers(Modifier.PUBLIC, Modifier.STATIC) //
        .build();

    ObjectGraphFieldSpec ogf = new ObjectGraphFieldSpec(objectField, chain);

    TypeName interceptor = ParameterizedTypeName.get(ClassName.get(interceptorClass), entity, entityField);

    FieldSpec aField = FieldSpec //
        .builder(interceptor, fieldAccessor).addModifiers(Modifier.PUBLIC, Modifier.STATIC) //
        .build();

    String alias;
    if (!isEmpty(searchSchemaAlias)) {
      alias = searchSchemaAlias;
    } else {
      alias = chain.stream().map(e -> e.getSimpleName().toString()).collect(Collectors.joining("_"));
      alias = collectionPrefix != null ? collectionPrefix + "_" + alias : alias;
    }

    String jsonPath = chain.stream().map(e -> e.getSimpleName().toString()).collect(Collectors.joining("."));
    jsonPath = "$." + (collectionPrefix != null ? collectionPrefix + "." + jsonPath : jsonPath);

    CodeBlock aFieldInit = CodeBlock //
        .builder() //
        .addStatement( //
            "$L = new $T(new $T(\"$L\", \"$L\", $L),$L)", //
            fieldAccessor, //
            interceptor, //
            SearchFieldAccessor.class, //
            alias, //
            jsonPath, //
            chainFieldName, //
            fieldIsIndexed //
        ) //
        .build();

    return Tuples.of(ogf, aField, aFieldInit);
  }

  private Triple<ObjectGraphFieldSpec, FieldSpec, CodeBlock> generateFieldMetamodel(List<Element> chain, //
      String chainFieldName, //
      TypeName interceptor //
  ) {
    String fieldAccessor = ObjectUtils.staticField(chainFieldName);

    FieldSpec objectField = FieldSpec.builder(Field.class, chainFieldName)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC).build();
    ObjectGraphFieldSpec ogf = new ObjectGraphFieldSpec(objectField, chain);

    FieldSpec aField = FieldSpec.builder(interceptor, fieldAccessor).addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .build();

    String searchSchemaAlias = chain.stream().map(e -> e.getSimpleName().toString()).collect(Collectors.joining("_"));
    String jsonPath = "$." + chain.stream().map(e -> e.getSimpleName().toString()).collect(Collectors.joining("."));

    CodeBlock aFieldInit = CodeBlock.builder()
        .addStatement("$L = new $T(new $T(\"$L\", \"$L\", $L),$L)", fieldAccessor, interceptor,
            SearchFieldAccessor.class, searchSchemaAlias, jsonPath, chainFieldName, true).build();

    return Tuples.of(ogf, aField, aFieldInit);
  }

  private Pair<FieldSpec, CodeBlock> generateUnboundMetamodelField(TypeName entity, String name, String alias,
      Class<?> type) {
    TypeName interceptor = ParameterizedTypeName.get(ClassName.get(MetamodelField.class), entity, TypeName.get(type));

    FieldSpec aField = FieldSpec.builder(interceptor, name).addModifiers(Modifier.PUBLIC, Modifier.STATIC).build();

    CodeBlock aFieldInit = CodeBlock.builder()
        .addStatement("$L = new $T(\"$L\", $T.class, $L)", name, interceptor, alias, type, true).build();

    return Tuples.of(aField, aFieldInit);
  }

  private Pair<FieldSpec, CodeBlock> generateThisMetamodelField(TypeName entity) {
    String name = "_THIS";
    String alias = "__this";
    TypeName interceptor = ParameterizedTypeName.get(ClassName.get(MetamodelField.class), entity, entity);

    FieldSpec aField = FieldSpec.builder(interceptor, name).addModifiers(Modifier.PUBLIC, Modifier.STATIC).build();

    CodeBlock aFieldInit = CodeBlock.builder()
        .addStatement("$L = new $T(\"$L\", $T.class, $L)", name, interceptor, alias, entity, true).build();

    return Tuples.of(aField, aFieldInit);
  }

  private boolean isEnum(ProcessingEnvironment processingEnv, TypeMirror typeMirror) {
    Types typeUtils = processingEnv.getTypeUtils();

    Element element = typeUtils.asElement(typeMirror);
    if (element != null) {
      return element.getKind() == ElementKind.ENUM;
    } else {
      return false;
    }
  }
}
