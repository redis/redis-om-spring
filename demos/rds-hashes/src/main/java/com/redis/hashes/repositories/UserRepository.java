package com.redis.hashes.repositories;

import java.util.List;
import java.util.Optional;

import com.redis.hashes.domain.User;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
  
  Optional<User> findOneByLastName(String lastName);

  List<User> findByFirstNameAndLastName(String firstName, String lastName);

  List<User> findByMiddleNameContains(String middleName);

  List<User> findByRole_RoleName(String roleName);
}
