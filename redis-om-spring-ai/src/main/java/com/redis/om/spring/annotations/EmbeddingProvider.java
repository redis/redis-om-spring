package com.redis.om.spring.annotations;

/**
 * Enumeration of supported embedding providers for automatic vectorization in Redis OM Spring.
 * <p>
 * This enum defines the various AI and machine learning providers that can be used to generate
 * embeddings from text, images, and other data types. These embeddings are used for vector
 * similarity search operations in Redis.
 * </p>
 * 
 * @see Vectorize
 * @see EmbeddingType
 * @see com.redis.om.spring.vectorize.EmbeddingModelFactory
 * @since 1.0.0
 */
public enum EmbeddingProvider {
  /**
   * Hugging Face Transformers provider for local embedding generation.
   * Supports various pre-trained models for text and sentence embeddings.
   */
  TRANSFORMERS,
  /**
   * Deep Java Library (DJL) provider for embedding generation.
   * Provides Java-native deep learning capabilities with support for multiple frameworks.
   */
  DJL,
  /**
   * OpenAI API provider for embedding generation.
   * Uses OpenAI's text embedding models (e.g., text-embedding-ada-002).
   */
  OPENAI,
  /**
   * Ollama provider for local LLM-based embedding generation.
   * Supports running various open-source models locally.
   */
  OLLAMA,
  /**
   * Azure OpenAI Service provider for embedding generation.
   * Uses OpenAI models hosted on Microsoft Azure.
   */
  AZURE_OPENAI,
  /**
   * Google Vertex AI provider for embedding generation.
   * Uses Google's text embedding models on Google Cloud Platform.
   */
  VERTEX_AI,
  /**
   * Amazon Bedrock provider using Cohere's embedding models.
   * Provides access to Cohere's multilingual embedding models through AWS.
   */
  AMAZON_BEDROCK_COHERE,
  /**
   * Amazon Bedrock provider using Amazon Titan embedding models.
   * Supports both text and image embeddings through AWS-native models.
   */
  AMAZON_BEDROCK_TITAN
}