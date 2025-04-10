package com.redis.om.spring;

import ai.djl.MalformedModelException;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.transform.CenterCrop;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.Pipeline;
import ai.djl.translate.Translator;
import com.redis.om.spring.vectorize.DefaultEmbedder;
import com.redis.om.spring.vectorize.Embedder;
import com.redis.om.spring.vectorize.EmbeddingModelFactory;
import com.redis.om.spring.vectorize.SpringAiProperties;
import com.redis.om.spring.vectorize.face.FaceDetectionTranslator;
import com.redis.om.spring.vectorize.face.FaceFeatureTranslator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

@ConditionalOnProperty(name = "redis.om.spring.ai.enabled")
@Configuration
@EnableConfigurationProperties({ RedisOMAiProperties.class })
public class RedisAiConfiguration {

  private static final Log logger = LogFactory.getLog(RedisAiConfiguration.class);

  @Bean
  public EmbeddingModelFactory embeddingModelFactory(
          RedisOMAiProperties properties,
          SpringAiProperties springAiProperties) {
    return new EmbeddingModelFactory(properties, springAiProperties);
  }

  @Bean(name = "djlImageFactory")
  public ImageFactory imageFactory() {
    return ImageFactory.getInstance();
  }

  @Bean(name = "djlImageEmbeddingModelCriteria")
  public Criteria<Image, float[]> imageEmbeddingModelCriteria(RedisOMAiProperties properties) {
    return Criteria.builder().setTypes(Image.class, float[].class) //
        .optEngine(properties.getDjl().getImageEmbeddingModelEngine())  //
        .optModelUrls(properties.getDjl().getImageEmbeddingModelModelUrls()) //
        .build();
  }

  @Bean(name = "djlFaceDetectionTranslator")
  public Translator<Image, DetectedObjects> faceDetectionTranslator() {
    double confThresh = 0.85f;
    double nmsThresh = 0.45f;
    double[] variance = { 0.1f, 0.2f };
    int topK = 5000;
    int[][] scales = { { 16, 32 }, { 64, 128 }, { 256, 512 } };
    int[] steps = { 8, 16, 32 };
    return new FaceDetectionTranslator(confThresh, nmsThresh, variance, topK, scales, steps);
  }

  @Bean(name = "djlFaceDetectionModelCriteria")
  public Criteria<Image, DetectedObjects> faceDetectionModelCriteria( //
      @Qualifier("djlFaceDetectionTranslator") Translator<Image, DetectedObjects> translator, //
      RedisOMAiProperties properties) {

    return Criteria.builder().setTypes(Image.class, DetectedObjects.class) //
        .optModelUrls(properties.getDjl().getFaceDetectionModelModelUrls()) //
        .optModelName(properties.getDjl().getFaceDetectionModelName()) //
        .optTranslator(translator) //
        .optEngine(properties.getDjl().getFaceDetectionModelEngine()) //
        .build();
  }

  @Bean(name = "djlFaceDetectionModel")
  public ZooModel<Image, DetectedObjects> faceDetectionModel(
      @Nullable @Qualifier("djlFaceDetectionModelCriteria") Criteria<Image, DetectedObjects> criteria) {
    try {
      return criteria != null ? ModelZoo.loadModel(criteria) : null;
    } catch (IOException | ModelNotFoundException | MalformedModelException ex) {
      logger.warn("Error retrieving default DJL face detection model", ex);
      return null;
    }
  }

  @Bean(name = "djlFaceEmbeddingTranslator")
  public Translator<Image, float[]> faceEmbeddingTranslator() {
    return new FaceFeatureTranslator();
  }

  @Bean(name = "djlFaceEmbeddingModelCriteria")
  public Criteria<Image, float[]> faceEmbeddingModelCriteria( //
      @Qualifier("djlFaceEmbeddingTranslator") Translator<Image, float[]> translator, //
      RedisOMAiProperties properties) {

    return Criteria.builder() //
        .setTypes(Image.class, float[].class) //
        .optModelUrls(properties.getDjl().getFaceEmbeddingModelModelUrls()) //
        .optModelName(properties.getDjl().getFaceEmbeddingModelName()) //
        .optTranslator(translator) //
        .optEngine(properties.getDjl().getFaceEmbeddingModelEngine()) //
        .build();
  }

  @Bean(name = "djlFaceEmbeddingModel")
  public ZooModel<Image, float[]> faceEmbeddingModel(
      @Nullable @Qualifier("djlFaceEmbeddingModelCriteria") Criteria<Image, float[]> criteria) {
    try {
      return criteria != null ? ModelZoo.loadModel(criteria) : null;
    } catch (Exception e) {
      logger.warn("Error retrieving default DJL face embeddings model", e);
      return null;
    }
  }

  @Bean(name = "djlImageEmbeddingModel")
  public ZooModel<Image, float[]> imageModel(
      @Nullable @Qualifier("djlImageEmbeddingModelCriteria") Criteria<Image, float[]> criteria)
      throws MalformedModelException, ModelNotFoundException, IOException {
    return criteria != null ? ModelZoo.loadModel(criteria) : null;
  }

  @Bean(name = "djlDefaultImagePipeline")
  public Pipeline defaultImagePipeline(RedisOMAiProperties properties) {
    Pipeline pipeline = new Pipeline();
    if (properties.getDjl().isDefaultImagePipelineCenterCrop()) {
      pipeline.add(new CenterCrop());
    }
    return pipeline //
            .add(new Resize( //
                    properties.getDjl().getDefaultImagePipelineResizeWidth(), //
                    properties.getDjl().getDefaultImagePipelineResizeHeight() //
            )) //
            .add(new ToTensor());
  }

  @Bean(name = "djlSentenceTokenizer")
  public HuggingFaceTokenizer sentenceTokenizer(RedisOMAiProperties properties) {
    Map<String, String> options = Map.of( //
            "maxLength", properties.getDjl().getSentenceTokenizerMaxLength(), //
            "modelMaxLength", properties.getDjl().getSentenceTokenizerModelMaxLength() //
    );

    try {
      //noinspection ResultOfMethodCallIgnored
      InetAddress.getByName("www.huggingface.co").isReachable(5000);
      return HuggingFaceTokenizer.newInstance(properties.getDjl().getSentenceTokenizerModel(), options);
    } catch (IOException ioe) {
      logger.warn("Error retrieving default DJL sentence tokenizer");
      return null;
    }
  }

  @Primary
  @Bean(name = "featureExtractor")
  public Embedder featureExtractor(
      @Nullable @Qualifier("djlImageEmbeddingModel") ZooModel<Image, float[]> imageEmbeddingModel,
      @Nullable @Qualifier("djlFaceEmbeddingModel") ZooModel<Image, float[]> faceEmbeddingModel,
      @Nullable @Qualifier("djlImageFactory") ImageFactory imageFactory,
      @Nullable @Qualifier("djlDefaultImagePipeline") Pipeline defaultImagePipeline,
      RedisOMAiProperties properties,
      EmbeddingModelFactory embeddingModelFactory,
      ApplicationContext ac) {
    return new DefaultEmbedder(ac,
        embeddingModelFactory,
        imageEmbeddingModel,
        faceEmbeddingModel,
        imageFactory,
        defaultImagePipeline,
        properties);
  }
}
