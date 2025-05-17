package com.redis.om.spring.annotations.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.MultiLingualDoc;
import com.redis.om.spring.fixtures.document.model.SpanishDoc;
import com.redis.om.spring.fixtures.document.repository.MultiLingualDocRepository;
import com.redis.om.spring.fixtures.document.repository.SpanishDocRepository;
import com.redis.om.spring.repository.query.SearchLanguage;

import redis.clients.jedis.search.SearchResult;
import redis.clients.jedis.util.SafeEncoder;

@SuppressWarnings(
  "SpellCheckingInspection"
)
class DocumentLanguageTest extends AbstractBaseDocumentTest {
  private static final String QUIJOTE = //
      "En un lugar de la Mancha, de cuyo nombre no quiero acordarme, no ha mucho tiempo " + //
          "que vivía un hidalgo de los de lanza en astillero, adarga antigua, rocín flaco y galgo corredor.";
  private static final String SOLEDAD = //
      "Muchos años después, frente al pelotón de fusilamiento, el coronel Aureliano Buendía había" + //
          " de recordar aquella tarde remota en que su padre lo llevó a conocer el hielo.";
  private static final Map<SearchLanguage, String> MULTILINGUAL_SENTENCES = new HashMap<>();

  static {
    MULTILINGUAL_SENTENCES.put(SearchLanguage.GERMAN, "Das Leben ist kein Ponyhof mit deinem Doppelgänger");
    MULTILINGUAL_SENTENCES.put(SearchLanguage.ENGLISH, "The rain in Spain stays mainly in the plains");
    MULTILINGUAL_SENTENCES.put(SearchLanguage.CATALAN,
        "RediSearch és un mòdul de Redis disponible en fonts que permet fer consultes, indexació secundària i cerca de text complet per a Redis.");
    MULTILINGUAL_SENTENCES.put(SearchLanguage.FRENCH, "Qui court deux lievres a la fois, n’en prend aucun");
    MULTILINGUAL_SENTENCES.put(SearchLanguage.HINDI, "अँगरेजी अँगरेजों अँगरेज़");
  }

  @Autowired
  SpanishDocRepository spanishDocRepository;
  @Autowired
  MultiLingualDocRepository multiLingualDocRepository;
  @Autowired
  private Gson gson;

  @BeforeEach
  void createDocs() {
    spanishDocRepository.deleteAll();
    multiLingualDocRepository.deleteAll();

    SpanishDoc quijote = SpanishDoc.of("Don Quijote", QUIJOTE);
    SpanishDoc soledad = SpanishDoc.of("Cien Años de Soledad", SOLEDAD);
    spanishDocRepository.saveAll(List.of(quijote, soledad));

    MULTILINGUAL_SENTENCES.forEach((language, sentence) -> multiLingualDocRepository.save(MultiLingualDoc.of(language
        .getValue(), sentence)));
  }

  @Test
  void testLanguage() {
    SearchResult result = spanishDocRepository.findByBody("fusil");
    assertThat(result.getTotalResults()).isEqualTo(1);
    SpanishDoc doc = gson.fromJson(SafeEncoder.encode((byte[]) result.getDocuments().get(0).get("$")),
        SpanishDoc.class);
    assertThat(doc.getTitle()).isEqualTo("Cien Años de Soledad");

    SearchResult result2 = spanishDocRepository.findByBody("manchas");
    assertThat(result2.getTotalResults()).isEqualTo(1);
    SpanishDoc doc2 = gson.fromJson(SafeEncoder.encode((byte[]) result2.getDocuments().get(0).get("$")),
        SpanishDoc.class);
    assertThat(doc2.getTitle()).isEqualTo("Don Quijote");
  }

  @Test
  void testLanguageStemmerSearches() {
    SearchResult doppelGe = multiLingualDocRepository.findByBody("Doppelgänger", SearchLanguage.GERMAN);
    SearchResult doppelEn = multiLingualDocRepository.findByBody("main plain", SearchLanguage.ENGLISH);

    assertAll( //
        () -> assertThat(doppelGe.getTotalResults()).isEqualTo(1), //
        () -> assertThat(doppelEn.getTotalResults()).isEqualTo(1), //
        () -> {
          MultiLingualDoc geDoc1 = gson.fromJson(SafeEncoder.encode((byte[]) doppelGe.getDocuments().get(0).get("$")),
              MultiLingualDoc.class);
          assertThat(geDoc1.getBody()).isEqualTo(MULTILINGUAL_SENTENCES.get(SearchLanguage.GERMAN));
          MultiLingualDoc enDoc1 = gson.fromJson(SafeEncoder.encode((byte[]) doppelEn.getDocuments().get(0).get("$")),
              MultiLingualDoc.class);
          assertThat(enDoc1.getBody()).isEqualTo(MULTILINGUAL_SENTENCES.get(SearchLanguage.ENGLISH));
        } //
    );
  }

  @Test
  void testLanguageStemmerSearches2() {
    SearchResult searchHi = multiLingualDocRepository.findByBody("अँगरेज़", SearchLanguage.HINDI);
    SearchResult searchCat = multiLingualDocRepository.findByBody("permet fer consultes", SearchLanguage.CATALAN);

    assertAll( //
        () -> assertThat(searchHi.getTotalResults()).isEqualTo(1), //
        () -> assertThat(searchCat.getTotalResults()).isEqualTo(1), //
        () -> {
          MultiLingualDoc hiDoc1 = gson.fromJson(SafeEncoder.encode((byte[]) searchHi.getDocuments().get(0).get("$")),
              MultiLingualDoc.class);
          MultiLingualDoc catDoc1 = gson.fromJson(SafeEncoder.encode((byte[]) searchCat.getDocuments().get(0).get("$")),
              MultiLingualDoc.class);
          assertThat(hiDoc1.getBody()).isEqualTo(MULTILINGUAL_SENTENCES.get(SearchLanguage.HINDI));
          assertThat(catDoc1.getBody()).isEqualTo(MULTILINGUAL_SENTENCES.get(SearchLanguage.CATALAN));
        } //
    );
  }
}
