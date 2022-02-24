package com.redis.om.spring.metamodel;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.springframework.data.geo.Point;
import org.springframework.util.ClassUtils;

import com.google.auto.service.AutoService;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import com.redis.om.spring.tuple.Triple;
import com.redis.om.spring.tuple.Tuples;
import com.redis.om.spring.util.ObjectUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

@SupportedAnnotationTypes("com.redis.om.spring.annotations.Document")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public final class MetamodelGenerator extends AbstractProcessor {

  protected static final String GET_PREFIX = "get";
  protected static final String IS_PREFIX = "is";

  private ProcessingEnvironment processingEnvironment;
  private Messager messager;

  @Override
  public synchronized void init(ProcessingEnvironment env) {
    super.init(env);

    this.processingEnvironment = env;
    processingEnvironment.getElementUtils();
    processingEnvironment.getTypeUtils();

    messager = processingEnvironment.getMessager();
    messager.printMessage(Diagnostic.Kind.NOTE, "Redis OM Spring Field Generator Processor");
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    if (annotations.isEmpty() || roundEnv.processingOver()) {
      // Allow other processors to run
      return false;
    }

    roundEnv.getElementsAnnotatedWith(Document.class).stream().filter(ae -> ae.getKind() == ElementKind.CLASS)
        .forEach(ae -> {
          try {
            generateMetaModelClass(ae);
          } catch (IOException e) {
            e.printStackTrace();
          }
        });

    return true;
  }

  void generateMetaModelClass(final Element annotatedElement) throws IOException {
    String qualifiedGenEntityName = annotatedElement.asType().toString() + "$";
    final String entityName = shortName(annotatedElement.asType().toString());
    final String genEntityName = entityName + "$";
    TypeName entity = TypeName.get(annotatedElement.asType());

    final Map<String, Element> getters = annotatedElement.getEnclosedElements().stream()
        .filter(ee -> ee.getKind() == ElementKind.METHOD)
        // Only consider methods with no parameters
        .filter(ee -> ee.getEnclosedElements().stream().noneMatch(eee -> eee.getKind() == ElementKind.PARAMETER))
        // Todo: Filter out methods that returns void or Void
        .collect(Collectors.toMap(e -> e.getSimpleName().toString(), Function.identity()));

    final Set<String> isGetters = getters.values().stream()
        // todo: Filter out methods only returning boolean or Boolean
        .map(Element::getSimpleName).map(Object::toString).filter(n -> n.startsWith(IS_PREFIX)).map(n -> n.substring(2))
        .map(MetamodelGenerator::lcfirst).collect(Collectors.toSet());

    // Retrieve all declared non-final instance fields of the annotated class
    Map<? extends Element, String> enclosedFields = annotatedElement.getEnclosedElements().stream()
        .filter(ee -> ee.getKind().isField() && !ee.getModifiers().contains(Modifier.STATIC) // Ignore static fields
            && !ee.getModifiers().contains(Modifier.FINAL)) // Ignore final fields
        .collect(Collectors.toMap(Function.identity(),
            ee -> findGetter(ee, getters, isGetters, entityName, lombokGetterAvailable(annotatedElement, ee))));

    final PackageElement packageElement = processingEnvironment.getElementUtils().getPackageOf(annotatedElement);
    String packageName;
    if (packageElement.isUnnamed()) {
      messager.printMessage(Diagnostic.Kind.WARNING, "Class " + entityName + " has an unnamed package.");
      packageName = "";
    } else {
      packageName = packageElement.getQualifiedName().toString();
    }

    List<FieldSpec> interceptors = new ArrayList<FieldSpec>();
    List<FieldSpec> fields = new ArrayList<FieldSpec>();
    List<CodeBlock> initCodeBlocks = new ArrayList<CodeBlock>();
    enclosedFields.forEach((field, getter) -> {
      boolean fieldIsIndexed = (field.getAnnotation(Searchable.class) != null) || (field.getAnnotation(Indexed.class) != null);
      
      String fieldName = field.getSimpleName().toString();
      TypeName entityField = TypeName.get(field.asType());
      
      TypeMirror fieldType = field.asType();
      String fullTypeClassName = fieldType.toString();
      String cls = ObjectUtils.getTargetClassName(fullTypeClassName);
      
      if (field.asType().getKind().isPrimitive()) {
        Class<?> primitive = ClassUtils.resolvePrimitiveClassName(cls);
        Class<?> primitiveWrapper = ClassUtils.resolvePrimitiveIfNecessary(primitive);
        entityField = TypeName.get(primitiveWrapper);
      } 
      
      Class<?> targetInterceptor = FieldOperationInterceptor.class;

      if (field.getAnnotation(Searchable.class) != null) {
        targetInterceptor = TextFieldOperationInterceptor.class;
      } else if (field.getAnnotation(Indexed.class) != null) {
        Class<?> targetCls;
        try {
          targetCls = ClassUtils.forName(cls, MetamodelGenerator.class.getClassLoader());
          
          //
          // Any Character class -> Tag Search Field
          //
          if (CharSequence.class.isAssignableFrom(targetCls)) {
            targetInterceptor = TagFieldOperationInterceptor.class;
          }
          //
          // Any Numeric class -> Numeric Search Field
          //
          else if (Number.class.isAssignableFrom(targetCls) || (targetCls == LocalDateTime.class) || (targetCls == LocalDate.class) || (targetCls == Date.class) ) {
            targetInterceptor = NumericFieldOperationInterceptor.class;
          }
          //
          // Set / List
          //
          else if (Set.class.isAssignableFrom(targetCls) || List.class.isAssignableFrom(targetCls)) {
            targetInterceptor = TagFieldOperationInterceptor.class;
          }
          //
          // Point
          //
          else if (targetCls == Point.class) {
            targetInterceptor = GeoFieldOperationInterceptor.class;
          }
        } catch (ClassNotFoundException cnfe) {
          messager.printMessage(Diagnostic.Kind.WARNING, "Processing class " + entityName + " could not resolve " + cls);
        }
      } 
      
      Triple<FieldSpec, FieldSpec, CodeBlock> fieldMetamodel = generateFieldMetamodel(entity, fieldName, entityField, targetInterceptor, fieldIsIndexed);

      fields.add(fieldMetamodel.getFirst());
      interceptors.add(fieldMetamodel.getSecond());
      initCodeBlocks.add(fieldMetamodel.getThird());
    });

    CodeBlock.Builder blockBuilder = CodeBlock.builder();

    blockBuilder.beginControlFlow("try");
    for (FieldSpec fieldSpec : fields) {
      blockBuilder.addStatement("$L = $T.class.getDeclaredField(\"$L\")", fieldSpec.name, entity, fieldSpec.name);
    }
    for (CodeBlock initCodeBlock : initCodeBlocks) {
      blockBuilder.add(initCodeBlock);
    }
    blockBuilder.nextControlFlow("catch($T | $T e)", NoSuchFieldException.class, SecurityException.class);
    blockBuilder.addStatement("System.err.println(\"e.getMessage()\")");
    blockBuilder.endControlFlow();

    CodeBlock staticBlock = blockBuilder.build();

    TypeSpec metaclass = TypeSpec.classBuilder(genEntityName)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL) //
        .addFields(fields) //
        .addStaticBlock(staticBlock) //
        .addFields(interceptors) //
        // .addJavadoc(filename, null)
        .build();

    JavaFile javaFile = JavaFile.builder(packageName, metaclass).build();
    JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(qualifiedGenEntityName);
    Writer writer = builderFile.openWriter();
    javaFile.writeTo(writer);

    writer.close();
  }

  /**
   * Returns the 'name' part of a long name. This is everything after the last dot
   * for non-parameterized types. For parameterized types the rule applies to the
   * part proceeding the bracket enclosed parameters e.g.
   * {@code long name java.util.Map<String, java.util.Date>} returns
   * {@code Map<String, java.util.Date>}.
   *
   * @param longName The long name.
   * @return The name part.
   */
  public static String shortName(String longName) {
    String temp = longName.replace('$', '.');
    final int openBrPos = temp.indexOf('<');
    String parameters = "";
    if (openBrPos > 0) {
      parameters = temp.substring(openBrPos);
      temp = temp.substring(0, openBrPos);
    }
    if (temp.contains(".")) {
      temp = temp.substring(temp.lastIndexOf('.') + 1);
    }
    return temp + parameters;
  }

  /**
   * Does something with the first character in the specified String.
   *
   * @param input    The String.
   * @param callback The something.
   * @return The new String.
   */
  public static String withFirst(String input, Function<Character, String> callback) {
    if (input == null) {
      return null;
    } else if (input.length() == 0) {
      return "";
    } else {
      return String.join("", callback.apply(input.charAt(0)), input.subSequence(1, input.length()));
    }
  }

  /**
   * Returns the specified text but with the first character uppercase.
   *
   * @param input The text.
   * @return The resulting text.
   */
  public static String ucfirst(String input) {
    return withFirst(input, first -> String.valueOf(Character.toUpperCase(first)));
  }

  /**
   * Returns the specified text but with the first character lowercase.
   *
   * @param input The text.
   * @return The resulting text.
   */
  public static String lcfirst(String input) {
    return withFirst(input, first -> String.valueOf(Character.toLowerCase(first)));
  }

  public static boolean isFirstLowerCase(String string) {
    String first = string.substring(0, 1);
    return first.toLowerCase().equals(first);
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

    return map.values().stream().map(AnnotationValue::toString).map(v -> v.substring(v.lastIndexOf('.') + 1)) // Format
                                                                                                              // as
                                                                                                              // simple
                                                                                                              // name
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

    final String lambdaName = lcfirst(entityName);

    if (!field.getModifiers().contains(Modifier.PROTECTED) && !field.getModifiers().contains(Modifier.PRIVATE)) {
// We can use a lambda. Great escape hatch!
      return lambdaName + " -> " + lambdaName + "." + fieldName;
    }

// default to thrower

// Todo: This should be an error in the future
    messager.printMessage(Diagnostic.Kind.WARNING, "Class " + entityName + " is not a proper JavaBean because "
        + field.getSimpleName().toString() + " has no standard getter. Fix the issue to ensure stability.");
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
    return toUnderscoreSeparated(javaNameFromExternal(externalName)).toUpperCase();
  }

  /**
   * Turns the specified string into an underscore-separated string.
   *
   * @param javaName the string to parse
   * @return as underscore separated
   */
  public static String toUnderscoreSeparated(final String javaName) {
    requireNonNull(javaName);
    final StringBuilder result = new StringBuilder();
    final String input = unQuote(javaName.trim());
    for (int i = 0; i < input.length(); i++) {
      final char c = input.charAt(i);
      if (result.length() == 0) {
        result.append(Character.toLowerCase(c));
      } else if (Character.isUpperCase(c)) {
        result.append("_").append(Character.toLowerCase(c));
      } else {
        result.append(c);
      }
    }
    return result.toString();
  }

  public static String javaNameFromExternal(final String externalName) {
    requireNonNull(externalName);
    return replaceIfIllegalJavaIdentifierCharacter(replaceIfJavaUsedWord(nameFromExternal(externalName)));
  }

  public static String nameFromExternal(final String externalName) {
    requireNonNull(externalName);
    String result = unQuote(externalName.trim()); // Trim if there are initial spaces or trailing spaces...
    /* CamelCase
     * http://stackoverflow.com/questions/4050381/regular-expression-for-checking-if
     * -capital-letters-are-found-consecutively-in-a [A-Z] -> \p{Lu} [^A-Za-z0-9] ->
     * [^\pL0-90-9] */
    result = Stream.of(result.replaceAll("([\\p{Lu}]+)", "_$1").split("[^\\pL0-9]")).map(String::toLowerCase)
        .map(MetamodelGenerator::ucfirst).collect(Collectors.joining());
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

  private static final Character REPLACEMENT_CHARACTER = '_';

  /**
   * Returns the string but with any leading and trailing quotation marks trimmed.
   *
   * @param s the string to unquote
   * @return the string without surrounding quotation marks
   */
  public static String unQuote(final String s) {
    requireNonNull(s);
    if (s.startsWith("\"") && s.endsWith("\"")) {
      // Un-quote the name
      return s.substring(1, s.length() - 1);
    }
    return s;
  }

  private Triple<FieldSpec, FieldSpec, CodeBlock> generateFieldMetamodel(TypeName entity, String fieldName, TypeName entityField, Class<?> interceptorClass, boolean fieldIsIndexed) {
    String fieldAccessor = staticField(fieldName);

    FieldSpec objectField = FieldSpec.builder(Field.class, fieldName)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC).build();

    TypeName interceptor = ParameterizedTypeName.get(ClassName.get(interceptorClass), entity, entityField);

    FieldSpec aField = FieldSpec.builder(interceptor, fieldAccessor)
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .build();

    CodeBlock aFieldInit = CodeBlock.builder()
        .addStatement("$L = new $T($L,$L)", fieldAccessor, interceptor, fieldName, fieldIsIndexed)
        .build();

    return Tuples.of(objectField, aField, aFieldInit);
  }

  static final Set<String> JAVA_LITERAL_WORDS = Collections
      .unmodifiableSet(Stream.of("true", "false", "null").collect(Collectors.toSet()));

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

  static final Set<Class<?>> JAVA_BUILT_IN_CLASSES = Collections
      .unmodifiableSet(
          Stream
              .of(Boolean.class, Byte.class, Character.class, Double.class, Float.class, Integer.class, Long.class,
                  Object.class, Short.class, String.class, BigDecimal.class, BigInteger.class, boolean.class,
                  byte.class, char.class, double.class, float.class, int.class, long.class, short.class)
              .collect(Collectors.toSet()));

  private static final Set<String> JAVA_BUILT_IN_CLASS_WORDS = Collections
      .unmodifiableSet(JAVA_BUILT_IN_CLASSES.stream().map(Class::getSimpleName).collect(Collectors.toSet()));

  private static final Set<String> JAVA_USED_WORDS = Collections
      .unmodifiableSet(Stream.of(JAVA_LITERAL_WORDS, JAVA_RESERVED_WORDS, JAVA_BUILT_IN_CLASS_WORDS)
          .flatMap(Collection::stream).collect(Collectors.toSet()));

  private static final Set<String> JAVA_USED_WORDS_LOWER_CASE = Collections
      .unmodifiableSet(JAVA_USED_WORDS.stream().map(String::toLowerCase).collect(Collectors.toSet()));

}
