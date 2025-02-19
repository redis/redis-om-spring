package com.redis.om.spring.vectorize;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.translator.ImageFeatureExtractor;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.Pipeline;
import ai.djl.translate.TranslateException;
import com.azure.ai.openai.OpenAIClient;
import com.redis.om.spring.RedisOMAiProperties;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.EmbeddingType;
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
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.ai.vertexai.embedding.VertexAiEmbeddingConnectionDetails;
import org.springframework.ai.vertexai.embedding.text.VertexAiTextEmbeddingModel;
import org.springframework.ai.vertexai.embedding.text.VertexAiTextEmbeddingOptions;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static com.redis.om.spring.annotations.EmbeddingType.SENTENCE;
import static com.redis.om.spring.util.ObjectUtils.byteArrayToFloatArray;
import static com.redis.om.spring.util.ObjectUtils.floatArrayToByteArray;

public class DefaultEmbedder implements Embedder {
  private static final Log logger = LogFactory.getLog(DefaultEmbedder.class);
  public final Pipeline imagePipeline;
  public final TransformersEmbeddingModel transformersEmbeddingModel;
  //public final HuggingFaceTokenizer sentenceTokenizer;
  private final ZooModel<Image, float[]> imageEmbeddingModel;
  private final ZooModel<Image, float[]> faceEmbeddingModel;
  private final ImageFactory imageFactory;
  private final ApplicationContext applicationContext;
  private final ImageFeatureExtractor imageFeatureExtractor;
  private final OpenAiEmbeddingModel defaultOpenAITextVectorizer;
  private final OllamaEmbeddingModel defaultOllamaEmbeddingModel;
  private final RedisOMAiProperties properties;
  private final OllamaApi ollamaApi;
  private final OpenAIClient azureOpenAIClient;
  private final VertexAiTextEmbeddingModel vertexAiTextEmbeddingModel;
  private final BedrockCohereEmbeddingModel bedrockCohereEmbeddingModel;
  private final BedrockTitanEmbeddingModel bedrockTitanEmbeddingModel;

  public DefaultEmbedder( //
      ApplicationContext applicationContext, //
      ZooModel<Image, float[]> imageEmbeddingModel, //
      ZooModel<Image, float[]> faceEmbeddingModel, //
      ImageFactory imageFactory, //
      Pipeline imagePipeline, //
      TransformersEmbeddingModel transformersEmbeddingModel, //
      OpenAiEmbeddingModel openAITextVectorizer, //
      OpenAIClient azureOpenAIClient, //
      VertexAiTextEmbeddingModel vertexAiTextEmbeddingModel, //
      BedrockCohereEmbeddingModel bedrockCohereEmbeddingModel, //
      BedrockTitanEmbeddingModel bedrockTitanEmbeddingModel, //
      RedisOMAiProperties properties //
  ) {
    this.applicationContext = applicationContext;
    this.imageEmbeddingModel = imageEmbeddingModel;
    this.faceEmbeddingModel = faceEmbeddingModel;
    this.imageFactory = imageFactory;
    this.imagePipeline = imagePipeline;
    this.transformersEmbeddingModel = transformersEmbeddingModel;

    // feature extractor
    this.imageFeatureExtractor = ImageFeatureExtractor.builder().setPipeline(imagePipeline).build();
    this.defaultOpenAITextVectorizer = openAITextVectorizer;
    this.azureOpenAIClient = azureOpenAIClient;
    this.vertexAiTextEmbeddingModel = vertexAiTextEmbeddingModel;
    this.bedrockCohereEmbeddingModel = bedrockCohereEmbeddingModel;
    this.bedrockTitanEmbeddingModel = bedrockTitanEmbeddingModel;
    this.properties = properties;

    this.ollamaApi = new OllamaApi(properties.getOllama().getBaseUrl());

    this.defaultOllamaEmbeddingModel = OllamaEmbeddingModel.builder().ollamaApi(this.ollamaApi)
        .defaultOptions(OllamaOptions.builder().model(OllamaModel.MISTRAL.id()).build()).build();
  }

  private List<byte[]> getImageEmbeddingsAsByteArrayFor(List<InputStream> isList) {
     var imgs = isList.stream().map(is -> {
         try {
           return imageFactory.fromInputStream(is);
         } catch (IOException e) {
           logger.warn("Error generating image embedding", e);
           return null;
         }
     }).toList();

     try {
       if (!imgs.contains(null)) {
         Predictor<Image, float[]> predictor = imageEmbeddingModel.newPredictor(imageFeatureExtractor);
         return predictor.batchPredict(imgs).stream().map(ObjectUtils::floatArrayToByteArray).toList();
       }
     } catch (TranslateException e) {
      logger.warn("Error generating image embedding", e);
     }

     return List.of();
  }

  private byte[] getImageEmbeddingsAsByteArrayFor(InputStream is) {
    try {
      var img = imageFactory.fromInputStream(is);
      Predictor<Image, float[]> predictor = imageEmbeddingModel.newPredictor(imageFeatureExtractor);
      return floatArrayToByteArray(predictor.predict(img));
    } catch (IOException | TranslateException e) {
      logger.warn("Error generating image embedding", e);
      return new byte[] {};
    }
  }

  private List<float[]> getImageEmbeddingsAsFloatArrayFor(List<InputStream> isList) {
    return getImageEmbeddingsAsByteArrayFor(isList).stream().map(ObjectUtils::byteArrayToFloatArray).toList();
  }

  private float[] getImageEmbeddingsAsFloatArrayFor(InputStream is) {
    return byteArrayToFloatArray(getImageEmbeddingsAsByteArrayFor(is));
  }

  private List<byte[]> getFacialImageEmbeddingsAsByteArrayFor(List<InputStream> isList) {
    return getFacialImageEmbeddingsAsFloatArrayFor(isList).stream().map(ObjectUtils::floatArrayToByteArray).toList();
  }

  private byte[] getFacialImageEmbeddingsAsByteArrayFor(InputStream is) throws IOException, TranslateException {
    return ObjectUtils.floatArrayToByteArray(getFacialImageEmbeddingsAsFloatArrayFor(is));
  }

  private List<float[]> getFacialImageEmbeddingsAsFloatArrayFor(List<InputStream> isList) {
    var imgs = isList.stream().map(is -> {
      try {
        return imageFactory.fromInputStream(is);
      } catch (IOException e) {
        logger.warn("Error generating face embedding", e);
        return null;
      }
    }).toList();

    if (!imgs.contains(null)) {
      try (Predictor<Image, float[]> predictor = faceEmbeddingModel.newPredictor()) {
        return predictor.batchPredict(imgs);
      } catch (TranslateException e) {
        logger.warn("Error generating face embedding", e);
      }
    }
    return List.of();
  }

  private float[] getFacialImageEmbeddingsAsFloatArrayFor(InputStream is) throws IOException, TranslateException {
    try (Predictor<Image, float[]> predictor = faceEmbeddingModel.newPredictor()) {
      var img = imageFactory.fromInputStream(is);
      return predictor.predict(img);
    }
  }

  private List<byte[]> getSentenceEmbeddingsAsByteArrayFor(List<String> texts) {
    List<float[]> encodings = transformersEmbeddingModel.embed(texts);
    return encodings.stream().map(ObjectUtils::floatArrayToByteArray).toList();
  }

  private List<float[]> getSentenceEmbeddingAsFloatArrayFor(List<String> texts) {
    return transformersEmbeddingModel.embed(texts);
  }

  private byte[] getSentenceEmbeddingsAsByteArrayFor(String text) {
    return ObjectUtils.floatArrayToByteArray(transformersEmbeddingModel.embed(text));
  }

  private float[] getSentenceEmbeddingAsFloatArrayFor(String text) {
    return transformersEmbeddingModel.embed(text);
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

  @Override
  public <S> void processEntities(Iterable<S> items) {
    if (!isReady()) {
      return;
    }

    int batchSize = 100; // TODO Replace with a propoerty
    List<FieldData> batch = new ArrayList<>(batchSize);

    for (Object item : items) {
      List<Field> fields = ObjectUtils.getFieldsWithAnnotation(item.getClass(), Vectorize.class);
      if (fields.isEmpty()) continue;

      PropertyAccessor accessor = PropertyAccessorFactory.forBeanPropertyAccess(item);
      boolean isDocument = item.getClass().isAnnotationPresent(Document.class);

      for (Field field : fields) {
        Vectorize vectorize = field.getAnnotation(Vectorize.class);
        Object fieldValue = accessor.getPropertyValue(field.getName());

        if (fieldValue != null) {
          batch.add(new FieldData(vectorize, item, field, accessor, fieldValue, isDocument));
        }

        if (batch.size() >= batchSize) {
          processBatch(batch);
          batch.clear();
        }
      }
    }

    // Process any remaining items
    if (!batch.isEmpty()) {
      processBatch(batch);
    }
  }

  private void processBatch(List<FieldData> batch) {
    batch.stream()
            .collect(Collectors.groupingBy(fd -> fd.vectorize().embeddingType()))
            .forEach(this::vectorizeBatch);
  }

  private void vectorizeBatch(EmbeddingType embeddingType, List<FieldData> fieldDataList) {
    switch (embeddingType) {
      case IMAGE -> processImageEmbedding(fieldDataList);
      case WORD -> {
        //TODO: implement me!
      }
      case FACE -> processFaceEmbedding(fieldDataList);
      case SENTENCE -> processSentencesEmbedding(fieldDataList);
    }
  }

  private void processImageEmbedding(List<FieldData> fieldDataList) {
    fieldDataList.stream()
            .collect(Collectors.groupingBy(FieldData::isDocument))
            .forEach((isDocument, groupedByIsDocument) ->
                    groupedByIsDocument.stream()
                            .collect(Collectors.groupingBy(FieldData::vectorize))
                            .forEach((vectorize, groupedByVectorize) -> {
                              List<?> embeddings = isDocument
                                      ? getImageEmbeddingsAsFloatArrayFor(mapValuesToResources(groupedByVectorize))
                                      : getImageEmbeddingsAsByteArrayFor(mapValuesToResources(groupedByVectorize));

                              if (embeddings != null) {
                                applyEmbeddings(groupedByVectorize, embeddings, vectorize);
                              }
                            })
            );
  }

  private List<InputStream> mapValuesToResources(List<FieldData> fieldDataList) {
    var resources = fieldDataList.stream().map(it -> {
        try {
            return applicationContext.getResource(it.value().toString()).getInputStream();
        } catch (IOException e) {
          logger.info("Error embedding image: ()" + it.value());
          return null;
        }
    }).toList();
    return resources.contains(null) ? List.of() : resources;
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

  private void processFaceEmbedding(List<FieldData> fieldDataList) {
    fieldDataList.stream()
            .collect(Collectors.groupingBy(FieldData::isDocument))
            .forEach((isDocument, groupedByIsDocument) ->
                    groupedByIsDocument.stream()
                            .collect(Collectors.groupingBy(FieldData::vectorize))
                            .forEach((vectorize, groupedByVectorize) -> {
                              List<?> embeddings = isDocument
                                      ? getFacialImageEmbeddingsAsFloatArrayFor(mapValuesToResources(groupedByVectorize))
                                      : getFacialImageEmbeddingsAsByteArrayFor(mapValuesToResources(groupedByVectorize));

                              if (embeddings != null) {
                                applyEmbeddings(groupedByVectorize, embeddings, vectorize);
                              }
                            })
            );
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

  private void processSentencesEmbedding(List<FieldData> fieldDataList) {
    fieldDataList.stream().collect(Collectors.groupingBy(it -> it.vectorize().provider()))
            .forEach((provider, groupedFieldDataList) -> {
              switch (provider) {
                case TRANSFORMERS -> processDjlSentenceEmbedding(groupedFieldDataList);
                case DJL -> {
                }
                case OPENAI -> processOpenAiSentenceEmbedding(groupedFieldDataList);
                case OLLAMA -> processOllamaSentenceEmbedding(groupedFieldDataList);
                case AZURE_OPENAI -> processAzureOpenAiSentenceEmbedding(groupedFieldDataList);
                case VERTEX_AI -> processVertexAiSentenceEmbedding(groupedFieldDataList);
                case AMAZON_BEDROCK_COHERE -> processBedrockCohereSentenceEmbedding(groupedFieldDataList);
                case AMAZON_BEDROCK_TITAN -> processBedrockTitanSentenceEmbedding(groupedFieldDataList);
              }
            });
  }

  private void processSentenceEmbedding(PropertyAccessor accessor, Vectorize vectorize, Object fieldValue,
      boolean isDocument) {
    switch (vectorize.provider()) {
      case TRANSFORMERS -> processDjlSentenceEmbedding(accessor, vectorize, fieldValue, isDocument);
      case DJL -> {
      }
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

  private void processDjlSentenceEmbedding(List<FieldData> fieldDataList) {
    fieldDataList.stream()
            .collect(Collectors.groupingBy(FieldData::isDocument))
            .forEach((isDocument, groupedByIsDocument) ->
                    groupedByIsDocument.stream()
                            .collect(Collectors.groupingBy(FieldData::vectorize))
                            .forEach((vectorize, groupedByVectorize) -> {
                              List<?> embeddings = isDocument
                                      ? getSentenceEmbeddingAsFloats(mapValues(groupedByVectorize), vectorize)
                                      : getSentenceEmbeddingAsBytes(mapValues(groupedByVectorize), vectorize);

                              if (embeddings != null) {
                                applyEmbeddings(groupedByVectorize, embeddings, vectorize);
                              }
                            })
            );
  }

  private List<String> mapValues(List<FieldData> fieldDataList) {
    return fieldDataList.stream().map(it -> it.value().toString()).toList();
  }

  private void applyEmbeddings(List<FieldData> fieldDataList, List<?> embeddings, Vectorize vectorize) {
    for (int i = 0; i < fieldDataList.size() && i < embeddings.size(); i++) {
      fieldDataList.get(i).accessor().setPropertyValue(vectorize.destination(), embeddings.get(i));
    }
  }

  private void processOpenAiSentenceEmbedding(List<FieldData> fieldDataList) {
    fieldDataList.stream()
            .collect(Collectors.groupingBy(FieldData::isDocument))
            .forEach((isDocument, groupedByIsDocument) ->
                    groupedByIsDocument.stream()
                            .collect(Collectors.groupingBy(FieldData::vectorize))
                            .forEach((vectorize, groupedByVectorize) -> {
                              OpenAiEmbeddingModel model = getOpenAiEmbeddingModel(vectorize);
                              List<?> embeddings = isDocument
                                      ? getEmbeddingAsFloatArrayFor(mapValues(groupedByVectorize), model)
                                      : getEmbeddingsAsByteArrayFor(mapValues(groupedByVectorize), model);

                              if (embeddings != null) {
                                applyEmbeddings(groupedByVectorize, embeddings, vectorize);
                              }
                            })
            );
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

  private void processOllamaSentenceEmbedding(List<FieldData> fieldDataList) {
    fieldDataList.stream()
            .collect(Collectors.groupingBy(FieldData::isDocument))
            .forEach((isDocument, groupedByIsDocument) ->
                    groupedByIsDocument.stream()
                            .collect(Collectors.groupingBy(FieldData::vectorize))
                            .forEach((vectorize, groupedByVectorize) -> {
                              OllamaEmbeddingModel model = getOllamaEmbeddingModel(vectorize);
                              List<?> embeddings = isDocument
                                      ? getEmbeddingAsFloatArrayFor(mapValues(groupedByVectorize), model)
                                      : getEmbeddingsAsByteArrayFor(mapValues(groupedByVectorize), model);

                              if (embeddings != null) {
                                applyEmbeddings(groupedByVectorize, embeddings, vectorize);
                              }
                            })
            );
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

  private void processAzureOpenAiSentenceEmbedding(List<FieldData> fieldDataList) {
    fieldDataList.stream()
            .collect(Collectors.groupingBy(FieldData::isDocument))
            .forEach((isDocument, groupedByIsDocument) ->
                    groupedByIsDocument.stream()
                            .collect(Collectors.groupingBy(FieldData::vectorize))
                            .forEach((vectorize, groupedByVectorize) -> {
                              AzureOpenAiEmbeddingModel model = getAzureOpenAiEmbeddingModel(vectorize);
                              List<?> embeddings = isDocument
                                      ? getEmbeddingAsFloatArrayFor(mapValues(groupedByVectorize), model)
                                      : getEmbeddingsAsByteArrayFor(mapValues(groupedByVectorize), model);

                              if (embeddings != null) {
                                applyEmbeddings(groupedByVectorize, embeddings, vectorize);
                              }
                            })
            );
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

  private void processVertexAiSentenceEmbedding(List<FieldData> fieldDataList) {
    fieldDataList.stream()
            .collect(Collectors.groupingBy(FieldData::isDocument))
            .forEach((isDocument, groupedByIsDocument) ->
                    groupedByIsDocument.stream()
                            .collect(Collectors.groupingBy(FieldData::vectorize))
                            .forEach((vectorize, groupedByVectorize) -> {
                              VertexAiTextEmbeddingModel model = getVertexAiPaLm2EmbeddingModel(vectorize);
                              List<?> embeddings = isDocument
                                      ? getEmbeddingAsFloatArrayFor(mapValues(groupedByVectorize), model)
                                      : getEmbeddingsAsByteArrayFor(mapValues(groupedByVectorize), model);

                              if (embeddings != null) {
                                applyEmbeddings(groupedByVectorize, embeddings, vectorize);
                              }
                            })
            );
  }

  private void processVertexAiSentenceEmbedding(PropertyAccessor accessor, Vectorize vectorize, Object fieldValue,
      boolean isDocument) {
    VertexAiTextEmbeddingModel model = getVertexAiPaLm2EmbeddingModel(vectorize);
    if (isDocument) {
      accessor.setPropertyValue(vectorize.destination(), getEmbeddingAsFloatArrayFor(fieldValue.toString(), model));
    } else {
      accessor.setPropertyValue(vectorize.destination(), getEmbeddingsAsByteArrayFor(fieldValue.toString(), model));
    }
  }

  private void processBedrockCohereSentenceEmbedding(List<FieldData> fieldDataList) {
    fieldDataList.stream()
            .collect(Collectors.groupingBy(FieldData::isDocument))
            .forEach((isDocument, groupedByIsDocument) ->
                    groupedByIsDocument.stream()
                            .collect(Collectors.groupingBy(FieldData::vectorize))
                            .forEach((vectorize, groupedByVectorize) -> {
                              BedrockCohereEmbeddingModel model = getBedrockCohereEmbeddingModel(vectorize);
                              List<?> embeddings = isDocument
                                      ? getEmbeddingAsFloatArrayFor(mapValues(groupedByVectorize), model)
                                      : getEmbeddingsAsByteArrayFor(mapValues(groupedByVectorize), model);

                              if (embeddings != null) {
                                applyEmbeddings(groupedByVectorize, embeddings, vectorize);
                              }
                            })
            );
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

  private void processBedrockTitanSentenceEmbedding(List<FieldData> fieldDataList) {
    fieldDataList.stream()
            .collect(Collectors.groupingBy(FieldData::isDocument))
            .forEach((isDocument, groupedByIsDocument) ->
                    groupedByIsDocument.stream()
                            .collect(Collectors.groupingBy(FieldData::vectorize))
                            .forEach((vectorize, groupedByVectorize) -> {
                              BedrockTitanEmbeddingModel model = getBedrockTitanEmbeddingModel(vectorize);
                              List<?> embeddings = isDocument
                                      ? getEmbeddingAsFloatArrayFor(mapValues(groupedByVectorize), model)
                                      : getEmbeddingsAsByteArrayFor(mapValues(groupedByVectorize), model);

                              if (embeddings != null) {
                                applyEmbeddings(groupedByVectorize, embeddings, vectorize);
                              }
                            })
            );
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
          OpenAiEmbeddingOptions.builder().model(vectorize.openAiEmbeddingModel().getValue()).build(),
          RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }
    return this.defaultOpenAITextVectorizer;
  }

  private OllamaEmbeddingModel getOllamaEmbeddingModel(Vectorize vectorize) {
    if (!vectorize.ollamaEmbeddingModel().id().equals(OllamaModel.MISTRAL.id())) {

      OllamaOptions options = OllamaOptions.builder().model(vectorize.ollamaEmbeddingModel()).truncate(false).build();

      return OllamaEmbeddingModel.builder().ollamaApi(this.ollamaApi).defaultOptions(options).build();
    }
    return this.defaultOllamaEmbeddingModel;
  }

  private AzureOpenAiEmbeddingModel getAzureOpenAiEmbeddingModel(Vectorize vectorize) {
    AzureOpenAiEmbeddingOptions options = AzureOpenAiEmbeddingOptions.builder()
        .deploymentName(vectorize.azureOpenAiDeploymentName()).build();
    return new AzureOpenAiEmbeddingModel(this.azureOpenAIClient, MetadataMode.EMBED, options);
  }

  private VertexAiTextEmbeddingModel getVertexAiPaLm2EmbeddingModel(Vectorize vectorize) {
    if (!vectorize.vertexAiPaLm2ApiModel().equals(VertexAiTextEmbeddingOptions.DEFAULT_MODEL_NAME)) {
      VertexAiEmbeddingConnectionDetails connectionDetails = VertexAiEmbeddingConnectionDetails.builder()
          .projectId(properties.getVertexAi().getProjectId()).location(properties.getVertexAi().getLocation()).build();

      VertexAiTextEmbeddingOptions options = VertexAiTextEmbeddingOptions.builder()
          .model(VertexAiTextEmbeddingOptions.DEFAULT_MODEL_NAME).build();

      return new VertexAiTextEmbeddingModel(connectionDetails, options);
    }
    return this.vertexAiTextEmbeddingModel;
  }

  private BedrockCohereEmbeddingModel getBedrockCohereEmbeddingModel(Vectorize vectorize) {
    if (!vectorize.cohereEmbeddingModel().equals(CohereEmbeddingModel.COHERE_EMBED_MULTILINGUAL_V3)) {
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
    //return this.faceEmbeddingModel != null && this.transformersEmbeddingModel != null;
    return true;
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
      case TRANSFORMERS -> getSentenceEmbeddingsAsByteArrayFor(texts);
      case DJL -> Collections.emptyList();
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
        VertexAiTextEmbeddingModel model = getVertexAiPaLm2EmbeddingModel(vectorize);
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
      case TRANSFORMERS -> getSentenceEmbeddingAsFloatArrayFor(texts);
      case DJL -> Collections.emptyList(); //TODO what to do here?
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
        VertexAiTextEmbeddingModel model = getVertexAiPaLm2EmbeddingModel(vectorize);
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

record FieldData(Vectorize vectorize, Object item, Field field, PropertyAccessor accessor, Object value, boolean isDocument) {}
