package com.redis.om.spring;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.Jedis;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ RedisOMProperties.class })
@ConditionalOnProperty(name = "redis.om.spring.authentication.entra-id.enabled", havingValue = "true", matchIfMissing = false)
public class EntraIDConfiguration {

  private static final Log logger = LogFactory.getLog(EntraIDConfiguration.class);

  @Value("${spring.data.redis.host}")
  private String host;
  @Value("${spring.data.redis.port}")
  private int port;
  @Value("${redis.om.spring.authentication.entra-id.enabled}")
  private String clientType;

  public EntraIDConfiguration() {
    logger.info("EntraIDConfiguration initialized");
    logger.info("Redis host: " + host);
    logger.info("Redis port: " + port);
    logger.info("Redis client type: " + clientType);
  }

  @Bean
  public JedisConnectionFactory jedisConnectionFactory() {
    logger.info("Creating JedisConnectionFactory for Entra ID authentication");
    
    // Create DefaultAzureCredential
    DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();
    TokenRequestContext trc = new TokenRequestContext().addScopes("https://redis.azure.com/.default");
    TokenRefreshCache tokenRefreshCache = new TokenRefreshCache(defaultAzureCredential, trc);
    AccessToken accessToken = tokenRefreshCache.getAccessToken();

    boolean useSsl = true;
    String token = accessToken.getToken();
    logger.trace("Token obtained successfully: \n" + token);
    String username = extractUsernameFromToken(token);
    logger.debug("Username extracted from token: " + username);

    JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(getRedisStandaloneConfiguration(username, token, useSsl));
    jedisConnectionFactory.setConvertPipelineAndTxResults(false);
    jedisConnectionFactory.setUseSsl(useSsl);
    logger.info("JedisConnectionFactory for EntraID created successfully");
    return jedisConnectionFactory;
  }

  private RedisStandaloneConfiguration getRedisStandaloneConfiguration(String username, String token, boolean useSsl) {
    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
    redisStandaloneConfiguration.setHostName(host);
    redisStandaloneConfiguration.setPort(port);
    redisStandaloneConfiguration.setUsername(username);
    redisStandaloneConfiguration.setPassword(token);
    return redisStandaloneConfiguration;
  }

  private String extractUsernameFromToken(String token) {
    // The token is a JWT, and the username is in the "sub" claim
    String[] parts = token.split("\\.");
    if (parts.length != 3) {
      throw new IllegalArgumentException("Invalid JWT token");
    }

    String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
    com.google.gson.JsonObject jsonObject = com.google.gson.JsonParser.parseString(payload).getAsJsonObject();
    return jsonObject.get("sub").getAsString();
  }

  /**
   * The token cache to store and proactively refresh the access token.
   */
  private class TokenRefreshCache {
    private final TokenCredential tokenCredential;
    private final TokenRequestContext tokenRequestContext;
    private final Timer timer;
    private volatile AccessToken accessToken;
    private final Duration maxRefreshOffset = Duration.ofMinutes(5);
    private final Duration baseRefreshOffset = Duration.ofMinutes(2);
    private Jedis jedisInstanceToAuthenticate;
    private String username;

    /**
     * Creates an instance of TokenRefreshCache
     * @param tokenCredential the token credential to be used for authentication.
     * @param tokenRequestContext the token request context to be used for authentication.
     */
    public TokenRefreshCache(TokenCredential tokenCredential, TokenRequestContext tokenRequestContext) {
      this.tokenCredential = tokenCredential;
      this.tokenRequestContext = tokenRequestContext;
      this.timer = new Timer();
    }

    /**
     * Gets the cached access token.
     * @return the AccessToken
     */
    public AccessToken getAccessToken() {
      if (accessToken != null) {
        return  accessToken;
      } else {
        TokenRefreshTask tokenRefreshTask = new TokenRefreshTask();
        accessToken = tokenCredential.getToken(tokenRequestContext).block();
        timer.schedule(tokenRefreshTask, getTokenRefreshDelay());
        return accessToken;
      }
    }

    private class TokenRefreshTask extends TimerTask {
      // Add your task here
      public void run() {
        accessToken = tokenCredential.getToken(tokenRequestContext).block();
        username = extractUsernameFromToken(accessToken.getToken());
        System.out.println("Refreshed Token with Expiry: " + accessToken.getExpiresAt().toEpochSecond());

        if (jedisInstanceToAuthenticate != null && !CoreUtils.isNullOrEmpty(username)) {
          jedisInstanceToAuthenticate.auth(username, accessToken.getToken());
          System.out.println("Refreshed Jedis Connection with fresh access token, token expires at : "
                  + accessToken.getExpiresAt().toEpochSecond());
        }
        timer.schedule(new TokenRefreshTask(), getTokenRefreshDelay());
      }
    }

    private long getTokenRefreshDelay() {
      return ((accessToken.getExpiresAt()
              .minusSeconds(ThreadLocalRandom.current().nextLong(baseRefreshOffset.getSeconds(), maxRefreshOffset.getSeconds()))
              .toEpochSecond() - OffsetDateTime.now().toEpochSecond()) * 1000);
    }

    /**
     * Sets the Jedis to proactively authenticate before token expiry.
     * @param jedisInstanceToAuthenticate the instance to authenticate
     * @return the updated instance
     */
    public TokenRefreshCache setJedisInstanceToAuthenticate(Jedis jedisInstanceToAuthenticate) {
      this.jedisInstanceToAuthenticate = jedisInstanceToAuthenticate;
      return this;
    }
  }
}