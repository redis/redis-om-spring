package com.redis.om.permits;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.geo.Point;

import com.redis.om.permits.models.Address;
import com.redis.om.permits.models.Permit;
import com.redis.om.permits.repositories.PermitRepository;
import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;

@SpringBootApplication
@EnableRedisDocumentRepositories(basePackages = "com.redis.om.permits.*")
public class PermitsApplication {

  @Autowired
  PermitRepository repo;

  @Bean
  CommandLineRunner loadTestData() {
    return args -> {
      repo.deleteAll();

      // # Document 1
      Address address1 = Address.of("Lisbon", "25 de Abril");
      Permit permit1 = Permit.of( //
          address1, //
          "To construct a single detached house with a front covered veranda.", //
          "single detached house", //
          Set.of("demolition", "reconstruction"), //
          42000L, //
          new Point(38.7635877,-9.2018309), //
          List.of("started", "in_progress", "approved") //
      );

      // # Document 2
      Address address2 = Address.of("Porto", "Av. da Liberdade");
      Permit permit2 = Permit.of( //
          address2, //
          "To construct a loft", //
          "apartment", //
          Set.of("construction"), //
          53000L, //
          new Point(38.7205373,-9.148091), //
          List.of("started", "in_progress", "rejected") //
      );
      
      // # Document 3
      Address address3 = Address.of("Lagos", "D. João");
      Permit permit3 = Permit.of( //
          address3, //
          "New house build", //
          "house", //
          Set.of("construction", "design"), //
          260000L, //
          new Point(37.0990749,-8.6868258), //
          List.of("started", "in_progress", "postponed") //
      );
 
      repo.saveAll(List.of(permit1, permit2, permit3));
    };
  }

  public static void main(String[] args) {
    SpringApplication.run(PermitsApplication.class, args);
  }

}
