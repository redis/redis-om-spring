package com.redis.om.spring.vectorize;

import static com.redis.om.spring.annotations.EmbeddingType.SENTENCE;
import static com.redis.om.spring.util.ObjectUtils.byteArrayToFloatArray;
import static com.redis.om.spring.util.ObjectUtils.floatArrayToByteArray;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ai.azure.openai.AzureOpenAiEmbeddingModel;
import org.springframework.ai.bedrock.cohere.BedrockCohereEmbeddingModel;
import org.springframework.ai.bedrock.titan.BedrockTitanEmbeddingModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.ai.vertexai.embedding.text.VertexAiTextEmbeddingModel;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import com.redis.om.spring.AIRedisOMProperties;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.EmbeddingType;
import com.redis.om.spring.annotations.Vectorize;
import com.redis.om.spring.metamodel.MetamodelField;
import com.redis.om.spring.util.ObjectUtils;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.translator.ImageFeatureExtractor;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.Pipeline;
import ai.djl.translate.TranslateException;

/**
 * Default implementation of the {@link Embedder} interface that provides comprehensive embedding capabilities
 * for text and image data using various AI providers.
 * 
 * <p>This class serves as the central hub for generating vector embeddings from different types of data:
 * <ul>
 * <li>Text embeddings using providers like OpenAI, Azure OpenAI, Ollama, Vertex AI, AWS Bedrock, and Transformers</li>
 * <li>Image embeddings for general image content</li>
 * <li>Facial embeddings for face recognition tasks</li>
 * </ul>
 * 
 * <p>The embedder automatically processes entities annotated with {@link Vectorize} and populates their
 * embedding fields based on the configured embedding type and provider. It supports both single entity
 * and batch processing for improved performance.
 * 
 * @see Embedder
 * @see Vectorize
 * @see EmbeddingModelFactory
 */
public class DefaultEmbedder implements Embedder {
  /** Logger instance for this class */
  private static final Log logger = LogFactory.getLog(DefaultEmbedder.class);

  /** Factory for creating embedding models with caching support */
  private final EmbeddingModelFactory embeddingModelFactory;

  /** Image processing pipeline for preprocessing images before embedding */
  public final Pipeline imagePipeline;

  /** Deep learning model for generating image embeddings */
  private final ZooModel<Image, float[]> imageEmbeddingModel;

  /** Deep learning model specifically trained for facial recognition embeddings */
  private final ZooModel<Image, float[]> faceEmbeddingModel;

  /** Factory for creating image objects from various sources */
  private final ImageFactory imageFactory;

  /** Spring application context for resource loading */
  private final ApplicationContext applicationContext;

  /** Feature extractor for processing images through the pipeline */
  private final ImageFeatureExtractor imageFeatureExtractor;

  /** Configuration properties for AI and embedding settings */
  private final AIRedisOMProperties properties;

  /**
   * Constructs a new DefaultEmbedder with all required dependencies.
   * 
   * @param applicationContext    Spring application context for resource loading
   * @param embeddingModelFactory Factory for creating and caching embedding models
   * @param imageEmbeddingModel   Deep learning model for general image embeddings
   * @param faceEmbeddingModel    Deep learning model for facial embeddings
   * @param imageFactory          Factory for creating image objects from input streams
   * @param imagePipeline         Processing pipeline for image preprocessing
   * @param properties            Configuration properties for AI services
   */
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

  /**
   * Generates image embeddings for a batch of images and returns them as byte arrays.
   * 
   * @param isList List of input streams containing image data
   * @return List of byte arrays representing the image embeddings, empty list if processing fails
   */
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

  /**
   * Generates image embedding for a single image and returns it as a byte array.
   * 
   * @param is Input stream containing image data
   * @return Byte array representing the image embedding, empty array if processing fails
   */
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

  /**
   * Generates image embeddings for a batch of images and returns them as float arrays.
   * 
   * @param isList List of input streams containing image data
   * @return List of float arrays representing the image embeddings
   */
  private List<float[]> getImageEmbeddingsAsFloatArrayFor(List<InputStream> isList) {
    return getImageEmbeddingsAsByteArrayFor(isList).stream().map(ObjectUtils::byteArrayToFloatArray).toList();
  }

  /**
   * Generates image embedding for a single image and returns it as a float array.
   * 
   * @param is Input stream containing image data
   * @return Float array representing the image embedding
   */
  private float[] getImageEmbeddingsAsFloatArrayFor(InputStream is) {
    return byteArrayToFloatArray(getImageEmbeddingsAsByteArrayFor(is));
  }

  /**
   * Generates facial embeddings for a batch of images and returns them as byte arrays.
   * 
   * @param isList List of input streams containing facial image data
   * @return List of byte arrays representing the facial embeddings
   */
  private List<byte[]> getFacialImageEmbeddingsAsByteArrayFor(List<InputStream> isList) {
    return getFacialImageEmbeddingsAsFloatArrayFor(isList).stream().map(ObjectUtils::floatArrayToByteArray).toList();
  }

  /**
   * Generates facial embedding for a single image and returns it as a byte array.
   * 
   * @param is Input stream containing facial image data
   * @return Byte array representing the facial embedding
   * @throws IOException        If there's an error reading the image data
   * @throws TranslateException If there's an error during model inference
   */
  private byte[] getFacialImageEmbeddingsAsByteArrayFor(InputStream is) throws IOException, TranslateException {
    return ObjectUtils.floatArrayToByteArray(getFacialImageEmbeddingsAsFloatArrayFor(is));
  }

  /**
   * Generates facial embeddings for a batch of images and returns them as float arrays.
   * 
   * @param isList List of input streams containing facial image data
   * @return List of float arrays representing the facial embeddings, empty list if processing fails
   */
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

  /**
   * Generates facial embedding for a single image and returns it as a float array.
   * 
   * @param is Input stream containing facial image data
   * @return Float array representing the facial embedding
   * @throws IOException        If there's an error reading the image data
   * @throws TranslateException If there's an error during model inference
   */
  private float[] getFacialImageEmbeddingsAsFloatArrayFor(InputStream is) throws IOException, TranslateException {
    try (Predictor<Image, float[]> predictor = faceEmbeddingModel.newPredictor()) {
      var img = imageFactory.fromInputStream(is);
      return predictor.predict(img);
    }
  }

  /**
   * Generates text embeddings for a batch of texts using the specified model and returns them as byte arrays.
   * 
   * @param texts List of text strings to embed
   * @param model The embedding model to use
   * @return List of byte arrays representing the text embeddings
   */
  private List<byte[]> getEmbeddingsAsByteArrayFor(List<String> texts, EmbeddingModel model) {
    return model.embed(texts).stream().map(ObjectUtils::floatArrayToByteArray).toList();
  }

  /**
   * Generates text embeddings for a batch of texts using the specified model and returns them as float arrays.
   * 
   * @param texts List of text strings to embed
   * @param model The embedding model to use
   * @return List of float arrays representing the text embeddings
   */
  private List<float[]> getEmbeddingAsFloatArrayFor(List<String> texts, EmbeddingModel model) {
    return model.embed(texts);
  }

  /**
   * Generates text embedding for a single text using the specified model and returns it as a byte array.
   * 
   * @param text  Text string to embed
   * @param model The embedding model to use
   * @return Byte array representing the text embedding
   */
  private byte[] getEmbeddingsAsByteArrayFor(String text, EmbeddingModel model) {
    return ObjectUtils.floatArrayToByteArray(model.embed(text));
  }

  /**
   * Generates text embedding for a single text using the specified model and returns it as a float array.
   * 
   * @param text  Text string to embed
   * @param model The embedding model to use
   * @return Float array representing the text embedding
   */
  private float[] getEmbeddingAsFloatArrayFor(String text, EmbeddingModel model) {
    return model.embed(text);
  }

  /**
   * {@inheritDoc}
   * 
   * Processes a single entity, examining fields annotated with {@link Vectorize} and generating
   * embeddings based on the configured embedding type and provider. The generated embeddings
   * are automatically set on the destination fields specified in the annotation.
   */
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

  /**
   * {@inheritDoc}
   * 
   * Processes multiple entities in batches for improved performance. Entities are grouped
   * by embedding type and processed together to leverage batch inference capabilities
   * of the underlying models.
   */
  @Override
  public <S> void processEntities(Iterable<S> items) {
    if (!isReady()) {
      return;
    }

    int batchSize = properties.getEmbeddingBatchSize();
    List<FieldData> batch = new ArrayList<>(batchSize);

    for (Object item : items) {
      List<Field> fields = ObjectUtils.getFieldsWithAnnotation(item.getClass(), Vectorize.class);
      if (fields.isEmpty())
        continue;

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

  /**
   * Processes a batch of field data by grouping them by embedding type and delegating
   * to the appropriate batch processing method.
   * 
   * @param batch List of field data to process
   */
  private void processBatch(List<FieldData> batch) {
    batch.stream().collect(Collectors.groupingBy(fd -> fd.vectorize().embeddingType())).forEach(this::vectorizeBatch);
  }

  /**
   * Vectorizes a batch of fields based on their embedding type.
   * 
   * @param embeddingType The type of embedding to generate
   * @param fieldDataList List of field data to vectorize
   */
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

  /**
   * Processes image embeddings for a batch of fields, handling both document and hash mappings.
   * 
   * @param fieldDataList List of field data containing image paths to process
   */
  private void processImageEmbedding(List<FieldData> fieldDataList) {
    fieldDataList.stream().collect(Collectors.groupingBy(FieldData::isDocument)).forEach((isDocument,
        groupedByIsDocument) -> groupedByIsDocument.stream().collect(Collectors.groupingBy(FieldData::vectorize))
            .forEach((vectorize, groupedByVectorize) -> {
              List<?> embeddings = isDocument ?
                  getImageEmbeddingsAsFloatArrayFor(mapValuesToResources(groupedByVectorize)) :
                  getImageEmbeddingsAsByteArrayFor(mapValuesToResources(groupedByVectorize));

              if (embeddings != null) {
                applyEmbeddings(groupedByVectorize, embeddings, vectorize);
              }
            }));
  }

  /**
   * Maps field values to resource input streams.
   * 
   * @param fieldDataList List of field data containing resource paths
   * @return List of input streams, empty list if any resource fails to load
   */
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

  /**
   * Processes image embedding for a single field.
   * 
   * @param accessor   Property accessor for setting the embedding value
   * @param vectorize  Vectorize annotation containing configuration
   * @param fieldValue The image resource path
   * @param isDocument Whether the entity is a document (affects embedding format)
   */
  private void processImageEmbedding(PropertyAccessor accessor, Vectorize vectorize, Object fieldValue,
      boolean isDocument) {
    Resource resource = applicationContext.getResource(fieldValue.toString());
    try {
      if (isDocument) {
        accessor.setPropertyValue(vectorize.destination(), getImageEmbeddingsAsFloatArrayFor(resource
            .getInputStream()));
      } else {
        accessor.setPropertyValue(vectorize.destination(), getImageEmbeddingsAsByteArrayFor(resource.getInputStream()));
      }
    } catch (IOException e) {
      logger.warn("Error generating image embedding", e);
    }
  }

  /**
   * Processes facial embeddings for a batch of fields, handling both document and hash mappings.
   * 
   * @param fieldDataList List of field data containing facial image paths to process
   */
  private void processFaceEmbedding(List<FieldData> fieldDataList) {
    fieldDataList.stream().collect(Collectors.groupingBy(FieldData::isDocument)).forEach((isDocument,
        groupedByIsDocument) -> groupedByIsDocument.stream().collect(Collectors.groupingBy(FieldData::vectorize))
            .forEach((vectorize, groupedByVectorize) -> {
              List<?> embeddings = isDocument ?
                  getFacialImageEmbeddingsAsFloatArrayFor(mapValuesToResources(groupedByVectorize)) :
                  getFacialImageEmbeddingsAsByteArrayFor(mapValuesToResources(groupedByVectorize));

              if (embeddings != null) {
                applyEmbeddings(groupedByVectorize, embeddings, vectorize);
              }
            }));
  }

  /**
   * Processes facial embedding for a single field.
   * 
   * @param accessor   Property accessor for setting the embedding value
   * @param vectorize  Vectorize annotation containing configuration
   * @param fieldValue The facial image resource path
   * @param isDocument Whether the entity is a document (affects embedding format)
   */
  private void processFaceEmbedding(PropertyAccessor accessor, Vectorize vectorize, Object fieldValue,
      boolean isDocument) {
    Resource resource = applicationContext.getResource(fieldValue.toString());
    try {
      if (isDocument) {
        accessor.setPropertyValue(vectorize.destination(), getFacialImageEmbeddingsAsFloatArrayFor(resource
            .getInputStream()));
      } else {
        accessor.setPropertyValue(vectorize.destination(), getFacialImageEmbeddingsAsByteArrayFor(resource
            .getInputStream()));
      }
    } catch (IOException | TranslateException e) {
      logger.warn("Error generating facial image embedding", e);
    }
  }

  /**
   * Processes sentence embeddings for a batch of fields, grouping by provider for efficient processing.
   * 
   * @param fieldDataList List of field data containing text to process
   */
  private void processSentencesEmbedding(List<FieldData> fieldDataList) {
    fieldDataList.stream().collect(Collectors.groupingBy(it -> it.vectorize().provider())).forEach((provider,
        groupedFieldDataList) -> {
      switch (provider) {
        case TRANSFORMERS -> processSentenceEmbedding(groupedFieldDataList, this::getTransformersEmbeddingModel);
        case DJL -> {
        }
        case OPENAI -> processSentenceEmbedding(groupedFieldDataList, this::getOpenAiEmbeddingModel);
        case OLLAMA -> processSentenceEmbedding(groupedFieldDataList, this::getOllamaEmbeddingModel);
        case AZURE_OPENAI -> processSentenceEmbedding(groupedFieldDataList, this::getAzureOpenAiEmbeddingModel);
        case VERTEX_AI -> processSentenceEmbedding(groupedFieldDataList, this::getVertexAiEmbeddingModel);
        case AMAZON_BEDROCK_COHERE -> processSentenceEmbedding(groupedFieldDataList,
            this::getBedrockCohereEmbeddingModel);
        case AMAZON_BEDROCK_TITAN -> processSentenceEmbedding(groupedFieldDataList,
            this::getBedrockTitanEmbeddingModel);
      }
    });
  }

  /**
   * Processes sentence embedding for a single field using the configured provider.
   * 
   * @param accessor   Property accessor for setting the embedding value
   * @param vectorize  Vectorize annotation containing configuration
   * @param fieldValue The text to embed
   * @param isDocument Whether the entity is a document (affects embedding format)
   */
  private void processSentenceEmbedding(PropertyAccessor accessor, Vectorize vectorize, Object fieldValue,
      boolean isDocument) {
    switch (vectorize.provider()) {
      case TRANSFORMERS -> processSentenceEmbedding(accessor, vectorize, fieldValue, isDocument,
          this::getTransformersEmbeddingModel);
      case DJL -> {
      }
      case OPENAI -> processSentenceEmbedding(accessor, vectorize, fieldValue, isDocument,
          this::getOpenAiEmbeddingModel);
      case OLLAMA -> processSentenceEmbedding(accessor, vectorize, fieldValue, isDocument,
          this::getOllamaEmbeddingModel);
      case AZURE_OPENAI -> processSentenceEmbedding(accessor, vectorize, fieldValue, isDocument,
          this::getAzureOpenAiEmbeddingModel);
      case VERTEX_AI -> processSentenceEmbedding(accessor, vectorize, fieldValue, isDocument,
          this::getVertexAiEmbeddingModel);
      case AMAZON_BEDROCK_COHERE -> processSentenceEmbedding(accessor, vectorize, fieldValue, isDocument,
          this::getBedrockCohereEmbeddingModel);
      case AMAZON_BEDROCK_TITAN -> processSentenceEmbedding(accessor, vectorize, fieldValue, isDocument,
          this::getBedrockTitanEmbeddingModel);
    }
  }

  /**
   * Maps field values to string representations.
   * 
   * @param fieldDataList List of field data
   * @return List of string values
   */
  private List<String> mapValues(List<FieldData> fieldDataList) {
    return fieldDataList.stream().map(it -> it.value().toString()).toList();
  }

  /**
   * Applies generated embeddings to their corresponding destination fields.
   * 
   * @param fieldDataList List of field data to update
   * @param embeddings    List of generated embeddings
   * @param vectorize     Vectorize annotation containing destination field information
   */
  private void applyEmbeddings(List<FieldData> fieldDataList, List<?> embeddings, Vectorize vectorize) {
    for (int i = 0; i < fieldDataList.size() && i < embeddings.size(); i++) {
      fieldDataList.get(i).accessor().setPropertyValue(vectorize.destination(), embeddings.get(i));
    }
  }

  /**
   * Processes sentence embeddings for a batch using a model function.
   * 
   * @param fieldDataList List of field data to process
   * @param modelFunction Function to create the appropriate embedding model
   */
  private void processSentenceEmbedding(List<FieldData> fieldDataList,
      Function<Vectorize, EmbeddingModel> modelFunction) {
    fieldDataList.stream().collect(Collectors.groupingBy(FieldData::isDocument)).forEach((isDocument,
        groupedByIsDocument) -> groupedByIsDocument.stream().collect(Collectors.groupingBy(FieldData::vectorize))
            .forEach((vectorize, groupedByVectorize) -> {
              EmbeddingModel model = modelFunction.apply(vectorize);
              List<?> embeddings = isDocument ?
                  getEmbeddingAsFloatArrayFor(mapValues(groupedByVectorize), model) :
                  getEmbeddingsAsByteArrayFor(mapValues(groupedByVectorize), model);

              if (embeddings != null) {
                applyEmbeddings(groupedByVectorize, embeddings, vectorize);
              }
            }));
  }

  /**
   * Processes sentence embedding for a single field using a model function.
   * 
   * @param accessor      Property accessor for setting the embedding value
   * @param vectorize     Vectorize annotation containing configuration
   * @param fieldValue    The text to embed
   * @param isDocument    Whether the entity is a document (affects embedding format)
   * @param modelFunction Function to create the appropriate embedding model
   */
  private void processSentenceEmbedding(PropertyAccessor accessor, Vectorize vectorize, Object fieldValue,
      boolean isDocument, Function<Vectorize, EmbeddingModel> modelFunction) {
    EmbeddingModel model = modelFunction.apply(vectorize);
    if (isDocument) {
      accessor.setPropertyValue(vectorize.destination(), getEmbeddingAsFloatArrayFor(fieldValue.toString(), model));
    } else {
      accessor.setPropertyValue(vectorize.destination(), getEmbeddingsAsByteArrayFor(fieldValue.toString(), model));
    }
  }

  /**
   * Creates or retrieves a Transformers embedding model based on the vectorize configuration.
   * 
   * @param vectorize Configuration containing model parameters
   * @return Configured TransformersEmbeddingModel instance
   */
  private TransformersEmbeddingModel getTransformersEmbeddingModel(Vectorize vectorize) {
    return embeddingModelFactory.createTransformersEmbeddingModel(vectorize);
  }

  /**
   * Creates or retrieves an OpenAI embedding model.
   *
   * @param vectorize Configuration containing model parameters
   * @return Configured EmbeddingModel instance for OpenAI
   */
  private EmbeddingModel getOpenAiEmbeddingModel(Vectorize vectorize) {
    return embeddingModelFactory.createOpenAiEmbeddingModel(vectorize.openAiEmbeddingModel());
  }

  /**
   * Creates or retrieves an Ollama embedding model.
   * 
   * @param vectorize Configuration containing model parameters
   * @return Configured OllamaEmbeddingModel instance
   */
  private OllamaEmbeddingModel getOllamaEmbeddingModel(Vectorize vectorize) {
    return embeddingModelFactory.createOllamaEmbeddingModel(vectorize.ollamaEmbeddingModel().id());
  }

  /**
   * Creates or retrieves an Azure OpenAI embedding model.
   * 
   * @param vectorize Configuration containing deployment name
   * @return Configured AzureOpenAiEmbeddingModel instance
   */
  private AzureOpenAiEmbeddingModel getAzureOpenAiEmbeddingModel(Vectorize vectorize) {
    return embeddingModelFactory.createAzureOpenAiEmbeddingModel(vectorize.azureOpenAiDeploymentName());
  }

  /**
   * Creates or retrieves a Vertex AI text embedding model.
   * 
   * @param vectorize Configuration containing model parameters
   * @return Configured VertexAiTextEmbeddingModel instance
   */
  private VertexAiTextEmbeddingModel getVertexAiEmbeddingModel(Vectorize vectorize) {
    return embeddingModelFactory.createVertexAiTextEmbeddingModel(vectorize.vertexAiApiModel());
  }

  /**
   * Creates or retrieves an AWS Bedrock Cohere embedding model.
   * 
   * @param vectorize Configuration containing model parameters
   * @return Configured BedrockCohereEmbeddingModel instance
   */
  private BedrockCohereEmbeddingModel getBedrockCohereEmbeddingModel(Vectorize vectorize) {
    return embeddingModelFactory.createCohereEmbeddingModel(vectorize.cohereEmbeddingModel().id());
  }

  /**
   * Creates or retrieves an AWS Bedrock Titan embedding model.
   * 
   * @param vectorize Configuration containing model parameters
   * @return Configured BedrockTitanEmbeddingModel instance
   */
  private BedrockTitanEmbeddingModel getBedrockTitanEmbeddingModel(Vectorize vectorize) {
    return embeddingModelFactory.createTitanEmbeddingModel(vectorize.titanEmbeddingModel().id());
  }

  /**
   * {@inheritDoc}
   * 
   * @return Always returns true as models are created on demand
   */
  @Override
  public boolean isReady() {
    //return this.faceEmbeddingModel != null && this.transformersEmbeddingModel != null;
    return true;
  }

  /**
   * {@inheritDoc}
   */
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

  /**
   * Generates sentence embeddings as byte arrays using the appropriate provider.
   * 
   * @param texts     List of texts to embed
   * @param vectorize Configuration specifying the provider and model
   * @return List of byte arrays representing the embeddings
   */
  private List<byte[]> getSentenceEmbeddingAsBytes(List<String> texts, Vectorize vectorize) {
    return switch (vectorize.provider()) {
      case TRANSFORMERS -> {
        TransformersEmbeddingModel model = getTransformersEmbeddingModel(vectorize);
        yield getEmbeddingsAsByteArrayFor(texts, model);
      }
      case DJL -> Collections.emptyList();
      case OPENAI -> {
        EmbeddingModel model = getOpenAiEmbeddingModel(vectorize);
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

  /**
   * Generates sentence embeddings as float arrays using the appropriate provider.
   *
   * @param texts     List of texts to embed
   * @param vectorize Configuration specifying the provider and model
   * @return List of float arrays representing the embeddings
   */
  private List<float[]> getSentenceEmbeddingAsFloats(List<String> texts, Vectorize vectorize) {
    return switch (vectorize.provider()) {
      case TRANSFORMERS -> {
        TransformersEmbeddingModel model = getTransformersEmbeddingModel(vectorize);
        yield getEmbeddingAsFloatArrayFor(texts, model);
      }
      case DJL -> Collections.emptyList(); //TODO what to do here?
      case OPENAI -> {
        EmbeddingModel model = getOpenAiEmbeddingModel(vectorize);
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

  /**
   * {@inheritDoc}
   */
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

  /**
   * {@inheritDoc}
   */
  @Override
  public List<byte[]> getTextEmbeddingsAsBytes(List<String> texts, MetamodelField<?, ?> metamodelField) {
    return getTextEmbeddingsAsBytes(texts, metamodelField.getSearchFieldAccessor().getField());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<float[]> getTextEmbeddingsAsFloats(List<String> texts, MetamodelField<?, ?> metamodelField) {
    return getTextEmbeddingsAsFloats(texts, metamodelField.getSearchFieldAccessor().getField());
  }
}

/**
 * Internal record for holding field data during batch processing.
 * 
 * @param vectorize  The Vectorize annotation configuration
 * @param item       The entity being processed
 * @param field      The field being vectorized
 * @param accessor   Property accessor for the entity
 * @param value      The field value to be vectorized
 * @param isDocument Whether the entity is a Document (affects embedding format)
 */
record FieldData(Vectorize vectorize, Object item, Field field, PropertyAccessor accessor, Object value,
                 boolean isDocument) {
}
