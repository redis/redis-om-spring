package com.redis.om.spring.annotations.document;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.SpanishDoc;
import com.redis.om.spring.annotations.document.fixtures.SpanishDocRepository;
import com.redis.om.spring.serialization.gson.GsonBuidlerFactory;

import io.redisearch.SearchResult;

class DocumentLanguageTest extends AbstractBaseDocumentTest {
  private static final Gson gson = GsonBuidlerFactory.getBuilder().create();
  private static final String QUIJOTE = //
      "En un lugar de la Mancha, de cuyo nombre no quiero acordarme, no ha mucho tiempo " + //
          "que vivía un hidalgo de los de lanza en astillero, adarga antigua, rocín flaco y galgo corredor.";
  private static final String SOLEDAD = //
      "Muchos años después, frente al pelotón de fusilamiento, el coronel Aureliano Buendía había" + //
          " de recordar aquella tarde remota en que su padre lo llevó a conocer el hielo.";

  @Autowired
  SpanishDocRepository repo;

  @BeforeEach
  void createDocs() {
    SpanishDoc quijote = SpanishDoc.of("Don Quijote", QUIJOTE);
    SpanishDoc soledad = SpanishDoc.of("Cien Años de Soledad", SOLEDAD);
    repo.saveAll(List.of(quijote, soledad));
  }

  @Test
  void testLanguage() {
    SearchResult result = repo.findByBody("fusil");
    assertThat(result.totalResults).isEqualTo(1);
    SpanishDoc doc = gson.fromJson(result.docs.get(0).get("$").toString(), SpanishDoc.class);
    assertThat(doc.getTitle()).isEqualTo("Cien Años de Soledad");
    
    SearchResult result2 = repo.findByBody("manchas");
    assertThat(result2.totalResults).isEqualTo(1);
    SpanishDoc doc2 = gson.fromJson(result2.docs.get(0).get("$").toString(), SpanishDoc.class);
    assertThat(doc2.getTitle()).isEqualTo("Don Quijote");
  }
}
