package com.redis.om.spring.fixtures.document.repository;

import java.util.Set;

import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;

import com.redis.om.spring.fixtures.document.model.DocWithSets;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings(
  "ALL"
)
public interface DocWithSetsRepository extends RedisDocumentRepository<DocWithSets, String> {
  Iterable<DocWithSets> findByTheNumbersContaining(Set<Integer> ints);

  Iterable<DocWithSets> findByTheNumbersContainingAll(Set<Integer> ints);

  Iterable<DocWithSets> findByTheLocationsNear(Point point, Distance distance);

  Iterable<DocWithSets> findByTheLocationsContaining(Set<Point> points);

  Iterable<DocWithSets> findByTheLocationsContainingAll(Set<Point> points);
}
