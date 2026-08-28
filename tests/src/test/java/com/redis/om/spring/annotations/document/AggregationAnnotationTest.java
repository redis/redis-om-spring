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
    // "brand"/"count" are not asserted here: GROUPBY without a SORTBY has unspecified row
    // order, and with LIMIT 1 that means an arbitrary brand/count wins. "dt"/"parsed_dt" are
    // computed from the literal timestamp 1517417144, so they're deterministic regardless of
    // which group's row is returned - that's what this test actually exercises.
    var result = repository.parseTime();
    assertThat(result.getTotalResults()).isEqualTo(293);

    var row = result.getRow(0);
    assertThat(row.getString("dt")).isEqualTo("2018-01-31T16:45:44Z");
    assertThat(row.getString("parsed_dt")).isEqualTo("1517417144");
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
    // Sorted by title (see GameRepository#stringFormat), so this exercises the first 10 titles
    // in alphabetical order - without a SORTBY, GROUPBY row order is unspecified.
    // Note: the source dataset stores some titles with un-decoded HTML entities
    // (e.g. literal "&quot;"/"&amp;" text), which is reproduced verbatim below.
    String[][][] expectedData = { //
        { { "title", "&quot;Blue Thunder&quot; PS3 Custom Modded Controller Exclusive Design - COD Ready Zomb..." },
            { "titleBrand",
                "&quot;Blue Thunder&quot; PS3 Custom Modded Controller Exclusive Design - COD Ready Zomb...|(null)|Mark|119.95" } }, //
        { { "title",
            "&quot;Enigma Silver Gold&quot; Chameleon PS4 Custom Modded Controller Exclusive Design - COD Ready Zombie Auto Aim, Drop Shot, Fast Reload, &amp; Menu for Ghost !" },
            { "titleBrand",
                "&quot;Enigma Silver Gold&quot; Chameleon PS4 Custom Modded Controller Exclusive Design - COD Ready Zombie Auto Aim, Drop Shot, Fast Reload, &amp; Menu for Ghost !|(null)|Mark|149.95" } }, //
        { { "title",
            "&quot;Green Skulls 3Mod xbox360 &quot; (10 Modes Dual Rapid Fire + S Quick Scope+ Central Button's Illumination) for wireless controller for Xbox 360 from Smarts Gifts Co." },
            { "titleBrand",
                "&quot;Green Skulls 3Mod xbox360 &quot; (10 Modes Dual Rapid Fire + S Quick Scope+ Central Button's Illumination) for wireless controller for Xbox 360 from Smarts Gifts Co.|(null)|Mark|3.79" } }, //
        { { "title",
            "&quot;Halo  &quot; skin , Three additional modes  (10 Modes Dual Rapid Fire +   Fast Aim Fire mode + Central Button's Illumination)   Wireless Original Microsoft controller  Xbox 360 (modded) ,the  Best  for MW1.2.3 , COD , BATTLEFIELD , HALO , other Shooter  Games" },
            { "titleBrand",
                "&quot;Halo  &quot; skin , Three additional modes  (10 Modes Dual Rapid Fire +   Fast Aim Fire mode + Central Button's Illumination)   Wireless Original Microsoft controller  Xbox 360 (modded) ,the  Best  for MW1.2.3 , COD , BATTLEFIELD , HALO , other Shooter  Games|(null)|Mark|16.49" } }, //
        { { "title",
            "&quot;Red Skulls&quot; PS4 Custom Modded Controller Exclusive Design - COD Ready Zombie Auto Aim, Drop Shot, Fast Reload, &amp; Menu for Ghost !" },
            { "titleBrand",
                "&quot;Red Skulls&quot; PS4 Custom Modded Controller Exclusive Design - COD Ready Zombie Auto Aim, Drop Shot, Fast Reload, &amp; Menu for Ghost !|(null)|Mark|-inf" } }, //
        { { "title", "&quot;Red Splatter&quot;PS4 Custom Modded Controller Exclusive Design w/Chrome Dpad &amp; R..." },
            { "titleBrand",
                "&quot;Red Splatter&quot;PS4 Custom Modded Controller Exclusive Design w/Chrome Dpad &amp; R...|(null)|Mark|179.95" } }, //
        { { "title",
            "&quot;W&amp;B 2Mod xbox &quot; (10 Modes Dual Rapid Fire + Fast Quick Scope) wireless controller Xbox360 for MW1.2.3 , COD , BATTLEFIELD , HALO" },
            { "titleBrand",
                "&quot;W&amp;B 2Mod xbox &quot; (10 Modes Dual Rapid Fire + Fast Quick Scope) wireless controller Xbox360 for MW1.2.3 , COD , BATTLEFIELD , HALO|(null)|Mark|99.99" } }, //
        { { "title", ".AUDIO 400 DSP FOLDING USB PC HEADSET S3 - Model#: 76921-11" }, { "titleBrand",
            ".AUDIO 400 DSP FOLDING USB PC HEADSET S3 - Model#: 76921-11|(null)|Mark|65" } }, //
        { { "title", "10 Button Light PC Computer USB Game Pad Joy Controller" }, { "titleBrand",
            "10 Button Light PC Computer USB Game Pad Joy Controller|(null)|Mark|15.99" } }, //
        { { "title", "10 Pak Clear Cartridge Cases For DS Games" }, { "titleBrand",
            "10 Pak Clear Cartridge Cases For DS Games|(null)|Mark|5.99" } } };

    var result = repository.stringFormat();
    assertThat(result.getTotalResults()).isEqualTo(2219);

    IntStream.range(0, expectedData.length).forEach(i -> {
      var row = result.getRow(i);
      IntStream.range(0, expectedData[i].length).forEach(j -> {
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
