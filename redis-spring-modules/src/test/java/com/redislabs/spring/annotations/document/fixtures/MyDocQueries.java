package com.redislabs.spring.annotations.document.fixtures;

import java.util.Optional;

public interface MyDocQueries {
  Optional<MyDoc> findByTitle(String title);
}
