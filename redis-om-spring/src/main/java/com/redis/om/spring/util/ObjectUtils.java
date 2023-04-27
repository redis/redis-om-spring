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
import org.springframework.core.io.Resource;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Distance;
import org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit;
import org.springframework.data.redis.core.convert.Bucket;
import org.springframework.data.redis.core.convert.RedisData;
import org.springframework.data.util.Pair;
import org.springframework.util.ReflectionUtils;
import redis.clients.jedis.args.GeoUnit;
import redis.clients.jedis.search.Document;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.util.Objects.requireNonNull;

public class ObjectUtils {
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

  public static Optional<Class<?>> getCollectionElementType(Field field) {
    if (Collection.class.isAssignableFrom(field.getType()) || Iterable.class.isAssignableFrom(field.getType())) {
      ResolvableType collectionType = ResolvableType.forField(field);
      Class<?> elementType = collectionType.getGeneric(0).getRawClass();
      return Optional.of(elementType);
    }

    return Optional.empty();
  }

  public static Optional<Field> getIdFieldForEntityClass(Class<?> cl) {
    return getDeclaredFieldsTransitively(cl).stream().filter(f -> f.isAnnotationPresent(Id.class)).findFirst();
  }

  public static Optional<?> getIdFieldForEntity(Object entity) {
    Optional<Field> maybeIdField = getIdFieldForEntityClass(entity.getClass());
    if (maybeIdField.isPresent()) {
      Field idField = maybeIdField.get();

      String getterName = "get" + ObjectUtils.ucfirst(idField.getName());
      Method getter = ReflectionUtils.findMethod(entity.getClass(), getterName);
      Object id = ReflectionUtils.invokeMethod(getter, entity);

      return Optional.of(id);
    } else {
      return Optional.empty();
    }
  }

  public static Object getIdFieldForEntity(Field idField, Object entity) {
    String getterName = "get" + ObjectUtils.ucfirst(idField.getName());
    Method getter = ReflectionUtils.findMethod(entity.getClass(), getterName);
    return ReflectionUtils.invokeMethod(getter, entity);
  }

  public static Method getGetterForField(Class<?> cls, Field field) {
    String getterName = "get" + ucfirst(field.getName());
    return ReflectionUtils.findMethod(cls, getterName);
  }

  public static Method getSetterForField(Class<?> cls, Field field) {
    String setterName = "set" + ucfirst(field.getName());
    return ReflectionUtils.findMethod(cls, setterName, field.getType());
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
    } else if (input.length() == 0) {
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
  public static String lcfirst(String input) {
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
    float[] floats = new float[input.length];
    for (int i = 0; i < input.length; i++) {
      floats[i] = Long.valueOf(input[i]).floatValue();
    }
    return floatArrayToByteArray(floats);
  }

  public static Path getResourcePath(Resource resource) {
    try (InputStream inputStream = resource.getInputStream()) {
      // Create the file rooted at the location pointed by the System property "java.io.tmpdir"
      // and named after the resource filename
      Path tempFile = Paths.get(System.getProperty("java.io.tmpdir"), resource.getFilename());

      // Check if the file exists
      if (!Files.exists(tempFile)) {
        // Create the file and copy the resource contents to it
        Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
      }

      return tempFile;
    } catch (IOException e) {
      return null;
    }
  }

  public static Path getDecompressedResourcePath(Resource resource) {
    try (InputStream inputStream = resource.getInputStream()) {
      // Get the filename without the extension
      String resourceName = resource.getFilename();
      String filenameWithoutExtension = resourceName.contains(".")
          ? resourceName.substring(0, resourceName.lastIndexOf('.'))
          : resourceName;

      // Create the temp directory rooted at the location pointed by the System property "java.io.tmpdir"
      // and named after the resource filename without the file extension
      Path tempDirectory = Paths.get(System.getProperty("java.io.tmpdir"), filenameWithoutExtension);

      // Check if the directory exists, if not, create it
      if (!Files.exists(tempDirectory)) {
        Files.createDirectories(tempDirectory);

        // Wrap the InputStream in a ZipInputStream
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
          ZipEntry entry;

          // Iterate through the entries in the ZIP file
          while ((entry = zipInputStream.getNextEntry()) != null) {
            if (!entry.isDirectory()) {
              Path entryPath = tempDirectory.resolve(entry.getName());

              // Create parent directories if necessary
              Files.createDirectories(entryPath.getParent());

              // Copy the entry contents to the temporary directory
              Files.copy(zipInputStream, entryPath, StandardCopyOption.REPLACE_EXISTING);
            }
            zipInputStream.closeEntry();
          }
        }
      }

      return tempDirectory;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private ObjectUtils() {
  }
}
