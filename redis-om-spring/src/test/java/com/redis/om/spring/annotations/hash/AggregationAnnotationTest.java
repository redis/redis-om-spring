package com.redis.om.spring.annotations.hash;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.annotations.hash.fixtures.Game;
import com.redis.om.spring.annotations.hash.fixtures.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class AggregationAnnotationTest extends AbstractBaseEnhancedRedisTest {
  @Autowired GameRepository repository;
  @Autowired Gson gson;

  @BeforeEach void beforeEach() throws IOException {
    // Load Sample Docs
    if (repository.count() == 0) {
      Reader reader = null;
      try {
        reader = Files.newBufferedReader(Paths.get("src/test/resources/data/games.json"));
        List<Game> entities = gson.fromJson(reader, new TypeToken<List<Game>>() {}.getType());
        repository.saveAll(entities);
      } finally {
        reader.close();
      }
    }
  }

  @Test void testCountAggregation() {
    // TODO: report - values from the aggregation get lower cased
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
          .contains(entry("brand", expectedData[i][0].toLowerCase())) //
          .contains(entry("count", expectedData[i][1])) //
          .hasSize(2);
    });
  }

  @Test void testMinPrice() {
    String[][] expectedData = { //
        { "Genius", "88.54" }, { "Logitech", "78.98" }, { "Monster", "69.95" }, { "Goliton", "15.69" },
        { "Lenmar", "15.41" }, { "Oceantree(TM)", "12.29" }, { "Oceantree", "11.39" }, { "oooo", "10.11" },
        { "Case Logic", "9.99" }, { "Neewer", "9.71" } //
    };
    var result = repository.minPricesContainingSony();
    assertThat(result.totalResults).isEqualTo(27);

    IntStream.range(0, expectedData.length - 1).forEach(i -> {
      var row = result.getRow(i);
      assertThat(row.getString("brand")).isEqualTo(expectedData[i][0].toLowerCase());
      assertThat(row.getString("minPrice")).isEqualTo(expectedData[i][1]);
    });
  }

  @Test void testMaxPrice() {
    String[][] expectedData = { //
        { "Sony", "695.8" }, { null, "303.59" }, { "Genius", "88.54" }, { "Logitech", "78.98" }, { "Monster", "69.95" },
        { "Playstation", "33.6" }, { "Neewer", "15.95" }, { "Goliton", "15.69" }, { "Lenmar", "15.41" },
        { "Oceantree", "12.45" } //
    };
    var result = repository.maxPricesContainingSony();
    assertThat(result.totalResults).isEqualTo(27);

    IntStream.range(0, expectedData.length - 1).forEach(i -> {
      var row = result.getRow(i);
      if (i != 1)
        assertThat(row.getString("brand")).isEqualTo(expectedData[i][0].toLowerCase());
      assertThat(row.getString("maxPrice")).isEqualTo(expectedData[i][1]);
    });
  }

  @Test void testCountDistinctByBrandHarcodedLimit() {
    // value for Mad Catz doesn't match the JSON version
    String[][] expectedData = { //
        { null, "1466" }, { "Generic", "39" }, { "SteelSeries", "37" }, { "Mad Catz", "35" }, { "Logitech", "34" } //
    };

    var result = repository.top5countDistinctByBrand();
    assertThat(result.totalResults).isEqualTo(293);

    IntStream.range(0, expectedData.length - 1).forEach(i -> {
      var row = result.getRow(i);
      if (i != 0)
        assertThat(row.getString("brand")).isEqualTo(expectedData[i][0].toLowerCase());
      assertThat(row.getString("count_distinct(title)")).isEqualTo(expectedData[i][1]);
    });
  }

  @Test void testQuantiles() {
    String[][] expectedData = { //
        { "q50","19.22" }, { "q90","95.91" }, { "q95", "144.96" }, { "__generated_aliasavgprice","29.7105941255" }, { "rowcount","1498" } //
    };

    var result = repository.priceQuantiles();
    assertThat(result.totalResults).isEqualTo(293);

    var row = result.getRow(0);
    IntStream.range(0, expectedData.length - 1).forEach(i -> {
      assertThat(row.getString(expectedData[i][0])).isEqualTo(expectedData[i][1]);
    });
  }

  @Test void testPriceStdDev() {
    String[][][] expectedData = { //
        { { "brand", null }, { "stddev(price)", "58.0682859441" }, { "avgPrice", "29.7105941255" },
            { "q50Price", "19.22" }, { "rowcount", "1498" } }, //
        { { "brand", "Mad Catz" }, { "stddev(price)", "63.3626941047" }, { "avgPrice", "92.4065116279" },
            { "q50Price", "84.99" }, { "rowcount", "43" } }, //
        { { "brand", "Generic" }, { "stddev(price)", "13.0528444292" }, { "avgPrice", "12.439" },
            { "q50Price", "6.69" }, { "rowcount", "40" } }, //
        { { "brand", "SteelSeries" }, { "stddev(price)", "44.5684434629" }, { "avgPrice", "50.0302702703" },
            { "q50Price", "39.69" }, { "rowcount", "37" } }, //
        { { "brand", "Logitech" }, { "stddev(price)", "48.016387201" }, { "avgPrice", "66.5488571429" },
            { "q50Price", "55" }, { "rowcount", "35" } }, //
        { { "brand", "Razer" }, { "stddev(price)", "49.0284634692" }, { "avgPrice", "98.4069230769" },
            { "q50Price", "80.49" }, { "rowcount", "26" } }, //
        { { "brand", "" }, { "stddev(price)", "11.6611915524" }, { "avgPrice", "13.711" }, { "q50Price", "10" },
            { "rowcount", "20" } }, //
        { { "brand", "ROCCAT" }, { "stddev(price)", "71.1336876222" }, { "avgPrice", "86.231" },
            { "q50Price", "58.72" }, { "rowcount", "20" } }, //
        { { "brand", "Sony" }, { "stddev(price)", "195.848045202" }, { "avgPrice", "109.536428571" },
            { "q50Price", "44.95" }, { "rowcount", "14" } }, //
        { { "brand", "Nintendo" }, { "stddev(price)", "71.1987671314" }, { "avgPrice", "53.2792307692" },
            { "q50Price", "17.99" }, { "rowcount", "13" } } //
    };

    var result = repository.priceStdDev();
    assertThat(result.totalResults).isEqualTo(293);

    IntStream.range(0, expectedData.length - 1).forEach(i -> {
      var row = result.getRow(i);
      IntStream.range(0, expectedData[i].length - 1).forEach(j -> {
        if (expectedData[i][j][1] != null) {
          assertThat(row.getString(expectedData[i][j][0])).isEqualTo(expectedData[i][j][1].toLowerCase());
        }
      });
    });
  }


  @Test void testParseTime() {
    String[][] expectedData = { //
        { "brand", "" }, { "count", "20" }, { "dt", "2018-01-31T16:45:44Z" }, { "parsed_dt", "1517417144" } //
    };

    var result = repository.parseTime();
    assertThat(result.totalResults).isEqualTo(293);

    var row = result.getRow(0);
    IntStream.range(0, expectedData.length - 1).forEach(i -> {
      assertThat(row.getString(expectedData[i][0])).isEqualTo(expectedData[i][1]);
    });
  }

  @Test void testRandomSample() {
    var result = repository.randomSample();
    assertThat(result.totalResults).isEqualTo(293);

    result.getResults().forEach(row -> {
      assertThat(row).isNotNull()//
          .isNotEmpty() //
          .containsKey("sample") //
          .hasSize(3);

      assertThat(row.get("sample")).asList().hasSize(10);
    });
  }

  @Test void testTimeFunctions() {
    String[][] expectedData = { //
        { "dt", "1517417144" }, { "timefmt", "2018-01-31T16:45:44Z" }, { "day", "1517356800" },
        { "hour", "1517414400" }, { "minute", "1517417100" }, { "month", "1514764800" }, { "dayofweek", "3" },
        { "dayofmonth", "31" }, //
        { "dayofyear", "30" }, { "year", "2018" } //
    };

    var result = repository.timeFunctions();
    assertThat(result.totalResults).isEqualTo(1);

    var row = result.getRow(0);
    IntStream.range(0, expectedData.length - 1).forEach(i -> {
      assertThat(row.getString(expectedData[i][0])).isEqualTo(expectedData[i][1]);
    });
  }

  @Test void testStringFormat() {
    String[][][] expectedData = { //
        { { "title", "mad catz mov088150/04/1 ps3 s-video cable" },
            { "titleBrand", "mad catz mov088150/04/1 ps3 s-video cable|(null)|Mark|0" } }, //
        { { "title", "franklin sdm-500224hsmp 2002 stedman's medical dictionary springboard module" }, { "titleBrand",
            "franklin sdm-500224hsmp 2002 stedman's medical dictionary springboard module|(null)|Mark|94.99" } }, //
        { { "title", "razer deathstalker ultimate gaming keyboard" },
            { "titleBrand", "razer deathstalker ultimate gaming keyboard|razer|Mark|255.35" } }, //
        { { "title", "igadgitz blue eva hard case cover for nintendo 2ds" },
            { "titleBrand", "igadgitz blue eva hard case cover for nintendo 2ds|(null)|Mark|6.99" } }, //
        { { "title",
            "buddies model wl2021 2.4 ghz wireless gamepad controller for pc/ps1/ps2/ps3 with rechargable lithium battery with one year warranty" },
            { "titleBrand",
                "buddies model wl2021 2.4 ghz wireless gamepad controller for pc/ps1/ps2/ps3 with rechargable lithium battery with one year warranty|(null)|Mark|19.95" } },
        //
        { { "title", "saitek x52 pro flight system controller" },
            { "titleBrand", "saitek x52 pro flight system controller|(null)|Mark|144.96" } }, //
        { { "title", "ideazon reaper gaming mouse" }, { "titleBrand", "ideazon reaper gaming mouse|(null)|Mark|0" } },
        //
        { { "title", "innovations 7-38012-48713-6 nes game pad" },
            { "titleBrand", "innovations 7-38012-48713-6 nes game pad|micro innovations|Mark|5.23" } }, //
        { { "title", "neuros mpeg-4 recorder 2 plus digital video recorder" },
            { "titleBrand", "neuros mpeg-4 recorder 2 plus digital video recorder|(null)|Mark|79.64" } }, //
        { { "title", "logitech cordless rumblepad 2 with vibration feedback (black)" },
            { "titleBrand", "logitech cordless rumblepad 2 with vibration feedback (black)|(null)|Mark|31.79" } }
    };

    var result = repository.stringFormat();
    assertThat(result.totalResults).isEqualTo(2218);

    IntStream.range(0, expectedData.length - 1).forEach(i -> {
      var row = result.getRow(i);
      IntStream.range(0, expectedData[i].length - 1).forEach(j -> {
        if (expectedData[i][j][1] != null) {
          assertThat(row.getString(expectedData[i][j][0])).isEqualTo(expectedData[i][j][1]);
        }
      });
    });
  }

  @Test void testSumPrice() {
    String[][][] expectedData = { //
        { { "brand", null }, { "count", "1498" }, { "sum(price)", "44506.47" } }, //
        { { "brand", "Mad Catz" }, { "count", "43" }, { "sum(price)", "3973.48" } }, //
        { { "brand", "Razer" }, { "count", "26" }, { "sum(price)", "2558.58" } }, //
        { { "brand", "Logitech" }, { "count", "35" }, { "sum(price)", "2329.21" } }, //
        { { "brand", "SteelSeries" }, { "count", "37" }, { "sum(price)", "1851.12" } }, //
    };

    var result = repository.sumPrice();
    assertThat(result.totalResults).isEqualTo(293);

    IntStream.range(0, expectedData.length - 1).forEach(i -> {
      var row = result.getRow(i);
      IntStream.range(0, expectedData[i].length - 1).forEach(j -> {
        if (expectedData[i][j][1] != null) {
          assertThat(row.getString(expectedData[i][j][0])).isEqualTo(expectedData[i][j][1].toLowerCase());
        }
      });
    });
  }

  @Test void testFilters() {
    var result = repository.filters();
    assertThat(result.totalResults).isEqualTo(293);

    IntStream.range(0, result.getResults().size() - 1).forEach(i -> {
      var row = result.getRow(i);
      assertThat(row.getLong("count")).isGreaterThan(2).isLessThan(5);
    });
  }

  @Test void testToList() {
    var result = repository.toList();
    assertThat(result.totalResults).isEqualTo(293);

    IntStream.range(0, result.getResults().size() - 1).forEach(i -> {
      var row = result.getRow(i);
      var prices = result.getResults().get(i).get("prices");
      assertThat(prices).asList().hasSize(Math.toIntExact(row.getLong("count")));
    });
  }

  @Test void testSortByMany() {
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
    assertThat(result.totalResults).isEqualTo(293);

    IntStream.range(0, expectedData.length - 1).forEach(i -> {
      var row = result.getRow(i);
      IntStream.range(0, expectedData[i].length - 1).forEach(j -> {
        assertThat(row.getString(expectedData[i][j][0])).isEqualTo(expectedData[i][j][1].toLowerCase());
      });
    });
  }

  @Test void testLoadWithSort() {
    String[][][] expectedData = { //
        { { "title", "Logitech MOMO Racing - Wheel and pedals set - 6 button(s) - PC, MAC - black" }, { "price", "759.12" } }, //
        { { "title", "Sony PSP Slim &amp; Lite 2000 Console" }, { "price", "695.8" } }, //
    };
    var result = repository.loadWithSort();
    assertThat(result.totalResults).isEqualTo(2265);

    IntStream.range(0, expectedData.length - 1).forEach(i -> {
      var row = result.getRow(i);
      IntStream.range(0, expectedData[i].length - 1).forEach(j -> {
        assertThat(row.getString(expectedData[i][j][0])).isEqualTo(expectedData[i][j][1]);
      });
    });
  }

  @Test void testLoadWithDocId() {
    String[][][] expectedData = { //
        { { "__key", "games:B00006JJIC" }, { "price", "759.12" } }, //
        { { "__key", "games:B000F6W1AG" }, { "price", "695.8" } }, //
        { { "__key", "games:B00002JXBD" }, { "price", "599.99" } }, //
        { { "__key", "games:B00006IZIL" }, { "price", "759.12" } }, //
    };
    var result = repository.loadWithDocId();
    assertThat(result.totalResults).isEqualTo(2265);

    IntStream.range(0, expectedData.length - 1).forEach(i -> {
      var row = result.getRow(i);
      IntStream.range(0, expectedData[i].length - 1).forEach(j -> {
        assertThat(row.getString(expectedData[i][j][0])).isEqualTo(expectedData[i][j][1]);
      });
    });
  }
}
