package com.redis.om.hashes.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.redis.om.hashes.domain.Role;

@Repository
public interface RoleRepository extends CrudRepository<Role, String> {
}
