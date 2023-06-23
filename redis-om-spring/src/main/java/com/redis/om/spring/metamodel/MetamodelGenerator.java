package com.redis.om.spring.metamodel;

import com.github.f4b6a3.ulid.Ulid;
import com.google.auto.service.AutoService;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.SchemaFieldType;
import com.redis.om.spring.annotations.Searchable;
import com.redis.om.spring.metamodel.indexed.*;
import com.redis.om.spring.metamodel.nonindexed.*;
import com.redis.om.spring.tuple.Pair;
import com.redis.om.spring.tuple.Triple;
import com.redis.om.spring.tuple.Tuples;
import com.redis.om.spring.util.ObjectUtils;
import com.squareup.javapoet.*;
import org.springframework.data.annotation.Id;
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
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@SupportedAnnotationTypes(value = {"com.redis.om.spring.annotations.Document","org.springframework.data.redis.core.RedisHash"})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public final class MetamodelGenerator extends AbstractProcessor {

  static final String GET_PREFIX = "get";
  static final String IS_PREFIX = "is";

  private ProcessingEnvironment processingEnvironment;
  private Messager messager;

  private TypeElement objectTypeElement;

  public MetamodelGenerator() {}

  @Override
  public synchronized void init(ProcessingEnvironment env) {
    super.init(env);

    this.processingEnvironment = env;
    processingEnvironment.getElementUtils();
    processingEnvironment.getTypeUtils();

    messager = processingEnvironment.getMessager();
    messager.printMessage(Diagnostic.Kind.NOTE, "Redis OM Spring Entity Metamodel Generator");

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
        messager.printMessage(Diagnostic.Kind.ERROR, "Cannot generate metamodel class for " + ae.getClass().getName() + " because " + ioe.getMessage());
      }
    });

    return true;
  }

  void generateMetaModelClass(final Element annotatedElement) throws IOException {
    String qualifiedGenEntityName = annotatedElement.asType().toString() + "$";
    final String entityName = ObjectUtils.shortName(annotatedElement.asType().toString());
    final String genEntityName = entityName + "$";
    TypeName entity = TypeName.get(annotatedElement.asType());

    messager.printMessage(Diagnostic.Kind.NOTE, "Generating Entity Metamodel: " + qualifiedGenEntityName);

    Map<? extends Element, String> enclosedFields = getInstanceFields(annotatedElement);

    final PackageElement packageElement = processingEnvironment.getElementUtils().getPackageOf(annotatedElement);
    String packageName;
    if (packageElement.isUnnamed()) {
      messager.printMessage(Diagnostic.Kind.WARNING, "Class " + entityName + " has an unnamed package.");
      packageName = "";
    } else {
      packageName = packageElement.getQualifiedName().toString();
    }

    List<FieldSpec> interceptors = new ArrayList<>();
    List<ObjectGraphFieldSpec> fields = new ArrayList<>();
    List<CodeBlock> initCodeBlocks = new ArrayList<>();
    List<FieldSpec> nestedFieldsConstants = new ArrayList<>();

    enclosedFields.forEach((field, getter) -> {
      List<Triple<ObjectGraphFieldSpec, FieldSpec, CodeBlock>> fieldMetamodels = processFieldMetamodel(entity, entityName, List.of(field));
      for (Triple<ObjectGraphFieldSpec, FieldSpec, CodeBlock> fieldMetamodel: fieldMetamodels) {
        FieldSpec fieldSpec = fieldMetamodel.getSecond();
        fields.add(fieldMetamodel.getFirst());
        interceptors.add(fieldMetamodel.getSecond());
        initCodeBlocks.add(fieldMetamodel.getThird());

        // Add _SCORE field to Vector
        if (fieldSpec.type.toString().startsWith(VectorField.class.getName())) {
          String fieldName = fieldMetamodel.getFirst().fieldSpec().name;
          Pair<FieldSpec, CodeBlock> vectorFieldScore = generateUnboundMetamodelField(entity, "_" + fieldSpec.name + "_SCORE", "__" + fieldName + "_score", Double.class);
          interceptors.add(vectorFieldScore.getFirst());
          initCodeBlocks.add(vectorFieldScore.getSecond());
        }
      }
    });

    Pair<FieldSpec, CodeBlock> keyAccessor = generateUnboundMetamodelField(entity, "_KEY", "__key", String.class);
    interceptors.add(keyAccessor.getFirst());
    initCodeBlocks.add(keyAccessor.getSecond());

    CodeBlock.Builder blockBuilder = CodeBlock.builder();

    boolean hasFields = !fields.isEmpty();
    if (hasFields) blockBuilder.beginControlFlow("try");
    for (ObjectGraphFieldSpec ogfs : fields) {
      StringBuilder sb = new StringBuilder("$T.class");
      for (int i = 0; i < ogfs.chain().size(); i++) {
        Element element = ogfs.chain().get(i);
        if (i != 0) {
          sb.append(".getType()");
        }
        String formattedString = String.format("com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(%s, \"%s\")", sb.toString(), element.getSimpleName());
        sb.setLength(0); // clear the builder
        sb.append(formattedString);
      }
      FieldSpec fieldSpec = ogfs.fieldSpec();
      blockBuilder.addStatement("$L = " + sb, fieldSpec.name, entity);
    }

    for (CodeBlock initCodeBlock : initCodeBlocks) {
      blockBuilder.add(initCodeBlock);
    }

    if (hasFields) {
      blockBuilder.nextControlFlow("catch($T | $T e)", NoSuchFieldException.class, SecurityException.class);
      blockBuilder.addStatement("System.err.println(e.getMessage())");
      blockBuilder.endControlFlow();
    }

    CodeBlock staticBlock = blockBuilder.build();

    TypeSpec metaClass = TypeSpec.classBuilder(genEntityName) //
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL) //
        .addFields(fields.stream().map(ObjectGraphFieldSpec::fieldSpec).toList()) //
        .addFields(nestedFieldsConstants) //
        .addStaticBlock(staticBlock) //
        .addFields(interceptors) //
        // .addJavadoc(filename, null)
        .build();

    JavaFile javaFile = JavaFile //
        .builder(packageName, metaClass) //
        .build();

    JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(qualifiedGenEntityName);
    Writer writer = builderFile.openWriter();
    javaFile.writeTo(writer);

    writer.close();
  }

  private List<Triple<ObjectGraphFieldSpec, FieldSpec, CodeBlock>> processFieldMetamodel(TypeName entity, String entityName, List<Element> chain) {
    return processFieldMetamodel(entity, entityName, chain, null);
  }

  private List<Triple<ObjectGraphFieldSpec, FieldSpec, CodeBlock>> processFieldMetamodel(TypeName entity, String entityName, List<Element> chain, String collectionPrefix) {
    List<Triple<ObjectGraphFieldSpec, FieldSpec, CodeBlock>> fieldMetamodelSpec = new ArrayList<>();

    Element field = chain.get(chain.size() - 1);

    boolean fieldIsIndexed = (field.getAnnotation(Searchable.class) != null)
        || (field.getAnnotation(Indexed.class) != null)
        || (field.getAnnotation(Id.class) != null);

    String chainedFieldName = chain.stream().map(Element::getSimpleName).collect(Collectors.joining("_"));
    messager.printMessage(Diagnostic.Kind.NOTE, "Processing " + chainedFieldName);
    TypeName entityField = TypeName.get(field.asType());

    TypeMirror fieldType = field.asType();
    String fullTypeClassName = fieldType.toString();
    String cls = ObjectUtils.getTargetClassName(fullTypeClassName);

    if (field.asType().getKind().isPrimitive()) {
      Class<?> primitive = ClassUtils.resolvePrimitiveClassName(cls);
      if (primitive == null) return Collections.emptyList();
      Class<?> primitiveWrapper = ClassUtils.resolvePrimitiveIfNecessary(primitive);
      entityField = TypeName.get(primitiveWrapper);
      fullTypeClassName = entityField.toString();
      cls = ObjectUtils.getTargetClassName(fullTypeClassName);
    }

    Class<?> targetInterceptor = null;
    Class<?> targetCls = null;

    var indexed = field.getAnnotation(Indexed.class);
    var searchable = field.getAnnotation(Searchable.class);

    if (searchable != null) {
      targetInterceptor = TextField.class;
    } else if (indexed != null || field.getAnnotation(Id.class) != null) {
      try {
        targetCls = ClassUtils.forName(cls, MetamodelGenerator.class.getClassLoader());
      } catch (ClassNotFoundException cnfe) {
        messager.printMessage(Diagnostic.Kind.WARNING,
            "Processing class " + entityName + " could not resolve " + cls + " while checking for nested @Indexed");
        fieldMetamodelSpec.addAll(processNestedIndexableFields(entity, chain));
      }

      if (indexed != null && indexed.schemaFieldType() != SchemaFieldType.AUTODETECT) {
        // here we do the non autodetect annotated fields
        switch (indexed.schemaFieldType()) {
          case TAG -> targetInterceptor = TextTagField.class;
          case NUMERIC -> targetInterceptor = NumericField.class;
          case GEO -> targetInterceptor = GeoField.class;
          case VECTOR -> targetInterceptor = VectorField.class;
        }
      } else if (indexed != null && targetCls == null && isEnum(processingEnv, fieldType)) {
        targetInterceptor = TextTagField.class;
      } else if (targetCls != null) {
        //
        // Any Character class -> Tag Search Field
        //
        if (CharSequence.class.isAssignableFrom(targetCls) || (targetCls == Ulid.class)) {
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
            || (targetCls == Date.class) || (targetCls == Instant.class)) {
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

    } else {
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
        else if (Number.class.isAssignableFrom(targetCls) || (targetCls == LocalDateTime.class)
            || (targetCls == LocalDate.class) || (targetCls == Date.class) || (targetCls == Instant.class)) {
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
        messager.printMessage(Diagnostic.Kind.WARNING,
            "Processing class " + entityName + " could not resolve " + cls);
      }
    }

    if (targetInterceptor != null) {
      fieldMetamodelSpec.add(generateFieldMetamodel(entity, chain, chainedFieldName, entityField, targetInterceptor, fieldIsIndexed, collectionPrefix));
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
    final String genEntityName =  qualifiedGenEntityName.substring(qualifiedGenEntityName.lastIndexOf('.') + 1);
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
      List<Triple<ObjectGraphFieldSpec, FieldSpec, CodeBlock>> fieldMetamodels = processFieldMetamodel(entity, entityName, List.of(field), chainedFieldName);
      for (Triple<ObjectGraphFieldSpec, FieldSpec, CodeBlock> fieldMetamodel: fieldMetamodels) {
        FieldSpec fieldSpec = fieldMetamodel.getSecond();
        fields.add(fieldMetamodel.getFirst());
        interceptors.add(fieldMetamodel.getSecond());
        initCodeBlocks.add(fieldMetamodel.getThird());

        // Add _SCORE field to Vector
        if (fieldSpec.type.toString().startsWith(VectorField.class.getName())) {
          String fieldName = fieldMetamodel.getFirst().fieldSpec().name;
          Pair<FieldSpec, CodeBlock> vectorFieldScore = generateUnboundMetamodelField(entity, "_" + fieldSpec.name + "_SCORE", "__" + fieldName + "_score", Double.class);
          interceptors.add(vectorFieldScore.getFirst());
          initCodeBlocks.add(vectorFieldScore.getSecond());
        }
      }
    });

    CodeBlock.Builder blockBuilder = CodeBlock.builder();

    blockBuilder.beginControlFlow("try");
    for (ObjectGraphFieldSpec ogfs : fields) {
      StringBuilder sb = new StringBuilder("$T.class");
      for (int i = 0; i < ogfs.chain().size(); i++) {
        Element element = ogfs.chain().get(i);
        if (i != 0) {
          sb.append(".getType()");
        }
        String formattedString = String.format("com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(%s, \"%s\")", sb.toString(), element.getSimpleName());
        sb.setLength(0); // clear the buffer
        sb.append(formattedString);
      }
      FieldSpec fieldSpec = ogfs.fieldSpec();
      blockBuilder.addStatement("$L = " + sb.toString(), fieldSpec.name, entity);
    }

    for (CodeBlock initCodeBlock : initCodeBlocks) {
      blockBuilder.add(initCodeBlock);
    }

    blockBuilder.nextControlFlow("catch($T | $T e)", NoSuchFieldException.class, SecurityException.class);
    blockBuilder.addStatement("System.err.println(e.getMessage())");
    blockBuilder.endControlFlow();

    CodeBlock staticBlock = blockBuilder.build();

    TypeSpec metaClass = TypeSpec.classBuilder(genEntityName) //
        .superclass(CollectionField.class) //
        .addMethod(MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(SearchFieldAccessor.class, "searchFieldAccessor")
            .addParameter(boolean.class, "indexed")
            .addStatement("super(searchFieldAccessor, indexed)")
            .build())
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL) //
        .addFields(fields.stream().map(ObjectGraphFieldSpec::fieldSpec).toList()) //
        .addFields(nestedFieldsConstants) //
        .addStaticBlock(staticBlock) //
        .addFields(interceptors) //
        .build();

    JavaFile javaFile = JavaFile //
        .builder(packageName, metaClass) //
        .build();

    JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(qualifiedGenEntityName);
    Writer writer = builderFile.openWriter();
    javaFile.writeTo(writer);

    writer.close();

    TypeName generatedTypeName = ClassName.bestGuess(qualifiedGenEntityName);

    return generateFieldMetamodel(chain, chainedFieldName, generatedTypeName, true);
  }

  private List<Triple<ObjectGraphFieldSpec, FieldSpec, CodeBlock>> processNestedIndexableFields(TypeName entity,
      List<Element> chain) {
    Element fieldElement = chain.get(chain.size() - 1);
    TypeMirror typeMirror = fieldElement.asType();
    DeclaredType asDeclaredType = (DeclaredType)typeMirror;
    Element entityField = asDeclaredType.asElement();

    List<Triple<ObjectGraphFieldSpec, FieldSpec, CodeBlock>> fieldMetamodels = new ArrayList<>();

    messager.printMessage(Diagnostic.Kind.NOTE, "Processing constants for " + fieldElement + " of type " + entityField);

    final String entityFieldName = fieldElement.toString();
    messager.printMessage(Diagnostic.Kind.NOTE, "entityFieldName => " + entityFieldName);

    Map<? extends Element, String> enclosedFields = getInstanceFields(entityField);

    messager.printMessage(Diagnostic.Kind.NOTE, "Enclosed subfield size() ==> " + enclosedFields.size());

    enclosedFields.forEach((field, getter) -> {
      boolean fieldIsIndexed = (field.getAnnotation(Indexed.class) != null) || (field.getAnnotation(Searchable.class) != null);

      if (fieldIsIndexed) {
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
        .map(ObjectUtils::lcfirst).collect(Collectors.toSet());

    // Retrieve all declared non-final instance fields of the annotated class
    Map<Element, String> results = element.getEnclosedElements().stream()
        .filter(ee -> ee.getKind().isField() && !ee.getModifiers().contains(Modifier.STATIC) // Ignore static fields
            && !ee.getModifiers().contains(Modifier.FINAL)) // Ignore final fields
        .collect(Collectors.toMap(Function.identity(),
            ee -> findGetter(ee, getters, isGetters, element.toString(), lombokGetterAvailable(element, ee))));

    Types types = processingEnvironment.getTypeUtils();
    List<? extends TypeMirror> superTypes = types.directSupertypes(element.asType());
    superTypes.stream()
            .map(types::asElement)
            .filter(superElement -> superElement.getKind().isClass())
            .findFirst()
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

  private static final Set<String> DISALLOWED_ACCESS_LEVELS = Stream.of("PROTECTED", "PRIVATE", "NONE")
      .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));

  private boolean lombokGetterAvailable(Element classElement, Element fieldElement) {
    final boolean globalEnable = isLombokAnnotated(classElement, "Data") || isLombokAnnotated(classElement, "Getter");
    final boolean localEnable = isLombokAnnotated(fieldElement, "Getter");
    final boolean disallowedAccessLevel = DISALLOWED_ACCESS_LEVELS
        .contains(getterAccessLevel(fieldElement).orElse("No access level defined"));
    return !disallowedAccessLevel && (globalEnable || localEnable);
  }

  private boolean isLombokAnnotated(final Element annotatedElement, final String lombokSimpleClassName) {
    try {
      final String className = "lombok." + lombokSimpleClassName;
      @SuppressWarnings("unchecked")
      final java.lang.Class<java.lang.annotation.Annotation> clazz = (java.lang.Class<java.lang.annotation.Annotation>) java.lang.Class
          .forName(className);
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
        .map(AnnotationValue::toString)
        .map(v -> v.substring(v.lastIndexOf('.') + 1)) // Format as simple name
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

    final String standardJavaName = javaNameFromExternal(fieldName);

    final String standardGetterName = getterPrefix + standardJavaName;

    final Element standardGetter = getters.get(standardGetterName);

    if (standardGetter != null || lombokGetterAvailable) {
      // We got lucky because the user elected to conform
      // to the standard JavaBean notation.
      return entityName + "::" + standardGetterName;
    }

    final String lambdaName = ObjectUtils.lcfirst(entityName);

    if (!field.getModifiers().contains(Modifier.PROTECTED) && !field.getModifiers().contains(Modifier.PRIVATE)) {
      // We can use a lambda. Great escape hatch!
      return lambdaName + " -> " + lambdaName + "." + fieldName;
    }

    // default to thrower
    messager.printMessage(Diagnostic.Kind.ERROR, "Class " + entityName + " is not a proper JavaBean because "
        + field.getSimpleName().toString() + " has no standard getter.");
    return lambdaName + " -> {throw new " + IllegalJavaBeanException.class.getSimpleName() + "(" + entityName
        + ".class, \"" + fieldName + "\");}";
  }

  /**
   * Returns a static field name representation of the specified camel-cased
   * string.
   *
   * @param externalName the string
   * @return the static field name representation
   */
  public static String staticField(final String externalName) {
    requireNonNull(externalName);
    return ObjectUtils.toUnderscoreSeparated(javaNameFromExternal(externalName)).toUpperCase();
  }

  public static String javaNameFromExternal(final String externalName) {
    requireNonNull(externalName);
    return MetamodelGenerator
        .replaceIfIllegalJavaIdentifierCharacter(replaceIfJavaUsedWord(nameFromExternal(externalName)));
  }

  public static String nameFromExternal(final String externalName) {
    requireNonNull(externalName);
    String result = ObjectUtils.unQuote(externalName.trim()); // Trim if there are initial spaces or trailing spaces...
    /* CamelCase
     * http://stackoverflow.com/questions/4050381/regular-expression-for-checking-if
     * -capital-letters-are-found-consecutively-in-a [A-Z] -> \p{Lu} [^A-Za-z0-9] ->
     * [^\pL0-90-9] */
    result = Stream.of(result.replaceAll("(\\p{Lu}+)", "_$1").split("[^\\pL\\d]")).map(String::toLowerCase)
        .map(ObjectUtils::ucfirst).collect(Collectors.joining());
    return result;
  }

  public static String replaceIfJavaUsedWord(final String word) {
    requireNonNull(word);
    // We need to replace regardless of case because we do not know how the returned
    // string is to be used
    if (JAVA_USED_WORDS_LOWER_CASE.contains(word.toLowerCase())) {
      // If it is a java reserved/literal/class, add a "_" at the end to avoid naming
      // conflicts
      return word + "_";
    }
    return word;
  }

  public static final Character REPLACEMENT_CHARACTER = '_';

  private Triple<ObjectGraphFieldSpec, FieldSpec, CodeBlock> generateFieldMetamodel( //
      TypeName entity, //
      List<Element> chain, //
      String chainFieldName, //
      TypeName entityField, //
      Class<?> interceptorClass, //
      boolean fieldIsIndexed, //
      String collectionPrefix //
  ) {
    String fieldAccessor = staticField(chainFieldName);

    FieldSpec objectField = FieldSpec //
        .builder(Field.class, chainFieldName).addModifiers(Modifier.PUBLIC, Modifier.STATIC) //
        .build();

    ObjectGraphFieldSpec ogf = new ObjectGraphFieldSpec(objectField, chain);

    TypeName interceptor = ParameterizedTypeName.get(ClassName.get(interceptorClass), entity, entityField);

    FieldSpec aField = FieldSpec //
        .builder(interceptor, fieldAccessor).addModifiers(Modifier.PUBLIC, Modifier.STATIC) //
        .build();

    String searchSchemaAlias = chain.stream().map(e -> e.getSimpleName().toString()).collect(Collectors.joining("_"));
    searchSchemaAlias = collectionPrefix != null ? collectionPrefix + "_" + searchSchemaAlias : searchSchemaAlias;

    CodeBlock aFieldInit = CodeBlock //
        .builder() //
        .addStatement( //
            "$L = new $T(new $T(\"$L\", $L),$L)", //
            fieldAccessor, //
            interceptor, //
            SearchFieldAccessor.class, //
            searchSchemaAlias, //
            chainFieldName, //
            fieldIsIndexed //
        ) //
        .build();

    return Tuples.of(ogf, aField, aFieldInit);
  }

  private Triple<ObjectGraphFieldSpec, FieldSpec, CodeBlock> generateFieldMetamodel(
      List<Element> chain, //
      String chainFieldName, //
      TypeName interceptor, //
      boolean fieldIsIndexed //
  ) {
    String fieldAccessor = staticField(chainFieldName);

    FieldSpec objectField = FieldSpec.builder(Field.class, chainFieldName).addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .build();
    ObjectGraphFieldSpec ogf = new ObjectGraphFieldSpec(objectField, chain);

    FieldSpec aField = FieldSpec.builder(interceptor, fieldAccessor).addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .build();

    String searchSchemaAlias = chain.stream().map(e -> e.getSimpleName().toString()).collect(Collectors.joining("_"));

    CodeBlock aFieldInit = CodeBlock.builder()
        .addStatement("$L = new $T(new $T(\"$L\", $L),$L)", fieldAccessor, interceptor, SearchFieldAccessor.class, searchSchemaAlias, chainFieldName, fieldIsIndexed).build();

    return Tuples.of(ogf, aField, aFieldInit);
  }


  private Pair<FieldSpec, CodeBlock> generateUnboundMetamodelField(TypeName entity, String name, String alias, Class<?> type) {
    TypeName interceptor = ParameterizedTypeName.get(ClassName.get(MetamodelField.class), entity, TypeName.get(type));

    FieldSpec aField = FieldSpec.builder(interceptor, name).addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .build();

    CodeBlock aFieldInit = CodeBlock.builder()
        .addStatement("$L = new $T(\"$L\", $T.class, $L)", name, interceptor, alias, type, true).build();

    return Tuples.of(aField, aFieldInit);
  }

  public static String replaceIfIllegalJavaIdentifierCharacter(final String word) {
    requireNonNull(word);
    if (word.isEmpty()) {
      return REPLACEMENT_CHARACTER.toString(); // No name is translated to REPLACEMENT_CHARACTER only
    }
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < word.length(); i++) {
      char c = word.charAt(i);
      if (i == 0) {
        if (Character.isJavaIdentifierStart(c)) {
          // Fine! Just add the first character
          sb.append(c);
        } else if (Character.isJavaIdentifierPart(c)) {
          // Not ok as the first, but ok otherwise. Add the replacement before it
          sb.append(REPLACEMENT_CHARACTER).append(c);
        } else {
          // Cannot be used as a java identifier. Replace it
          sb.append(REPLACEMENT_CHARACTER);
        }
      } else if (Character.isJavaIdentifierPart(c)) {
        // Fine! Just add it
        sb.append(c);
      } else {
        // Cannot be used as a java identifier. Replace it
        sb.append(REPLACEMENT_CHARACTER);
      }

    }
    return sb.toString();
  }

  static final Set<String> JAVA_LITERAL_WORDS = Set.of("true", "false", "null");

  // Java reserved keywords
  static final Set<String> JAVA_RESERVED_WORDS = Collections.unmodifiableSet(Stream.of(
      // Unused
      "const", "goto",
      // The real ones...
      "abstract", "continue", "for", "new", "switch", "assert", "default", "goto", "package", "synchronized", "boolean",
      "do", "if", "private", "this", "break", "double", "implements", "protected", "throw", "byte", "else", "import",
      "public", "throws", "case", "enum", "instanceof", "return", "transient", "catch", "extends", "int", "short",
      "try", "char", "final", "interface", "static", "void", "class", "finally", "long", "strictfp", "volatile",
      "const", "float", "native", "super", "while").collect(Collectors.toSet()));

  static final Set<Class<?>> JAVA_BUILT_IN_CLASSES = Set.of(Boolean.class, Byte.class, Character.class, Double.class,
      Float.class, Integer.class, Long.class, Object.class, Short.class, String.class, BigDecimal.class,
      BigInteger.class, boolean.class, byte.class, char.class, double.class, float.class, int.class, long.class,
      short.class);

  private static final Set<String> JAVA_BUILT_IN_CLASS_WORDS = Collections
      .unmodifiableSet(JAVA_BUILT_IN_CLASSES.stream().map(Class::getSimpleName).collect(Collectors.toSet()));

  private static final Set<String> JAVA_USED_WORDS = Collections
      .unmodifiableSet(Stream.of(JAVA_LITERAL_WORDS, JAVA_RESERVED_WORDS, JAVA_BUILT_IN_CLASS_WORDS)
          .flatMap(Collection::stream).collect(Collectors.toSet()));

  private static final Set<String> JAVA_USED_WORDS_LOWER_CASE = Collections
      .unmodifiableSet(JAVA_USED_WORDS.stream().map(String::toLowerCase).collect(Collectors.toSet()));

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
