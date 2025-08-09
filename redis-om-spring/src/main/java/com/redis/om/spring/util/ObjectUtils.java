package com.redis.om.spring.util;

import static java.util.Objects.requireNonNull;
import static org.springframework.util.ClassUtils.resolvePrimitiveIfNecessary;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.geo.Distance;
import org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit;
import org.springframework.data.redis.core.convert.Bucket;
import org.springframework.data.redis.core.convert.RedisData;
import org.springframework.data.util.Pair;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ReflectionUtils;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import com.redis.om.spring.annotations.EnableRedisEnhancedRepositories;
import com.redis.om.spring.convert.MappingRedisOMConverter;
import com.redis.om.spring.tuple.Tuples;

import redis.clients.jedis.args.GeoUnit;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Schema;

/**
 * Utility class providing various object manipulation, reflection, and conversion methods
 * for Redis OM Spring framework. Contains helper methods for entity processing, type conversions,
 * field access, and schema operations.
 */
public class ObjectUtils {
  /**
   * Character used as replacement for illegal Java identifier characters.
   */
  public static final Character REPLACEMENT_CHARACTER = '_';
  static final Set<String> JAVA_LITERAL_WORDS = Set.of("true", "false", "null");
  private static final ConcurrentHashMap<Class<?>, Boolean> HAS_REDIS_KEY_CACHE = new ConcurrentHashMap<>();
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
  private static final ExpressionParser SPEL_EXPRESSION_PARSER = new SpelExpressionParser();
  private static final Set<String> JAVA_BUILT_IN_CLASS_WORDS = Collections.unmodifiableSet(JAVA_BUILT_IN_CLASSES
      .stream().map(Class::getSimpleName).collect(Collectors.toSet()));
  private static final Set<String> JAVA_USED_WORDS = Collections.unmodifiableSet(Stream.of(JAVA_LITERAL_WORDS,
      JAVA_RESERVED_WORDS, JAVA_BUILT_IN_CLASS_WORDS).flatMap(Collection::stream).collect(Collectors.toSet()));
  private static final Set<String> JAVA_USED_WORDS_LOWER_CASE = Collections.unmodifiableSet(JAVA_USED_WORDS.stream()
      .map(String::toLowerCase).collect(Collectors.toSet()));

  private ObjectUtils() {
  }

  /**
   * Converts a Spring Data Distance object to Redis distance string format.
   *
   * @param distance the Distance object to convert
   * @return the distance in Redis string format (e.g., "10 km")
   */
  public static String getDistanceAsRedisString(Distance distance) {
    return String.format("%s %s", distance.getValue(), distance.getUnit());
  }

  /**
   * Retrieves all fields from a class hierarchy that are annotated with the specified annotation.
   *
   * @param clazz           the class to examine
   * @param annotationClass the annotation class to search for
   * @return a list of fields that have the specified annotation
   */
  public static List<Field> getFieldsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
    return getDeclaredFieldsTransitively(clazz) //
        .stream() //
        .filter(f -> f.isAnnotationPresent(annotationClass)) //
        .toList();
  }

  /**
   * Converts a Spring Data Distance unit to Redis Jedis GeoUnit.
   *
   * @param distance the Distance object containing the unit to convert
   * @return the corresponding Redis GeoUnit (defaults to meters if not recognized)
   */
  public static GeoUnit getDistanceUnit(Distance distance) {
    if (distance.getUnit().equals(DistanceUnit.MILES.getAbbreviation())) {
      return GeoUnit.MI;
    } else if (distance.getUnit().equals(DistanceUnit.FEET.getAbbreviation())) {
      return GeoUnit.FT;
    } else if (distance.getUnit().equals(DistanceUnit.KILOMETERS.getAbbreviation())) {
      return GeoUnit.KM;
    } else {
      return GeoUnit.M;
    }
  }

  /**
   * Extracts the target class name from a full type class name string.
   * Removes generic type parameters and handles compound type names.
   *
   * @param fullTypeClassName the full type class name string (e.g., "java.util.List&lt;java.lang.String&gt;")
   * @return the clean class name without generic parameters (e.g., "java.util.List")
   */
  public static String getTargetClassName(String fullTypeClassName) {
    String[] splitted = fullTypeClassName.split(" ");
    String cls = splitted[splitted.length - 1];
    if (cls.contains("<")) {
      cls = cls.substring(0, cls.indexOf("<"));
    }
    return cls;
  }

  /**
   * Extracts the generic type parameter class name from a collection type string.
   *
   * @param fullTypeClassName the full type class name (e.g., "java.util.List&lt;java.lang.String&gt;")
   * @return the generic type parameter class name (e.g., "java.lang.String")
   */
  public static String getCollectionTargetClassName(String fullTypeClassName) {
    String[] splitted = fullTypeClassName.split(" ");
    String cls = splitted[splitted.length - 1];
    if (cls.contains("<")) {
      cls = cls.substring(cls.indexOf("<") + 1, cls.indexOf(">"));
    }
    return cls;
  }

  /**
   * Converts the first character of the given string to lowercase.
   *
   * @param string the input string to process
   * @return the string with the first character converted to lowercase
   */
  public static String firstToLowercase(String string) {
    char[] c = string.toCharArray();
    c[0] = Character.toLowerCase(c[0]);
    return new String(c);
  }

  /**
   * Retrieves the element class type from a collection field using Spring's ResolvableType.
   *
   * @param field the collection field to analyze
   * @return an Optional containing the element class if the field is a collection, empty otherwise
   */
  public static Optional<Class<?>> getCollectionElementClass(Field field) {
    if (isCollection(field)) {
      ResolvableType collectionType = ResolvableType.forField(field);
      Class<?> elementType = collectionType.getGeneric(0).getRawClass();
      return elementType != null ? Optional.of(elementType) : Optional.empty();
    }

    return Optional.empty();
  }

  /**
   * Retrieves the value type from a Map field.
   *
   * @param field the Map field to analyze
   * @return an Optional containing the value class if the field is a Map, empty otherwise
   */
  public static Optional<Class<?>> getMapValueClass(Field field) {
    if (Map.class.isAssignableFrom(field.getType())) {
      ResolvableType mapType = ResolvableType.forField(field);
      Class<?> valueType = mapType.getGeneric(1).getRawClass();
      return valueType != null ? Optional.of(valueType) : Optional.empty();
    }
    return Optional.empty();
  }

  /**
   * Extracts the Map value class name from a Map type string.
   *
   * @param fullTypeClassName the full type name string like "java.util.Map&lt;java.lang.String, java.lang.Integer&gt;"
   * @return the value type class name
   */
  public static String getMapValueClassName(String fullTypeClassName) {
    int openBracketPos = fullTypeClassName.indexOf('<');
    int closeBracketPos = fullTypeClassName.lastIndexOf('>');
    if (openBracketPos != -1 && closeBracketPos != -1) {
      String genericTypes = fullTypeClassName.substring(openBracketPos + 1, closeBracketPos);
      String[] types = genericTypes.split(",");
      if (types.length == 2) {
        return types[1].trim();
      }
    }
    return "java.lang.Object";
  }

  /**
   * Retrieves the generic element type from a collection field.
   *
   * @param field the collection field to analyze
   * @return an Optional containing the element Type if the field is a collection, empty otherwise
   */
  public static Optional<Type> getCollectionElementType(Field field) {
    if (isCollection(field)) {
      ResolvableType collectionType = ResolvableType.forField(field);
      Type elementType = collectionType.getGeneric(0).getType();
      return Optional.of(elementType);
    } else {
      return Optional.empty();
    }
  }

  /**
   * Determines whether a field represents a collection type.
   * Checks if the field type is assignable from Collection or Iterable interfaces.
   *
   * @param field the field to examine
   * @return true if the field is a collection type, false otherwise
   */
  public static boolean isCollection(Field field) {
    return Collection.class.isAssignableFrom(field.getType()) || Iterable.class.isAssignableFrom(field.getType());
  }

  /**
   * Determines whether a class represents a collection type.
   * Checks if the class is assignable from Collection or Iterable interfaces.
   *
   * @param cls the class to examine
   * @return true if the class is a collection type, false otherwise
   */
  public static boolean isCollection(Class<?> cls) {
    return Collection.class.isAssignableFrom(cls) || Iterable.class.isAssignableFrom(cls);
  }

  /**
   * Finds the first field annotated with @Id in an entity class hierarchy.
   *
   * @param cl the entity class to examine
   * @return an Optional containing the ID field if found, empty otherwise
   */
  public static Optional<Field> getIdFieldForEntityClass(Class<?> cl) {
    return getDeclaredFieldsTransitively(cl).stream().filter(f -> f.isAnnotationPresent(Id.class)).findFirst();
  }

  /**
   * Finds all fields annotated with @Id in an entity class hierarchy.
   *
   * @param cl the entity class to examine
   * @return a list of all ID fields found in the class hierarchy
   */
  public static List<Field> getIdFieldsForEntityClass(Class<?> cl) {
    return getDeclaredFieldsTransitively(cl).stream().filter(f -> f.isAnnotationPresent(Id.class)).toList();
  }

  /**
   * Extracts the ID field value from an entity instance using reflection.
   *
   * @param entity the entity instance to extract the ID from
   * @return the ID field value, or null if no ID field is found or accessible
   */
  public static Object getIdFieldForEntity(Object entity) {
    Optional<Field> maybeIdField = getIdFieldForEntityClass(entity.getClass());
    if (maybeIdField.isEmpty())
      return null;

    Field idField = maybeIdField.get();

    String getterName = "get" + ObjectUtils.ucfirst(idField.getName());
    Method getter = ReflectionUtils.findMethod(entity.getClass(), getterName);
    return getter != null ? ReflectionUtils.invokeMethod(getter, entity) : null;
  }

  /**
   * Extracts the ID field value from an entity instance using a specified ID field.
   *
   * @param idField the ID field to extract the value from
   * @param entity  the entity instance containing the ID value
   * @return the ID field value
   * @throws NullPointerException if the getter method is not found
   */
  public static Object getIdFieldForEntity(Field idField, Object entity) {
    String getterName = "get" + ObjectUtils.ucfirst(idField.getName());
    Method getter = ReflectionUtils.findMethod(entity.getClass(), getterName);
    return ReflectionUtils.invokeMethod(requireNonNull(getter), entity);
  }

  /**
   * Finds the getter method for a specific field in a class.
   *
   * @param cls   the class to search in
   * @param field the field to find the getter for
   * @return the getter Method, or null if not found
   */
  public static Method getGetterForField(Class<?> cls, Field field) {
    String getterName = "get" + ucfirst(field.getName());
    return ReflectionUtils.findMethod(cls, getterName);
  }

  /**
   * Finds the setter method for a specific field in a class.
   * Constructs the setter name using JavaBeans naming conventions.
   *
   * @param cls   the class to search in
   * @param field the field to find the setter for
   * @return the setter Method, or null if not found
   */
  public static Method getSetterForField(Class<?> cls, Field field) {
    String setterName = "set" + ucfirst(field.getName());
    return ReflectionUtils.findMethod(cls, setterName, field.getType());
  }

  /**
   * Retrieves the value of a specific field from an entity instance using its getter method.
   * Uses JavaBeans naming conventions to locate the appropriate getter.
   *
   * @param field  the field to retrieve the value for
   * @param entity the entity instance to extract the value from
   * @return the field value, or null if the getter is not found or invocation fails
   */
  public static Object getValueForField(Field field, Object entity) {
    String getterName = "get" + ObjectUtils.ucfirst(field.getName());
    Method getter = ReflectionUtils.findMethod(entity.getClass(), getterName);
    return getter != null ? ReflectionUtils.invokeMethod(getter, entity) : null;
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
   * Does something with the first character in the specified String.
   *
   * @param input    The String.
   * @param callback The something.
   * @return The new String.
   */
  public static String withFirst(String input, Function<Character, String> callback) {
    if (input == null) {
      return null;
    } else if (input.isEmpty()) {
      return "";
    } else {
      return String.join("", callback.apply(input.charAt(0)), input.subSequence(1, input.length()));
    }
  }

  /**
   * Checks if the first character of a string is a lowercase letter.
   * Validates that the first character is both a letter and in lowercase form.
   *
   * @param string the string to examine
   * @return true if the first character is a lowercase letter, false otherwise
   */
  public static boolean isFirstLowerCase(String string) {
    String first = string.substring(0, 1);
    return Character.isLetter(first.charAt(0)) && first.toLowerCase().equals(first);
  }

  /**
   * Returns the specified text but with the first character lowercase.
   *
   * @param input The text.
   * @return The resulting text.
   */
  public static String toLowercaseFirstCharacter(String input) {
    return withFirst(input, first -> String.valueOf(Character.toLowerCase(first)));
  }

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
      if (result.isEmpty()) {
        result.append(Character.toLowerCase(c));
      } else if (Character.isUpperCase(c)) {
        result.append("_").append(Character.toLowerCase(c));
      } else {
        result.append(c);
      }
    }
    return result.toString();
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
   * Checks if a specific property field in a class is annotated with the given annotation.
   * Uses reflection to find the field and check for the presence of the annotation.
   *
   * @param cls             the class to examine
   * @param property        the name of the property field to check
   * @param annotationClass the annotation class to look for
   * @return true if the property field is annotated with the specified annotation, false otherwise
   */
  public static boolean isPropertyAnnotatedWith(Class<?> cls, String property,
      Class<? extends Annotation> annotationClass) {
    Field field;
    try {
      field = ReflectionUtils.findField(cls, property);
      if (field == null) {
        return false;
      }
      return field.isAnnotationPresent(annotationClass);
    } catch (SecurityException e) {
      return false;
    }

  }

  /**
   * Converts a Redis search Document to a Java object using the provided mapping converter.
   *
   * @param document           the Redis Document to convert
   * @param returnedObjectType the target class type for conversion
   * @param mappingConverter   the Redis OM mapping converter to use for deserialization
   * @return the converted Java object instance
   */
  public static Object documentToObject(Document document, Class<?> returnedObjectType,
      MappingRedisOMConverter mappingConverter) {
    Bucket b = new Bucket();
    document.getProperties().forEach(p -> b.put(p.getKey(), (byte[]) p.getValue()));

    return mappingConverter.read(returnedObjectType, new RedisData(b));
  }

  /**
   * Converts a map of properties to a Java object using the provided mapping converter.
   * Creates a Redis Bucket from the map entries and uses the converter for deserialization.
   *
   * @param properties         the map of property key-value pairs to convert
   * @param returnedObjectType the target class type for conversion
   * @param mappingConverter   the Redis OM mapping converter to use for deserialization
   * @return the converted Java object instance
   */
  public static Object mapToObject(Map<String, Object> properties, Class<?> returnedObjectType,
      MappingRedisOMConverter mappingConverter) {
    Bucket b = new Bucket();
    properties.forEach((k, v) -> b.put(k, v.toString().getBytes()));

    return mappingConverter.read(returnedObjectType, new RedisData(b));
  }

  /**
   * Converts a Redis search Document to a typed entity using the provided mapping converter.
   *
   * @param <T>              the type of the target entity
   * @param document         the Redis Document to convert
   * @param classOfT         the target entity class
   * @param mappingConverter the Redis OM mapping converter to use for deserialization
   * @return the converted typed entity instance
   */
  public static <T> T documentToEntity(Document document, Class<T> classOfT, MappingRedisOMConverter mappingConverter) {
    Bucket b = new Bucket();
    document.getProperties().forEach(p -> b.put(p.getKey(), (byte[]) p.getValue()));

    return mappingConverter.read(classOfT, new RedisData(b));
  }

  /**
   * Converts an object value to its string representation using the mapping converter.
   *
   * @param value            the object value to convert
   * @param mappingConverter the Redis OM mapping converter to use for conversion
   * @return the string representation of the value
   */
  public static String asString(Object value, MappingRedisOMConverter mappingConverter) {
    if (value instanceof String valueAsString) {
      return valueAsString;
    } else {
      return mappingConverter.getConversionService().convert(value, String.class);
    }
  }

  /**
   * Scans for and retrieves bean definitions for classes annotated with the specified annotations
   * within the configured base packages of Redis repository configurations.
   *
   * @param ac      the ApplicationContext to scan
   * @param classes the annotation classes to search for
   * @return a set of BeanDefinition objects for matching classes
   */
  @SuppressWarnings(
    { "unchecked", "rawtypes" }
  )
  public static Set<BeanDefinition> getBeanDefinitionsFor(ApplicationContext ac, Class... classes) {
    Set<BeanDefinition> beanDefs = new HashSet<>();

    ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
    for (Class cls : classes) {
      provider.addIncludeFilter(new AnnotationTypeFilter(cls));
    }

    List<Pair<EnableRedisDocumentRepositories, String>> erdrs = getEnableRedisDocumentRepositories(ac);
    for (Pair<EnableRedisDocumentRepositories, String> pair : erdrs) {
      EnableRedisDocumentRepositories edr = pair.getFirst();
      if (edr.basePackages().length > 0) {
        for (String pkg : edr.basePackages()) {
          beanDefs.addAll(provider.findCandidateComponents(pkg));
        }
      } else if (edr.basePackageClasses().length > 0) {
        for (Class<?> pkg : edr.basePackageClasses()) {
          beanDefs.addAll(provider.findCandidateComponents(pkg.getPackageName()));
        }
      } else {
        beanDefs.addAll(provider.findCandidateComponents(pair.getSecond()));
      }
    }

    List<Pair<EnableRedisEnhancedRepositories, String>> erers = getEnableRedisEnhancedRepositories(ac);
    for (Pair<EnableRedisEnhancedRepositories, String> pair : erers) {
      EnableRedisEnhancedRepositories er = pair.getFirst();
      if (er.basePackages().length > 0) {
        for (String pkg : er.basePackages()) {
          beanDefs.addAll(provider.findCandidateComponents(pkg));
        }
      } else if (er.basePackageClasses().length > 0) {
        for (Class<?> pkg : er.basePackageClasses()) {
          beanDefs.addAll(provider.findCandidateComponents(pkg.getPackageName()));
        }
      } else {
        beanDefs.addAll(provider.findCandidateComponents(pair.getSecond()));
      }
    }

    return beanDefs;
  }

  /**
   * Finds all @EnableRedisDocumentRepositories annotations in the application context
   * along with their associated package names.
   *
   * @param ac the ApplicationContext to search
   * @return a list of pairs containing the annotation and its base package
   */
  public static List<Pair<EnableRedisDocumentRepositories, String>> getEnableRedisDocumentRepositories(
      ApplicationContext ac) {
    Map<String, Object> annotatedBeans = ac.getBeansWithAnnotation(SpringBootApplication.class);
    annotatedBeans.putAll(ac.getBeansWithAnnotation(Configuration.class));
    List<Pair<EnableRedisDocumentRepositories, String>> erdrs = new ArrayList<>();
    for (Object ab : annotatedBeans.values()) {
      Class<?> cls = ab.getClass();
      if (cls.isAnnotationPresent(EnableRedisDocumentRepositories.class)) {
        EnableRedisDocumentRepositories edr = cls.getAnnotation(EnableRedisDocumentRepositories.class);
        erdrs.add(Pair.of(edr, cls.getPackageName()));
      }
    }

    return erdrs;
  }

  /**
   * Finds all @EnableRedisEnhancedRepositories annotations in the application context
   * along with their associated package names.
   *
   * @param ac the ApplicationContext to search
   * @return a list of pairs containing the annotation and its base package
   */
  public static List<Pair<EnableRedisEnhancedRepositories, String>> getEnableRedisEnhancedRepositories(
      ApplicationContext ac) {
    Map<String, Object> annotatedBeans = ac.getBeansWithAnnotation(SpringBootApplication.class);
    annotatedBeans.putAll(ac.getBeansWithAnnotation(Configuration.class));
    List<Pair<EnableRedisEnhancedRepositories, String>> erers = new ArrayList<>();
    for (Object ab : annotatedBeans.values()) {
      Class<?> cls = ab.getClass();
      if (cls.isAnnotationPresent(EnableRedisEnhancedRepositories.class)) {
        EnableRedisEnhancedRepositories edr = cls.getAnnotation(EnableRedisEnhancedRepositories.class);
        erers.add(Pair.of(edr, cls.getPackageName()));
      }
    }

    return erers;
  }

  /**
   * Retrieves all declared fields from a class and its entire inheritance hierarchy.
   *
   * @param clazz the class to examine
   * @return a list of all declared fields including those from superclasses
   */
  public static List<Field> getDeclaredFieldsTransitively(Class<?> clazz) {
    List<Field> fields = new ArrayList<>();
    while (clazz != null) {
      fields.addAll(Arrays.stream(clazz.getDeclaredFields()).toList());
      clazz = clazz.getSuperclass();
    }
    return fields;
  }

  /**
   * Finds a specific field by name in a class hierarchy.
   *
   * @param clazz     the class to search in
   * @param fieldName the name of the field to find
   * @return the Field object if found
   * @throws NoSuchFieldException if the field is not found in the class hierarchy
   */
  public static Field getDeclaredFieldTransitively(Class<?> clazz, String fieldName) throws NoSuchFieldException {
    Field field = ReflectionUtils.findField(clazz, fieldName);
    if (field == null) {
      throw new NoSuchFieldException(fieldName);
    }
    return field;
  }

  /**
   * Converts a float array to a byte array using little-endian byte order.
   * Used for vector embedding storage in Redis.
   *
   * @param input the float array to convert
   * @return the byte array representation
   */
  public static byte[] floatArrayToByteArray(float[] input) {
    byte[] bytes = new byte[Float.BYTES * input.length];
    ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().put(input);
    return bytes;
  }

  /**
   * Converts a long array to a byte array by first converting to float array.
   * Used for vector embedding storage where long values need to be converted to float format.
   *
   * @param input the long array to convert
   * @return the byte array representation
   */
  public static byte[] longArrayToByteArray(long[] input) {
    return floatArrayToByteArray(longArrayToFloatArray(input));
  }

  /**
   * Converts a long array to a float array by casting each element.
   * Used for vector embedding processing where long values need to be converted to float format.
   *
   * @param input the long array to convert
   * @return the float array representation
   */
  public static float[] longArrayToFloatArray(long[] input) {
    float[] floats = new float[input.length];
    for (int i = 0; i < input.length; i++) {
      floats[i] = input[i];
    }
    return floats;
  }

  /**
   * Converts a byte array to a float array using little-endian byte order.
   * Used for retrieving vector embeddings from Redis storage.
   *
   * @param bytes the byte array to convert
   * @return the float array representation
   */
  public static float[] byteArrayToFloatArray(byte[] bytes) {
    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
    float[] floatArray = new float[floatBuffer.capacity()];
    floatBuffer.get(floatArray);
    return floatArray;
  }

  /**
   * Determines if a class is a primitive type that corresponds to a specific wrapper class.
   * Uses Spring's ClassUtils to resolve primitive types to their wrapper equivalents.
   *
   * @param clazz   the class to check (potentially primitive)
   * @param wrapper the wrapper class to compare against
   * @return true if clazz is a primitive type that corresponds to the wrapper class
   */
  public static boolean isPrimitiveOfType(Class<?> clazz, Class<?> wrapper) {
    return clazz.isPrimitive() && resolvePrimitiveIfNecessary(clazz) == wrapper;
  }

  /**
   * Constructs a Redis key by combining a keyspace with an entity ID.
   *
   * @param keyspace the Redis keyspace prefix
   * @param id       the entity identifier
   * @return the formatted Redis key (e.g., "keyspace:id" or "keyspace:id" if keyspace already ends with ":")
   */
  public static String getKey(String keyspace, Object id) {
    String format = keyspace.endsWith(":") ? "%s%s" : "%s:%s";
    return String.format(format, keyspace, id);
  }

  /**
   * Extracts a value from an object using a JSONPath-like expression converted to SpEL.
   * Supports nested object navigation and array handling with flattening.
   *
   * @param target the target object to extract the value from
   * @param path   the JSONPath-like expression (e.g., "$.property.nestedProperty")
   * @return the extracted value, or null if the path cannot be resolved
   */
  public static Object getValueByPath(Object target, String path) {
    // Remove JSONPath prefix
    String safeSpelPath = path.replace("$.", "");
    // does the expression have any arrays
    boolean hasNestedObject = path.contains("[0:]");

    Object value = null;

    if (!hasNestedObject) {
      safeSpelPath = safeSpelPath //
          .replace("[*]", "") //
          .replace(".", "?.");

      value = SPEL_EXPRESSION_PARSER.parseExpression(safeSpelPath).getValue(target);
    } else {
      String[] tempParts = safeSpelPath.split("\\[0:]", 2);
      String[] parts = tempParts[1].split("\\.", 2);
      String leftPath = tempParts[0].replace(".", "?.");
      String rightPath = parts[1].replace(".", "?.") //
          .replace("[*]", "");

      Expression leftExp = SPEL_EXPRESSION_PARSER.parseExpression(leftPath);
      Expression rightExp = SPEL_EXPRESSION_PARSER.parseExpression(rightPath);
      Collection<?> left = (Collection<?>) leftExp.getValue(target);
      if (left != null && !left.isEmpty()) {
        value = flattenCollection(left.stream().map(rightExp::getValue).toList());
      }
    }

    return value;
  }

  /**
   * Recursively flattens a nested collection structure into a single-level collection.
   * Used for processing complex nested query results.
   *
   * @param inputCollection the nested collection to flatten
   * @return a flattened collection containing all elements
   */
  @SuppressWarnings(
    "unchecked"
  )
  public static Collection<Object> flattenCollection(Collection<Object> inputCollection) {
    List<Object> flatList = new ArrayList<>();

    for (Object element : inputCollection) {
      if (element instanceof Collection) {
        flatList.addAll(flattenCollection((Collection<Object>) element));
      } else {
        flatList.add(element);
      }
    }

    return flatList;
  }

  /**
   * Replaces illegal Java identifier characters in a string with the replacement character.
   * Ensures the first character is valid for a Java identifier start, and all subsequent
   * characters are valid Java identifier parts.
   *
   * @param word the string to process
   * @return the string with illegal Java identifier characters replaced
   * @throws NullPointerException if word is null
   */
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

  /**
   * Converts an external name to a valid Java identifier name.
   * Processes the external name through character replacement and Java keyword checking.
   *
   * @param externalName the external name to convert
   * @return a valid Java identifier name
   * @throws NullPointerException if externalName is null
   */
  public static String javaNameFromExternal(final String externalName) {
    requireNonNull(externalName);
    return ObjectUtils.replaceIfIllegalJavaIdentifierCharacter(replaceIfJavaUsedWord(nameFromExternal(externalName)));
  }

  /**
   * Converts an external name to a camelCase Java name by processing case patterns.
   * Handles consecutive uppercase letters and non-alphanumeric characters to create
   * proper camelCase naming.
   *
   * @param externalName the external name to convert
   * @return the camelCase Java name
   * @throws NullPointerException if externalName is null
   */
  public static String nameFromExternal(final String externalName) {
    requireNonNull(externalName);
    String result = ObjectUtils.unQuote(externalName.trim()); // Trim if there are initial spaces or trailing spaces...
    result = Stream.of(result.replaceAll("(\\p{Lu}+)", "_$1").split("[^\\p{L}\\d]")).map(String::toLowerCase).map(
        ObjectUtils::ucfirst).collect(Collectors.joining());
    return result;
  }

  /**
   * Checks if a word conflicts with Java reserved words, literals, or built-in class names.
   * Appends an underscore to the word if it conflicts to avoid naming collisions.
   *
   * @param word the word to check for Java naming conflicts
   * @return the word with "_" appended if it's a Java used word, otherwise the original word
   * @throws NullPointerException if word is null
   */
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

  /**
   * Extracts the field name from a Redis Schema.Field object by parsing its string representation.
   *
   * @param field the Schema.Field to extract the name from
   * @return the field name, or empty string if parsing fails
   */
  public static String getSchemaFieldName(Schema.Field field) {
    String toStringOutput = field.toString();
    // Splitting by single quote character to isolate the name field and other fields
    String[] parts = toStringOutput.split("'");
    // parts[1] should now contain 'fieldNameValue' or 'fieldNameValue AS alias'
    if (parts.length > 1) {
      // Further splitting by ' AS ' to handle alias
      String[] nameParts = parts[1].split(" AS ");
      // The actual field name will always be the first part before ' AS '
      return nameParts[0];  // Return fieldNameValue, before any ' AS ' part
    }
    return "";  // Return empty string if not found or invalid format
  }

  /**
   * Extracts the field type from a Redis Schema.Field object by parsing its string representation.
   * Parses the toString() output to find the type value.
   *
   * @param field the Schema.Field to extract the type from
   * @return the field type string, or empty string if parsing fails
   */
  public static String getSchemaFieldType(Schema.Field field) {
    String toStringOutput = field.toString();
    // Assuming the format is exactly as provided: Field{name='fieldNameValue', type=typeValue, sortable=booleanValue, noindex=booleanValue}
    // Splitting the string around ", " to get each field
    String[] parts = toStringOutput.split(", ");

    // Now find and return the part that starts with "type="
    for (String part : parts) {
      if (part.startsWith("type=")) {
        // Assuming type value does not contain ','
        return part.substring(5);  // "type=".length() == 5
      }
    }
    return "";  // Return null if type is not found or invalid format
  }

  /**
   * Converts a list of Double values to a byte array for Redis storage.
   * The doubles are first converted to floats, then to bytes using little-endian order.
   *
   * @param doubleList the list of Double values to convert
   * @return the byte array representation
   */
  public static byte[] doubleListToByteArray(List<Double> doubleList) {
    byte[] bytes = new byte[Float.BYTES * doubleList.size()];
    float[] input = doubleListToFloatArray(doubleList);
    ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().put(input);
    return bytes;
  }

  /**
   * Converts a list of Double values to a float array.
   * Used for vector embedding processing where precision can be reduced from double to float.
   *
   * @param doubleList the list of Double values to convert
   * @return the float array representation
   */
  public static float[] doubleListToFloatArray(List<Double> doubleList) {
    // Initialize float array of the same size as the List<Double>
    float[] floatArray = new float[doubleList.size()];

    // Iterate over the List<Double>, convert each Double to float, and store it in the float array
    for (int i = 0; i < doubleList.size(); i++) {
      floatArray[i] = doubleList.get(i).floatValue();
    }

    return floatArray;
  }

  /**
   * Determines if an annotation processing element is an inner class and returns its enclosing class information.
   * Used during compile-time annotation processing to handle nested class structures.
   *
   * @param element the annotation processing element to examine
   * @return a Pair containing: Boolean (true if inner class) and String (enclosing class name, or null)
   */
  public static com.redis.om.spring.tuple.Pair<Boolean, String> isInnerClassWithEnclosing(Element element) {
    Element enclosingElement = element.getEnclosingElement();
    if (enclosingElement.getKind() == ElementKind.CLASS) {
      return Tuples.of(true, enclosingElement.getSimpleName().toString());
    }
    return Tuples.of(false, null);
  }

  /**
   * Retrieves all declared field names from an entity class.
   * Used for reflection-based property access and Query By Example operations.
   *
   * @param entityType the entity class to analyze
   * @return a list of all declared field names
   */
  public static List<String> getAllProperties(Class<?> entityType) {
    List<String> properties = new ArrayList<>();
    for (Field field : entityType.getDeclaredFields()) {
      properties.add(field.getName());
    }
    return properties;
  }

  /**
   * Determines whether a property should be included in Query By Example matching
   * based on the ExampleMatcher configuration. Checks for specific property matchers
   * and ignored paths.
   *
   * @param matcher      the ExampleMatcher configuration
   * @param propertyName the name of the property to check
   * @return true if the property should be included in matching, false otherwise
   */
  public static boolean shouldIncludeProperty(ExampleMatcher matcher, String propertyName) {
    ExampleMatcher.PropertySpecifier specifier = matcher.getPropertySpecifiers().getForPath(propertyName);
    if (specifier != null) {
      // If a specific matcher is defined for this property, include it
      return true;
    }
    // If no specific matcher is defined, include the property if it's not in the ignored paths
    return !matcher.isIgnoredPath(propertyName);
  }

  /**
   * Retrieves a property value from an object using JavaBeans PropertyDescriptor.
   * Used for Query By Example operations and dynamic property access.
   *
   * @param object       the object to get the property value from
   * @param propertyName the name of the property to retrieve
   * @return the property value
   * @throws RuntimeException if the property cannot be accessed
   */
  public static Object getPropertyValue(Object object, String propertyName) {
    try {
      PropertyDescriptor propertyDescriptor = new PropertyDescriptor(propertyName, object.getClass());
      Method getter = propertyDescriptor.getReadMethod();
      return getter.invoke(object);
    } catch (Exception e) {
      throw new RuntimeException("Error getting property value", e);
    }
  }

  /**
   * Populates fields annotated with @RedisKey with the Redis key value.
   * This method searches for fields annotated with @RedisKey and sets their value
   * to the provided Redis key.
   *
   * @param entity   the entity to populate
   * @param redisKey the Redis key to set
   * @param <T>      the entity type
   * @return the entity with populated @RedisKey field
   */
  public static <T> T populateRedisKey(T entity, String redisKey) {
    if (entity == null || redisKey == null) {
      return entity;
    }

    Class<?> clazz = entity.getClass();

    // Quick check: if this class doesn't have @RedisKey fields, return early
    Boolean hasRedisKey = HAS_REDIS_KEY_CACHE.computeIfAbsent(clazz, cls -> {
      List<Field> fields = getDeclaredFieldsTransitively(cls);
      return fields.stream().anyMatch(f -> f.isAnnotationPresent(com.redis.om.spring.annotations.RedisKey.class));
    });

    if (!hasRedisKey) {
      return entity;
    }

    // If we get here, we know there's at least one @RedisKey field
    List<Field> fields = getDeclaredFieldsTransitively(clazz);

    for (Field field : fields) {
      if (field.isAnnotationPresent(com.redis.om.spring.annotations.RedisKey.class)) {
        try {
          field.setAccessible(true);
          field.set(entity, redisKey);
        } catch (IllegalAccessException e) {
          throw new RuntimeException("Failed to set @RedisKey field", e);
        }
      }
    }

    return entity;
  }
}
