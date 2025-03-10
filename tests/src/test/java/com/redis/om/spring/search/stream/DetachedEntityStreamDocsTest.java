package com.redis.om.spring.search.stream;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.metamodel.SearchFieldAccessor;
import com.redis.om.spring.metamodel.indexed.NumericField;
import com.redis.om.spring.metamodel.indexed.TextField;
import com.redis.om.spring.util.ObjectUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.search.FTCreateParams;
import redis.clients.jedis.search.IndexDataType;
import redis.clients.jedis.search.schemafields.SchemaField;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class DetachedEntityStreamDocsTest extends AbstractBaseDocumentTest {

  @Autowired
  EntityStream entityStream;

  @Autowired
  JedisConnectionFactory jedisConnectionFactory;

  private UnifiedJedis jedis;

  @BeforeEach
  void beforeEach() {
    jedis = new JedisPooled(Objects.requireNonNull(jedisConnectionFactory.getPoolConfig()),
        jedisConnectionFactory.getHostName(), jedisConnectionFactory.getPort());

    try {
      jedis.ftDropIndex("idx:bicycle");
    } catch (JedisDataException e) {
      //Do Nothing
    }

    SchemaField[] schema = {
        redis.clients.jedis.search.schemafields.TextField.of("$.brand").as("brand"),
        redis.clients.jedis.search.schemafields.TextField.of("$.model").as("model"),
        redis.clients.jedis.search.schemafields.TextField.of("$.description").as("description"),
        redis.clients.jedis.search.schemafields.NumericField.of("$.price").as("price"),
        redis.clients.jedis.search.schemafields.TagField.of("$.condition").as("condition")
    };

    jedis.ftCreate("idx:bicycle",
        FTCreateParams.createParams()
            .on(IndexDataType.JSON)
            .addPrefix("bicycle:"),
        schema
    );

    Bicycle[] bicycles = {
        new Bicycle(
            "bicycle:0",
            "Velorim",
            "Jigger",
            new BigDecimal(270),
            "new",
            "Small and powerful, the Jigger is the best ride " +
                "for the smallest of tikes! This is the tiniest " +
                "kids’ pedal bike on the market available without" +
                " a coaster brake, the Jigger is the vehicle of " +
                "choice for the rare tenacious little rider " +
                "raring to go."
        ),
        new Bicycle(
            "bicycle:1",
            "Bicyk",
            "Hillcraft",
            new BigDecimal(1200),
            "used",
            "Kids want to ride with as little weight as possible." +
                " Especially on an incline! They may be at the age " +
                "when a 27.5 inch wheel bike is just too clumsy coming " +
                "off a 24 inch bike. The Hillcraft 26 is just the solution" +
                " they need!"
        ),
        new Bicycle(
            "bicycle:2",
            "Nord",
            "Chook air 5",
            new BigDecimal(815),
            "used",
            "The Chook Air 5  gives kids aged six years and older " +
                "a durable and uberlight mountain bike for their first" +
                " experience on tracks and easy cruising through forests" +
                " and fields. The lower  top tube makes it easy to mount" +
                " and dismount in any situation, giving your kids greater" +
                " safety on the trails."
        ),
        new Bicycle(
            "bicycle:3",
            "Eva",
            "Eva 291",
            new BigDecimal(3400),
            "used",
            "The sister company to Nord, Eva launched in 2005 as the" +
                " first and only women-dedicated bicycle brand. Designed" +
                " by women for women, allEva bikes are optimized for the" +
                " feminine physique using analytics from a body metrics" +
                " database. If you like 29ers, try the Eva 291. It's a " +
                "brand new bike for 2022.. This full-suspension, " +
                "cross-country ride has been designed for velocity. The" +
                " 291 has 100mm of front and rear travel, a superlight " +
                "aluminum frame and fast-rolling 29-inch wheels. Yippee!"
        ),
        new Bicycle(
            "bicycle:4",
            "Noka Bikes",
            "Kahuna",
            new BigDecimal(3200),
            "used",
            "Whether you want to try your hand at XC racing or are " +
                "looking for a lively trail bike that's just as inspiring" +
                " on the climbs as it is over rougher ground, the Wilder" +
                " is one heck of a bike built specifically for short women." +
                " Both the frames and components have been tweaked to " +
                "include a women’s saddle, different bars and unique " +
                "colourway."
        ),
        new Bicycle(
            "bicycle:5",
            "Breakout",
            "XBN 2.1 Alloy",
            new BigDecimal(810),
            "new",
            "The XBN 2.1 Alloy is our entry-level road bike – but that’s" +
                " not to say that it’s a basic machine. With an internal " +
                "weld aluminium frame, a full carbon fork, and the slick-shifting" +
                " Claris gears from Shimano’s, this is a bike which doesn’t" +
                " break the bank and delivers craved performance."
        ),
        new Bicycle(
            "bicycle:6",
            "ScramBikes",
            "WattBike",
            new BigDecimal(2300),
            "new",
            "The WattBike is the best e-bike for people who still feel young" +
                " at heart. It has a Bafang 1000W mid-drive system and a 48V" +
                " 17.5AH Samsung Lithium-Ion battery, allowing you to ride for" +
                " more than 60 miles on one charge. It’s great for tackling hilly" +
                " terrain or if you just fancy a more leisurely ride. With three" +
                " working modes, you can choose between E-bike, assisted bicycle," +
                " and normal bike modes."
        ),
        new Bicycle(
            "bicycle:7",
            "Peaknetic",
            "Secto",
            new BigDecimal(430),
            "new",
            "If you struggle with stiff fingers or a kinked neck or back after" +
                " a few minutes on the road, this lightweight, aluminum bike" +
                " alleviates those issues and allows you to enjoy the ride. From" +
                " the ergonomic grips to the lumbar-supporting seat position, the" +
                " Roll Low-Entry offers incredible comfort. The rear-inclined seat" +
                " tube facilitates stability by allowing you to put a foot on the" +
                " ground to balance at a stop, and the low step-over frame makes it" +
                " accessible for all ability and mobility levels. The saddle is" +
                " very soft, with a wide back to support your hip joints and a" +
                " cutout in the center to redistribute that pressure. Rim brakes" +
                " deliver satisfactory braking control, and the wide tires provide" +
                " a smooth, stable ride on paved roads and gravel. Rack and fender" +
                " mounts facilitate setting up the Roll Low-Entry as your preferred" +
                " commuter, and the BMX-like handlebar offers space for mounting a" +
                " flashlight, bell, or phone holder."
        ),
        new Bicycle(
            "bicycle:8",
            "nHill",
            "Summit",
            new BigDecimal(1200),
            "new",
            "This budget mountain bike from nHill performs well both on bike" +
                " paths and on the trail. The fork with 100mm of travel absorbs" +
                " rough terrain. Fat Kenda Booster tires give you grip in corners" +
                " and on wet trails. The Shimano Tourney drivetrain offered enough" +
                " gears for finding a comfortable pace to ride uphill, and the" +
                " Tektro hydraulic disc brakes break smoothly. Whether you want an" +
                " affordable bike that you can take to work, but also take trail in" +
                " mountains on the weekends or you’re just after a stable," +
                " comfortable ride for the bike path, the Summit gives a good value" +
                " for money."
        ),
        new Bicycle(
            "bicycle:9",
            "ThrillCycle",
            "BikeShind",
            new BigDecimal(815),
            "refurbished",
            "An artsy,  retro-inspired bicycle that’s as functional as it is" +
                " pretty: The ThrillCycle steel frame offers a smooth ride. A" +
                " 9-speed drivetrain has enough gears for coasting in the city, but" +
                " we wouldn’t suggest taking it to the mountains. Fenders protect" +
                " you from mud, and a rear basket lets you transport groceries," +
                " flowers and books. The ThrillCycle comes with a limited lifetime" +
                " warranty, so this little guy will last you long past graduation."
        ),
    };

    for (int i = 0; i < bicycles.length; i++) {
      jedis.jsonSetWithEscape(bicycles[i].getId(), bicycles[i]);
    }
  }

  @Test
  void testEntityStreamFromExistingIndexFindAll() {
      SearchStream<Bicycle> stream = entityStream.of(Bicycle.class, "idx:bicycle", "id");
      List<Bicycle> bicycles = stream.collect(Collectors.toList());
      assertThat(bicycles).hasSize(10);
  }

  @Test
  void testEntityStreamFilterWithManuallyCreatedMetamodelField() throws NoSuchFieldException {
    Field field = ObjectUtils.getDeclaredFieldTransitively(Bicycle.class, "model");
    SearchFieldAccessor sfa = new SearchFieldAccessor("model", "$.model", field);
    TextField<Bicycle, String> MODEL = new TextField<>(sfa, true);

    SearchStream<Bicycle> stream = entityStream.of(Bicycle.class, "idx:bicycle", "id");
    List<Bicycle> bicycles = stream.filter(MODEL.eq("Jigger")).collect(Collectors.toList());

    assertThat(bicycles).hasSize(1);
    assertThat(bicycles.get(0).getModel()).isEqualTo("Jigger");
  }

  @Test
  void testEntityStreamFilterWithFieldApi() {
    TextField<Bicycle, String> MODEL = new TextField<>(Bicycle.class, "model");
    SearchStream<Bicycle> stream = entityStream.of(Bicycle.class, "idx:bicycle", "id");
    List<Bicycle> bicycles = stream.filter(MODEL.eq("Jigger")).collect(Collectors.toList());

    assertThat(bicycles).hasSize(1);
    assertThat(bicycles.get(0).getModel()).isEqualTo("Jigger");
  }

  @Test
  void testEntityStreamFilterWithFieldApiWithNumericRange() {
    TextField<Bicycle, String> BRAND = new TextField<>(Bicycle.class, "brand");
    NumericField<Bicycle, BigDecimal> PRICE = new NumericField<>(Bicycle.class, "price");
    SearchStream<Bicycle> stream = entityStream.of(Bicycle.class, "idx:bicycle", "id");
    List<Bicycle> bicycles = stream.filter(BRAND.eq("ThrillCycle")) //
        .filter(PRICE.between(BigDecimal.valueOf(815), BigDecimal.valueOf(815))) //
        .collect(Collectors.toList());

    assertThat(bicycles).hasSize(1);
    assertThat(bicycles.get(0).getModel()).isEqualTo("BikeShind");
    assertThat(bicycles.get(0).getBrand()).isEqualTo("ThrillCycle");
    assertThat(bicycles.get(0).getPrice()).isEqualTo(BigDecimal.valueOf(815));
  }

  @Test
  void testEntityStreamFilterWithFieldApiWithMultipleFields() {
    NumericField<Bicycle, BigDecimal> PRICE = new NumericField<>(Bicycle.class, "price");
    SearchStream<Bicycle> stream = entityStream.of(Bicycle.class, "idx:bicycle", "id");
    List<Bicycle> bicycles = stream.filter(PRICE.between(BigDecimal.valueOf(815), BigDecimal.valueOf(815))) //
        .collect(Collectors.toList());

    assertThat(bicycles).hasSize(2);
    assertThat(bicycles.get(0).getModel()).isEqualTo("Chook air 5");
    assertThat(bicycles.get(1).getModel()).isEqualTo("BikeShind");
    assertThat(bicycles.get(0).getPrice()).isEqualTo(BigDecimal.valueOf(815));
    assertThat(bicycles.get(1).getPrice()).isEqualTo(BigDecimal.valueOf(815));
  }

  @Data
  @RequiredArgsConstructor(staticName = "of")
  @AllArgsConstructor(access = AccessLevel.PROTECTED)
  private static class Bicycle {
    private String id;
    private String brand;
    private String model;
    private BigDecimal price;
    private String condition;
    private String description;
  }
}
