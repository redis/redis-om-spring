package com.foogaro.modeling.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.foogaro.modeling.model.TextData;
import com.foogaro.modeling.service.TextDataService;

@RestController
@RequestMapping(
  "/api/text-data"
)
public class TextDataController {

  @Autowired
  private TextDataService service;

  @PostMapping(
      path = "/"
  )
  public ResponseEntity<TextData> create(@RequestBody TextData textData) {
    return Optional.ofNullable(service.save(textData)).map(ResponseEntity::ok).orElse(ResponseEntity.notFound()
        .build());
  }

  @GetMapping(
      path = "/load"
  )
  public ResponseEntity<Integer> load() {
    return Optional.of(service.load(1)).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @GetMapping(
      path = "/load/{count}"
  )
  public ResponseEntity<Integer> load(@PathVariable int count) {
    return Optional.of(service.load(count)).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping(
      path = "/deleteById/{id}"
  )
  public ResponseEntity<Void> deleteById(@PathVariable String id) {
    service.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping(
      path = "/findById/{id}"
  )
  public ResponseEntity<TextData> findById(@PathVariable String id) {
    return Optional.ofNullable(service.findById(id)).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @GetMapping(
      path = "/findByName/{name}"
  )
  public ResponseEntity<TextData> findByName(@PathVariable String name) {
    return Optional.ofNullable(service.findByName(name)).map(ResponseEntity::ok).orElse(ResponseEntity.notFound()
        .build());
  }

  @GetMapping(
      path = "/findByDescription/{description}"
  )
  public ResponseEntity<List<TextData>> findByDescription(@PathVariable String description) {
    return Optional.ofNullable(service.findByDescription(description)).filter(list -> !list.isEmpty()).map(
        ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @GetMapping(
      path = "/findByYear/{year}"
  )
  public ResponseEntity<List<TextData>> findByYear(@PathVariable int year) {
    return Optional.ofNullable(service.findByYear(year)).filter(list -> !list.isEmpty()).map(ResponseEntity::ok).orElse(
        ResponseEntity.notFound().build());
  }

  @GetMapping(
      path = "/findByYearBetween/{fromYear}/{toYear}"
  )
  public ResponseEntity<List<TextData>> findByYearBetween(@PathVariable int fromYear, @PathVariable int toYear) {
    return Optional.ofNullable(service.findByYearBetween(fromYear, toYear)).filter(list -> !list.isEmpty()).map(
        ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @GetMapping(
      path = "/findByScore/{score}"
  )
  public ResponseEntity<List<TextData>> findByScore(@PathVariable double score) {
    return Optional.ofNullable(service.findByScore(score)).filter(list -> !list.isEmpty()).map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping(
      path = "/findByScoreBetween/{fromScore}/{toScore}"
  )
  public ResponseEntity<List<TextData>> findByScoreBetween(@PathVariable double fromScore,
      @PathVariable double toScore) {
    return Optional.ofNullable(service.findByScoreBetween(fromScore, toScore)).filter(list -> !list.isEmpty()).map(
        ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @GetMapping(
      path = "/findByMeasurementsIn/{measurement}"
  )
  public ResponseEntity<List<TextData>> findByMeasurementsIn(@PathVariable double measurement) {
    return Optional.ofNullable(service.findByMeasurementsIn(measurement)).filter(list -> !list.isEmpty()).map(
        ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @GetMapping(
      path = "/findByMissingDescription"
  )
  public ResponseEntity<List<TextData>> findByMissingDescription() {
    return Optional.ofNullable(service.findByMissingDescription()).filter(list -> !list.isEmpty()).map(
        ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

}
