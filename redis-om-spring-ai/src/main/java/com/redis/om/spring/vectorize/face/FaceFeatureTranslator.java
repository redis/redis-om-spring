package com.redis.om.spring.vectorize.face;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.transform.Normalize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.translate.Pipeline;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

/**
 * Translator for extracting facial feature embeddings from face images.
 * This class implements the DJL (Deep Java Library) Translator interface to handle
 * the pre-processing and post-processing of face recognition models.
 * 
 * <p>The translator is designed to work with face recognition models that generate
 * high-dimensional feature vectors (embeddings) from facial images. These embeddings
 * can be used for:
 * <ul>
 * <li>Face verification (comparing two faces)</li>
 * <li>Face identification (matching against a database)</li>
 * <li>Face clustering and similarity search</li>
 * <li>Vector similarity search in Redis using the generated embeddings</li>
 * </ul>
 * 
 * <p>The pre-processing pipeline includes:
 * <ul>
 * <li>Converting images to tensors</li>
 * <li>Normalizing pixel values to a standard range</li>
 * </ul>
 * 
 * <p>The output is a float array representing the facial feature embedding,
 * which can be stored and indexed in Redis for efficient similarity search.
 * 
 * @see ai.djl.translate.Translator
 * @see com.redis.om.spring.vectorize.face.FaceDetectionTranslator
 * @see com.redis.om.spring.annotations.VectorIndexed
 */
public class FaceFeatureTranslator implements Translator<Image, float[]> {

  /**
   * Default constructor for face feature translator.
   * This constructor is used to create instances of the translator for extracting
   * facial feature embeddings from face images.
   */
  public FaceFeatureTranslator() {
    // Default constructor
  }

  /**
   * Pre-processes the input face image for feature extraction.
   * 
   * <p>This method applies a transformation pipeline that:
   * <ul>
   * <li>Converts the image to an NDArray with COLOR flag</li>
   * <li>Applies ToTensor transformation to convert pixel values to tensor format</li>
   * <li>Normalizes the tensor using mean values of 127.5/255.0 and standard deviation of 128.0/255.0</li>
   * </ul>
   * 
   * <p>The normalization parameters are specifically tuned for face recognition models
   * and help ensure consistent feature extraction across different lighting conditions
   * and image variations.
   * 
   * @param ctx   the translator context providing NDManager and other resources
   * @param input the input face image to process (should be cropped to contain primarily the face)
   * @return an NDList containing the pre-processed image tensor ready for the model
   */
  @Override
  public NDList processInput(TranslatorContext ctx, Image input) {
    NDArray array = input.toNDArray(ctx.getNDManager(), Image.Flag.COLOR);
    Pipeline pipeline = new Pipeline();
    pipeline
        // .add(new Resize(160))
        .add(new ToTensor()).add(new Normalize(new float[] { 127.5f / 255.0f, 127.5f / 255.0f, 127.5f / 255.0f },
            new float[] { 128.0f / 255.0f, 128.0f / 255.0f, 128.0f / 255.0f }));

    return pipeline.transform(new NDList(array));
  }

  /**
   * Post-processes the model output to extract the facial feature embedding.
   * 
   * <p>This method converts the model's output tensor into a float array representing
   * the facial feature embedding. The method handles multi-dimensional outputs by:
   * <ul>
   * <li>Extracting individual elements from the output tensor</li>
   * <li>Converting each element to a float value</li>
   * <li>Assembling the values into a single feature vector</li>
   * </ul>
   * 
   * <p>The resulting float array is a high-dimensional feature vector that uniquely
   * represents the facial characteristics of the input image. This embedding can be:
   * <ul>
   * <li>Stored in Redis as a vector for similarity search</li>
   * <li>Compared with other face embeddings using distance metrics</li>
   * <li>Used for face matching, verification, or clustering tasks</li>
   * </ul>
   * 
   * @param ctx  the translator context providing NDManager and other resources
   * @param list the NDList containing the model's output tensor
   * @return a float array representing the facial feature embedding
   * @throws IllegalArgumentException if the output format is unexpected
   */
  @Override
  public float[] processOutput(TranslatorContext ctx, NDList list) {
    try (NDList result = new NDList()) {
      long numOutputs = list.singletonOrThrow().getShape().get(0);
      for (int i = 0; i < numOutputs; i++) {
        result.add(list.singletonOrThrow().get(i));
      }
      float[][] embeddings = result.stream().map(NDArray::toFloatArray).toArray(float[][]::new);
      float[] feature = new float[embeddings.length];
      for (int i = 0; i < embeddings.length; i++) {
        feature[i] = embeddings[i][0];
      }
      return feature;
    }
  }
}
