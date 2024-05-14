package com.redis.om.spring.search.stream;

import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.annotations.hash.fixtures.Text;
import com.redis.om.spring.annotations.hash.fixtures.Text$;
import com.redis.om.spring.annotations.hash.fixtures.TextRepository;
import com.redis.om.spring.tuple.Tuples;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class EntityStreamHighlightHashTest extends AbstractBaseEnhancedRedisTest {

  @Autowired
  EntityStream entityStream;

  @Autowired
  TextRepository repository;

  @BeforeEach
  void loadText(@Value("classpath:/data/genesis.txt") File dataFile) throws IOException {
    String content = new String(Files.readAllBytes(dataFile.toPath()));
    repository.save(Text.of(content));
  }

  /**
   * "FT.SEARCH" "com.redis.om.spring.annotations.hash.fixtures.TextIdx" "abraham isaac jacob"
   * "LIMIT" "0" "10000"
   * "HIGHLIGHT" "FIELDS" "1" "body"
   * "SUMMARIZE" "FIELDS" "1" "body"
   * "DIALECT" "1"
   */
  @Test
  void testHighlightSummarize() {
    SearchStream<Text> stream = entityStream.of(Text.class);

    List<Text> texts = stream //
      .filter("abraham isaac jacob") //
      .summarize(Text$.BODY) //
      .highlight(Text$.BODY) //
      .collect(Collectors.toList());

    String result = texts.stream().findFirst().map(Text::getBody).get();

    assertAll( //
      () -> assertThat(result).contains("<b>Abraham</b>"), //
      () -> assertThat(result).contains("<b>Isaac</b>"), //
      () -> assertThat(result).contains("<b>Jacob</b>"));
  }

  /**
   * "FT.SEARCH" "com.redis.om.spring.annotations.hash.fixtures.TextIdx" "abraham isaac jacob"
   * "LIMIT" "0" "10000"
   * "HIGHLIGHT" "FIELDS" "1" "body" "TAGS" "<strong>" "</strong>"
   * "SUMMARIZE" "FIELDS" "1" "body" "DIALECT" "1"
   */
  @Test
  void testHighlightSummarizeWithCustomTags() {
    SearchStream<Text> stream = entityStream.of(Text.class);

    List<Text> texts = stream //
      .filter("abraham isaac jacob") //
      .summarize(Text$.BODY) //
      .highlight(Text$.BODY, Tuples.of("<strong>", "</strong>")) //
      .collect(Collectors.toList());

    String result = texts.stream().findFirst().map(Text::getBody).get();

    assertAll( //
      () -> assertThat(result).contains("<strong>Abraham</strong>"), //
      () -> assertThat(result).contains("<strong>Isaac</strong>"), //
      () -> assertThat(result).contains("<strong>Jacob</strong>"));
  }

  /**
   * "FT.SEARCH" "com.redis.om.spring.annotations.hash.fixtures.TextIdx" "abraham isaac jacob" "LIMIT" "0" "10000"
   * "SUMMARIZE" "FIELDS" "1" "body" "FRAGS" "100" "LEN" "20" "SEPARATOR" "<frag/>"
   * "DIALECT" "1"
   */
  @Test
  void testHighlightSummarizeWithFragmentSizeAndSeparator() {
    SearchStream<Text> stream = entityStream.of(Text.class);

    List<Text> texts = stream //
      .filter("abraham isaac jacob") //
      .summarize(Text$.BODY, SummarizeParams.instance().fragments(100).separator("<frag/>")) //
      .collect(Collectors.toList());

    List<String> fragments = Arrays.stream(texts.stream().findFirst().map(Text::getBody).get().split("<frag/>"))
      .toList();
    assertThat(fragments).hasSize(100);
  }

  /**
   * "FT.SEARCH" "com.redis.om.spring.annotations.hash.fixtures.TextIdx" "isaac"
   * "LIMIT" "0" "10000"
   * "SUMMARIZE" "FIELDS" "1" "body" "FRAGS" "4" "LEN" "3" "SEPARATOR" "\r\n" "DIALECT" "1"
   */
  @Test
  void testHighlightSummarizeWithCustomSeparator() {
    SearchStream<Text> stream = entityStream.of(Text.class);

    List<Text> texts = stream //
      .filter("isaac") //
      .summarize(Text$.BODY, SummarizeParams.instance().fragments(4).size(3).separator("\r\n")) //
      .collect(Collectors.toList());

    String result = texts.stream().findFirst().map(Text::getBody).get();
    assertThat(result).isEqualTo(
      "name Isaac: and\r\nwith Isaac,\r\nIsaac. {21:4} And Abraham circumcised his son Isaac\r\nson Isaac was\r\n");
  }

  /**
   * "FT.SEARCH" "com.redis.om.spring.annotations.hash.fixtures.TextIdx" "-blah"
   * "LIMIT" "0" "10000"
   * "SUMMARIZE" "FIELDS" "1" "body" "FRAGS" "3" "LEN" "3" "SEPARATOR" "..."
   * "DIALECT" "1"
   */
  @Test
  void testAttemptQueryWithNoCorrespondingMatchedTerm() {
    SearchStream<Text> stream = entityStream.of(Text.class);

    List<Text> texts = stream //
      .filter("-blah") //
      .summarize(Text$.BODY, SummarizeParams.instance().size(3)) //
      .collect(Collectors.toList());

    String result = texts.stream().findFirst().map(Text::getBody).get();
    assertThat(result).isEqualTo(" The First Book of Moses, called Genesis {1:1} In");
  }
}
