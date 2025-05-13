package com.redis.om.amr.entraid.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.redis.om.amr.entraid.model.MyData;
import com.redis.om.amr.entraid.repository.MyDataRepository;

@Service
public class MyDataService {

  @Autowired
  private MyDataRepository repository;

  public MyData save(MyData myData) {
    return repository.save(myData);
  }

  public MyData findById(String id) {
    return repository.findById(id).orElse(null);
  }

}
