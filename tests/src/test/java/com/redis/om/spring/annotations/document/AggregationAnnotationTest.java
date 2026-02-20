package com.redis.om.spring.annotations.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.io.IOException;
import java.util.stream.IntStream;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.repository.GameRepository;

@SuppressWarnings(
  "SpellCheckingInspection"
)
class AggregationAnnotationTest extends AbstractBaseDocumentTest {
  @Autowired
  GameRepository repository;

  @BeforeEach
  void beforeEach() throws IOException {
    // Load Sample Docs
    if (repository.count() == 0) {
      repository.bulkLoad("src/test/resources/data/games.json");
    }
  }

  @Test
  void testCountAggregation() {
    String[][] expectedData = { //
        { "", "1498" }, { "Mad Catz", "43" }, { "Generic", "40" }, { "SteelSeries", "37" }, { "Logitech", "35" } //
    };

    var result = repository.countByBrand(PageRequest.of(0, 5));
    assertThat(result).hasSize(5);
    assertThat(result.getTotalElements()).isEqualTo(293);
    var resultAsList = result.toList();

    IntStream.range(0, expectedData.length - 1).forEach(i -> {
      var row = resultAsList.get(i);
      assertThat(row).isNotNull()//
          .isNotEmpty() //
          .contains(entry("brand", expectedData[i][0])) //
          .contains(entry("count", expectedData[i][1])) //
          .hasSize(2);
    });
  }

  @Test
  void testMinPrice() {
    String[][] expectedData = { //
        { "Genius", "88.54" }, { "Logitech", "78.98" }, { "Monster", "69.95" }, { "Goliton", "15.69" }, { "Lenmar",
            "15.41" }, { "Oceantree(TM)", "12.29" }, { "Oceantree", "11.39" }, { "oooo", "10.11" }, { "Case Logic",
                "9.99" }, { "Neewer", "9.71" } //
    };
    var result = repository.minPricesContainingSony();
    assertThat(result.getTotalResults()).isEqualTo(27);

    IntStream.range(0, expectedData.length - 1).forEach(i -> {
      var row = result.getRow(i);
      assertThat(row.getString("brand")).isEqualTo(expectedData[i][0]);
      assertThat(row.getString("minPrice")).isEqualTo(expectedData[i][1]);
    });
  }

  @Test
  void testMaxPrice() {
    String[][] expectedData = { //
        { "Sony", "695.8" }, { null, "303.59" }, { "Genius", "88.54" }, { "Logitech", "78.98" }, { "Monster", "69.95" },
        { "Playstation", "33.6" }, { "Neewer", "15.95" }, { "Goliton", "15.69" }, { "Lenmar", "15.41" }, { "Oceantree",
            "12.45" } //
    };
    var result = repository.maxPricesContainingSony();
    assertThat(result.getTotalResults()).isEqualTo(27);

    IntStream.range(0, expectedData.length - 1).forEach(i -> {
      var row = result.getRow(i);
      if (i != 1)
        assertThat(row.getString("brand")).isEqualTo(expectedData[i][0]);
      assertThat(row.getString("maxPrice")).isEqualTo(expectedData[i][1]);
    });
  }

  @Test
  void testCountDistinctByBrandHarcodedLimit() {
    String[][] expectedData = { //
        { null, "1466" }, { "Generic", "39" }, { "SteelSeries", "37" }, { "Mad Catz", "36" }, { "Logitech", "34" } //
    };

    var result = repository.top5countDistinctByBrand();
    assertThat(result.getTotalResults()).isEqualTo(293);

    IntStream.range(0, expectedData.length - 1).forEach(i -> {
      var row = result.getRow(i);
      if (i != 0)
        assertThat(row.getString("brand")).isEqualTo(expectedData[i][0]);
      assertThat(row.getString("count_distinct(title)")).isEqualTo(expectedData[i][1]);
    });
  }

  @Test
  void testQuantiles() {
    var result = repository.priceQuantiles();
    assertThat(result.getTotalResults()).isGreaterThan(0);

    var row = result.getRow(0);
    double q50 = Double.parseDouble(row.getString("q50"));
    double q90 = Double.parseDouble(row.getString("q90"));
    double q95 = Double.parseDouble(row.getString("q95"));
    double avg = Double.parseDouble(row.getString("__generated_aliasavgprice"));
    long rowcount = Long.parseLong(row.getString("rowcount"));

    assertThat(q50).isGreaterThan(0);
    assertThat(q90).isGreaterThanOrEqualTo(q50);
    assertThat(q95).isGreaterThanOrEqualTo(q90);
    assertThat(avg).isGreaterThan(0);
    assertThat(rowcount).isGreaterThan(1000);
  }

  @Test
  void testPriceStdDev() {
    var result = repository.priceStdDev();
    assertThat(result.getTotalResults()).isGreaterThan(0);

    // Verify structural correctness: results sorted by rowcount DESC with valid statistics
    long previousRowCount = Long.MAX_VALUE;
    for (int i = 0; i < Math.min(result.getResults().size(), 10); i++) {
      var row = result.getRow(i);
      double stddev = Double.parseDouble(row.getString("stddev(price)"));
      double avgPrice = Double.parseDouble(row.getString("avgPrice"));
      double q50Price = Double.parseDouble(row.getString("q50Price"));
      long rowcount = Long.parseLong(row.getString("rowcount"));

      assertThat(stddev).isGreaterThanOrEqualTo(0);
      assertThat(avgPrice).isGreaterThanOrEqualTo(0);
      assertThat(q50Price).isGreaterThanOrEqualTo(0);
      assertThat(rowcount).isGreaterThan(0);
      assertThat(rowcount).isLessThanOrEqualTo(previousRowCount);
      previousRowCount = rowcount;
    }
  }

  @Test
  void testParseTime() {
    String[][] expectedData = { //
        { "brand", "" }, { "count", "20" }, { "dt", "2018-01-31T16:45:44Z" }, { "parsed_dt", "1517417144" } //
    };

    var result = repository.parseTime();
    assertThat(result.getTotalResults()).isEqualTo(293);

    var row = result.getRow(0);
    IntStream.range(0, expectedData.length - 1).forEach(i -> assertThat(row.getString(expectedData[i][0])).isEqualTo(
        expectedData[i][1]));
  }

  @Test
  void testRandomSample() {
    var result = repository.randomSample();
    assertThat(result.getTotalResults()).isEqualTo(293);

    result.getResults().forEach(row -> {
      assertThat(row).isNotNull()//
          .isNotEmpty() //
          .containsKey("sample") //
          .hasSize(3);

      assertThat(row.get("sample")).asInstanceOf(InstanceOfAssertFactories.LIST).hasSizeBetween(1, 10);
    });
  }

  @Test
  void testTimeFunctions() {
    String[][] expectedData = { //
        { "dt", "1517417144" }, { "timefmt", "2018-01-31T16:45:44Z" }, { "day", "1517356800" }, { "hour",
            "1517414400" }, { "minute", "1517417100" }, { "month", "1514764800" }, { "dayofweek", "3" }, { "dayofmonth",
                "31" }, //
        { "dayofyear", "30" }, { "year", "2018" } //
    };

    var result = repository.timeFunctions();
    assertThat(result.getTotalResults()).isEqualTo(1);

    var row = result.getRow(0);
    IntStream.range(0, expectedData.length - 1).forEach(i -> assertThat(row.getString(expectedData[i][0])).isEqualTo(
        expectedData[i][1]));
  }

  @Test
  void testStringFormat() {
    String[][][] expectedData = { //
        { { "title", "Standard Single Gang 4 Port Faceplate, ABS 94V-0, Black, 1/pkg" }, { "titleBrand",
            "Standard Single Gang 4 Port Faceplate, ABS 94V-0, Black, 1/pkg|Hellermann Tyton|Mark|4.95" } },
        //
        { { "title",
            "250G HDD Hard Disk Drive For Microsoft Xbox 360 E Slim with USB 2.0 AGPtek All-in-One Card Reader" }, {
                "titleBrand",
                "250G HDD Hard Disk Drive For Microsoft Xbox 360 E Slim with USB 2.0 AGPtek All-in-One Card Reader|(null)|Mark|51.79" } },
        //
        { { "title",
            "Portable Emergency AA Battery Charger Extender suitable for the Sony PSP - with Gomadic Brand TipExchange Technology" },
            { "titleBrand",
                "Portable Emergency AA Battery Charger Extender suitable for the Sony PSP - with Gomadic Brand TipExchange Technology|(null)|Mark|19.66" } },
        //
        { { "title", "Mad Catz S.T.R.I.K.E.5 Gaming Keyboard for PC" }, { "titleBrand",
            "Mad Catz S.T.R.I.K.E.5 Gaming Keyboard for PC|Mad Catz|Mark|193.26" } }, //
        { { "title", "iConcepts THE SHOCK MASTER For Use With PC" }, { "titleBrand",
            "iConcepts THE SHOCK MASTER For Use With PC|(null)|Mark|9.99" } }, //
        { { "title", "Saitek CES432110002/06/1 Pro Flight Cessna Trim Wheel" }, { "titleBrand",
            "Saitek CES432110002/06/1 Pro Flight Cessna Trim Wheel|Mad Catz|Mark|47.02" } }, //
        { { "title",
            "Noppoo Choc Mini 84 USB NKRO Mechanical Gaming Keyboard Cherry MX Switches (BLUE switch + Black body + POM key cap)" },
            { "titleBrand",
                "Noppoo Choc Mini 84 USB NKRO Mechanical Gaming Keyboard Cherry MX Switches (BLUE switch + Black body + POM key cap)|(null)|Mark|34.98" } },
        //
        { { "title",
            "iiMash&reg; Ipega Universal Wireless Bluetooth 3.0 Game Controller Gamepad Joypad for Apple Ios Iphone 5 4 4s Ipad 4 3 2 New Mini Ipod Android Phone HTC One X Samsung Galaxy S3 2 Note 2 N7100 N8000 Tablet Google Nexus 7&quot; 10&quot; Pc" },
            { "titleBrand",
                "iiMash&reg; Ipega Universal Wireless Bluetooth 3.0 Game Controller Gamepad Joypad for Apple Ios Iphone 5 4 4s Ipad 4 3 2 New Mini Ipod Android Phone HTC One X Samsung Galaxy S3 2 Note 2 N7100 N8000 Tablet Google Nexus 7&quot; 10&quot; Pc|iiMash&reg;|Mark|35.98" } },
        //
        { { "title", "16 in 1 Plastic Game Card Case Holder Box For Nintendo 3DS DSi DSi XL DS LITE" }, { "titleBrand",
            "16 in 1 Plastic Game Card Case Holder Box For Nintendo 3DS DSi DSi XL DS LITE|Meco|Mark|3.99" } }, //
        { { "title",
            "Apocalypse Red Design Protective Decal Skin Sticker (High Gloss Coating) for Nintendo DSi XL Game Device" },
            { "titleBrand",
                "Apocalypse Red Design Protective Decal Skin Sticker (High Gloss Coating) for Nintendo DSi XL Game Device|(null)|Mark|14.99" } }
        //
    };

    var result = repository.stringFormat();
    assertThat(result.getTotalResults()).isEqualTo(2219);

    IntStream.range(0, expectedData.length - 1).forEach(i -> {
      var row = result.getRow(i);
      IntStream.range(0, expectedData[i].length - 1).forEach(j -> {
        if (expectedData[i][j][1] != null) {
          assertThat(row.getString(expectedData[i][j][0])).isEqualTo(expectedData[i][j][1]);
        }
      });
    });
  }

  @Test
  void testSumPrice() {
    String[][][] expectedData = { //
        { { "brand", null }, { "count", "1498" }, { "sum(price)", "44506.47" } }, //
        { { "brand", "Mad Catz" }, { "count", "43" }, { "sum(price)", "3973.48" } }, //
        { { "brand", "Razer" }, { "count", "26" }, { "sum(price)", "2558.58" } }, //
        { { "brand", "Logitech" }, { "count", "35" }, { "sum(price)", "2329.21" } }, //
        { { "brand", "SteelSeries" }, { "count", "37" }, { "sum(price)", "1851.12" } }, //
    };

    var result = repository.sumPrice();
    assertThat(result.getTotalResults()).isEqualTo(293);

    IntStream.range(0, expectedData.length - 1).forEach(i -> {
      var row = result.getRow(i);
      IntStream.range(0, expectedData[i].length - 1).forEach(j -> {
        if (expectedData[i][j][1] != null) {
          assertThat(row.getString(expectedData[i][j][0])).isEqualTo(expectedData[i][j][1]);
        }
      });
    });
  }

  @Test
  void testFilters() {
    var result = repository.filters();
    assertThat(result.getTotalResults()).isGreaterThan(0);

    IntStream.range(0, result.getResults().size() - 1).forEach(i -> {
      var row = result.getRow(i);
      assertThat(row.getLong("count")).isGreaterThan(2).isLessThan(5);
    });
  }

  @Test
  void testToList() {
    var result = repository.toList();
    assertThat(result.getTotalResults()).isEqualTo(293);

    IntStream.range(0, result.getResults().size() - 1).forEach(i -> {
      var row = result.getRow(i);
      var prices = result.getResults().get(i).get("prices");
      assertThat(prices).asInstanceOf(InstanceOfAssertFactories.LIST).hasSize(Math.toIntExact(row.getLong("count")));
    });
  }

  @Test
  void testSortByMany() {
    String[][][] expectedData = { //
        { { "brand", "Myiico" }, { "price", "0" } }, //
        { { "brand", "Crystal Dynamics" }, { "price" } }, //
        { { "brand", "yooZoo" }, { "price", "0" } }, //
        { { "brand", "Century Accessory" }, { "price", "1" } }, //
        { { "brand", "sumoto" }, { "price", "1" } }, //
        { { "brand", "eGames" }, { "price", "1" } }, //
        { { "brand", "Veecome" }, { "price", "1" } }, //
        { { "brand", "Wimex" }, { "price", "1" } }, //
        { { "brand", "gospel-online" }, { "price", "1" } }, //
        { { "brand", "ETHAHE" }, { "price", "1" } }, //
    };

    var result = repository.sortByMany();
    assertThat(result.getTotalResults()).isEqualTo(293);

    IntStream.range(0, expectedData.length - 1).forEach(i -> {
      var row = result.getRow(i);
      IntStream.range(0, expectedData[i].length - 1).forEach(j -> assertThat(row.getString(expectedData[i][j][0]))
          .isEqualTo(expectedData[i][j][1]));
    });
  }

  @Test
  void testLoadWithSort() {
    String[][][] expectedData = { //
        { { "title", "Logitech MOMO Racing - Wheel and pedals set - 6 button(s) - PC, MAC - black" }, { "price",
            "759.12" } }, //
        { { "title", "Sony PSP Slim &amp; Lite 2000 Console" }, { "price", "695.8" } }, //
    };
    var result = repository.loadWithSort();
    assertThat(result.getTotalResults()).isEqualTo(2265);

    IntStream.range(0, expectedData.length - 1).forEach(i -> {
      var row = result.getRow(i);
      IntStream.range(0, expectedData[i].length - 1).forEach(j -> assertThat(row.getString(expectedData[i][j][0]))
          .isEqualTo(expectedData[i][j][1]));
    });
  }

  @Test
  void testLoadWithDocId() {
    String[][][] expectedData = { //
        { { "__key", "games:B00006JJIC" }, { "price", "759.12" } }, //
        { { "__key", "games:B000F6W1AG" }, { "price", "695.8" } }, //
        { { "__key", "games:B00002JXBD" }, { "price", "599.99" } }, //
        { { "__key", "games:B00006IZIL" }, { "price", "759.12" } }, //
    };
    var result = repository.loadWithDocId();
    assertThat(result.getTotalResults()).isEqualTo(2265);

    IntStream.range(0, expectedData.length - 1).forEach(i -> {
      var row = result.getRow(i);
      IntStream.range(0, expectedData[i].length - 1).forEach(j -> assertThat(row.getString(expectedData[i][j][0]))
          .isEqualTo(expectedData[i][j][1]));
    });
  }

  @Test
  void testFirstValue() {
    var result = repository.firstValue();

    // Verify structural correctness: each row has brand, top_item, top_price, bottom_item, bottom_price
    // and top_price >= bottom_price (most expensive >= least expensive)
    for (int i = 0; i < Math.min(result.getResults().size(), 4); i++) {
      var row = result.getRow(i);
      assertThat(row.getString("brand")).isNotNull();
      assertThat(row.getString("top_item")).isNotNull();
      assertThat(row.getString("bottom_item")).isNotNull();

      double topPrice = Double.parseDouble(row.getString("top_price"));
      double bottomPrice = Double.parseDouble(row.getString("bottom_price"));
      assertThat(topPrice).isGreaterThanOrEqualTo(bottomPrice);
    }
  }

  @Test
  void testAggregationParams() {
    String[][] expectedData = { //
        { "Genius", "88.54" }, { "Logitech", "78.98" }, { "Monster", "69.95" }, { "Goliton", "15.69" }, { "Lenmar",
            "15.41" }, { "Oceantree(TM)", "12.29" }, { "Oceantree", "11.39" }, { "oooo", "10.11" }, { "Case Logic",
                "9.99" }, { "Neewer", "9.71" } //
    };
    var result = repository.minPricesByBrand("sony");
    assertThat(result.getTotalResults()).isEqualTo(27);

    IntStream.range(0, expectedData.length - 1).forEach(i -> {
      var row = result.getRow(i);
      assertThat(row.getString("brand").toLowerCase()).isEqualTo(expectedData[i][0].toLowerCase());
      assertThat(row.getString("minPrice")).isEqualTo(expectedData[i][1]);
    });
  }
}
