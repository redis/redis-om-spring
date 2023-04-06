package com.redis.om.spring.annotations.vectorize.face;

import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.DetectedObjects.DetectedObject;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import com.redis.om.spring.AbstractBaseEnhancedRedisTest;
import com.redis.om.spring.tuple.Quad;
import com.redis.om.spring.tuple.Triple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.redis.om.spring.util.ObjectUtils.floatArrayToByteArray;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class FaceDetectionTest extends AbstractBaseEnhancedRedisTest {
  @Autowired
  private ApplicationContext applicationContext;

  @Autowired
  public ZooModel<Image, DetectedObjects> faceDetectionModel;

  @Autowired
  public ZooModel<Image, float[]> faceEmbeddingModel;

  @Test
  void testFaceDetection() throws TranslateException, IOException {
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

  @Test
  void testFaceEmbedding() throws TranslateException, IOException {
    Resource resource = applicationContext.getResource("classpath:/images/morgan.jpg");
    Image img = ImageFactory.getInstance().fromInputStream(resource.getInputStream());
    try (Predictor<Image, float[]> predictor = faceEmbeddingModel.newPredictor()) {
      float[] embedding = predictor.predict(img);
      byte[] embeddingAsByteArray = floatArrayToByteArray(embedding);

      assertAll( //
          () -> assertThat(embedding).hasSize(512),
          () -> assertThat(embeddingAsByteArray).hasSize(2048)
      );
    }
  }
}
