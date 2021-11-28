package com.redis.hashes.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.redis.hashes.domain.User;
import com.redis.hashes.repositories.UserRepository;

@RestController
@RequestMapping("/api/users")
public class UserController {

  @Autowired
  private UserRepository userRepository;

  @PostMapping("/")
  public User save(@RequestBody User user) {
    return userRepository.save(user);
  }
  
  @GetMapping("/q")
  public List<User> findByName(@RequestParam String firstName, @RequestParam String lastName) {
    return userRepository.findByFirstNameAndLastName(firstName, lastName);
  }
  
  @GetMapping("name/{lastName}")
  Optional<User> byName(@PathVariable("lastName") String lastName) {
    return userRepository.findOneByLastName(lastName);
  }
  
  @GetMapping("/exists/")
  boolean isEmailTaken(@RequestParam("email") String email) {
    return userRepository.existsByEmail(email);
  }

}
