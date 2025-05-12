package com.foogaro.modeling.config;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRedisDocumentRepositories(basePackages = "com.foogaro.*")
//@EnableRedisEnhancedRepositories(basePackages = "com.foogaro.*")
public class RedisConfiguration {

    @Value("${spring.data.redis.host}")
    private String host;
    @Value("${spring.data.redis.port}")
    private int port;
    @Value("${spring.data.redis.username}")
    private String username;
    @Value("${spring.data.redis.password}")
    private String password;

//    @Bean
//    public JedisConnectionFactory redisConnectionFactory() {
//        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
//        redisStandaloneConfiguration.setHostName(host);
//        redisStandaloneConfiguration.setPort(port);
//        redisStandaloneConfiguration.setUsername(username);
//        redisStandaloneConfiguration.setPassword(password);
//        JedisConnectionFactory jediConnectionFactory = new JedisConnectionFactory(redisStandaloneConfiguration);
//        jediConnectionFactory.setConvertPipelineAndTxResults(false);
//        return jediConnectionFactory;
//    }
//
//    @Bean
//    public RedisTemplate<String, String> redisTemplate() {
//
//        RedisTemplate<String, String> template = new RedisTemplate<>();
//        template.setConnectionFactory(redisConnectionFactory());
//        return template;
//    }

}