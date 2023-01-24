package com.redis.om.documents;

import java.util.Arrays;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.geo.Point;

import com.redis.om.documents.domain.Company;
import com.redis.om.documents.domain.CompanyMeta;
import com.redis.om.documents.domain.Person;
import com.redis.om.documents.repositories.CompanyRepository;
import com.redis.om.documents.repositories.PersonRepository;
import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;

@SpringBootApplication
@EnableRedisDocumentRepositories
public class RomsDocumentsApplication {

  @Autowired
  CompanyRepository companyRepo;

  @Autowired
  PersonRepository personRepo;
  
  @Bean
  public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
    return args -> {

      System.out.println("Let's inspect the beans provided by Spring Boot:");

      String[] beanNames = ctx.getBeanDefinitionNames();
      Arrays.sort(beanNames);
      for (String beanName : beanNames) {
        System.out.println(beanName);
      }
    };
  }    

  @Bean
  CommandLineRunner loadTestData() {
    return args -> {
      companyRepo.deleteAll();
      Company redis = Company.of("Redis", "https://redis.com", new Point(-122.066540, 37.377690), 526, 2011, Set.of(CompanyMeta.of("Redis", 100, Set.of("RedisTag"))));
      redis.setTags(Set.of("fast", "scalable", "reliable"));

      Company microsoft = Company.of("Microsoft", "https://microsoft.com", new Point(-122.124500, 47.640160), 182268, 1975, Set.of(CompanyMeta.of("MS", 50, Set.of("MsTag"))));
      microsoft.setTags(Set.of("innovative", "reliable"));

      companyRepo.save(redis);
      companyRepo.save(redis); // save again to test @LastModifiedDate
      companyRepo.save(microsoft);

      personRepo.deleteAll();
      personRepo.save(Person.of("Brian", "Sam-Bodden", "bsb@redis.com"));
      personRepo.save(Person.of("Guy", "Royse", "guy.royse@redis.com"));
      personRepo.save(Person.of("Guy", "Korland", "guy.korland@redis.com"));
    };
  }

  public static void main(String[] args) {
    SpringApplication.run(RomsDocumentsApplication.class, args);
  }

}
