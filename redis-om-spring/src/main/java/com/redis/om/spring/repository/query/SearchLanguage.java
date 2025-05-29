package com.redis.om.spring.repository.query;

/**
 * Enumeration of supported search languages for RediSearch stemming and text analysis.
 * These language codes are used to configure language-specific text processing in
 * RediSearch indexes, enabling proper stemming, tokenization, and search behavior
 * for different languages.
 * 
 * <p>The language settings affect how text is processed during indexing and search
 * operations, including word stemming (reducing words to their root forms) and
 * language-specific text analysis rules.</p>
 * 
 * <p>Based on the language support in
 * <a href="https://github.com/RediSearch/RediSearch/blob/master/src/language.c">RediSearch language.c</a></p>
 * 
 * @since 1.0.0
 */
public enum SearchLanguage {
  /** Arabic language support for stemming and text analysis. */
  ARABIC("arabic"),

  /** Armenian language support for stemming and text analysis. */
  ARMENIAN("armenian"),

  /** Basque language support for stemming and text analysis. */
  BASQUE("basque"),

  /** Catalan language support for stemming and text analysis. */
  CATALAN("catalan"),

  /** Danish language support for stemming and text analysis. */
  DANISH("danish"),

  /** Dutch language support for stemming and text analysis. */
  DUTCH("dutch"),

  /** English language support for stemming and text analysis. */
  ENGLISH("english"),

  /** Finnish language support for stemming and text analysis. */
  FINNISH("finnish"),

  /** French language support for stemming and text analysis. */
  FRENCH("french"),

  /** German language support for stemming and text analysis. */
  GERMAN("german"),

  /** Greek language support for stemming and text analysis. */
  GREEK("greek"),

  /** Hindi language support for stemming and text analysis. */
  HINDI("hindi"),

  /** Hungarian language support for stemming and text analysis. */
  HUNGARIAN("hungarian"),

  /** Indonesian language support for stemming and text analysis. */
  INDONESIAN("indonesian"),

  /** Irish language support for stemming and text analysis. */
  IRISH("irish"),

  /** Italian language support for stemming and text analysis. */
  ITALIAN("italian"),

  /** Lithuanian language support for stemming and text analysis. */
  LITHUANIAN("lithuanian"),

  /** Nepali language support for stemming and text analysis. */
  NEPALI("nepali"),

  /** Norwegian language support for stemming and text analysis. */
  NORWEGIAN("norwegian"),

  /** Portuguese language support for stemming and text analysis. */
  PORTUGUESE("portuguese"),

  /** Romanian language support for stemming and text analysis. */
  ROMANIAN("romanian"),

  /** Russian language support for stemming and text analysis. */
  RUSSIAN("russian"),

  /** Serbian language support for stemming and text analysis. */
  SERBIAN("serbian"),

  /** Spanish language support for stemming and text analysis. */
  SPANISH("spanish"),

  /** Swedish language support for stemming and text analysis. */
  SWEDISH("swedish"),

  /** Tamil language support for stemming and text analysis. */
  TAMIL("tamil"),

  /** Turkish language support for stemming and text analysis. */
  TURKISH("turkish"),

  /** Yiddish language support for stemming and text analysis. */
  YIDDISH("yiddish"),

  /** Chinese language support for stemming and text analysis. */
  CHINESE("chinese");

  private final String value;

  /**
   * Creates a SearchLanguage with the specified RediSearch language code.
   * 
   * @param value the language code used by RediSearch
   */
  SearchLanguage(String value) {
    this.value = value;
  }

  /**
   * Returns the RediSearch language code for this language.
   * 
   * @return the language code string
   */
  public String getValue() {
    return value;
  }
}
