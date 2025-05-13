package com.foogaro.modeling.repository;

import java.util.List;

import com.foogaro.modeling.model.TextData;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface TextDataRepository extends RedisDocumentRepository<TextData, String> {

  TextData findByName(String name);

  List<TextData> findByDescription(String description);

  List<TextData> findByYear(int year);

  List<TextData> findByYearBetween(int fromYear, int toYear);

  List<TextData> findByScore(double score);

  List<TextData> findByScoreBetween(double fromScore, double toScore);

  List<TextData> findByMeasurementsIn(double measurement);

}
