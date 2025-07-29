package com.redis.om.spring.annotations.document;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.fixtures.document.model.AddressWithNumericIndexed;
import com.redis.om.spring.fixtures.document.model.ValidDocumentIndexedNestedWithNumericIndexed;
import com.redis.om.spring.fixtures.document.model.ValidDocumentIndexedNestedWithNumericIndexed$;
import com.redis.om.spring.fixtures.document.repository.ValidDocumentIndexedNestedWithNumericIndexedRepository;
import com.redis.om.spring.search.stream.EntityStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class NumericIndexedNestedTest extends AbstractBaseDocumentTest {
  
  @Autowired
  ValidDocumentIndexedNestedWithNumericIndexedRepository repository;
  
  @Autowired
  EntityStream entityStream;

  @BeforeEach
  void setup() {
    repository.deleteAll();
  }

  @Test
  void testNestedNumericIndexedMetamodelGeneration() {
    // Test that the metamodel is generated correctly for nested @NumericIndexed fields
    
    // Create test data
    ValidDocumentIndexedNestedWithNumericIndexed doc = ValidDocumentIndexedNestedWithNumericIndexed.of("doc1");
    AddressWithNumericIndexed address = new AddressWithNumericIndexed();
    address.setStreet("123 Main St");
    address.setCity("Anytown");
    address.setZipCode(12345);
    doc.setAddress(address);
    
    repository.save(doc);
    
    // Test that we can query using the metamodel
    // This should work if the metamodel is generated correctly
    List<ValidDocumentIndexedNestedWithNumericIndexed> results = entityStream
        .of(ValidDocumentIndexedNestedWithNumericIndexed.class)
        .filter(ValidDocumentIndexedNestedWithNumericIndexed$.ADDRESS_ZIP_CODE.eq(12345))
        .collect(Collectors.toList());
    
    assertThat(results).hasSize(1);
    assertThat(results.get(0).getAddress().getZipCode()).isEqualTo(12345);
  }
}