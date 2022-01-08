package com.redis.om.permits.controlllers;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.redis.om.permits.models.Permit;
import com.redis.om.permits.repositories.PermitRepository;

@RestController
@RequestMapping("/api/permits")
public class PermitController {
  @Autowired
  PermitRepository repository;
  
  @GetMapping("value/{value}")
  Iterable<Permit> byConstructionValue(@PathVariable("value") long value) {
    return repository.findByConstructionValue(value);
  }
  
  @GetMapping("search/{q}")
  Iterable<Permit> fullTextSearch(@PathVariable("q") String q) {
    return repository.search(q);
  }
  
  @GetMapping("building_type/{type}")
  Iterable<Permit> byBuildingType(@PathVariable("type") String type) {
    return repository.findByBuildingType(type);
  }
  
  @GetMapping("city/{city}")
  Iterable<Permit> byCity(@PathVariable("city") String city) {
    return repository.findByAddress_City(city);
  }
  
  @GetMapping("worktypes")
  Iterable<Permit> byTags(@RequestParam("types") Set<String> wts) {
    return repository.findByWorkType(wts);
  }
  
  @GetMapping("worktypes/all")
  Iterable<Permit> byAllTags(@RequestParam("types") Set<String> wts) {
    return repository.findByWorkTypeContainingAll(wts);
  }
  
  @GetMapping("building_type_and_description")
  Iterable<Permit> byBuildingTypeAndDescription(
      @RequestParam("buildingType") String buildingType, //
      @RequestParam("description") String description) {
    return repository.findByBuildingTypeAndDescription(buildingType, description);
  }
  
  @GetMapping("city_or_description")
  Iterable<Permit> byCityOrDescription(
      @RequestParam("city") String city, //
      @RequestParam("description") String description) {
    return repository.findByAddress_CityOrDescription(city, description);
  }
}
