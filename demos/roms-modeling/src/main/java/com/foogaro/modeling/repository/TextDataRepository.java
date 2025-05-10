package com.foogaro.modeling.repository;

import com.foogaro.modeling.model.TextData;
import com.redis.om.spring.repository.RedisDocumentRepository;
import com.redis.om.spring.search.stream.predicates.fulltext.IsMissingPredicate;

import java.util.List;

public interface TextDataRepository extends RedisDocumentRepository<TextData, String> {

    TextData findByName(String name);
    List<TextData> findByDescription(String description);
    List<TextData> findByYear(int year);
    List<TextData> findByYearBetween(int fromYear, int toYear);
    List<TextData> findByScore(double score);
    List<TextData> findByScoreBetween(double fromScore, double toScore);
    List<TextData> findByMeasurementsIn(double measurement);

}
