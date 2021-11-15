package com.redis.hashes.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.redis.hashes.domain.Role;

@Repository
public interface RoleRepository extends CrudRepository<Role, String> {
}
