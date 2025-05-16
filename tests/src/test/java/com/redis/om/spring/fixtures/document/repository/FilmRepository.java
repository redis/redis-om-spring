package com.redis.om.spring.fixtures.document.repository;

import java.util.List;

import com.redis.om.spring.fixtures.document.model.Film;
import com.redis.om.spring.repository.RedisDocumentRepository;

public interface FilmRepository extends RedisDocumentRepository<Film, Integer> {
  List<Film> search(String text);

  List<Film> searchByTitle(String title);

  List<Film> searchByTitleAndLengthLessThanEqual(String title, Integer length);
}
