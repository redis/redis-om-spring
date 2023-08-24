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
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Vectorize;
import com.redis.om.spring.util.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;

import static com.redis.om.spring.util.ObjectUtils.byteArrayToFloatArray;
import static com.redis.om.spring.util.ObjectUtils.longArrayToFloatArray;

public class DefaultFeatureExtractor implements FeatureExtractor {
  private final ZooModel<Image, byte[]> imageEmbeddingModel;
  private final ZooModel<Image, float[]> faceEmbeddingModel;
  private final ImageFactory imageFactory;
  private final ApplicationContext applicationContext;
  private final ImageFeatureExtractor imageFeatureExtractor;
  public final Pipeline imagePipeline;
  public final HuggingFaceTokenizer sentenceTokenizer;

  private static final Log logger = LogFactory.getLog(DefaultFeatureExtractor.class);

  public DefaultFeatureExtractor( //
      ApplicationContext applicationContext, //
      ZooModel<Image, byte[]> imageEmbeddingModel, //
      ZooModel<Image, float[]> faceEmbeddingModel, //
      ImageFactory imageFactory, //
      Pipeline imagePipeline, HuggingFaceTokenizer sentenceTokenizer) {
    this.applicationContext = applicationContext;
    this.imageEmbeddingModel = imageEmbeddingModel;
    this.faceEmbeddingModel = faceEmbeddingModel;
    this.imageFactory = imageFactory;
    this.imagePipeline = imagePipeline;
    this.sentenceTokenizer = sentenceTokenizer;

    // feature extractor
    this.imageFeatureExtractor = ImageFeatureExtractor.builder().setPipeline(imagePipeline).build();
  }

  @Override
  public byte[] getImageEmbeddingsAsByteArrayFor(InputStream is) {
    try {
      var img = imageFactory.fromInputStream(is);
      Predictor<Image, byte[]> predictor = imageEmbeddingModel.newPredictor(imageFeatureExtractor);
      return predictor.predict(img);
    } catch (IOException | TranslateException e) {
      logger.warn("Error generating image embedding", e);
      return new byte[] {};
    }
  }

  @Override
  public float[] getImageEmbeddingsAsFloatArrayFor(InputStream is) {
    return byteArrayToFloatArray(getImageEmbeddingsAsByteArrayFor(is));
  }

  @Override
  public byte[] getFacialImageEmbeddingsAsByteArrayFor(InputStream is) throws IOException, TranslateException {
    return ObjectUtils.floatArrayToByteArray(getFacialImageEmbeddingsAsFloatArrayFor(is));
  }

  @Override
  public float[] getFacialImageEmbeddingsAsFloatArrayFor(InputStream is) throws IOException, TranslateException {
    try (Predictor<Image, float[]> predictor = faceEmbeddingModel.newPredictor()) {
      var img = imageFactory.fromInputStream(is);
      return predictor.predict(img);
    }
  }

  @Override
  public byte[] getSentenceEmbeddingsAsByteArrayFor(String text) {
    Encoding encoding = sentenceTokenizer.encode(text);
    return ObjectUtils.longArrayToByteArray(encoding.getIds());
  }

  @Override
  public float[] getSentenceEmbeddingAsFloatArrayFor(String text) {
    Encoding encoding = sentenceTokenizer.encode(text);
    return longArrayToFloatArray(encoding.getIds());
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
            case IMAGE -> {
              Resource resource = applicationContext.getResource(fieldValue.toString());
              try {
                if (isDocument) {
                  accessor.setPropertyValue(vectorize.destination(), getImageEmbeddingsAsFloatArrayFor(resource.getInputStream()));
                } else {
                  accessor.setPropertyValue(vectorize.destination(), getImageEmbeddingsAsByteArrayFor(resource.getInputStream()));
                }
              } catch (IOException e) {
                logger.warn("Error generating image embedding", e);
              }
            }
            case WORD -> {
              //TODO: implement me!
            }
            case FACE -> {
              Resource resource = applicationContext.getResource(fieldValue.toString());
              try {
                if (isDocument) {
                  accessor.setPropertyValue(vectorize.destination(), getFacialImageEmbeddingsAsFloatArrayFor(resource.getInputStream()));
                } else {
                  accessor.setPropertyValue(vectorize.destination(), getFacialImageEmbeddingsAsByteArrayFor(resource.getInputStream()));
                }
              } catch (IOException | TranslateException e) {
                logger.warn("Error generating facial image embedding", e);
              }
            }
            case SENTENCE -> {
              if (isDocument) {
                accessor.setPropertyValue(vectorize.destination(), getSentenceEmbeddingAsFloatArrayFor(fieldValue.toString()));
              } else {
                accessor.setPropertyValue(vectorize.destination(), getSentenceEmbeddingsAsByteArrayFor(fieldValue.toString()));
              }
            }
          }
        }
      });
    }
  }

  @Override
  public boolean isReady() {
    return this.faceEmbeddingModel != null && this.sentenceTokenizer != null;
  }
}
