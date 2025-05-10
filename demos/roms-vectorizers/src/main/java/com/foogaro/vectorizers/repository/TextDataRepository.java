package com.foogaro.vectorizers.repository;

import com.foogaro.vectorizers.model.TextData;
import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.List;

public interface TextDataRepository extends RedisDocumentRepository<TextData, String> {

    TextData findByName(String name);
    List<TextData> findByDescription(String description);
    List<TextData> findByYear(int year);
    List<TextData> findByYearBetween(int fromYear, int toYear);
    List<TextData> findByScore(double score);
    List<TextData> findByScoreBetween(double fromScore, double toScore);

}
