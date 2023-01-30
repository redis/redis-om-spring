package com.redis.om.spring.repository.query;

/**
 * From <a href="https://github.com/RediSearch/RediSearch/blob/master/src/language.c">...</a>
 * TODO: potentially move to Jedis
 */
public enum SearchLanguage {
  ARABIC("arabic"),
  ARMENIAN("armenian"),
  BASQUE("basque"),
  CATALAN("catalan"),
  DANISH("danish"),
  DUTCH("dutch"),
  ENGLISH("english"),
  FINNISH("finnish"),
  FRENCH("french"),
  GERMAN("german"),
  GREEK("greek"),
  HINDI("hindi"),
  HUNGARIAN("hungarian"),
  INDONESIAN("indonesian"),
  IRISH("irish"),
  ITALIAN("italian"),
  LITHUANIAN("lithuanian"),
  NEPALI("nepali"),
  NORWEGIAN("norwegian"),
  PORTUGUESE("portuguese"),
  ROMANIAN("romanian"),
  RUSSIAN("russian"),
  SERBIAN("serbian"),
  SPANISH("spanish"),
  SWEDISH("swedish"),
  TAMIL("tamil"),
  TURKISH("turkish"),
  YIDDISH("yiddish"),
  CHINESE("chinese");

  private final String value;

  SearchLanguage(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
