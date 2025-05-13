package com.foogaro.modeling.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.foogaro.modeling.model.TextData;
import com.foogaro.modeling.model.TextData$;
import com.foogaro.modeling.repository.TextDataRepository;
import com.github.javafaker.Faker;
import com.redis.om.spring.search.stream.EntityStream;
import com.redis.om.spring.search.stream.SearchStream;
import com.redis.om.spring.tuple.Fields;
import com.redis.om.spring.tuple.Pair;

@Service
public class TextDataService {

  private Logger logger = LoggerFactory.getLogger(getClass());

  @Autowired
  private TextDataRepository repository;

  @Autowired
  private EntityStream entityStream;

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

  public List<TextData> findByMeasurementsIn(Double measurement) {
    List<TextData> textDataList = new ArrayList<>();
    SearchStream<TextData> searchStream = entityStream.of(TextData.class);
    List<Pair<TextData, TextData>> matching = searchStream.filter(TextData$.MEASUREMENTS.in(List.of(measurement))).map(
        Fields.of(TextData$._THIS, TextData$._THIS)).collect(Collectors.toList());

    logger.info("matching: {}", matching);

    textDataList.addAll(matching.stream().map(Pair::getFirst).toList());
    //                .collect(Collectors.toList()));

    return textDataList;
  }

  public List<TextData> findByMissingDescription() {
    List<TextData> textDataList = new ArrayList<>();
    //        FT.SEARCH com.foogaro.modeling.model.TextDataIdx 'ismissing(@description)' DIALECT 2
    SearchStream<TextData> searchStream = entityStream.of(TextData.class);
    List<Pair<TextData, TextData>> matching = searchStream.filter(TextData$.DESCRIPTION.isMissing()).map(Fields.of(
        TextData$._THIS, TextData$._THIS)).collect(Collectors.toList());

    logger.info("matching: {}", matching);

    textDataList.addAll(matching.stream().map(Pair::getFirst).toList());

    return textDataList;
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
        textData.setScore(faker.number().randomDouble(2, 1, 100));
        save(textData);
        created.getAndIncrement();
      } catch (Exception e) {
        logger.error("Error while creating new TextData: {}", textData, e);
      }
    });
    logger.info("Created {} TextData.", created.get());
    return created.get();
  }

}
