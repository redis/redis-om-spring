package com.redis.om.spring;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import redis.clients.jedis.Jedis;

/**
 * Configuration class for Microsoft Entra ID (formerly Azure Active Directory) authentication with Redis.
 * <p>
 * This configuration automatically sets up Redis connections using Entra ID authentication
 * when the property {@code redis.om.spring.authentication.entra-id.enabled} is set to {@code true}.
 * It handles token acquisition, refresh, and authentication with Azure Redis instances.
 * </p>
 * 
 * @since 1.0.0
 */
@Configuration(
    proxyBeanMethods = false
)
@EnableConfigurationProperties(
  { RedisOMProperties.class }
)
@ConditionalOnProperty(
    name = "redis.om.spring.authentication.entra-id.enabled", havingValue = "true", matchIfMissing = false
)
public class EntraIDConfiguration {

  private static final Log logger = LogFactory.getLog(EntraIDConfiguration.class);

  @Value(
    "${spring.data.redis.host}"
  )
  private String host;
  @Value(
    "${spring.data.redis.port}"
  )
  private int port;
  @Value(
    "${redis.om.spring.authentication.entra-id.enabled}"
  )
  private String clientType;

  /**
   * Constructs a new EntraIDConfiguration instance.
   * Logs the initialization and configuration details for debugging purposes.
   */
  public EntraIDConfiguration() {
    logger.info("EntraIDConfiguration initialized");
    logger.info("Redis host: " + host);
    logger.info("Redis port: " + port);
    logger.info("Redis client type: " + clientType);
  }

  /**
   * Creates and configures a JedisConnectionFactory with Entra ID authentication.
   * <p>
   * This method sets up the Jedis connection factory using Azure credentials
   * obtained through the DefaultAzureCredential chain. It handles token acquisition,
   * username extraction from JWT tokens, and SSL configuration.
   * </p>
   * 
   * @return a configured JedisConnectionFactory with Entra ID authentication
   */
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

    JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(getRedisStandaloneConfiguration(username,
        token, useSsl));
    jedisConnectionFactory.setConvertPipelineAndTxResults(false);
    jedisConnectionFactory.setUseSsl(useSsl);
    logger.info("JedisConnectionFactory for EntraID created successfully");
    return jedisConnectionFactory;
  }

  /**
   * Creates a Redis standalone configuration with the provided credentials.
   * 
   * @param username the username extracted from the JWT token
   * @param token    the access token for authentication
   * @param useSsl   whether to use SSL for the connection
   * @return a configured RedisStandaloneConfiguration
   */
  private RedisStandaloneConfiguration getRedisStandaloneConfiguration(String username, String token, boolean useSsl) {
    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
    redisStandaloneConfiguration.setHostName(host);
    redisStandaloneConfiguration.setPort(port);
    redisStandaloneConfiguration.setUsername(username);
    redisStandaloneConfiguration.setPassword(token);
    return redisStandaloneConfiguration;
  }

  /**
   * Extracts the username from a JWT access token.
   * <p>
   * This method parses the JWT token and extracts the subject (sub) claim
   * which contains the username for Redis authentication.
   * </p>
   * 
   * @param token the JWT access token
   * @return the username extracted from the token's subject claim
   * @throws IllegalArgumentException if the token format is invalid
   */
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
   * Internal cache for storing and proactively refreshing access tokens.
   * <p>
   * This class manages the lifecycle of Azure access tokens, including
   * automatic refresh before expiration and re-authentication of Jedis connections.
   * </p>
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
     * Creates an instance of TokenRefreshCache.
     * 
     * @param tokenCredential     the token credential to be used for authentication
     * @param tokenRequestContext the token request context to be used for authentication
     */
    public TokenRefreshCache(TokenCredential tokenCredential, TokenRequestContext tokenRequestContext) {
      this.tokenCredential = tokenCredential;
      this.tokenRequestContext = tokenRequestContext;
      this.timer = new Timer();
    }

    /**
     * Gets the cached access token, requesting a new one if none exists.
     * <p>
     * If no token is cached, this method will request a new token and
     * schedule a refresh task for automatic token renewal.
     * </p>
     * 
     * @return the cached or newly acquired AccessToken
     */
    public AccessToken getAccessToken() {
      if (accessToken != null) {
        return accessToken;
      } else {
        TokenRefreshTask tokenRefreshTask = new TokenRefreshTask();
        accessToken = tokenCredential.getToken(tokenRequestContext).block();
        timer.schedule(tokenRefreshTask, getTokenRefreshDelay());
        return accessToken;
      }
    }

    /**
     * Timer task responsible for refreshing access tokens before they expire.
     */
    private class TokenRefreshTask extends TimerTask {
      /**
       * Executes the token refresh operation.
       * <p>
       * This method acquires a new access token, updates the username,
       * and re-authenticates any existing Jedis connections.
       * </p>
       */
      public void run() {
        accessToken = tokenCredential.getToken(tokenRequestContext).block();
        username = extractUsernameFromToken(accessToken.getToken());
        System.out.println("Refreshed Token with Expiry: " + accessToken.getExpiresAt().toEpochSecond());

        if (jedisInstanceToAuthenticate != null && !CoreUtils.isNullOrEmpty(username)) {
          jedisInstanceToAuthenticate.auth(username, accessToken.getToken());
          System.out.println("Refreshed Jedis Connection with fresh access token, token expires at : " + accessToken
              .getExpiresAt().toEpochSecond());
        }
        timer.schedule(new TokenRefreshTask(), getTokenRefreshDelay());
      }
    }

    /**
     * Calculates the delay in milliseconds until the next token refresh.
     * <p>
     * The delay is calculated based on the token expiration time minus
     * a random offset to prevent thundering herd problems.
     * </p>
     * 
     * @return the delay in milliseconds until the next refresh
     */
    private long getTokenRefreshDelay() {
      return ((accessToken.getExpiresAt().minusSeconds(ThreadLocalRandom.current().nextLong(baseRefreshOffset
          .getSeconds(), maxRefreshOffset.getSeconds())).toEpochSecond() - OffsetDateTime.now()
              .toEpochSecond()) * 1000);
    }

    /**
     * Sets the Jedis instance to proactively authenticate before token expiry.
     * <p>
     * When a Jedis instance is set, it will be automatically re-authenticated
     * whenever the access token is refreshed.
     * </p>
     * 
     * @param jedisInstanceToAuthenticate the Jedis instance to authenticate
     * @return this TokenRefreshCache instance for method chaining
     */
    public TokenRefreshCache setJedisInstanceToAuthenticate(Jedis jedisInstanceToAuthenticate) {
      this.jedisInstanceToAuthenticate = jedisInstanceToAuthenticate;
      return this;
    }
  }
}