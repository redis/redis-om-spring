/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package com.redis.om.spring.vectorize.face;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.*;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDArrays;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

/**
 * Translator for face detection that processes images and detects faces with their landmarks.
 * This class implements the DJL (Deep Java Library) Translator interface to handle the
 * pre-processing and post-processing of face detection models.
 * 
 * <p>The translator performs the following operations:
 * <ul>
 * <li>Pre-processes input images for face detection models</li>
 * <li>Post-processes model outputs to extract face bounding boxes and landmarks</li>
 * <li>Applies non-maximum suppression (NMS) to filter overlapping detections</li>
 * <li>Extracts facial landmarks (5 key points) for each detected face</li>
 * </ul>
 * 
 * <p>This translator is designed to work with face detection models that output:
 * <ul>
 * <li>Bounding box predictions</li>
 * <li>Confidence scores</li>
 * <li>Facial landmark coordinates</li>
 * </ul>
 * 
 * @see ai.djl.translate.Translator
 * @see ai.djl.modality.cv.output.DetectedObjects
 * @see com.redis.om.spring.vectorize.face.FaceFeatureTranslator
 */
public class FaceDetectionTranslator implements Translator<Image, DetectedObjects> {

  /** Confidence threshold for filtering low-confidence detections */
  private final double confThresh;

  /** Non-maximum suppression threshold for filtering overlapping detections */
  private final double nmsThresh;

  /** Maximum number of face detections to return */
  private final int topK;

  /** Variance values used for decoding bounding box predictions */
  private final double[] variance;

  /** Scale configurations for multi-scale face detection */
  private final int[][] scales;

  /** Step sizes for feature map generation at different scales */
  private final int[] steps;

  /** Width of the input image */
  private int width;

  /** Height of the input image */
  private int height;

  /**
   * Constructs a FaceDetectionTranslator with the specified parameters.
   * 
   * @param confThresh confidence threshold for filtering detections (0.0 to 1.0)
   * @param nmsThresh  non-maximum suppression threshold for filtering overlapping boxes (0.0 to 1.0)
   * @param variance   variance values for decoding predictions, typically {0.1, 0.2}
   * @param topK       maximum number of detections to return
   * @param scales     scale configurations for multi-scale detection, e.g., {{10, 16, 24}, {32, 48}, {64, 96}}
   * @param steps      step sizes for feature maps at different scales, e.g., {8, 16, 32}
   */
  public FaceDetectionTranslator(double confThresh, double nmsThresh, double[] variance, int topK, int[][] scales,
      int[] steps) {
    this.confThresh = confThresh;
    this.nmsThresh = nmsThresh;
    this.variance = variance;
    this.topK = topK;
    this.scales = scales;
    this.steps = steps;
  }

  /**
   * Pre-processes the input image for face detection.
   * 
   * <p>This method performs the following transformations:
   * <ul>
   * <li>Converts the image to an NDArray</li>
   * <li>Transposes from HWC (Height-Width-Channel) to CHW format</li>
   * <li>Flips color channels from RGB to BGR</li>
   * <li>Converts to FLOAT32 data type if necessary</li>
   * <li>Subtracts mean values [104, 117, 123] for normalization</li>
   * </ul>
   * 
   * @param ctx   the translator context providing NDManager and other resources
   * @param input the input image to process
   * @return an NDList containing the pre-processed image tensor
   */
  @Override
  public NDList processInput(TranslatorContext ctx, Image input) {
    width = input.getWidth();
    height = input.getHeight();
    NDArray array = input.toNDArray(ctx.getNDManager(), Image.Flag.COLOR);
    array = array.transpose(2, 0, 1).flip(0); // HWC -> CHW RGB -> BGR
    // The network by default takes float32
    if (!array.getDataType().equals(DataType.FLOAT32)) {
      array = array.toType(DataType.FLOAT32, false);
    }
    NDArray mean = ctx.getNDManager().create(new float[] { 104f, 117f, 123f }, new Shape(3, 1, 1));
    array = array.sub(mean);
    return new NDList(array);
  }

  /**
   * Post-processes the model output to extract detected faces with landmarks.
   * 
   * <p>This method performs the following operations:
   * <ul>
   * <li>Decodes bounding box predictions using anchor boxes and variance</li>
   * <li>Decodes facial landmark coordinates (5 key points per face)</li>
   * <li>Filters detections based on confidence threshold</li>
   * <li>Applies non-maximum suppression to remove overlapping detections</li>
   * <li>Returns the top-K detections with highest confidence scores</li>
   * </ul>
   * 
   * <p>The expected model outputs are:
   * <ul>
   * <li>list[0]: Bounding box predictions</li>
   * <li>list[1]: Confidence scores</li>
   * <li>list[2]: Facial landmark predictions</li>
   * </ul>
   * 
   * @param ctx  the translator context providing NDManager and other resources
   * @param list the NDList containing model outputs
   * @return DetectedObjects containing detected faces with their bounding boxes, confidence scores, and landmarks
   */
  @Override
  public DetectedObjects processOutput(TranslatorContext ctx, NDList list) {
    NDManager manager = ctx.getNDManager();
    double scaleXY = variance[0];
    double scaleWH = variance[1];

    NDArray prob = list.get(1).get(":, 1:");
    prob = NDArrays.stack(new NDList(prob.argMax(1).toType(DataType.FLOAT32, false), prob.max(new int[] { 1 })));

    NDArray boxRecover = boxRecover(manager, width, height, scales, steps);
    NDArray boundingBoxes = list.get(0);
    NDArray bbWH = boundingBoxes.get(":, 2:").mul(scaleWH).exp().mul(boxRecover.get(":, 2:"));
    NDArray bbXY = boundingBoxes.get(":, :2").mul(scaleXY).mul(boxRecover.get(":, 2:")).add(boxRecover.get(":, :2"))
        .sub(bbWH.mul(0.5f));

    boundingBoxes = NDArrays.concat(new NDList(bbXY, bbWH), 1);

    NDArray landms = list.get(2);
    landms = decodeLandm(landms, boxRecover, scaleXY);

    // filter the result below the threshold
    NDArray cutOff = prob.get(1).gt(confThresh);
    boundingBoxes = boundingBoxes.transpose().booleanMask(cutOff, 1).transpose();
    landms = landms.transpose().booleanMask(cutOff, 1).transpose();
    prob = prob.booleanMask(cutOff, 1);

    // start categorical filtering
    long[] order = prob.get(1).argSort().get(":" + topK).toLongArray();
    prob = prob.transpose();
    List<String> retNames = new ArrayList<>();
    List<Double> retProbs = new ArrayList<>();
    List<BoundingBox> retBB = new ArrayList<>();

    Map<Integer, List<BoundingBox>> recorder = new ConcurrentHashMap<>();

    for (int i = order.length - 1; i >= 0; i--) {
      long currMaxLoc = order[i];
      float[] classProb = prob.get(currMaxLoc).toFloatArray();
      int classId = (int) classProb[0];
      double probability = classProb[1];

      double[] boxArr = boundingBoxes.get(currMaxLoc).toDoubleArray();
      double[] landmsArr = landms.get(currMaxLoc).toDoubleArray();
      Rectangle rect = new Rectangle(boxArr[0], boxArr[1], boxArr[2], boxArr[3]);
      List<BoundingBox> boxes = recorder.getOrDefault(classId, new ArrayList<>());
      boolean belowIoU = true;
      for (BoundingBox box : boxes) {
        if (box.getIoU(rect) > nmsThresh) {
          belowIoU = false;
          break;
        }
      }
      if (belowIoU) {
        List<Point> keyPoints = new ArrayList<>();
        for (int j = 0; j < 5; j++) { // 5 face landmarks
          double x = landmsArr[j * 2];
          double y = landmsArr[j * 2 + 1];
          keyPoints.add(new Point(x * width, y * height));
        }
        Landmark landmark = new Landmark(boxArr[0], boxArr[1], boxArr[2], boxArr[3], keyPoints);

        boxes.add(landmark);
        recorder.put(classId, boxes);
        String className = "Face"; // classes.get(classId)
        retNames.add(className);
        retProbs.add(probability);
        retBB.add(landmark);
      }
    }

    return new DetectedObjects(retNames, retProbs, retBB);
  }

  /**
   * Generates anchor boxes (default boxes) for face detection at multiple scales.
   * 
   * <p>This method creates a grid of anchor boxes across the image at different scales
   * and aspect ratios. These anchor boxes serve as reference boxes for the model's
   * bounding box predictions.
   * 
   * @param manager the NDManager for creating NDArrays
   * @param width   the width of the input image
   * @param height  the height of the input image
   * @param scales  the scale configurations for each feature map level
   * @param steps   the step sizes (stride) for each feature map level
   * @return an NDArray containing normalized anchor box coordinates [cx, cy, w, h]
   */
  private NDArray boxRecover(NDManager manager, int width, int height, int[][] scales, int[] steps) {
    int[][] aspectRatio = new int[steps.length][2];
    for (int i = 0; i < steps.length; i++) {
      int wRatio = (int) Math.ceil((float) width / steps[i]);
      int hRatio = (int) Math.ceil((float) height / steps[i]);
      aspectRatio[i] = new int[] { hRatio, wRatio };
    }

    List<double[]> defaultBoxes = new ArrayList<>();

    for (int idx = 0; idx < steps.length; idx++) {
      int[] scale = scales[idx];
      for (int h = 0; h < aspectRatio[idx][0]; h++) {
        for (int w = 0; w < aspectRatio[idx][1]; w++) {
          for (int i : scale) {
            double skx = i * 1.0 / width;
            double sky = i * 1.0 / height;
            double cx = (w + 0.5) * steps[idx] / width;
            double cy = (h + 0.5) * steps[idx] / height;
            defaultBoxes.add(new double[] { cx, cy, skx, sky });
          }
        }
      }
    }

    double[][] boxes = new double[defaultBoxes.size()][defaultBoxes.get(0).length];
    for (int i = 0; i < defaultBoxes.size(); i++) {
      boxes[i] = defaultBoxes.get(i);
    }
    return manager.create(boxes).clip(0.0, 1.0);
  }

  /**
   * Decodes facial landmark predictions to absolute coordinates.
   * 
   * <p>This method decodes the model's landmark predictions from relative offsets
   * to absolute coordinates. Each face has 5 key landmarks:
   * <ul>
   * <li>Left eye</li>
   * <li>Right eye</li>
   * <li>Nose tip</li>
   * <li>Left mouth corner</li>
   * <li>Right mouth corner</li>
   * </ul>
   * 
   * @param pre     the predicted landmark offsets from the model
   * @param priors  the anchor box coordinates
   * @param scaleXY the scaling factor for landmark predictions
   * @return an NDArray containing decoded landmark coordinates for all detections
   */
  private NDArray decodeLandm(NDArray pre, NDArray priors, double scaleXY) {
    NDArray point1 = pre.get(":, :2").mul(scaleXY).mul(priors.get(":, 2:")).add(priors.get(":, :2"));
    NDArray point2 = pre.get(":, 2:4").mul(scaleXY).mul(priors.get(":, 2:")).add(priors.get(":, :2"));
    NDArray point3 = pre.get(":, 4:6").mul(scaleXY).mul(priors.get(":, 2:")).add(priors.get(":, :2"));
    NDArray point4 = pre.get(":, 6:8").mul(scaleXY).mul(priors.get(":, 2:")).add(priors.get(":, :2"));
    NDArray point5 = pre.get(":, 8:10").mul(scaleXY).mul(priors.get(":, 2:")).add(priors.get(":, :2"));
    return NDArrays.concat(new NDList(point1, point2, point3, point4, point5), 1);
  }
}
