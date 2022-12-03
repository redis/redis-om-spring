package com.redis.om.spring.annotations.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redis.om.spring.annotations.document.fixtures.MultiLingualDoc;
import com.redis.om.spring.annotations.document.fixtures.MultiLingualDocRepository;
import com.redis.om.spring.repository.query.SearchLanguage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.annotations.document.fixtures.SpanishDoc;
import com.redis.om.spring.annotations.document.fixtures.SpanishDocRepository;

import io.redisearch.SearchResult;

class DocumentLanguageTest extends AbstractBaseDocumentTest {
  @Autowired
  private Gson gson;
  private static final String QUIJOTE = //
      "En un lugar de la Mancha, de cuyo nombre no quiero acordarme, no ha mucho tiempo " + //
          "que vivía un hidalgo de los de lanza en astillero, adarga antigua, rocín flaco y galgo corredor.";
  private static final String SOLEDAD = //
      "Muchos años después, frente al pelotón de fusilamiento, el coronel Aureliano Buendía había" + //
          " de recordar aquella tarde remota en que su padre lo llevó a conocer el hielo.";

  private static Map<SearchLanguage,String> MULTILINGUAL_SENTENCES = new HashMap<>();
  static {
    MULTILINGUAL_SENTENCES.put(SearchLanguage.GERMAN, "Das Leben ist kein Ponyhof mit deinem Doppelgänger");
    MULTILINGUAL_SENTENCES.put(SearchLanguage.ENGLISH,"The rain in Spain stays mainly in the plains");
    MULTILINGUAL_SENTENCES.put(SearchLanguage.CATALAN, "RediSearch és un mòdul de Redis disponible en fonts que permet fer consultes, indexació secundària i cerca de text complet per a Redis.");
    MULTILINGUAL_SENTENCES.put(SearchLanguage.FRENCH, "Qui court deux lievres a la fois, n’en prend aucun");
    MULTILINGUAL_SENTENCES.put(SearchLanguage.HINDI, "अँगरेजी अँगरेजों अँगरेज़");
  }

  @Autowired
  SpanishDocRepository spanishDocRepository;

  @Autowired
  MultiLingualDocRepository multiLingualDocRepository;

  @BeforeEach
  void createDocs() {
    spanishDocRepository.deleteAll();
    multiLingualDocRepository.deleteAll();

    SpanishDoc quijote = SpanishDoc.of("Don Quijote", QUIJOTE);
    SpanishDoc soledad = SpanishDoc.of("Cien Años de Soledad", SOLEDAD);
    spanishDocRepository.saveAll(List.of(quijote, soledad));

    MULTILINGUAL_SENTENCES.forEach((language, sentence) -> {
      multiLingualDocRepository.save(MultiLingualDoc.of(language.getValue(), sentence));
    });
  }

  @Test
  void testLanguage() {
    SearchResult result = spanishDocRepository.findByBody("fusil");
    assertThat(result.totalResults).isEqualTo(1);
    SpanishDoc doc = gson.fromJson(result.docs.get(0).get("$").toString(), SpanishDoc.class);
    assertThat(doc.getTitle()).isEqualTo("Cien Años de Soledad");

    SearchResult result2 = spanishDocRepository.findByBody("manchas");
    assertThat(result2.totalResults).isEqualTo(1);
    SpanishDoc doc2 = gson.fromJson(result2.docs.get(0).get("$").toString(), SpanishDoc.class);
    assertThat(doc2.getTitle()).isEqualTo("Don Quijote");
  }

  @Test
  void testLanguageStemmerSearches() {
    SearchResult doppelGe = multiLingualDocRepository.findByBody("Doppelgänger", SearchLanguage.GERMAN);
    SearchResult doppelEn = multiLingualDocRepository.findByBody("main plain", SearchLanguage.ENGLISH);

    assertAll( //
        () -> assertThat(doppelGe.totalResults).isEqualTo(1), //
        () -> assertThat(doppelEn.totalResults).isEqualTo(1), //
        () -> {
          MultiLingualDoc geDoc1 = gson.fromJson(doppelGe.docs.get(0).get("$").toString(), MultiLingualDoc.class);
          assertThat(geDoc1.getBody()).isEqualTo(MULTILINGUAL_SENTENCES.get(SearchLanguage.GERMAN));
          MultiLingualDoc enDoc1 = gson.fromJson(doppelEn.docs.get(0).get("$").toString(), MultiLingualDoc.class);
          assertThat(enDoc1.getBody()).isEqualTo(MULTILINGUAL_SENTENCES.get(SearchLanguage.ENGLISH));
        } //
    );
  }

  @Test
  void testLanguageStemmerSearches2() {
    SearchResult searchHi = multiLingualDocRepository.findByBody("अँगरेज़", SearchLanguage.HINDI);
    SearchResult searchCat = multiLingualDocRepository.findByBody("permet fer consultes", SearchLanguage.CATALAN);

    assertAll( //
        () -> assertThat(searchHi.totalResults).isEqualTo(1), //
        () -> assertThat(searchCat.totalResults).isEqualTo(1), //
        () -> {
          MultiLingualDoc hiDoc1 = gson.fromJson(searchHi.docs.get(0).get("$").toString(), MultiLingualDoc.class);
          MultiLingualDoc catDoc1 = gson.fromJson(searchCat.docs.get(0).get("$").toString(), MultiLingualDoc.class);
          assertThat(hiDoc1.getBody()).isEqualTo(MULTILINGUAL_SENTENCES.get(SearchLanguage.HINDI));
          assertThat(catDoc1.getBody()).isEqualTo(MULTILINGUAL_SENTENCES.get(SearchLanguage.CATALAN));
        } //
    );
  }
}
