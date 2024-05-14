package com.redis.om.spring.search.stream;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.Doc;
import com.redis.om.spring.annotations.document.fixtures.DocRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class DialectSwitchTest extends AbstractBaseDocumentTest {
  @Autowired
  DocRepository docRepository;

  @Autowired
  EntityStream entityStream;

  /**
   * Consider the differences in parser behavior in example hello world | "goodbye" moon:
   * <p>
   * In DIALECT 1, this query is interpreted as searching for (hello world | "goodbye") moon.
   * In DIALECT 2 or greater, this query is interpreted as searching for either hello world OR "goodbye" moon.
   */
  @Test
  void testSimpleQuery() {
    docRepository.deleteAll();
    Doc helloWorld = docRepository.save(Doc.of("doc1", "hello world"));
    Doc goodbyeMoon = docRepository.save(Doc.of("doc2", "goodbye moon"));

    var dialectOne = entityStream.of(Doc.class) //
      .dialect(1) //
      .filter("hello world | \"goodbye\" moon") //
      .collect(Collectors.toList());

    var dialectTwo = entityStream.of(Doc.class) //
      .dialect(2) //
      .filter("hello world | \"goodbye\" moon") //
      .collect(Collectors.toList());

    assertAll( //
      () -> assertThat(dialectOne).containsExactly(goodbyeMoon),
      () -> assertThat(dialectTwo).containsExactly(helloWorld, goodbyeMoon));
  }

  /**
   * Consider a simple query with negation -hello world:
   * <p>
   * In DIALECT 1, this query is interpreted as "find values in any field that does not contain hello AND does not
   * contain world". The equivalent is -(hello world) or -hello -world.
   * In DIALECT 2 or greater, this query is interpreted as -hello AND world (only hello is negated).
   * In DIALECT 2 or greater, to achieve the default behavior of DIALECT 1, update your query to -(hello world).
   */
  @Test
  void testSimpleQueryWithNegation() {
    docRepository.deleteAll();
    Doc hello = docRepository.save(Doc.of("doc1", "hello"));
    Doc world = docRepository.save(Doc.of("doc2", "world"));
    docRepository.save(Doc.of("doc3", "hello world"));

    var dialectOne = entityStream.of(Doc.class) //
      .dialect(1) //
      .filter("-hello world") //
      .collect(Collectors.toList());

    var dialectTwo = entityStream.of(Doc.class) //
      .dialect(2) //
      .filter("-hello world") //
      .collect(Collectors.toList());

    assertAll( //
      () -> assertThat(dialectOne).containsExactly(hello, world), () -> assertThat(dialectTwo).containsExactly(world));
  }

}
