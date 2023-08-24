package com.redis.om.spring.annotations.hash.vectorize.face;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.DetectedObjects.DetectedObject;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.redis.om.spring.util.ObjectUtils.floatArrayToByteArray;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class FaceDetectionTest extends AbstractBaseEnhancedRedisTest {
  @Autowired
  private ApplicationContext applicationContext;

  @Autowired(required = false)
  public ZooModel<Image, DetectedObjects> faceDetectionModel;

  @Autowired(required = false)
  public ZooModel<Image, float[]> faceEmbeddingModel;

  @Test
  void testFaceDetection() throws TranslateException, IOException {
    if (this.faceDetectionModel != null) {
      Resource resource = applicationContext.getResource("classpath:/images/largest_selfie.jpg");
      Image img = ImageFactory.getInstance().fromInputStream(resource.getInputStream());
      try (Predictor<Image, DetectedObjects> predictor = faceDetectionModel.newPredictor()) {
        DetectedObjects detection = predictor.predict(img);
        List<DetectedObject> detectedObjects = IntStream //
            .range(0, detection.getNumberOfObjects()) //
            .mapToObj(i -> (DetectedObject)detection.item(i)) //
            .collect(Collectors.toList());

        assertAll( //
            () -> assertThat(detectedObjects).hasSize(115),
            () -> assertThat(detectedObjects).map(DetectedObject::getClassName).allMatch(cn -> cn.equalsIgnoreCase("Face"))
        );
      }
    }
  }

  @Test
  void testFaceEmbedding() throws TranslateException, IOException {
    if (this.faceEmbeddingModel != null) {
      Resource resource = applicationContext.getResource("classpath:/images/morgan.jpg");
      Image img = ImageFactory.getInstance().fromInputStream(resource.getInputStream());
      try (Predictor<Image, float[]> predictor = faceEmbeddingModel.newPredictor()) {
        float[] embedding = predictor.predict(img);
        byte[] embeddingAsByteArray = floatArrayToByteArray(embedding);

        assertAll( //
            () -> assertThat(embedding).hasSize(512), () -> assertThat(embeddingAsByteArray).hasSize(2048));
      }
    }
  }
}
