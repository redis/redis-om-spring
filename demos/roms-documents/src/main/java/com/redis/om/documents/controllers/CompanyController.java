package com.redis.om.documents.controllers;

import com.redis.om.documents.domain.Company;
import com.redis.om.documents.repositories.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {
  @Autowired
  CompanyRepository repository;
  
  @GetMapping("employees/count/{count}")
  Iterable<Company> byNumberOfEmployees(@PathVariable("count") int count) {
    return repository.findByNumberOfEmployees(count);
  }
  
  @GetMapping("employees/range/{low}/{high}")
  Iterable<Company> byNumberOfEmployeesRange(@PathVariable("low") int low, @PathVariable("high") int high) {
    return repository.findByNumberOfEmployeesBetween(low, high);
  }
  
  @GetMapping("all")
  Page<Company> all(Pageable pageable) {
    return repository.findAll(pageable);
  }
  
  @GetMapping("all-ids")
  Page<String> allIds(Pageable pageable) {
    return repository.getIds(pageable);
  }

  @GetMapping("near")
  Iterable<Company> byLocationNear(//
      @RequestParam("lat") double lat, //
      @RequestParam("lon") double lon, //
      @RequestParam("d") double distance) {
    return repository.findByLocationNear(new Point(lon, lat), new Distance(distance, Metrics.MILES));
  }
  
  @GetMapping("name/starts/{prefix}")
  Iterable<Company> byNameStartingWith(@PathVariable("prefix") String prefix) {
    return repository.findByNameStartingWith(prefix);
  }

  @GetMapping("name/{name}")
  Optional<Company> byName(@PathVariable("name") String name) {
    return repository.findOneByName(name);
  }

  @GetMapping("tags")
  Iterable<Company> byTags(@RequestParam("tags") Set<String> tags) {
    return repository.findByTags(tags);
  }

  @GetMapping("{id}")
  Optional<Company> byId(@PathVariable("id") String id) {
    return repository.findById(id);
  }
}
