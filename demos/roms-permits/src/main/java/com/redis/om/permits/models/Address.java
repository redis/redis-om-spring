package com.redis.om.permits.models;

import com.redis.om.spring.annotations.Indexed;
import com.redis.om.spring.annotations.Searchable;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/*
"address": {
  "street":"Bennelong Point",
  "city":"Sydney"
},

with an index like:

   $.address.street AS address TEXT NOSTEM
   $.address.city AS city TAG
*/
@Data
@RequiredArgsConstructor(staticName = "of")
public class Address {
  @NonNull
  @Indexed
  private String city;

  @NonNull
  @Searchable(nostem = true)
  private String street;
}
