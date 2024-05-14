package com.redis.om.permits;

import com.google.common.collect.Lists;
import com.redis.om.permits.models.Address;
import com.redis.om.permits.models.Attribute;
import com.redis.om.permits.models.Order;
import com.redis.om.permits.models.Permit;
import com.redis.om.permits.repositories.PermitRepository;
import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.geo.Point;

import java.util.List;
import java.util.Set;

@SpringBootApplication
@EnableRedisDocumentRepositories(basePackages = "com.redis.om.permits.*")
public class PermitsApplication {

  @Autowired
  PermitRepository repo;

  public static void main(String[] args) {
    SpringApplication.run(PermitsApplication.class, args);
  }

  @Bean
  CommandLineRunner loadTestData() {
    return args -> {
      repo.deleteAll();

      // # Document 1
      Address address1 = Address.of("Lisbon", "25 de Abril");
      Order order1 = Order.of("O11", 1.5);
      Order order2 = Order.of("O12", 5.6);
      Attribute attribute11 = Attribute.of("size", "S", Lists.newArrayList(order1));
      Attribute attribute12 = Attribute.of("size", "M", Lists.newArrayList(order2));
      List<Attribute> attrList1 = Lists.newArrayList(attribute11, attribute12);
      Permit permit1 = Permit.of( //
        address1, //
        "To construct a single detached house with a front covered veranda.", //
        "single detached house", //
        Set.of("demolition", "reconstruction"), //
        42000L, //
        new Point(38.7635877, -9.2018309), //
        List.of("started", "in_progress", "approved"), //
        attrList1);

      // # Document 2
      Address address2 = Address.of("Porto", "Av. da Liberdade");
      Order order21 = Order.of("O21", 1.2);
      Order order22 = Order.of("O22", 5.6);
      Attribute attribute21 = Attribute.of("color", "red", Lists.newArrayList(order21));
      Attribute attribute22 = Attribute.of("color", "blue", Lists.newArrayList(order22));
      List<Attribute> attrList2 = Lists.newArrayList(attribute21, attribute22);
      Permit permit2 = Permit.of( //
        address2, //
        "To construct a loft", //
        "apartment", //
        Set.of("construction"), //
        53000L, //
        new Point(38.7205373, -9.148091), //
        List.of("started", "in_progress", "rejected"), //
        attrList2);

      // # Document 3
      Address address3 = Address.of("Lagos", "D. João");
      Order order31 = Order.of("ABC", 1.6);
      Order order32 = Order.of("DEF", 1.3);
      Order order33 = Order.of("GHJ", 1.6);
      Order order34 = Order.of("VBN", 1.0);
      Attribute attribute31 = Attribute.of("brand", "A", Lists.newArrayList(order31, order33));
      Attribute attribute32 = Attribute.of("brand", "B", Lists.newArrayList(order32, order34));
      List<Attribute> attrList3 = Lists.newArrayList(attribute31, attribute32);
      Permit permit3 = Permit.of( //
        address3, //
        "New house build", //
        "house", //
        Set.of("construction", "design"), //
        260000L, //
        new Point(37.0990749, -8.6868258), //
        List.of("started", "in_progress", "postponed"), //
        attrList3);

      repo.saveAll(List.of(permit1, permit2, permit3));
    };
  }

}
