package com.redis.om.spring.metamodel;

import com.karuslabs.elementary.Results;
import com.karuslabs.elementary.junit.JavacExtension;
import com.karuslabs.elementary.junit.annotations.Classpath;
import com.karuslabs.elementary.junit.annotations.Options;
import com.karuslabs.elementary.junit.annotations.Processors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.tools.JavaFileObject;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SuppressWarnings("SpellCheckingInspection")
@ExtendWith(JavacExtension.class)
@Options("-Werror")
@Processors({ MetamodelGenerator.class })
class MetamodelGeneratorTest {
  @Test
  @Classpath("data.metamodel.ValidDocumentIndexed")
  void testValidDocumentIndexed(Results results) throws IOException {
    List<String> warnings = getWarningStrings(results);
    assertThat(warnings).isEmpty();

    List<String> errors = getErrorStrings(results);
    assertThat(errors).isEmpty();

    assertThat(results.generated).hasSize(1);
    JavaFileObject metamodel = results.generated.get(0);
    assertThat(metamodel.getName()).isEqualTo("/SOURCE_OUTPUT/valid/ValidDocumentIndexed$.java");

    var fileContents = metamodel.getCharContent(true);

    assertAll( //

        // test package matches source package
        () -> assertThat(fileContents).contains("package valid;"), //

        // test Fields generation
        () -> assertThat(fileContents).contains("public static Field createdDate;"), //
        () -> assertThat(fileContents).contains("public static Field lastModifiedDate;"), //
        () -> assertThat(fileContents).contains("public static Field email;"), //
        () -> assertThat(fileContents).contains("public static Field publiclyListed;"), //
        () -> assertThat(fileContents).contains("public static Field lastValuation;"), //
        () -> assertThat(fileContents).contains("public static Field id;"), //
        () -> assertThat(fileContents).contains("public static Field yearFounded;"), //
        () -> assertThat(fileContents).contains("public static Field name;"), //
        () -> assertThat(fileContents).contains("public static Field location;"), //
        () -> assertThat(fileContents).contains("public static Field tags;"), //

        // test fields initialization
        () -> assertThat(fileContents).contains(
            "createdDate = com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(ValidDocumentIndexed.class, \"createdDate\");"),
        //
        () -> assertThat(fileContents).contains(
            "lastModifiedDate = com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(ValidDocumentIndexed.class, \"lastModifiedDate\");"),
        //
        () -> assertThat(fileContents).contains(
            "email = com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(ValidDocumentIndexed.class, \"email\");"),
        //
        () -> assertThat(fileContents).contains(
            "publiclyListed = com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(ValidDocumentIndexed.class, \"publiclyListed\");"),
        //
        () -> assertThat(fileContents).contains(
            "lastValuation = com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(ValidDocumentIndexed.class, \"lastValuation\");"),
        //
        () -> assertThat(fileContents).contains(
            "id = com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(ValidDocumentIndexed.class, \"id\");"),
        //
        () -> assertThat(fileContents).contains(
            "yearFounded = com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(ValidDocumentIndexed.class, \"yearFounded\");"),
        //
        () -> assertThat(fileContents).contains(
            "name = com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(ValidDocumentIndexed.class, \"name\");"),
        //
        () -> assertThat(fileContents).contains(
            "location = com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(ValidDocumentIndexed.class, \"location\");"),
        //
        () -> assertThat(fileContents).contains(
            "tags = com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(ValidDocumentIndexed.class, \"tags\");"),
        //

        // test Metamodel Field generation
        () -> assertThat(fileContents).contains(
            "public static NonIndexedNumericField<ValidDocumentIndexed, Date> CREATED_DATE;"), //
        () -> assertThat(fileContents).contains(
            "public static NonIndexedNumericField<ValidDocumentIndexed, Date> LAST_MODIFIED_DATE;"), //
        () -> assertThat(fileContents).contains("public static TextTagField<ValidDocumentIndexed, String> EMAIL;"), //
        () -> assertThat(fileContents).contains(
            "public static BooleanField<ValidDocumentIndexed, Boolean> PUBLICLY_LISTED;"), //
        () -> assertThat(fileContents).contains(
            "public static DateField<ValidDocumentIndexed, LocalDate> LAST_VALUATION;"), //
        () -> assertThat(fileContents).contains("public static TextTagField<ValidDocumentIndexed, String> ID;"), //
        () -> assertThat(fileContents).contains(
            "public static NumericField<ValidDocumentIndexed, Integer> YEAR_FOUNDED;"), //
        () -> assertThat(fileContents).contains("public static TextField<ValidDocumentIndexed, String> NAME;"), //
        () -> assertThat(fileContents).contains("public static GeoField<ValidDocumentIndexed, Point> LOCATION;"), //
        () -> assertThat(fileContents).contains("public static TagField<ValidDocumentIndexed, Set<String>> TAGS;"), //

        // test Metamodel Field initialization
        () -> assertThat(fileContents).contains(
            "CREATED_DATE = new NonIndexedNumericField<ValidDocumentIndexed, Date>(new SearchFieldAccessor(\"createdDate\", \"$.createdDate\", createdDate),false);"),
        //
        () -> assertThat(fileContents).contains(
            "LAST_MODIFIED_DATE = new NonIndexedNumericField<ValidDocumentIndexed, Date>(new SearchFieldAccessor(\"lastModifiedDate\", \"$.lastModifiedDate\", lastModifiedDate),false);"),
        //
        () -> assertThat(fileContents).contains(
            "PUBLICLY_LISTED = new BooleanField<ValidDocumentIndexed, Boolean>(new SearchFieldAccessor(\"publiclyListed\", \"$.publiclyListed\", publiclyListed),true);"),
        //
        () -> assertThat(fileContents).contains(
            "LAST_VALUATION = new DateField<ValidDocumentIndexed, LocalDate>(new SearchFieldAccessor(\"lastValuation\", \"$.lastValuation\", lastValuation),true);"),
        //
        () -> assertThat(fileContents).contains(
            "ID = new TextTagField<ValidDocumentIndexed, String>(new SearchFieldAccessor(\"id\", \"$.id\", id),true);"),
        //
        () -> assertThat(fileContents).contains(
            "YEAR_FOUNDED = new NumericField<ValidDocumentIndexed, Integer>(new SearchFieldAccessor(\"yearFounded\", \"$.yearFounded\", yearFounded),true);"),
        //
        () -> assertThat(fileContents).contains(
            "NAME = new TextField<ValidDocumentIndexed, String>(new SearchFieldAccessor(\"name\", \"$.name\", name),true);"),
        //
        () -> assertThat(fileContents).contains(
            "LOCATION = new GeoField<ValidDocumentIndexed, Point>(new SearchFieldAccessor(\"location\", \"$.location\", location),true);"),
        //
        () -> assertThat(fileContents).contains(
            "TAGS = new TagField<ValidDocumentIndexed, Set<String>>(new SearchFieldAccessor(\"tags\", \"$.tags\", tags),true);"),
        //
        () -> assertThat(fileContents).contains(
            "EMAIL = new TextTagField<ValidDocumentIndexed, String>(new SearchFieldAccessor(\"email\", \"$.email\", email),true);"),
        () -> assertThat(fileContents).contains(
            "_KEY = new MetamodelField<ValidDocumentIndexed, String>(\"__key\", String.class, true);"));
  }

  @Test
  @Classpath("data.metamodel.ValidDocumentUnindexed")
  void testValidDocumentUnindexed(Results results) throws IOException {
    List<String> warnings = getWarningStrings(results);
    assertThat(warnings).isEmpty();

    List<String> errors = getErrorStrings(results);
    assertThat(errors).isEmpty();

    assertThat(results.generated).hasSize(1);
    JavaFileObject metamodel = results.generated.get(0);
    assertThat(metamodel.getName()).isEqualTo("/SOURCE_OUTPUT/valid/ValidDocumentUnindexed$.java");

    var fileContents = metamodel.getCharContent(true);

    assertAll( //
        // test package matches source package
        () -> assertThat(fileContents).contains("package valid;"), //

        // test Fields generation
        () -> assertThat(fileContents).contains("public final class ValidDocumentUnindexed$ {"), //
        () -> assertThat(fileContents).contains("public static Field id;"), //
        () -> assertThat(fileContents).contains("public static Field ulid;"), //
        () -> assertThat(fileContents).contains("public static Field setThings;"), //
        () -> assertThat(fileContents).contains("public static Field localDateTime;"), //
        () -> assertThat(fileContents).contains("public static Field point;"), //
        () -> assertThat(fileContents).contains("public static Field listThings;"), //
        () -> assertThat(fileContents).contains("public static Field date;"), //
        () -> assertThat(fileContents).contains("public static Field localDate;"), //
        () -> assertThat(fileContents).contains("public static Field integerWrapper;"), //
        () -> assertThat(fileContents).contains("public static Field integerPrimitive;"), //
        () -> assertThat(fileContents).contains("public static Field string;"), //
        () -> assertThat(fileContents).contains("public static Field bool;"), //

        // test fields initialization
        () -> assertThat(fileContents).contains(
            "id = com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(ValidDocumentUnindexed.class, \"id\");"),
        //
        () -> assertThat(fileContents).contains(
            "setThings = com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(ValidDocumentUnindexed.class, \"setThings\");"),
        //
        () -> assertThat(fileContents).contains(
            "localDateTime = com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(ValidDocumentUnindexed.class, \"localDateTime\");"),
        //
        () -> assertThat(fileContents).contains(
            "point = com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(ValidDocumentUnindexed.class, \"point\");"),
        //
        () -> assertThat(fileContents).contains(
            "listThings = com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(ValidDocumentUnindexed.class, \"listThings\");"),
        //
        () -> assertThat(fileContents).contains(
            "date = com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(ValidDocumentUnindexed.class, \"date\");"),
        //
        () -> assertThat(fileContents).contains(
            "localDate = com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(ValidDocumentUnindexed.class, \"localDate\");"),
        //
        () -> assertThat(fileContents).contains(
            "ulid = com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(ValidDocumentUnindexed.class, \"ulid\");"),
        //
        () -> assertThat(fileContents).contains(
            "integerWrapper = com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(ValidDocumentUnindexed.class, \"integerWrapper\");"),
        //
        () -> assertThat(fileContents).contains(
            "integerPrimitive = com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(ValidDocumentUnindexed.class, \"integerPrimitive\");"),
        //
        () -> assertThat(fileContents).contains(
            "string = com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(ValidDocumentUnindexed.class, \"string\");"),
        //
        () -> assertThat(fileContents).contains(
            "bool = com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(ValidDocumentUnindexed.class, \"bool\");"),
        //

        // test Metamodel Field generation
        () -> assertThat(fileContents).contains("public static TextTagField<ValidDocumentUnindexed, String> ID;"), //
        () -> assertThat(fileContents).contains(
            "public static NonIndexedTagField<ValidDocumentUnindexed, Set<String>> SET_THINGS;"), //
        () -> assertThat(fileContents).contains(
            "public static NonIndexedNumericField<ValidDocumentUnindexed, LocalDateTime> LOCAL_DATE_TIME;"), //
        () -> assertThat(fileContents).contains(
            "public static NonIndexedGeoField<ValidDocumentUnindexed, Point> POINT;"),
        //
        () -> assertThat(fileContents).contains(
            "public static NonIndexedTagField<ValidDocumentUnindexed, List<String>> LIST_THINGS;"), //
        () -> assertThat(fileContents).contains(" static NonIndexedNumericField<ValidDocumentUnindexed, Date> DATE;"),
        //
        () -> assertThat(fileContents).contains(
            "public static NonIndexedNumericField<ValidDocumentUnindexed, LocalDate> LOCAL_DATE;"), //
        () -> assertThat(fileContents).contains(
            "public static NonIndexedTextField<ValidDocumentUnindexed, Ulid> ULID;"),
        //
        () -> assertThat(fileContents).contains(
            "public static NonIndexedNumericField<ValidDocumentUnindexed, Integer> INTEGER_WRAPPER;"), //
        () -> assertThat(fileContents).contains(
            "public static NonIndexedNumericField<ValidDocumentUnindexed, Integer> INTEGER_PRIMITIVE;"), //
        () -> assertThat(fileContents).contains(
            "public static NonIndexedTextField<ValidDocumentUnindexed, String> STRING_;"), //
        () -> assertThat(fileContents).contains(
            "public static NonIndexedBooleanField<ValidDocumentUnindexed, Boolean> BOOL;"), //

        // test Metamodel Field initialization
        () -> assertThat(fileContents).contains(
            "ID = new TextTagField<ValidDocumentUnindexed, String>(new SearchFieldAccessor(\"id\", \"$.id\", id),true);"),
        //
        () -> assertThat(fileContents).contains(
            "SET_THINGS = new NonIndexedTagField<ValidDocumentUnindexed, Set<String>>(new SearchFieldAccessor(\"setThings\", \"$.setThings\", setThings),false);"),
        //
        () -> assertThat(fileContents).contains(
            "LOCAL_DATE_TIME = new NonIndexedNumericField<ValidDocumentUnindexed, LocalDateTime>(new SearchFieldAccessor(\"localDateTime\", \"$.localDateTime\", localDateTime),false);"),
        //
        () -> assertThat(fileContents).contains(
            "POINT = new NonIndexedGeoField<ValidDocumentUnindexed, Point>(new SearchFieldAccessor(\"point\", \"$.point\", point),false);"),
        //
        () -> assertThat(fileContents).contains(
            "LIST_THINGS = new NonIndexedTagField<ValidDocumentUnindexed, List<String>>(new SearchFieldAccessor(\"listThings\", \"$.listThings\", listThings),false);"),
        //
        () -> assertThat(fileContents).contains(
            "DATE = new NonIndexedNumericField<ValidDocumentUnindexed, Date>(new SearchFieldAccessor(\"date\", \"$.date\", date),false);"),
        //
        () -> assertThat(fileContents).contains(
            "LOCAL_DATE = new NonIndexedNumericField<ValidDocumentUnindexed, LocalDate>(new SearchFieldAccessor(\"localDate\", \"$.localDate\", localDate),false);"),
        //
        () -> assertThat(fileContents).contains(
            "ULID = new NonIndexedTextField<ValidDocumentUnindexed, Ulid>(new SearchFieldAccessor(\"ulid\", \"$.ulid\", ulid),false);"),
        //
        () -> assertThat(fileContents).contains(
            "INTEGER_WRAPPER = new NonIndexedNumericField<ValidDocumentUnindexed, Integer>(new SearchFieldAccessor(\"integerWrapper\", \"$.integerWrapper\", integerWrapper),false);"),
        //
        () -> assertThat(fileContents).contains(
            "INTEGER_PRIMITIVE = new NonIndexedNumericField<ValidDocumentUnindexed, Integer>(new SearchFieldAccessor(\"integerPrimitive\", \"$.integerPrimitive\", integerPrimitive),false);"),
        //
        () -> assertThat(fileContents).contains(
            "STRING_ = new NonIndexedTextField<ValidDocumentUnindexed, String>(new SearchFieldAccessor(\"string\", \"$.string\", string),false);"),
        //
        () -> assertThat(fileContents).contains(
            "BOOL = new NonIndexedBooleanField<ValidDocumentUnindexed, Boolean>(new SearchFieldAccessor(\"bool\", \"$.bool\", bool),false);"),
        //
        () -> assertThat(fileContents).contains(
            "_KEY = new MetamodelField<ValidDocumentUnindexed, String>(\"__key\", String.class, true);") //
    );
  }

  @Test
  @Classpath("data.metamodel.ValidDocumentIndexedNested")
  @Classpath("data.metamodel.Address")
  void testValidDocumentIndexedNested(Results results) throws IOException {
    List<String> warnings = getWarningStrings(results);
    assertThat(warnings).hasSize(1).containsOnly(
        "Processing class ValidDocumentIndexedNested could not resolve valid.Address while checking for nested @Indexed");

    List<String> errors = getErrorStrings(results);
    assertAll( //
        () -> assertThat(errors).hasSize(1), //
        () -> assertThat(errors).containsOnly("warnings found and -Werror specified") //
    );

    assertThat(results.generated).hasSize(1);
    JavaFileObject metamodel = results.generated.get(0);
    assertThat(metamodel.getName()).isEqualTo("/SOURCE_OUTPUT/valid/ValidDocumentIndexedNested$.java");

    var fileContents = metamodel.getCharContent(true);

    assertAll( //

        // test package matches source package
        () -> assertThat(fileContents).contains("package valid;"), //

        // test Fields generation
        () -> assertThat(fileContents).contains("public final class ValidDocumentIndexedNested$ {"), //
        () -> assertThat(fileContents).contains("public static Field id;"), //
        () -> assertThat(fileContents).contains("public static Field address_street;"), //
        () -> assertThat(fileContents).contains("public static Field address_city;"), //

        // test fields initialization
        () -> assertThat(fileContents).contains(
            "id = com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(ValidDocumentIndexedNested.class, \"id\");"),
        //
        () -> assertThat(fileContents).contains(
            "address_street = com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(ValidDocumentIndexedNested.class, \"address\").getType(), \"street\");"),
        //
        () -> assertThat(fileContents).contains(
            "address_city = com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(com.redis.om.spring.util.ObjectUtils.getDeclaredFieldTransitively(ValidDocumentIndexedNested.class, \"address\").getType(), \"city\");"),
        //

        // test Metamodel Field generation
        () -> assertThat(fileContents).contains("public static TextTagField<ValidDocumentIndexedNested, String> ID;"),
        //
        () -> assertThat(fileContents).contains(
            "public static TextField<ValidDocumentIndexedNested, String> ADDRESS_STREET;"), //
        () -> assertThat(fileContents).contains(
            "public static TextTagField<ValidDocumentIndexedNested, String> ADDRESS_CITY;"), //

        // test Metamodel Field initialization
        () -> assertThat(fileContents).contains(
            "ID = new TextTagField<ValidDocumentIndexedNested, String>(new SearchFieldAccessor(\"id\", \"$.id\", id),true);"),
        //
        () -> assertThat(fileContents).contains(
            "ADDRESS_STREET = new TextField<ValidDocumentIndexedNested, String>(new SearchFieldAccessor(\"address_street\", \"$.address.street\", address_street),true);"),
        //
        () -> assertThat(fileContents).contains(
            "ADDRESS_CITY = new TextTagField<ValidDocumentIndexedNested, String>(new SearchFieldAccessor(\"address_city\", \"$.address.city\", address_city),true);"),
        //
        () -> assertThat(fileContents).contains(
            "_KEY = new MetamodelField<ValidDocumentIndexedNested, String>(\"__key\", String.class, true);"));
  }

  @Test
  @Classpath("data.metamodel.ValidDocumentUnindexedWoPackage")
  void testValidDocumentUnindexedWithoutPackage(Results results) {
    assertThat(results.generated).hasSize(1);
    List<String> warnings = getWarningStrings(results);
    assertAll( //
        () -> assertThat(warnings).hasSize(1), //
        () -> assertThat(warnings).containsOnly("Class ValidDocumentUnindexedWoPackage has an unnamed package.") //
    );

    List<String> errors = getErrorStrings(results);
    assertAll( //
        () -> assertThat(errors).hasSize(1), //
        () -> assertThat(errors).containsOnly("warnings found and -Werror specified") //
    );
  }

  @Test
  @Classpath("data.metamodel.BadBean")
  void testValidDocumentInBadJavaBean(Results results) {
    assertThat(results.generated).hasSize(1);

    List<String> errors = getErrorStrings(results);
    assertAll( //
        () -> assertThat(errors).hasSize(3), //
        () -> assertThat(errors).contains(
            "Class valid.BadBean is not a proper JavaBean because id has no standard getter."), //
        () -> assertThat(errors).contains(
            "Class valid.BadBean is not a proper JavaBean because name has no standard getter."), //
        () -> assertThat(errors).contains(
            "Class valid.BadBean is not a proper JavaBean because age has no standard getter.") //
    );
  }

  @Test
  @Classpath("data.metamodel.IdOnly")
  void testValidIdOnlyDocument(Results results) throws IOException {
    List<String> warnings = getWarningStrings(results);
    assertThat(warnings).isEmpty();

    List<String> errors = getErrorStrings(results);
    assertThat(errors).isEmpty();

    assertThat(results.generated).hasSize(1);
    JavaFileObject metamodel = results.generated.get(0);
    assertThat(metamodel.getName()).isEqualTo("/SOURCE_OUTPUT/valid/IdOnly$.java");

    var fileContents = metamodel.getCharContent(true);

    var expected = """
        package valid;
         
         import com.redis.om.spring.metamodel.MetamodelField;
         import java.lang.String;
         
         public final class IdOnly$ {
           public static MetamodelField<IdOnly, String> _KEY;
         
           public static MetamodelField<IdOnly, IdOnly> _THIS;
         
           static {
             _KEY = new MetamodelField<IdOnly, String>("__key", String.class, true);
             _THIS = new MetamodelField<IdOnly, IdOnly>("__this", IdOnly.class, true);
           }
         }
         """;

    assertThat(fileContents).containsIgnoringWhitespaces(expected);
  }

  private List<String> getWarningStrings(Results results) {
    return results.find().warnings().list().stream().map(w -> w.getMessage(Locale.US)).collect(Collectors.toList());
  }

  private List<String> getErrorStrings(Results results) {
    return results.find().errors().list().stream().map(w -> w.getMessage(Locale.US)).collect(Collectors.toList());
  }
}
