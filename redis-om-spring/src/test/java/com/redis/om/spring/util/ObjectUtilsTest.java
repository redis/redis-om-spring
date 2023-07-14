package com.redis.om.spring.util;

import com.google.common.collect.Sets;
import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.AutoComplete;
import com.redis.om.spring.annotations.AutoCompletePayload;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import com.redis.om.spring.annotations.document.fixtures.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit;
import org.springframework.util.ReflectionUtils;
import redis.clients.jedis.args.GeoUnit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SuppressWarnings({ "ConstantConditions", "SpellCheckingInspection" }) class ObjectUtilsTest extends AbstractBaseDocumentTest {

  @Autowired
  CompanyRepository companyRepository;

  @Autowired
  DocWithCustomNameIdRepository docWithCustomNameIdRepository;

  @AfterEach
  void cleanUp() {
    companyRepository.deleteAll();
    docWithCustomNameIdRepository.deleteAll();
  }

  @Test
  void testGetDistanceAsRedisString() {
    var distance1 = new Distance(48.5, DistanceUnit.MILES);
    var distanceAsString1 = ObjectUtils.getDistanceAsRedisString(distance1);
    var distance2 = new Distance(55, DistanceUnit.KILOMETERS);
    var distanceAsString2 = ObjectUtils.getDistanceAsRedisString(distance2);
    var distance3 = new Distance(1.80, DistanceUnit.METERS);
    var distanceAsString3 = ObjectUtils.getDistanceAsRedisString(distance3);
    var distance4 = new Distance(20, DistanceUnit.FEET);
    var distanceAsString4 = ObjectUtils.getDistanceAsRedisString(distance4);
    assertThat(distanceAsString1).isEqualTo("48.5 mi");
    assertThat(distanceAsString2).isEqualTo("55.0 km");
    assertThat(distanceAsString3).isEqualTo("1.8 m");
    assertThat(distanceAsString4).isEqualTo("20.0 ft");
  }

  @Test
  void testGetFieldsWithAnnotation() {
    List<Field> fields01 = ObjectUtils.getFieldsWithAnnotation(Address.class, Indexed.class);
    List<String> fieldNames01 = fields01.stream().map(Field::getName).collect(Collectors.toList());
    assertThat(fieldNames01).containsExactly("city");

    List<Field> fields02 = ObjectUtils.getFieldsWithAnnotation(Address.class, Searchable.class);
    List<String> fieldNames02 = fields02.stream().map(Field::getName).collect(Collectors.toList());
    assertThat(fieldNames02).containsExactly("street");

    List<Field> fields03 = ObjectUtils.getFieldsWithAnnotation(Airport.class, AutoComplete.class);
    List<String> fieldNames03 = fields03.stream().map(Field::getName).collect(Collectors.toList());
    assertThat(fieldNames03).contains("name");

    List<Field> fields04 = ObjectUtils.getFieldsWithAnnotation(Airport.class, AutoCompletePayload.class);
    List<String> fieldNames04 = fields04.stream().map(Field::getName).collect(Collectors.toList());
    assertThat(fieldNames04).contains("code", "state");
  }

  @Test
  void testGetDistanceUnit() {
    Distance d30Miles = new Distance(30, DistanceUnit.MILES);
    GeoUnit unitFor30Miles = ObjectUtils.getDistanceUnit(d30Miles);
    assertThat(unitFor30Miles).isEqualTo(GeoUnit.MI);

    Distance d25Kilometers = new Distance(25, DistanceUnit.KILOMETERS);
    GeoUnit unitFor25Kilometers = ObjectUtils.getDistanceUnit(d25Kilometers);
    assertThat(unitFor25Kilometers).isEqualTo(GeoUnit.KM);

    Distance d20Meters = new Distance(25, DistanceUnit.METERS);
    GeoUnit unitFor20Meters = ObjectUtils.getDistanceUnit(d20Meters);
    assertThat(unitFor20Meters).isEqualTo(GeoUnit.M);

    Distance d6Feet = new Distance(6, DistanceUnit.FEET);
    GeoUnit unitFor6Feet = ObjectUtils.getDistanceUnit(d6Feet);
    assertThat(unitFor6Feet).isEqualTo(GeoUnit.FT);
  }

  @Test
  void testGetTargetClassName() throws SecurityException {
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") List<String> lofs = new ArrayList<>();
    int[] inta = new int[] {};
    Field field = ReflectionUtils.findField(Company.class, "publiclyListed");
    assertThat(field).isNotNull();
    String typeName = field.getType().getName();

    assertThat(ObjectUtils.getTargetClassName(String.class.getTypeName())).isEqualTo(String.class.getTypeName());
    assertThat(ObjectUtils.getTargetClassName(lofs.getClass().getTypeName())).isEqualTo(ArrayList.class.getTypeName());
    assertThat(ObjectUtils.getTargetClassName(inta.getClass().getTypeName())).isEqualTo(int[].class.getTypeName());
    assertThat(ObjectUtils.getTargetClassName(typeName)).isEqualTo(boolean.class.getTypeName());
    assertThat(ObjectUtils.getTargetClassName(
        "java.util.List<com.redis.om.spring.annotations.document.fixtures.Attribute>")).isEqualTo(List.class.getTypeName());
  }

  @Test
  void testFirstToLowercase() {
    assertThat(ObjectUtils.firstToLowercase("Spam")).isEqualTo("spam");
    assertThat(ObjectUtils.firstToLowercase("spam")).isEqualTo("spam");
    assertThat(ObjectUtils.firstToLowercase("*light")).isEqualTo("*light");
    assertThat(ObjectUtils.firstToLowercase("8675309")).isEqualTo("8675309");
  }

  static class BunchOfCollections {
    public final List<String> lofs = new ArrayList<>();
    public final Set<Integer> sois = new HashSet<>();
    public final Iterable<Company> ioc = new ArrayList<>();
  }

  @Test
  void testGetCollectionElementType() throws SecurityException {
    Field lofsField = ReflectionUtils.findField(BunchOfCollections.class, "lofs");
    Field soisField = ReflectionUtils.findField(BunchOfCollections.class, "sois");
    Field iocField = ReflectionUtils.findField(BunchOfCollections.class, "ioc");

    assertThat(lofsField).isNotNull();
    assertThat(soisField).isNotNull();
    assertThat(iocField).isNotNull();

    Optional<Class<?>> maybeContentsOfLofs = ObjectUtils.getCollectionElementClass(lofsField);
    Optional<Class<?>> maybeContentsOfSois = ObjectUtils.getCollectionElementClass(soisField);
    Optional<Class<?>> maybeContentsOfIoc = ObjectUtils.getCollectionElementClass(iocField);

    assertAll( //
        () -> assertThat(maybeContentsOfLofs).isPresent(), //
        () -> assertThat(maybeContentsOfLofs).contains(String.class), //

        () -> assertThat(maybeContentsOfSois).isPresent(), //
        () -> assertThat(maybeContentsOfSois).contains(Integer.class), //

        () -> assertThat(maybeContentsOfIoc).isPresent(), //
        () -> assertThat(maybeContentsOfIoc).contains(Company.class) //
    );
  }

  @Test
  void testGetIdFieldForEntityClass() {
    Optional<Field> idFieldForCompany = ObjectUtils.getIdFieldForEntityClass(Company.class);

    assertThat(idFieldForCompany).isPresent();
    assertThat(idFieldForCompany.get().getName()).isEqualTo("id");

    Optional<Field> idFieldForDocWithCustomNameId = ObjectUtils.getIdFieldForEntityClass(DocWithCustomNameId.class);

    assertThat(idFieldForDocWithCustomNameId).isPresent();
    assertThat(idFieldForDocWithCustomNameId.get().getName()).isEqualTo("identidad");
  }

  @Test
  void testGetIdFieldForEntity() {
    Company redis = companyRepository.save(
        Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690), "stack@redis.com"));
    String actualCompanyId = redis.getId();

    Object id = ObjectUtils.getIdFieldForEntity(redis);

    assertThat(id).isNotNull();
    assertThat(id).hasToString(actualCompanyId);

    DocWithCustomNameId doc = docWithCustomNameIdRepository.save(new DocWithCustomNameId());
    String actualDocId = doc.getIdentidad();

    Object docId = ObjectUtils.getIdFieldForEntity(doc);

    assertThat(docId).isNotNull();
    assertThat(docId).hasToString(actualDocId);

    Object noEntityId = ObjectUtils.getIdFieldForEntity("");
    assertThat(noEntityId).isNull();
  }

  @Test
  void testGetGetterForField() {
    Optional<Field> idFieldForCompany = ObjectUtils.getIdFieldForEntityClass(Company.class);
    assertThat(idFieldForCompany).isPresent();
    Method companyIdGetter = ObjectUtils.getGetterForField(Company.class, idFieldForCompany.get());
    assertThat(companyIdGetter).isNotNull();
    assertThat(companyIdGetter.getName()).isEqualTo("getId");

    Optional<Field> idFieldForDocWithCustomNameId = ObjectUtils.getIdFieldForEntityClass(DocWithCustomNameId.class);
    assertThat(idFieldForDocWithCustomNameId).isPresent();
    Method docIdGetter = ObjectUtils.getGetterForField(DocWithCustomNameId.class, idFieldForDocWithCustomNameId.get());
    assertThat(docIdGetter).isNotNull();
    assertThat(docIdGetter.getName()).isEqualTo("getIdentidad");
  }

  @Test
  void testGetSetterForField() {
    Optional<Field> idFieldForCompany = ObjectUtils.getIdFieldForEntityClass(Company.class);
    assertThat(idFieldForCompany).isPresent();
    Method companyIdGetter = ObjectUtils.getSetterForField(Company.class, idFieldForCompany.get());
    assertThat(companyIdGetter).isNotNull();
    assertThat(companyIdGetter.getName()).isEqualTo("setId");

    Optional<Field> idFieldForDocWithCustomNameId = ObjectUtils.getIdFieldForEntityClass(DocWithCustomNameId.class);
    assertThat(idFieldForDocWithCustomNameId).isPresent();
    Method docIdGetter = ObjectUtils.getSetterForField(DocWithCustomNameId.class, idFieldForDocWithCustomNameId.get());
    assertThat(docIdGetter).isNotNull();
    assertThat(docIdGetter.getName()).isEqualTo("setIdentidad");
  }

  @Test
  void testUcfirst() {
    assertThat(ObjectUtils.ucfirst("Spam")).isEqualTo("Spam");
    assertThat(ObjectUtils.ucfirst("spam")).isEqualTo("Spam");
    assertThat(ObjectUtils.ucfirst("*light")).isEqualTo("*light");
    assertThat(ObjectUtils.ucfirst("8675309")).isEqualTo("8675309");
  }

  @Test
  void testWithFirst() {
    String toUpper = ObjectUtils.withFirst("spam", first -> String.valueOf(Character.toUpperCase(first)));
    String toLower = ObjectUtils.withFirst("Spam", first -> String.valueOf(Character.toLowerCase(first)));
    String toX = ObjectUtils.withFirst("Spam", first -> "X");
    String nullToX = ObjectUtils.withFirst(null, first -> "X");
    String emptyToX = ObjectUtils.withFirst("", first -> "X");

    assertThat(toUpper).isEqualTo("Spam");
    assertThat(toLower).isEqualTo("spam");
    assertThat(toX).isEqualTo("Xpam");
    assertThat(nullToX).isNull();
    assertThat(emptyToX).isEmpty();
  }

  @Test
  void testIsFirstLowerCase() {
    assertThat(ObjectUtils.isFirstLowerCase("Spam")).isFalse();
    assertThat(ObjectUtils.isFirstLowerCase("spam")).isTrue();
    assertThat(ObjectUtils.isFirstLowerCase("*light")).isFalse();
    assertThat(ObjectUtils.isFirstLowerCase("8675309")).isFalse();
  }

  @Test
  void testLcfirst() {
    assertThat(ObjectUtils.lcfirst("Spam")).isEqualTo("spam");
    assertThat(ObjectUtils.lcfirst("spam")).isEqualTo("spam");
    assertThat(ObjectUtils.lcfirst("*light")).isEqualTo("*light");
    assertThat(ObjectUtils.lcfirst("8675309")).isEqualTo("8675309");
  }

  @Test
  void testUnQuote() {
    assertThat(ObjectUtils.unQuote("\"Spam\"")).isEqualTo("Spam");
    assertThat(ObjectUtils.unQuote("Spam")).isEqualTo("Spam");
    assertThat(ObjectUtils.unQuote("\"The quick \\\"brown\\\" fox \\\"jumps\\\" over the lazy dog\"")).isEqualTo(
        "The quick \\\"brown\\\" fox \\\"jumps\\\" over the lazy dog");
  }

  @Test
  void testShortName() {
    assertThat(ObjectUtils.shortName("java.util.Map<String, java.util.Date>")).isEqualTo("Map<String, java.util.Date>");
  }

  @Test
  void testToUnderscoreSeparated() {
    assertThat(ObjectUtils.toUnderscoreSeparated("city")).isEqualTo("city");
    assertThat(ObjectUtils.toUnderscoreSeparated("someValue")).isEqualTo("some_value");
    assertThat(ObjectUtils.toUnderscoreSeparated("someOtherValue")).isEqualTo("some_other_value");
  }

  @Test
  void testIsPropertyAnnotatedWith() {
    assertThat(ObjectUtils.isPropertyAnnotatedWith(Address.class, "city", Indexed.class)).isTrue();
    assertThat(ObjectUtils.isPropertyAnnotatedWith(Address.class, "city", Searchable.class)).isFalse();
    assertThat(ObjectUtils.isPropertyAnnotatedWith(Address.class, "street", Searchable.class)).isTrue();
    assertThat(ObjectUtils.isPropertyAnnotatedWith(Address.class, "street", Indexed.class)).isFalse();
    assertThat(ObjectUtils.isPropertyAnnotatedWith(Airport.class, "name", AutoComplete.class)).isTrue();
    assertThat(ObjectUtils.isPropertyAnnotatedWith(Airport.class, "name", AutoCompletePayload.class)).isFalse();
    assertThat(ObjectUtils.isPropertyAnnotatedWith(Airport.class, "code", AutoCompletePayload.class)).isTrue();
    assertThat(ObjectUtils.isPropertyAnnotatedWith(Airport.class, "code", Indexed.class)).isFalse();
    assertThat(ObjectUtils.isPropertyAnnotatedWith(Airport.class, "state", AutoCompletePayload.class)).isTrue();
    assertThat(ObjectUtils.isPropertyAnnotatedWith(Airport.class, "state", Searchable.class)).isFalse();
    assertThat(ObjectUtils.isPropertyAnnotatedWith(Airport.class, "nonExistentField", Searchable.class)).isFalse();
  }

  @Test
  void testGetValueByPath() {
    Company redis = Company.of("RedisInc", 2011, LocalDate.of(2021, 5, 1), new Point(-122.066540, 37.377690),
        "stack@redis.com");
    redis.setId("8675309");
    redis.setMetaList(Set.of(CompanyMeta.of("RD", 100, Set.of("RedisTag", "CommonTag"))));
    redis.setTags(Set.of("fast", "scalable", "reliable", "database", "nosql"));

    Set<Employee> employees = Sets.newHashSet(Employee.of("Brian Sam-Bodden"), Employee.of("Guy Royse"),
        Employee.of("Justin Castilla"));
    redis.setEmployees(employees);

    String id = (String) ObjectUtils.getValueByPath(redis, "$.id");
    String name = (String) ObjectUtils.getValueByPath(redis, "$.name");
    Integer yearFounded = (Integer) ObjectUtils.getValueByPath(redis, "$.yearFounded");
    LocalDate lastValuation = (LocalDate) ObjectUtils.getValueByPath(redis, "$.lastValuation");
    Point location = (Point) ObjectUtils.getValueByPath(redis, "$.location");
    Set<String> tags = (Set<String>) ObjectUtils.getValueByPath(redis, "$.tags[*]");
    String email = (String) ObjectUtils.getValueByPath(redis, "$.email");
    boolean publiclyListed = (boolean) ObjectUtils.getValueByPath(redis, "$.publiclyListed");
    Collection<Integer> metaList_numberValue = (Collection<Integer>) ObjectUtils.getValueByPath(redis, "$.metaList[0:].numberValue");
    Collection<String> metaList_stringValue = (Collection<String>) ObjectUtils.getValueByPath(redis, "$.metaList[0:].stringValue");
    Collection<String> employees_name = (Collection<String>) ObjectUtils.getValueByPath(redis, "$.employees[0:].name");

    assertAll( //
        () -> assertThat(id).isEqualTo(redis.getId()),
        () -> assertThat(name).isEqualTo(redis.getName()),
        () -> assertThat(yearFounded).isEqualTo(redis.getYearFounded()),
        () -> assertThat(lastValuation).isEqualTo(redis.getLastValuation()),
        () -> assertThat(location).isEqualTo(redis.getLocation()),
        () -> assertThat(tags).isEqualTo(redis.getTags()),
        () -> assertThat(email).isEqualTo(redis.getEmail()),
        () -> assertThat(publiclyListed).isEqualTo(redis.isPubliclyListed()),
        () -> assertThat(metaList_numberValue).containsExactlyElementsOf(redis.getMetaList().stream().map(CompanyMeta::getNumberValue).toList()),
        () -> assertThat(metaList_stringValue).containsExactlyElementsOf(redis.getMetaList().stream().map(CompanyMeta::getStringValue).toList()),
        () -> assertThat(employees_name).containsExactlyElementsOf(redis.getEmployees().stream().map(Employee::getName).toList())
    );
  }

  @Test
  void testFlattenCollection() {
    var nested = List.of(List.of(List.of("a", "b")), List.of("c", "d"), "e", List.of(List.of(List.of("f"))));
    var flatten = ObjectUtils.flattenCollection(nested);
    assertThat(flatten).containsExactly("a", "b", "c", "d", "e", "f");
  }

  @Test
  void testPageFromSlice() {
    List<String> strings = List.of("Pantufla", "Mondongo", "Latifundio", "Alcachofa");
    Slice<String> slice = new SliceImpl<>(strings);

    Page<String> page = ObjectUtils.pageFromSlice(slice);

    assertThat(page.getContent()).hasSize(4);
    assertThat(page.getContent().get(0)).isEqualTo("Pantufla");
    assertThat(page.getNumber()).isEqualTo(slice.getNumber());
    assertThat(page.getSize()).isEqualTo(slice.getSize());
    assertThat(page.getNumberOfElements()).isEqualTo(slice.getNumberOfElements());
    assertThat(page.getSort()).isEqualTo(slice.getSort());
    assertThat(page.hasContent()).isEqualTo(slice.hasContent());
    assertThat(page.hasNext()).isEqualTo(slice.hasNext());
    assertThat(page.hasPrevious()).isEqualTo(slice.hasPrevious());
    assertThat(page.isFirst()).isEqualTo(slice.isFirst());
    assertThat(page.isLast()).isEqualTo(slice.isLast());
    assertThat(page.nextPageable()).isEqualTo(slice.nextPageable());
    assertThat(page.previousPageable()).isEqualTo(slice.previousPageable());
    assertThat(page.getTotalPages()).isEqualTo(-1);
    assertThat(page.getPageable()).isEqualTo(Pageable.ofSize(4));
  }

  @Test
  public void testEmptyString() {
    String result = ObjectUtils.replaceIfIllegalJavaIdentifierCharacter("");
    assertThat(result).isEqualTo(ObjectUtils.REPLACEMENT_CHARACTER.toString());
  }

  @Test
  public void testValidIdentifier() {
    String input = "validIdentifier";
    String result = ObjectUtils.replaceIfIllegalJavaIdentifierCharacter(input);
    assertThat(result).isEqualTo(input);
  }

  @Test
  public void testInvalidStartCharacter() {
    String result = ObjectUtils.replaceIfIllegalJavaIdentifierCharacter("1invalid");
    assertThat(result).startsWith(ObjectUtils.REPLACEMENT_CHARACTER.toString());
  }

  @Test
  public void testInvalidPartCharacter() {
    String result = ObjectUtils.replaceIfIllegalJavaIdentifierCharacter("invalid*identifier");
    assertThat(result).isEqualTo("invalid" + ObjectUtils.REPLACEMENT_CHARACTER.toString() + "identifier");
  }

  @Test
  public void testCompletelyInvalidString() {
    String result = ObjectUtils.replaceIfIllegalJavaIdentifierCharacter("!@#*%^&*()");
    String expected = "__________";
    assertThat(result).isEqualTo(expected);
  }
}
