package com.redis.romsmultiaclaccount;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.data.redis.autoconfigure.DataRedisRepositoriesAutoConfiguration;

@SpringBootApplication(
    exclude = DataRedisRepositoriesAutoConfiguration.class
)
public class RomsMultiAclAccountApplication {

  public static void main(String[] args) {
    SpringApplication.run(RomsMultiAclAccountApplication.class, args);
  }

}
