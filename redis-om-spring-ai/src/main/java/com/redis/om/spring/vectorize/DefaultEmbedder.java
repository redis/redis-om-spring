package com.redis.om.spring.vectorize;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.translator.ImageFeatureExtractor;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.Pipeline;
import ai.djl.translate.TranslateException;
import com.redis.om.spring.AIRedisOMProperties;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.EmbeddingType;
import com.redis.om.spring.annotations.Vectorize;
import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.util.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingModel;
import org.springframework.ai.bedrock.cohere.BedrockCohereEmbeddingModel;
import org.springframework.ai.bedrock.titan.BedrockTitanEmbeddingModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.ai.vertexai.embedding.text.VertexAiTextEmbeddingModel;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.redis.om.spring.annotations.EmbeddingType.SENTENCE;
import static com.redis.om.spring.util.ObjectUtils.byteArrayToFloatArray;
import static com.redis.om.spring.util.ObjectUtils.floatArrayToByteArray;

public class DefaultEmbedder implements Embedder {
  private static final Log logger = LogFactory.getLog(DefaultEmbedder.class);
  private final EmbeddingModelFactory embeddingModelFactory;
  public final Pipeline imagePipeline;
  //public final HuggingFaceTokenizer sentenceTokenizer;
  private final ZooModel<Image, float[]> imageEmbeddingModel;
  private final ZooModel<Image, float[]> faceEmbeddingModel;
  private final ImageFactory imageFactory;
  private final ApplicationContext applicationContext;
  private final ImageFeatureExtractor imageFeatureExtractor;
  private final AIRedisOMProperties properties;

  public DefaultEmbedder( //
                          ApplicationContext applicationContext, //
                          EmbeddingModelFactory embeddingModelFactory, //
                          ZooModel<Image, float[]> imageEmbeddingModel, //
                          ZooModel<Image, float[]> faceEmbeddingModel, //
                          ImageFactory imageFactory, //
                          Pipeline imagePipeline, //
                          AIRedisOMProperties properties //
  ) {
    this.applicationContext = applicationContext;
    this.embeddingModelFactory = embeddingModelFactory;
    this.imageEmbeddingModel = imageEmbeddingModel;
    this.faceEmbeddingModel = faceEmbeddingModel;
    this.imageFactory = imageFactory;
    this.imagePipeline = imagePipeline;

    // feature extractor
    this.imageFeatureExtractor = ImageFeatureExtractor.builder().setPipeline(imagePipeline).build();
    this.properties = properties;
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

  private List<byte[]> getEmbeddingsAsByteArrayFor(List<String> texts, EmbeddingModel model) {
    return model.embed(texts).stream().map(ObjectUtils::floatArrayToByteArray).toList();
  }

  private List<float[]> getEmbeddingAsFloatArrayFor(List<String> texts, EmbeddingModel model) {
    return model.embed(texts);
  }

  private byte[] getEmbeddingsAsByteArrayFor(String text, EmbeddingModel model) {
    return ObjectUtils.floatArrayToByteArray(model.embed(text));
  }

  private float[] getEmbeddingAsFloatArrayFor(String text, EmbeddingModel model) {
    return model.embed(text);
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

    int batchSize = properties.getEmbeddingBatchSize();
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
                case TRANSFORMERS -> processSentenceEmbedding(groupedFieldDataList, this::getTransformersEmbeddingModel);
                case DJL -> {
                }
                case OPENAI -> processSentenceEmbedding(groupedFieldDataList, this::getOpenAiEmbeddingModel);
                case OLLAMA -> processSentenceEmbedding(groupedFieldDataList, this::getOllamaEmbeddingModel);
                case AZURE_OPENAI -> processSentenceEmbedding(groupedFieldDataList, this::getAzureOpenAiEmbeddingModel);
                case VERTEX_AI -> processSentenceEmbedding(groupedFieldDataList, this::getVertexAiEmbeddingModel);
                case AMAZON_BEDROCK_COHERE -> processSentenceEmbedding(groupedFieldDataList, this::getBedrockCohereEmbeddingModel);
                case AMAZON_BEDROCK_TITAN -> processSentenceEmbedding(groupedFieldDataList, this::getBedrockTitanEmbeddingModel);
              }
            });
  }

  private void processSentenceEmbedding(PropertyAccessor accessor, Vectorize vectorize, Object fieldValue,
      boolean isDocument) {
    switch (vectorize.provider()) {
      case TRANSFORMERS -> processSentenceEmbedding(accessor, vectorize, fieldValue, isDocument, this::getTransformersEmbeddingModel);
      case DJL -> {
      }
      case OPENAI -> processSentenceEmbedding(accessor, vectorize, fieldValue, isDocument, this::getOpenAiEmbeddingModel);
      case OLLAMA -> processSentenceEmbedding(accessor, vectorize, fieldValue, isDocument, this::getOllamaEmbeddingModel);
      case AZURE_OPENAI -> processSentenceEmbedding(accessor, vectorize, fieldValue, isDocument, this::getAzureOpenAiEmbeddingModel);
      case VERTEX_AI -> processSentenceEmbedding(accessor, vectorize, fieldValue, isDocument, this::getVertexAiEmbeddingModel);
      case AMAZON_BEDROCK_COHERE -> processSentenceEmbedding(accessor, vectorize, fieldValue, isDocument, this::getBedrockCohereEmbeddingModel);
      case AMAZON_BEDROCK_TITAN -> processSentenceEmbedding(accessor, vectorize, fieldValue, isDocument, this::getBedrockTitanEmbeddingModel);
    }
  }

  private List<String> mapValues(List<FieldData> fieldDataList) {
    return fieldDataList.stream().map(it -> it.value().toString()).toList();
  }

  private void applyEmbeddings(List<FieldData> fieldDataList, List<?> embeddings, Vectorize vectorize) {
    for (int i = 0; i < fieldDataList.size() && i < embeddings.size(); i++) {
      fieldDataList.get(i).accessor().setPropertyValue(vectorize.destination(), embeddings.get(i));
    }
  }

  private void processSentenceEmbedding(List<FieldData> fieldDataList, Function<Vectorize, EmbeddingModel> modelFunction) {
    fieldDataList.stream()
            .collect(Collectors.groupingBy(FieldData::isDocument))
            .forEach((isDocument, groupedByIsDocument) ->
                    groupedByIsDocument.stream()
                            .collect(Collectors.groupingBy(FieldData::vectorize))
                            .forEach((vectorize, groupedByVectorize) -> {
                              EmbeddingModel model = modelFunction.apply(vectorize);
                              List<?> embeddings = isDocument
                                      ? getEmbeddingAsFloatArrayFor(mapValues(groupedByVectorize), model)
                                      : getEmbeddingsAsByteArrayFor(mapValues(groupedByVectorize), model);

                              if (embeddings != null) {
                                applyEmbeddings(groupedByVectorize, embeddings, vectorize);
                              }
                            })
            );
  }

  private void processSentenceEmbedding(PropertyAccessor accessor, Vectorize vectorize, Object fieldValue,
                                        boolean isDocument, Function<Vectorize, EmbeddingModel> modelFunction) {
    EmbeddingModel model = modelFunction.apply(vectorize);
    if (isDocument) {
      accessor.setPropertyValue(vectorize.destination(), getEmbeddingAsFloatArrayFor(fieldValue.toString(), model));
    } else {
      accessor.setPropertyValue(vectorize.destination(), getEmbeddingsAsByteArrayFor(fieldValue.toString(), model));
    }
  }

  private TransformersEmbeddingModel getTransformersEmbeddingModel(Vectorize vectorize) {
    return embeddingModelFactory.createTransformersEmbeddingModel(vectorize);
  }

  private OpenAiEmbeddingModel getOpenAiEmbeddingModel(Vectorize vectorize) {
    return embeddingModelFactory.createOpenAiEmbeddingModel(vectorize.openAiEmbeddingModel());
  }

  private OllamaEmbeddingModel getOllamaEmbeddingModel(Vectorize vectorize) {
    return embeddingModelFactory.createOllamaEmbeddingModel(vectorize.ollamaEmbeddingModel().id());
  }

  private AzureOpenAiEmbeddingModel getAzureOpenAiEmbeddingModel(Vectorize vectorize) {
    return embeddingModelFactory.createAzureOpenAiEmbeddingModel(vectorize.azureOpenAiDeploymentName());
  }

  private VertexAiTextEmbeddingModel getVertexAiEmbeddingModel(Vectorize vectorize) {
    return embeddingModelFactory.createVertexAiTextEmbeddingModel(vectorize.vertexAiApiModel());
  }

  private BedrockCohereEmbeddingModel getBedrockCohereEmbeddingModel(Vectorize vectorize) {
    return embeddingModelFactory.createCohereEmbeddingModel(vectorize.cohereEmbeddingModel().id());
  }

  private BedrockTitanEmbeddingModel getBedrockTitanEmbeddingModel(Vectorize vectorize) {
    return embeddingModelFactory.createTitanEmbeddingModel(vectorize.titanEmbeddingModel().id());
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
      case TRANSFORMERS -> {
        TransformersEmbeddingModel model = getTransformersEmbeddingModel(vectorize);
        yield getEmbeddingsAsByteArrayFor(texts, model);
      }
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
        VertexAiTextEmbeddingModel model = getVertexAiEmbeddingModel(vectorize);
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
      case TRANSFORMERS -> {
        TransformersEmbeddingModel model = getTransformersEmbeddingModel(vectorize);
        yield getEmbeddingAsFloatArrayFor(texts, model);
      }
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
        VertexAiTextEmbeddingModel model = getVertexAiEmbeddingModel(vectorize);
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
