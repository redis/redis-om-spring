package com.redis.spring.ops.ai;

import java.io.File;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.redis.spring.AbstractBaseDocumentTest;
import com.redis.spring.ops.RedisModulesOperations;
import com.redislabs.redisai.Backend;
import com.redislabs.redisai.Device;
import com.redislabs.redisai.Tensor;

public class OpsForAITest extends AbstractBaseDocumentTest {
  @Autowired
  RedisModulesOperations<String, String> modulesOperations;

  @Test
  public void testRunModel() {
    AIOperations<String> ai = modulesOperations.opsForAI();

    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource("ai/graph.pb").getFile());
    String modelPath = file.getAbsolutePath();

    ai.setModel("model", Backend.TF, Device.CPU, new String[] { "a", "b" }, new String[] { "mul" }, modelPath);

    ai.setTensor("a", new float[] { 2, 3 }, new int[] { 2 });
    ai.setTensor("b", new float[] { 2, 3 }, new int[] { 2 });

    Assert.assertTrue(ai.runModel("model", new String[] { "a", "b" }, new String[] { "c" }));
    Tensor tensor = ai.getTensor("c");
    float[] values = (float[]) tensor.getValues();
    float[] expected = new float[] { 4, 9 };
    Assert.assertEquals("Assert same shape of values", 2, values.length);
    Assert.assertArrayEquals(expected, values, (float) 0.1);
  }

  // @Test
  public void testTorchScriptModelRun() {
    // $ redis-cli -x AI.MODELSTORE iris TORCH CPU BLOB < iris.pt
    AIOperations<String> ai = modulesOperations.opsForAI();

    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource("ai/iris.pt").getFile());
    String modelPath = file.getAbsolutePath();

    ai.setModel("iris-torch", Backend.TORCH, Device.CPU, new String[] { "iris:in" },
        new String[] { "iris:inferences", "iris:scores" }, modelPath);

    // AI.TENSORSET iris:in FLOAT 2 4 VALUES 5.0 3.4 1.6 0.4 6.0 2.2 5.0 1.5
    ai.setTensor("iris:in", new double[][] { { 5.0, 3.4, 1.6, 0.4 }, { 6.0, 2.2, 5.0, 1.5 } }, new int[] { 2, 4 });

    // AI.MODELEXECUTE iris INPUTS 1 iris:in OUTPUTS 2 iris:inferences iris:scores
    Assert.assertTrue(
        ai.runModel("iris-torch", new String[] { "iris:in" }, new String[] { "iris:inferences", "iris:scores" }));

    Tensor in = ai.getTensor("iris:in");
    System.out.println(">>> IN --> " + in);

    // AI.TENSORGET iris:inferences VALUES
    Tensor inferences = ai.getTensor("iris:inferences");
    System.out.println(">>> INFERENCES --> " + inferences);

    // AI.TENSORGET iris:scores VALUES
    Tensor scores = ai.getTensor("iris:scores");
    System.out.println(">>> SCORES --> " + scores);
  }
}
