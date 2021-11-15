package com.redis.hashes;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redis.hashes.domain.Role;
import com.redis.hashes.domain.User;
import com.redis.hashes.repositories.RoleRepository;
import com.redis.hashes.repositories.UserRepository;
import com.redis.om.spring.annotations.EnableRedisEnhancedRepositories;

@SpringBootApplication
@Configuration
@EnableRedisEnhancedRepositories(basePackages = "com.redis.hashes.*")
public class RdsHashesApplication {
  
  @Autowired
  private UserRepository userRepo;
  
  @SuppressWarnings("unused")
  @Autowired
  private RoleRepository roleRepo;
  
  @Bean
  CommandLineRunner loadTestData() {
    return args -> {
      Role bass = Role.of("BASS");
      Role vocals = Role.of("VOCALS");
      Role guitar = Role.of("GUITAR");
      Role drums = Role.of("DRUMS");
      
      //TODO: handle @Reference deserialization
      //roleRepo.saveAll(List.of(bass, vocals, guitar, drums));
      
      User john = User.of("Zack", "de la Rocha", bass);
      User tim = User.of("Tim", "Commerford", vocals);
      User tom = User.of("Tom", "Morello", guitar);
      User brad = User.of("Brad", "Wilk", drums);
      
      userRepo.saveAll(List.of(john, tim, tom, brad));
    };
  }

	public static void main(String[] args) {
		SpringApplication.run(RdsHashesApplication.class, args);
	}

}
