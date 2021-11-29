package com.redis.om.hashes.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.redis.om.hashes.domain.User;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
  
  Optional<User> findOneByLastName(String lastName);

  List<User> findByFirstNameAndLastName(String firstName, String lastName);
  
  boolean existsByEmail(String email);
}
