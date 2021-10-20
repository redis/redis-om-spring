package com.redis.hashes.repositories;

import java.util.List;

import com.redis.hashes.domain.User;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, String> {

  List<User> findByFirstNameAndLastName(String firstName, String lastName);

  List<User> findByMiddleNameContains(String firstName);

  List<User> findByRole_RoleName(String roleName);
}
