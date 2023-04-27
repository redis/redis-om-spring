package com.redis.om.hashes;

import com.redis.om.hashes.domain.Role;
import com.redis.om.hashes.domain.User;
import com.redis.om.hashes.repositories.RoleRepository;
import com.redis.om.hashes.repositories.UserRepository;
import com.redis.om.spring.annotations.EnableRedisEnhancedRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
@EnableRedisEnhancedRepositories(basePackages = "com.redis.om.hashes.*")
public class RomsHashesApplication {

  @Autowired
  private UserRepository userRepo;

  @Autowired
  private RoleRepository roleRepo;

  @Bean
  CommandLineRunner loadTestData() {
    return args -> {
      Role bass = Role.of("BASS");
      Role vocals = Role.of("VOCALS");
      Role guitar = Role.of("GUITAR");
      Role drums = Role.of("DRUMS");

      roleRepo.saveAll(List.of(bass, vocals, guitar, drums));

      User john = User.of("Zack", "de la Rocha", "zack@ratm.com", bass);
      User tim = User.of("Tim", "Commerford", "tim@ratm.com", vocals);
      User tom = User.of("Tom", "Morello", "tom@ratm.com", guitar);
      User brad = User.of("Brad", "Wilk", "brad@ratm.com", drums);

      userRepo.saveAll(List.of(john, tim, tom, brad));
      userRepo.save(tom); // save again to trigger @LastModifiedDate
    };
  }

	public static void main(String[] args) {
		SpringApplication.run(RomsHashesApplication.class, args);
	}

}
