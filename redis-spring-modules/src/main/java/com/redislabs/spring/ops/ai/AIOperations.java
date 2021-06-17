package com.redislabs.spring.ops.ai;

import java.util.List;
import java.util.Map;

import com.redislabs.redisai.Backend;
import com.redislabs.redisai.Dag;
import com.redislabs.redisai.Device;
import com.redislabs.redisai.Model;
import com.redislabs.redisai.Script;
import com.redislabs.redisai.Tensor;

public interface AIOperations<K> {
  boolean setTensor(K key, Object values, int[] shape);

  boolean setTensor(K key, Tensor tensor);

  Tensor getTensor(K key);

  boolean setModel(K key, Backend backend, Device device, String[] inputs, String[] outputs, String modelPath);

  boolean setModel(K key, Model model);

  boolean storeModel(K key, Model model);

  Model getModel(K key);

  boolean delModel(K key);

  boolean setScriptFile(K key, Device device, String scriptFile);

  boolean setScript(K key, Device device, String source);

  boolean setScript(K key, Script script);

  Script getScript(K key);

  boolean delScript(K key);

  boolean runModel(K key, String[] inputs, String[] outputs);

  boolean executeModel(K key, String[] inputs, String[] outputs);

  boolean executeModel(K key, String[] inputs, String[] outputs, long timeout);

  boolean runScript(K key, String function, String[] inputs, String[] outputs);

  List<?> dagRun(String[] loadKeys, String[] persistKeys, Dag dag);

  List<?> dagRunReadOnly(String[] loadKeys, Dag dag);
  
  List<?> dagExecute(String[] loadTensors, String[] persistTensors, String routingHint, Dag dag);

  List<?> dagExecuteReadOnly(String[] loadKeys, String routingHint, Dag dag);

  Map<String, Object> getInfo(K key);

  boolean resetStat(K key);

  boolean setBackendsPath(String path);

  boolean loadBackend(Backend backEnd, String path);

}
