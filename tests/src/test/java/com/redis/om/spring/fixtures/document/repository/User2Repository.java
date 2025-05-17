package com.redis.om.spring.fixtures.document.repository;

import org.springframework.data.repository.query.Param;

import com.redis.om.spring.annotations.Query;
import com.redis.om.spring.fixtures.document.model.User2;
import com.redis.om.spring.repository.RedisDocumentRepository;

@SuppressWarnings(
  "unused"
)
public interface User2Repository extends RedisDocumentRepository<User2, String> {

  @Query(
    "(@name:{$name}) (@address:{$address}) (@addressComplement:{$addressComp})"
  )
  Iterable<User2> findUser( //
      @Param(
        "name"
      ) String name, //
      @Param(
        "address"
      ) String strAdd, //
      @Param(
        "addressComp"
      ) String strAddComp //
  );
}
