package com.redis.om.spring.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.AutoComplete;
import com.redis.om.spring.annotations.AutoCompletePayload;
import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;
import com.redis.om.spring.annotations.document.fixtures.Address;
import com.redis.om.spring.annotations.document.fixtures.Airport;
import com.redis.om.spring.annotations.document.fixtures.Company;
import com.redis.om.spring.annotations.document.fixtures.CompanyRepository;
import com.redis.om.spring.annotations.document.fixtures.DocWithCustomNameId;
import com.redis.om.spring.annotations.document.fixtures.DocWithCustomNameIdRepository;

import io.redisearch.querybuilder.GeoValue;
import io.redisearch.querybuilder.GeoValue.Unit;

class ObjectUtilsTest extends AbstractBaseDocumentTest {

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
    Unit unitFor30Miles = ObjectUtils.getDistanceUnit(d30Miles);
    assertThat(unitFor30Miles).isEqualTo(GeoValue.Unit.MILES);

    Distance d25Kilometers = new Distance(25, DistanceUnit.KILOMETERS);
    Unit unitFor25Kilometers = ObjectUtils.getDistanceUnit(d25Kilometers);
    assertThat(unitFor25Kilometers).isEqualTo(GeoValue.Unit.KILOMETERS);

    Distance d20Meters = new Distance(25, DistanceUnit.METERS);
    Unit unitFor20Meters = ObjectUtils.getDistanceUnit(d20Meters);
    assertThat(unitFor20Meters).isEqualTo(GeoValue.Unit.METERS);

    Distance d6Feet = new Distance(6, DistanceUnit.FEET);
    Unit unitFor6Feet = ObjectUtils.getDistanceUnit(d6Feet);
    assertThat(unitFor6Feet).isEqualTo(GeoValue.Unit.FEET);
  }

  @Test
  void testGetTargetClassName() throws NoSuchFieldException, SecurityException, NoSuchMethodException {
    List<String> lofs = new ArrayList<String>();
    int[] inta = new int[] {};
    String typeName = Company.class.getDeclaredField("publiclyListed").getType().getName();

    assertThat(ObjectUtils.getTargetClassName(String.class.getTypeName())).isEqualTo(String.class.getTypeName());
    assertThat(ObjectUtils.getTargetClassName(lofs.getClass().getTypeName())).isEqualTo(ArrayList.class.getTypeName());
    assertThat(ObjectUtils.getTargetClassName(inta.getClass().getTypeName())).isEqualTo(int[].class.getTypeName());
    assertThat(ObjectUtils.getTargetClassName(typeName)).isEqualTo(boolean.class.getTypeName());
    assertThat(
        ObjectUtils.getTargetClassName("java.util.List<com.redis.om.spring.annotations.document.fixtures.Attribute>"))
            .isEqualTo(List.class.getTypeName());
  }

  @Test
  void testFirstToLowercase() {
    assertThat(ObjectUtils.firstToLowercase("Spam")).isEqualTo("spam");
    assertThat(ObjectUtils.firstToLowercase("spam")).isEqualTo("spam");
    assertThat(ObjectUtils.firstToLowercase("*light")).isEqualTo("*light");
    assertThat(ObjectUtils.firstToLowercase("8675309")).isEqualTo("8675309");
  }

  static class BunchOfCollections {
    public List<String> lofs = new ArrayList<String>();
    public Set<Integer> sois = new HashSet<Integer>();
    public Iterable<Company> ioc = new ArrayList<Company>();
  }

  @Test
  void testGetCollectionElementType() throws NoSuchFieldException, SecurityException {
    Field lofsField = BunchOfCollections.class.getDeclaredField("lofs");
    Field soisField = BunchOfCollections.class.getDeclaredField("sois");
    Field iocField = BunchOfCollections.class.getDeclaredField("ioc");

    Optional<Class<?>> maybeContentsOfLofs = ObjectUtils.getCollectionElementType(lofsField);
    Optional<Class<?>> maybeContentsOfSois = ObjectUtils.getCollectionElementType(soisField);
    Optional<Class<?>> maybeContentsOfIoc = ObjectUtils.getCollectionElementType(iocField);

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

    Optional<?> maybeId = ObjectUtils.getIdFieldForEntity(redis);

    assertThat(maybeId).isPresent();
    assertThat(maybeId.get()).hasToString(actualCompanyId);

    DocWithCustomNameId doc = docWithCustomNameIdRepository.save(new DocWithCustomNameId());
    String actualDocId = doc.getIdentidad();

    Optional<?> maybeDocId = ObjectUtils.getIdFieldForEntity(doc);

    assertThat(maybeDocId).isPresent();
    assertThat(maybeDocId.get()).hasToString(actualDocId);

    Optional<?> noEntityId = ObjectUtils.getIdFieldForEntity(new String());
    assertThat(noEntityId).isEmpty();
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
    assertThat(ObjectUtils.unQuote("\"The quick \\\"brown\\\" fox \\\"jumps\\\" over the lazy dog\""))
        .isEqualTo("The quick \\\"brown\\\" fox \\\"jumps\\\" over the lazy dog");
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
}
