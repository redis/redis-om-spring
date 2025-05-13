package com.redis.om.vssmovies.domain;

import java.util.List;

import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import com.redis.om.spring.annotations.*;
import com.redis.om.spring.indexing.DistanceMetric;
import com.redis.om.spring.indexing.VectorType;

import redis.clients.jedis.search.schemafields.VectorField;

@RedisHash
public class Movie {

  @Id
  private String title;

  @Indexed(
      sortable = true
  )
  private int year;

  @Indexed
  private List<String> cast;

  @Indexed
  private List<String> genres;

  private String href;

  @Vectorize(
      destination = "embeddedExtract", embeddingType = EmbeddingType.SENTENCE, provider = EmbeddingProvider.OPENAI,
      openAiEmbeddingModel = OpenAiApi.EmbeddingModel.TEXT_EMBEDDING_3_LARGE
  )
  private String extract;

  @Indexed(
      schemaFieldType = SchemaFieldType.VECTOR, algorithm = VectorField.VectorAlgorithm.FLAT, type = VectorType.FLOAT32,
      dimension = 3072, distanceMetric = DistanceMetric.COSINE, initialCapacity = 10
  )
  private byte[] embeddedExtract;

  private String thumbnail;
  private int thumbnailWidth;
  private int thumbnailHeight;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public List<String> getCast() {
    return cast;
  }

  public void setCast(List<String> cast) {
    this.cast = cast;
  }

  public List<String> getGenres() {
    return genres;
  }

  public void setGenres(List<String> genres) {
    this.genres = genres;
  }

  public String getHref() {
    return href;
  }

  public void setHref(String href) {
    this.href = href;
  }

  public String getExtract() {
    return extract;
  }

  public void setExtract(String extract) {
    this.extract = extract;
  }

  public String getThumbnail() {
    return thumbnail;
  }

  public void setThumbnail(String thumbnail) {
    this.thumbnail = thumbnail;
  }

  public int getThumbnailWidth() {
    return thumbnailWidth;
  }

  public void setThumbnailWidth(int thumbnailWidth) {
    this.thumbnailWidth = thumbnailWidth;
  }

  public int getThumbnailHeight() {
    return thumbnailHeight;
  }

  public void setThumbnailHeight(int thumbnailHeight) {
    this.thumbnailHeight = thumbnailHeight;
  }

  public byte[] getEmbeddedExtract() {
    return embeddedExtract;
  }

  public void setEmbeddedExtract(byte[] embeddedExtract) {
    this.embeddedExtract = embeddedExtract;
  }
}
