package com.redis.om.amr.entraid.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.redis.om.amr.entraid.model.MyData;
import com.redis.om.amr.entraid.service.MyDataService;

@RestController
@RequestMapping(
  "/api/my-data"
)
public class MyDataController {

  @Autowired
  private MyDataService service;

  @PostMapping(
      path = "/"
  )
  public ResponseEntity<MyData> create(@RequestBody MyData myData) {
    return ResponseEntity.ok(service.save(myData));
  }

  @GetMapping(
      path = "/{id}"
  )
  public ResponseEntity<MyData> findById(@PathVariable String id) {
    return ResponseEntity.ok(service.findById(id));
  }

}
