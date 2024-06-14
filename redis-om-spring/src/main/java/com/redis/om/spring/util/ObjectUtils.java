package com.redis.om.spring.util;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import com.redis.om.spring.annotations.EnableRedisEnhancedRepositories;
import com.redis.om.spring.convert.MappingRedisOMConverter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Distance;
import org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit;
import org.springframework.data.redis.core.convert.Bucket;
import org.springframework.data.redis.core.convert.RedisData;
import org.springframework.data.util.Pair;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ReflectionUtils;
import redis.clients.jedis.args.GeoUnit;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Schema;

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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.springframework.util.ClassUtils.resolvePrimitiveIfNecessary;

public class ObjectUtils {
  public static final Character REPLACEMENT_CHARACTER = '_';
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
  private static final ExpressionParser SPEL_EXPRESSION_PARSER = new SpelExpressionParser();
  private static final Set<String> JAVA_BUILT_IN_CLASS_WORDS = Collections.unmodifiableSet(
      JAVA_BUILT_IN_CLASSES.stream().map(Class::getSimpleName).collect(Collectors.toSet()));
  private static final Set<String> JAVA_USED_WORDS = Collections.unmodifiableSet(
      Stream.of(JAVA_LITERAL_WORDS, JAVA_RESERVED_WORDS, JAVA_BUILT_IN_CLASS_WORDS).flatMap(Collection::stream)
          .collect(Collectors.toSet()));
  private static final Set<String> JAVA_USED_WORDS_LOWER_CASE = Collections.unmodifiableSet(
      JAVA_USED_WORDS.stream().map(String::toLowerCase).collect(Collectors.toSet()));

  private ObjectUtils() {
  }

  public static String getDistanceAsRedisString(Distance distance) {
    return String.format("%s %s", distance.getValue(), distance.getUnit());
  }

  public static List<Field> getFieldsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
    return getDeclaredFieldsTransitively(clazz) //
        .stream() //
        .filter(f -> f.isAnnotationPresent(annotationClass)) //
        .toList();
  }

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

  public static String getTargetClassName(String fullTypeClassName) {
    String[] splitted = fullTypeClassName.split(" ");
    String cls = splitted[splitted.length - 1];
    if (cls.contains("<")) {
      cls = cls.substring(0, cls.indexOf("<"));
    }
    return cls;
  }

  public static String getCollectionTargetClassName(String fullTypeClassName) {
    String[] splitted = fullTypeClassName.split(" ");
    String cls = splitted[splitted.length - 1];
    if (cls.contains("<")) {
      cls = cls.substring(cls.indexOf("<") + 1, cls.indexOf(">"));
    }
    return cls;
  }

  public static String firstToLowercase(String string) {
    char[] c = string.toCharArray();
    c[0] = Character.toLowerCase(c[0]);
    return new String(c);
  }

  public static Optional<Class<?>> getCollectionElementClass(Field field) {
    if (isCollection(field)) {
      ResolvableType collectionType = ResolvableType.forField(field);
      Class<?> elementType = collectionType.getGeneric(0).getRawClass();
      return elementType != null ? Optional.of(elementType) : Optional.empty();
    }

    return Optional.empty();
  }

  public static Optional<Type> getCollectionElementType(Field field) {
    if (isCollection(field)) {
      ResolvableType collectionType = ResolvableType.forField(field);
      Type elementType = collectionType.getGeneric(0).getType();
      return Optional.of(elementType);
    } else {
      return Optional.empty();
    }
  }

  public static boolean isCollection(Field field) {
    return Collection.class.isAssignableFrom(field.getType()) || Iterable.class.isAssignableFrom(field.getType());
  }

  public static boolean isCollection(Class<?> cls) {
    return Collection.class.isAssignableFrom(cls) || Iterable.class.isAssignableFrom(cls);
  }

  public static Optional<Field> getIdFieldForEntityClass(Class<?> cl) {
    return getDeclaredFieldsTransitively(cl).stream().filter(f -> f.isAnnotationPresent(Id.class)).findFirst();
  }

  public static Object getIdFieldForEntity(Object entity) {
    Optional<Field> maybeIdField = getIdFieldForEntityClass(entity.getClass());
    if (maybeIdField.isEmpty())
      return null;

    Field idField = maybeIdField.get();

    String getterName = "get" + ObjectUtils.ucfirst(idField.getName());
    Method getter = ReflectionUtils.findMethod(entity.getClass(), getterName);
    return getter != null ? ReflectionUtils.invokeMethod(getter, entity) : null;
  }

  public static Object getIdFieldForEntity(Field idField, Object entity) {
    String getterName = "get" + ObjectUtils.ucfirst(idField.getName());
    Method getter = ReflectionUtils.findMethod(entity.getClass(), getterName);
    return ReflectionUtils.invokeMethod(requireNonNull(getter), entity);
  }

  public static Method getGetterForField(Class<?> cls, Field field) {
    String getterName = "get" + ucfirst(field.getName());
    return ReflectionUtils.findMethod(cls, getterName);
  }

  public static Method getSetterForField(Class<?> cls, Field field) {
    String setterName = "set" + ucfirst(field.getName());
    return ReflectionUtils.findMethod(cls, setterName, field.getType());
  }

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

  public static Object documentToObject(Document document, Class<?> returnedObjectType,
      MappingRedisOMConverter mappingConverter) {
    Bucket b = new Bucket();
    document.getProperties().forEach(p -> b.put(p.getKey(), (byte[]) p.getValue()));

    return mappingConverter.read(returnedObjectType, new RedisData(b));
  }

  public static Object mapToObject(Map<String, Object> properties, Class<?> returnedObjectType,
      MappingRedisOMConverter mappingConverter) {
    Bucket b = new Bucket();
    properties.forEach((k, v) -> b.put(k, v.toString().getBytes()));

    return mappingConverter.read(returnedObjectType, new RedisData(b));
  }

  public static <T> T documentToEntity(Document document, Class<T> classOfT, MappingRedisOMConverter mappingConverter) {
    Bucket b = new Bucket();
    document.getProperties().forEach(p -> b.put(p.getKey(), (byte[]) p.getValue()));

    return mappingConverter.read(classOfT, new RedisData(b));
  }

  public static String asString(Object value, MappingRedisOMConverter mappingConverter) {
    if (value instanceof String valueAsString) {
      return valueAsString;
    } else {
      return mappingConverter.getConversionService().convert(value, String.class);
    }
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
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

  public static List<Field> getDeclaredFieldsTransitively(Class<?> clazz) {
    List<Field> fields = new ArrayList<>();
    while (clazz != null) {
      fields.addAll(Arrays.stream(clazz.getDeclaredFields()).toList());
      clazz = clazz.getSuperclass();
    }
    return fields;
  }

  public static Field getDeclaredFieldTransitively(Class<?> clazz, String fieldName) throws NoSuchFieldException {
    Field field = ReflectionUtils.findField(clazz, fieldName);
    if (field == null) {
      throw new NoSuchFieldException(fieldName);
    }
    return field;
  }

  public static byte[] floatArrayToByteArray(float[] input) {
    byte[] bytes = new byte[Float.BYTES * input.length];
    ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().put(input);
    return bytes;
  }

  public static byte[] longArrayToByteArray(long[] input) {
    return floatArrayToByteArray(longArrayToFloatArray(input));
  }

  public static float[] longArrayToFloatArray(long[] input) {
    float[] floats = new float[input.length];
    for (int i = 0; i < input.length; i++) {
      floats[i] = input[i];
    }
    return floats;
  }

  public static float[] byteArrayToFloatArray(byte[] bytes) {
    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
    float[] floatArray = new float[floatBuffer.capacity()];
    floatBuffer.get(floatArray);
    return floatArray;
  }

  public static boolean isPrimitiveOfType(Class<?> clazz, Class<?> wrapper) {
    return clazz.isPrimitive() && resolvePrimitiveIfNecessary(clazz) == wrapper;
  }

  public static String getKey(String keyspace, Object id) {
    String format = keyspace.endsWith(":") ? "%s%s" : "%s:%s";
    return String.format(format, keyspace, id);
  }

  public static <T> Page<T> pageFromSlice(Slice<T> slice) {
    return new Page<>() {
      @Override
      public int getTotalPages() {
        return -1;
      }

      @Override
      public long getTotalElements() {
        return -1;
      }

      @Override
      public <U> Page<U> map(Function<? super T, ? extends U> converter) {
        return pageFromSlice(slice.map(converter));
      }

      @Override
      public int getNumber() {
        return slice.getNumber();
      }

      @Override
      public int getSize() {
        return slice.getSize();
      }

      @Override
      public int getNumberOfElements() {
        return slice.getNumberOfElements();
      }

      @Override
      public List<T> getContent() {
        return slice.getContent();
      }

      @Override
      public boolean hasContent() {
        return slice.hasContent();
      }

      @Override
      public Sort getSort() {
        return slice.getSort();
      }

      @Override
      public boolean isFirst() {
        return slice.isFirst();
      }

      @Override
      public boolean isLast() {
        return slice.isLast();
      }

      @Override
      public boolean hasNext() {
        return slice.hasNext();
      }

      @Override
      public boolean hasPrevious() {
        return slice.hasPrevious();
      }

      @Override
      public Pageable nextPageable() {
        return slice.nextPageable();
      }

      @Override
      public Pageable previousPageable() {
        return slice.previousPageable();
      }

      @Override
      public Iterator<T> iterator() {
        return slice.iterator();
      }
    };
  }

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
      String[] tempParts = safeSpelPath.split("\\[0:\\]", 2);
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

  @SuppressWarnings("unchecked")
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

  public static String javaNameFromExternal(final String externalName) {
    requireNonNull(externalName);
    return ObjectUtils.replaceIfIllegalJavaIdentifierCharacter(replaceIfJavaUsedWord(nameFromExternal(externalName)));
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

  public static byte[] doubleListToByteArray(List<Double> doubleList) {
    byte[] bytes = new byte[Float.BYTES * doubleList.size()];
    float[] input = doubleListToFloatArray(doubleList);
    ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().put(input);
    return bytes;
  }

  public static float[] doubleListToFloatArray(List<Double> doubleList) {
    // Initialize float array of the same size as the List<Double>
    float[] floatArray = new float[doubleList.size()];

    // Iterate over the List<Double>, convert each Double to float, and store it in the float array
    for (int i = 0; i < doubleList.size(); i++) {
      floatArray[i] = doubleList.get(i).floatValue();
    }

    return floatArray;
  }
}
