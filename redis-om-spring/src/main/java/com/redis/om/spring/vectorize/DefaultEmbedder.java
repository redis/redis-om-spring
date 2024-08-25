package com.redis.om.spring.vectorize;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.translator.ImageFeatureExtractor;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.Pipeline;
import ai.djl.translate.TranslateException;
import com.azure.ai.openai.OpenAIClient;
import com.redis.om.spring.RedisOMProperties;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Vectorize;
import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.util.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingModel;
import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingOptions;
import org.springframework.ai.bedrock.cohere.BedrockCohereEmbeddingModel;
import org.springframework.ai.bedrock.cohere.api.CohereEmbeddingBedrockApi;
import org.springframework.ai.bedrock.cohere.api.CohereEmbeddingBedrockApi.CohereEmbeddingModel;
import org.springframework.ai.bedrock.titan.BedrockTitanEmbeddingModel;
import org.springframework.ai.bedrock.titan.api.TitanEmbeddingBedrockApi;
import org.springframework.ai.bedrock.titan.api.TitanEmbeddingBedrockApi.TitanEmbeddingModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.vertexai.palm2.VertexAiPaLm2EmbeddingModel;
import org.springframework.ai.vertexai.palm2.api.VertexAiPaLm2Api;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.web.client.RestClient;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.redis.om.spring.annotations.EmbeddingType.SENTENCE;
import static com.redis.om.spring.util.ObjectUtils.byteArrayToFloatArray;
import static com.redis.om.spring.util.ObjectUtils.longArrayToFloatArray;

public class DefaultEmbedder implements Embedder {
  private static final Log logger = LogFactory.getLog(DefaultEmbedder.class);
  public final Pipeline imagePipeline;
  public final HuggingFaceTokenizer sentenceTokenizer;
  private final ZooModel<Image, byte[]> imageEmbeddingModel;
  private final ZooModel<Image, float[]> faceEmbeddingModel;
  private final ImageFactory imageFactory;
  private final ApplicationContext applicationContext;
  private final ImageFeatureExtractor imageFeatureExtractor;
  private final OpenAiEmbeddingModel defaultOpenAITextVectorizer;
  private final OllamaEmbeddingModel defaultOllamaEmbeddingModel;
  private final RedisOMProperties properties;
  private final OllamaApi ollamaApi;
  private final OpenAIClient azureOpenAIClient;
  private final VertexAiPaLm2EmbeddingModel vertexAiPaLm2EmbeddingModel;
  private final BedrockCohereEmbeddingModel bedrockCohereEmbeddingModel;
  private final BedrockTitanEmbeddingModel bedrockTitanEmbeddingModel;

  public DefaultEmbedder( //
      ApplicationContext applicationContext, //
      ZooModel<Image, byte[]> imageEmbeddingModel, //
      ZooModel<Image, float[]> faceEmbeddingModel, //
      ImageFactory imageFactory, //
      Pipeline imagePipeline, //
      HuggingFaceTokenizer sentenceTokenizer, //
      OpenAiEmbeddingModel openAITextVectorizer, //
      OpenAIClient azureOpenAIClient, //
      VertexAiPaLm2EmbeddingModel vertexAiPaLm2EmbeddingModel, //
      BedrockCohereEmbeddingModel bedrockCohereEmbeddingModel, //
      BedrockTitanEmbeddingModel bedrockTitanEmbeddingModel, //
      RedisOMProperties properties //
  ) {
    this.applicationContext = applicationContext;
    this.imageEmbeddingModel = imageEmbeddingModel;
    this.faceEmbeddingModel = faceEmbeddingModel;
    this.imageFactory = imageFactory;
    this.imagePipeline = imagePipeline;
    this.sentenceTokenizer = sentenceTokenizer;

    // feature extractor
    this.imageFeatureExtractor = ImageFeatureExtractor.builder().setPipeline(imagePipeline).build();
    this.defaultOpenAITextVectorizer = openAITextVectorizer;
    this.azureOpenAIClient = azureOpenAIClient;
    this.vertexAiPaLm2EmbeddingModel = vertexAiPaLm2EmbeddingModel;
    this.bedrockCohereEmbeddingModel = bedrockCohereEmbeddingModel;
    this.bedrockTitanEmbeddingModel = bedrockTitanEmbeddingModel;
    this.properties = properties;

    this.ollamaApi = new OllamaApi(properties.getOllama().getBaseUrl());

    this.defaultOllamaEmbeddingModel = new OllamaEmbeddingModel(ollamaApi,
        new OllamaOptions().withModel(OllamaOptions.DEFAULT_MODEL));
  }

  private byte[] getImageEmbeddingsAsByteArrayFor(InputStream is) {
    try {
      var img = imageFactory.fromInputStream(is);
      Predictor<Image, byte[]> predictor = imageEmbeddingModel.newPredictor(imageFeatureExtractor);
      return predictor.predict(img);
    } catch (IOException | TranslateException e) {
      logger.warn("Error generating image embedding", e);
      return new byte[] {};
    }
  }

  private float[] getImageEmbeddingsAsFloatArrayFor(InputStream is) {
    return byteArrayToFloatArray(getImageEmbeddingsAsByteArrayFor(is));
  }

  private byte[] getFacialImageEmbeddingsAsByteArrayFor(InputStream is) throws IOException, TranslateException {
    return ObjectUtils.floatArrayToByteArray(getFacialImageEmbeddingsAsFloatArrayFor(is));
  }

  private float[] getFacialImageEmbeddingsAsFloatArrayFor(InputStream is) throws IOException, TranslateException {
    try (Predictor<Image, float[]> predictor = faceEmbeddingModel.newPredictor()) {
      var img = imageFactory.fromInputStream(is);
      return predictor.predict(img);
    }
  }

  private List<byte[]> getSentenceEmbeddingsAsByteArrayFor(List<String> texts) {
    Encoding[] encodings = sentenceTokenizer.batchEncode(texts);
    return Arrays.stream(encodings).map(e -> ObjectUtils.longArrayToByteArray(e.getIds())).toList();
  }

  private List<float[]> getSentenceEmbeddingAsFloatArrayFor(List<String> texts) {
    Encoding[] encodings = sentenceTokenizer.batchEncode(texts);
    return Arrays.stream(encodings).map(e -> ObjectUtils.longArrayToFloatArray(e.getIds())).toList();
  }

  private byte[] getSentenceEmbeddingsAsByteArrayFor(String text) {
    Encoding encoding = sentenceTokenizer.encode(text);
    return ObjectUtils.longArrayToByteArray(encoding.getIds());
  }

  private float[] getSentenceEmbeddingAsFloatArrayFor(String text) {
    Encoding encoding = sentenceTokenizer.encode(text);
    return longArrayToFloatArray(encoding.getIds());
  }

  private List<byte[]> getEmbeddingsAsByteArrayFor(List<String> texts, EmbeddingModel model) {
    EmbeddingResponse embeddingResponse = model.embedForResponse(texts);
    List<Embedding> embeddings = embeddingResponse.getResults();

    return embeddings.stream().map(e -> ObjectUtils.floatArrayToByteArray(e.getOutput())).toList();
  }

  private List<float[]> getEmbeddingAsFloatArrayFor(List<String> texts, EmbeddingModel model) {
    EmbeddingResponse embeddingResponse = model.embedForResponse(texts);
    List<Embedding> embeddings = embeddingResponse.getResults();
    return embeddings.stream().map(Embedding::getOutput).toList();
  }

  private byte[] getEmbeddingsAsByteArrayFor(String text, EmbeddingModel model) {
    EmbeddingResponse embeddingResponse = model.embedForResponse(List.of(text));
    Embedding embedding = embeddingResponse.getResult();
    return ObjectUtils.floatArrayToByteArray(embedding.getOutput());
  }

  private float[] getEmbeddingAsFloatArrayFor(String text, EmbeddingModel model) {
    EmbeddingResponse embeddingResponse = model.embedForResponse(List.of(text));
    Embedding embedding = embeddingResponse.getResult();
    return embedding.getOutput();
  }

  @Override
  public void processEntity(Object item) {
    if (!isReady()) {
      return;
    }
    List<Field> fields = ObjectUtils.getFieldsWithAnnotation(item.getClass(), Vectorize.class);
    if (!fields.isEmpty()) {
      PropertyAccessor accessor = PropertyAccessorFactory.forBeanPropertyAccess(item);
      fields.forEach(f -> {
        Vectorize vectorize = f.getAnnotation(Vectorize.class);
        Object fieldValue = accessor.getPropertyValue(f.getName());
        boolean isDocument = item.getClass().isAnnotationPresent(Document.class);

        if (fieldValue != null) {
          switch (vectorize.embeddingType()) {
            case IMAGE -> processImageEmbedding(accessor, vectorize, fieldValue, isDocument);
            case WORD -> {
              //TODO: implement me!
            }
            case FACE -> processFaceEmbedding(accessor, vectorize, fieldValue, isDocument);
            case SENTENCE -> processSentenceEmbedding(accessor, vectorize, fieldValue, isDocument);
          }
        }
      });
    }
  }

  private void processImageEmbedding(PropertyAccessor accessor, Vectorize vectorize, Object fieldValue,
      boolean isDocument) {
    Resource resource = applicationContext.getResource(fieldValue.toString());
    try {
      if (isDocument) {
        accessor.setPropertyValue(vectorize.destination(),
            getImageEmbeddingsAsFloatArrayFor(resource.getInputStream()));
      } else {
        accessor.setPropertyValue(vectorize.destination(), getImageEmbeddingsAsByteArrayFor(resource.getInputStream()));
      }
    } catch (IOException e) {
      logger.warn("Error generating image embedding", e);
    }
  }

  private void processFaceEmbedding(PropertyAccessor accessor, Vectorize vectorize, Object fieldValue,
      boolean isDocument) {
    Resource resource = applicationContext.getResource(fieldValue.toString());
    try {
      if (isDocument) {
        accessor.setPropertyValue(vectorize.destination(),
            getFacialImageEmbeddingsAsFloatArrayFor(resource.getInputStream()));
      } else {
        accessor.setPropertyValue(vectorize.destination(),
            getFacialImageEmbeddingsAsByteArrayFor(resource.getInputStream()));
      }
    } catch (IOException | TranslateException e) {
      logger.warn("Error generating facial image embedding", e);
    }
  }

  private void processSentenceEmbedding(PropertyAccessor accessor, Vectorize vectorize, Object fieldValue,
      boolean isDocument) {
    switch (vectorize.provider()) {
      case DJL -> processDjlSentenceEmbedding(accessor, vectorize, fieldValue, isDocument);
      case OPENAI -> processOpenAiSentenceEmbedding(accessor, vectorize, fieldValue, isDocument);
      case OLLAMA -> processOllamaSentenceEmbedding(accessor, vectorize, fieldValue, isDocument);
      case AZURE_OPENAI -> processAzureOpenAiSentenceEmbedding(accessor, vectorize, fieldValue, isDocument);
      case VERTEX_AI -> processVertexAiSentenceEmbedding(accessor, vectorize, fieldValue, isDocument);
      case AMAZON_BEDROCK_COHERE -> processBedrockCohereSentenceEmbedding(accessor, vectorize, fieldValue, isDocument);
      case AMAZON_BEDROCK_TITAN -> processBedrockTitanSentenceEmbedding(accessor, vectorize, fieldValue, isDocument);
    }
  }

  private void processDjlSentenceEmbedding(PropertyAccessor accessor, Vectorize vectorize, Object fieldValue,
      boolean isDocument) {
    if (isDocument) {
      accessor.setPropertyValue(vectorize.destination(), getSentenceEmbeddingAsFloatArrayFor(fieldValue.toString()));
    } else {
      accessor.setPropertyValue(vectorize.destination(), getSentenceEmbeddingsAsByteArrayFor(fieldValue.toString()));
    }
  }

  private void processOpenAiSentenceEmbedding(PropertyAccessor accessor, Vectorize vectorize, Object fieldValue,
      boolean isDocument) {
    OpenAiEmbeddingModel model = getOpenAiEmbeddingModel(vectorize);
    if (isDocument) {
      accessor.setPropertyValue(vectorize.destination(), getEmbeddingAsFloatArrayFor(fieldValue.toString(), model));
    } else {
      accessor.setPropertyValue(vectorize.destination(), getEmbeddingsAsByteArrayFor(fieldValue.toString(), model));
    }
  }

  private void processOllamaSentenceEmbedding(PropertyAccessor accessor, Vectorize vectorize, Object fieldValue,
      boolean isDocument) {
    OllamaEmbeddingModel model = getOllamaEmbeddingModel(vectorize);
    if (isDocument) {
      accessor.setPropertyValue(vectorize.destination(), getEmbeddingAsFloatArrayFor(fieldValue.toString(), model));
    } else {
      accessor.setPropertyValue(vectorize.destination(), getEmbeddingsAsByteArrayFor(fieldValue.toString(), model));
    }
  }

  private void processAzureOpenAiSentenceEmbedding(PropertyAccessor accessor, Vectorize vectorize, Object fieldValue,
      boolean isDocument) {
    AzureOpenAiEmbeddingModel model = getAzureOpenAiEmbeddingModel(vectorize);
    if (isDocument) {
      accessor.setPropertyValue(vectorize.destination(), getEmbeddingAsFloatArrayFor(fieldValue.toString(), model));
    } else {
      accessor.setPropertyValue(vectorize.destination(), getEmbeddingsAsByteArrayFor(fieldValue.toString(), model));
    }
  }

  private void processVertexAiSentenceEmbedding(PropertyAccessor accessor, Vectorize vectorize, Object fieldValue,
      boolean isDocument) {
    VertexAiPaLm2EmbeddingModel model = getVertexAiPaLm2EmbeddingModel(vectorize);
    if (isDocument) {
      accessor.setPropertyValue(vectorize.destination(), getEmbeddingAsFloatArrayFor(fieldValue.toString(), model));
    } else {
      accessor.setPropertyValue(vectorize.destination(), getEmbeddingsAsByteArrayFor(fieldValue.toString(), model));
    }
  }

  private void processBedrockCohereSentenceEmbedding(PropertyAccessor accessor, Vectorize vectorize, Object fieldValue,
      boolean isDocument) {
    BedrockCohereEmbeddingModel model = getBedrockCohereEmbeddingModel(vectorize);
    if (isDocument) {
      accessor.setPropertyValue(vectorize.destination(), getEmbeddingAsFloatArrayFor(fieldValue.toString(), model));
    } else {
      accessor.setPropertyValue(vectorize.destination(), getEmbeddingsAsByteArrayFor(fieldValue.toString(), model));
    }
  }

  private void processBedrockTitanSentenceEmbedding(PropertyAccessor accessor, Vectorize vectorize, Object fieldValue,
      boolean isDocument) {
    BedrockTitanEmbeddingModel model = getBedrockTitanEmbeddingModel(vectorize);
    if (isDocument) {
      accessor.setPropertyValue(vectorize.destination(), getEmbeddingAsFloatArrayFor(fieldValue.toString(), model));
    } else {
      accessor.setPropertyValue(vectorize.destination(), getEmbeddingsAsByteArrayFor(fieldValue.toString(), model));
    }
  }

  private OpenAiEmbeddingModel getOpenAiEmbeddingModel(Vectorize vectorize) {
    if (vectorize.openAiEmbeddingModel() != OpenAiApi.EmbeddingModel.TEXT_EMBEDDING_ADA_002) {
      var openAiApi = new OpenAiApi(properties.getOpenAi().getApiKey());
      return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED,
          OpenAiEmbeddingOptions.builder().withModel(vectorize.openAiEmbeddingModel().getValue()).build(),
          RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }
    return this.defaultOpenAITextVectorizer;
  }

  private OllamaEmbeddingModel getOllamaEmbeddingModel(Vectorize vectorize) {
    if (!vectorize.ollamaEmbeddingModel().id().equals(OllamaOptions.DEFAULT_MODEL)) {
      return new OllamaEmbeddingModel(ollamaApi, new OllamaOptions().withModel(vectorize.ollamaEmbeddingModel().id()));
    }
    return this.defaultOllamaEmbeddingModel;
  }

  private AzureOpenAiEmbeddingModel getAzureOpenAiEmbeddingModel(Vectorize vectorize) {
    AzureOpenAiEmbeddingOptions options = AzureOpenAiEmbeddingOptions.builder()
        .withDeploymentName(vectorize.azureOpenAiDeploymentName()).build();
    return new AzureOpenAiEmbeddingModel(this.azureOpenAIClient, MetadataMode.EMBED, options);
  }

  private VertexAiPaLm2EmbeddingModel getVertexAiPaLm2EmbeddingModel(Vectorize vectorize) {
    if (!vectorize.vertexAiPaLm2ApiModel().equals(VertexAiPaLm2Api.DEFAULT_EMBEDDING_MODEL)) {
      VertexAiPaLm2Api vertexAiApi = new VertexAiPaLm2Api(properties.getVertexAi().getEndPoint(),
          properties.getVertexAi().getApiKey(), VertexAiPaLm2Api.DEFAULT_GENERATE_MODEL,
          vectorize.vertexAiPaLm2ApiModel(), RestClient.builder());
      return new VertexAiPaLm2EmbeddingModel(vertexAiApi);
    }
    return this.vertexAiPaLm2EmbeddingModel;
  }

  private BedrockCohereEmbeddingModel getBedrockCohereEmbeddingModel(Vectorize vectorize) {
    if (!vectorize.cohereEmbeddingModel().equals(CohereEmbeddingModel.COHERE_EMBED_MULTILINGUAL_V1)) {
      AwsCredentials credentials = AwsBasicCredentials.create(properties.getBedrockCohere().getAccessKey(),
          properties.getBedrockCohere().getSecretKey());
      var cohereEmbeddingApi = new CohereEmbeddingBedrockApi(vectorize.cohereEmbeddingModel().id(),
          StaticCredentialsProvider.create(credentials), properties.getBedrockCohere().getRegion(),
          ModelOptionsUtils.OBJECT_MAPPER);
      return new BedrockCohereEmbeddingModel(cohereEmbeddingApi);
    }
    return this.bedrockCohereEmbeddingModel;
  }

  private BedrockTitanEmbeddingModel getBedrockTitanEmbeddingModel(Vectorize vectorize) {
    if (!vectorize.titanEmbeddingModel().equals(TitanEmbeddingModel.TITAN_EMBED_IMAGE_V1)) {
      AwsCredentials credentials = AwsBasicCredentials.create(properties.getBedrockCohere().getAccessKey(),
          properties.getBedrockCohere().getSecretKey());
      var titanEmbeddingApi = new TitanEmbeddingBedrockApi(vectorize.cohereEmbeddingModel().id(),
          StaticCredentialsProvider.create(credentials), properties.getBedrockTitan().getRegion(),
          ModelOptionsUtils.OBJECT_MAPPER, Duration.ofMinutes(5L));
      return new BedrockTitanEmbeddingModel(titanEmbeddingApi);
    }
    return this.bedrockTitanEmbeddingModel;
  }

  @Override
  public boolean isReady() {
    return this.faceEmbeddingModel != null && this.sentenceTokenizer != null;
  }

  @Override
  public List<byte[]> getTextEmbeddingsAsBytes(List<String> texts, Field field) {
    if (field.isAnnotationPresent(Vectorize.class)) {
      Vectorize vectorize = field.getAnnotation(Vectorize.class);
      return vectorize.embeddingType() == SENTENCE ?
          getSentenceEmbeddingAsBytes(texts, vectorize) :
          Collections.emptyList();
    } else {
      return Collections.emptyList();
    }
  }

  private List<byte[]> getSentenceEmbeddingAsBytes(List<String> texts, Vectorize vectorize) {
    return switch (vectorize.provider()) {
      case DJL -> getSentenceEmbeddingsAsByteArrayFor(texts);
      case OPENAI -> {
        OpenAiEmbeddingModel model = getOpenAiEmbeddingModel(vectorize);
        yield getEmbeddingsAsByteArrayFor(texts, model);
      }
      case OLLAMA -> {
        OllamaEmbeddingModel model = getOllamaEmbeddingModel(vectorize);
        yield getEmbeddingsAsByteArrayFor(texts, model);
      }
      case AZURE_OPENAI -> {
        AzureOpenAiEmbeddingModel model = getAzureOpenAiEmbeddingModel(vectorize);
        yield getEmbeddingsAsByteArrayFor(texts, model);
      }
      case VERTEX_AI -> {
        VertexAiPaLm2EmbeddingModel model = getVertexAiPaLm2EmbeddingModel(vectorize);
        yield getEmbeddingsAsByteArrayFor(texts, model);
      }
      case AMAZON_BEDROCK_COHERE -> {
        BedrockCohereEmbeddingModel model = getBedrockCohereEmbeddingModel(vectorize);
        yield getEmbeddingsAsByteArrayFor(texts, model);
      }
      case AMAZON_BEDROCK_TITAN -> {
        BedrockTitanEmbeddingModel model = getBedrockTitanEmbeddingModel(vectorize);
        yield getEmbeddingsAsByteArrayFor(texts, model);
      }
    };
  }

  private List<float[]> getSentenceEmbeddingAsFloats(List<String> texts, Vectorize vectorize) {
    return switch (vectorize.provider()) {
      case DJL -> getSentenceEmbeddingAsFloatArrayFor(texts);
      case OPENAI -> {
        OpenAiEmbeddingModel model = getOpenAiEmbeddingModel(vectorize);
        yield getEmbeddingAsFloatArrayFor(texts, model);
      }
      case OLLAMA -> {
        OllamaEmbeddingModel model = getOllamaEmbeddingModel(vectorize);
        yield getEmbeddingAsFloatArrayFor(texts, model);
      }
      case AZURE_OPENAI -> {
        AzureOpenAiEmbeddingModel model = getAzureOpenAiEmbeddingModel(vectorize);
        yield getEmbeddingAsFloatArrayFor(texts, model);
      }
      case VERTEX_AI -> {
        VertexAiPaLm2EmbeddingModel model = getVertexAiPaLm2EmbeddingModel(vectorize);
        yield getEmbeddingAsFloatArrayFor(texts, model);
      }
      case AMAZON_BEDROCK_COHERE -> {
        BedrockCohereEmbeddingModel model = getBedrockCohereEmbeddingModel(vectorize);
        yield getEmbeddingAsFloatArrayFor(texts, model);
      }
      case AMAZON_BEDROCK_TITAN -> {
        BedrockTitanEmbeddingModel model = getBedrockTitanEmbeddingModel(vectorize);
        yield getEmbeddingAsFloatArrayFor(texts, model);
      }
    };
  }

  @Override
  public List<float[]> getTextEmbeddingsAsFloats(List<String> texts, Field field) {
    if (field.isAnnotationPresent(Vectorize.class)) {
      Vectorize vectorize = field.getAnnotation(Vectorize.class);
      return vectorize.embeddingType() == SENTENCE ?
          getSentenceEmbeddingAsFloats(texts, vectorize) :
          Collections.emptyList();
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public List<byte[]> getTextEmbeddingsAsBytes(List<String> texts, MetamodelField<?, ?> metamodelField) {
    return getTextEmbeddingsAsBytes(texts, metamodelField.getSearchFieldAccessor().getField());
  }

  @Override
  public List<float[]> getTextEmbeddingsAsFloats(List<String> texts, MetamodelField<?, ?> metamodelField) {
    return getTextEmbeddingsAsFloats(texts, metamodelField.getSearchFieldAccessor().getField());
  }
}
