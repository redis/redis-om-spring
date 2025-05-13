package com.foogaro.vectorizers.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.foogaro.vectorizers.model.TextData;
import com.foogaro.vectorizers.model.TextData$;
import com.foogaro.vectorizers.repository.TextDataRepository;
import com.github.javafaker.Faker;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.SearchStream;
import com.redis.om.spring.tuple.Fields;
import com.redis.om.spring.tuple.Pair;
import com.redis.om.spring.vectorize.Embedder;

import redis.clients.jedis.search.aggr.SortedField;

@Service
public class TextDataService {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private TextDataRepository repository;

  @Autowired
  private EntityStream entityStream;

  @Autowired
  private Embedder embedder;

  private final int numberOfNearestNeighbors = 6;

  public TextData save(TextData textData) {
    return repository.save(textData);
  }

  public void deleteById(String id) {
    repository.deleteById(id);
  }

  public void deleteAll() {
    repository.deleteAll();
  }

  public TextData findById(String id) {
    return repository.findById(id).orElse(null);
  }

  public TextData findByName(String name) {
    return repository.findByName(name);
  }

  public List<TextData> findByDescription(String description) {
    return repository.findByDescription(description);
  }

  public List<TextData> findByYear(int year) {
    return repository.findByYear(year);
  }

  public List<TextData> findByYearBetween(int fromYear, int toYear) {
    return repository.findByYearBetween(fromYear, toYear);
  }

  public List<TextData> findByScore(double score) {
    return repository.findByScore(score);
  }

  public List<TextData> findByScoreBetween(double fromScore, double toScore) {
    return repository.findByScoreBetween(fromScore, toScore);
  }

  public int load(int count) {
    Faker faker = new Faker();
    logger.info("Creating {} TextData...", count);
    var range = LongStream.rangeClosed(1, count);
    AtomicInteger created = new AtomicInteger();
    range.parallel().forEach(j -> {
      TextData textData = TextData.of();
      try {
        textData.setId(j + "");
        textData.setName(faker.dune().character());
        textData.setYear(faker.number().numberBetween(3000, 10_000));
        textData.setDescription(faker.dune().quote());
        save(textData);
        created.getAndIncrement();
      } catch (Exception e) {
        logger.error("Error while creating new TextData: {}", textData, e);
      }
    });
    logger.info("Created {} TextData.", created.get());
    return created.get();
  }

  public List<TextData> semanticSearch(String prompt) {
    return semanticSearch(prompt, numberOfNearestNeighbors);
  }

  private List<TextData> semanticSearch(String prompt, int limit) {
    List<TextData> textDataList = new ArrayList<>();

    float[] embeddedQuery = embedder.getTextEmbeddingsAsFloats(List.of(prompt), TextData$.DESCRIPTION).getFirst();

    SearchStream<TextData> stream = entityStream.of(TextData.class);
    List<Pair<TextData, Double>> matchWithScore = stream.filter(TextData$.TEXT_EMBEDDING.knn(limit, embeddedQuery))
        .sorted(TextData$._TEXT_EMBEDDING_SCORE, SortedField.SortOrder.ASC).limit(limit).map(Fields.of(TextData$._THIS,
            TextData$._TEXT_EMBEDDING_SCORE)).collect(Collectors.toList());

    for (Pair<TextData, Double> pair : matchWithScore) {
      TextData td = pair.getFirst();
      Double score = pair.getSecond();
      td.setScore(score);
      textDataList.add(td);
    }
    return textDataList;
  }

  public List<TextData> hybridSearch(TextData textData) {
    List<TextData> textDataList = new ArrayList<>();

    String prompt = textData.getDescription();
    byte[] embeddedQuery = embedder.getTextEmbeddingsAsBytes(List.of(prompt), TextData$.DESCRIPTION).getFirst();

    SearchStream<TextData> stream = entityStream.of(TextData.class);
    List<Pair<TextData, Double>> matchWithScore = stream.filter(TextData$.TEXT_EMBEDDING.knn(numberOfNearestNeighbors,
        embeddedQuery)).filter(TextData$.YEAR.between(textData.getYear(), textData.getYear())).sorted(
            TextData$._TEXT_EMBEDDING_SCORE, SortedField.SortOrder.ASC).limit(numberOfNearestNeighbors).map(Fields.of(
                TextData$._THIS, TextData$._TEXT_EMBEDDING_SCORE)).collect(Collectors.toList());

    for (Pair<TextData, Double> pair : matchWithScore) {
      TextData td = pair.getFirst();
      Double score = pair.getSecond();
      td.setScore(score);
      textDataList.add(td);
    }
    return textDataList;
  }

}
