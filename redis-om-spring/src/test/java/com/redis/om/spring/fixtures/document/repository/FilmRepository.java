package com.redis.om.spring.fixtures.document.repository;

import com.redis.om.spring.fixtures.document.model.Film;
import com.redis.om.spring.repository.RedisDocumentRepository;

import java.util.List;

public interface FilmRepository extends RedisDocumentRepository<Film, Integer> {
  List<Film> search(String text);

  List<Film> searchByTitle(String title);

  List<Film> searchByTitleAndLengthLessThanEqual(String title, Integer length);
}
