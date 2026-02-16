package com.redis.om.spring.vectorize;

import java.time.Duration;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Compatibility layer for OpenAI embeddings on Spring Framework 7.
 *
 * <p>Spring AI 1.0.1 was compiled against Spring Framework 6 where {@code HttpHeaders}
 * implemented {@code MultiValueMap}. In Spring Framework 7, {@code HttpHeaders} no longer
 * implements {@code MultiValueMap}, causing {@code NoSuchMethodError} at runtime in
 * {@code OpenAiApi} for both {@code addAll(MultiValueMap)} and {@code containsKey(Object)}.
 *
 * <p>This class provides a compatible {@code EmbeddingModel} implementation that calls the
 * OpenAI REST API directly with a properly-built {@code RestClient}, bypassing the broken
 * {@code OpenAiApi} class entirely.
 *
 * <p>TODO: Remove this class when upgrading to Spring AI 2.0+ which natively supports
 * Spring Framework 7.
 */
final class OpenAiCompat {

  /**
   * Runtime detection: {@code true} when {@code HttpHeaders} no longer implements
   * {@code MultiValueMap}, meaning we are on Spring Framework 7+ and need the
   * compatibility path.
   */
  static final boolean NEEDED = !MultiValueMap.class.isAssignableFrom(HttpHeaders.class);

  private static final String DEFAULT_BASE_URL = "https://api.openai.com";

  private OpenAiCompat() {
  }

  /**
   * Creates a Spring Framework 7 compatible {@code EmbeddingModel} for OpenAI.
   *
   * @param apiKey             OpenAI API key
   * @param model              Model identifier (e.g., "text-embedding-ada-002")
   * @param readTimeoutSeconds Timeout for the HTTP read in seconds
   * @return A configured {@code EmbeddingModel} that calls OpenAI directly
   */
  static org.springframework.ai.embedding.EmbeddingModel createEmbeddingModel(String apiKey, String model,
      long readTimeoutSeconds) {
    return new DirectOpenAiEmbeddingModel(apiKey, model, readTimeoutSeconds);
  }

  /**
   * {@code EmbeddingModel} implementation that calls OpenAI's {@code /v1/embeddings}
   * endpoint directly using {@code RestClient} with Spring 7 compatible header methods.
   */
  private static final class DirectOpenAiEmbeddingModel implements org.springframework.ai.embedding.EmbeddingModel {

    private final RestClient restClient;
    private final String model;

    DirectOpenAiEmbeddingModel(String apiKey, String model, long readTimeoutSeconds) {
      this.model = model;

      SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
      factory.setReadTimeout(Duration.ofSeconds(readTimeoutSeconds));

      this.restClient = RestClient.builder().baseUrl(DEFAULT_BASE_URL).requestFactory(factory).defaultHeaders(
          headers -> {
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
          }).build();
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
      List<String> inputs = request.getInstructions();
      String requestModel = this.model;

      if (request.getOptions() != null && request.getOptions().getModel() != null) {
        requestModel = request.getOptions().getModel();
      }

      ApiRequest apiRequest = new ApiRequest(inputs, requestModel);

      ApiResponse apiResponse = restClient.post().uri("/v1/embeddings").body(apiRequest).retrieve().body(
          ApiResponse.class);

      if (apiResponse == null || apiResponse.data() == null) {
        return new EmbeddingResponse(List.of());
      }

      List<Embedding> embeddings = apiResponse.data().stream().map(d -> new Embedding(d.embedding(), d.index()))
          .toList();

      return new EmbeddingResponse(embeddings);
    }

    @Override
    public float[] embed(Document document) {
      return embed(document.getText());
    }
  }

  // --- JSON request/response records for OpenAI /v1/embeddings ---
  // Defined here to avoid loading OpenAiApi's inner types which reference
  // the broken HttpHeaders methods.

  record ApiRequest(@JsonProperty(
    "input"
  ) List<String> input, @JsonProperty(
    "model"
  ) String model) {
  }

  record ApiResponse(@JsonProperty(
    "data"
  ) List<EmbeddingData> data, @JsonProperty(
    "model"
  ) String model, @JsonProperty(
    "usage"
  ) ApiUsage usage) {
  }

  record EmbeddingData(@JsonProperty(
    "embedding"
  ) float[] embedding, @JsonProperty(
    "index"
  ) int index) {
  }

  record ApiUsage(@JsonProperty(
    "prompt_tokens"
  ) int promptTokens, @JsonProperty(
    "total_tokens"
  ) int totalTokens) {
  }
}
