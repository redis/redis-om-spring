package com.redis.om.spring.ops.ai;

import java.util.List;
import java.util.Map;

import com.redis.om.spring.client.RedisModulesClient;
import com.redislabs.redisai.Backend;
import com.redislabs.redisai.Dag;
import com.redislabs.redisai.Device;
import com.redislabs.redisai.Model;
import com.redislabs.redisai.Script;
import com.redislabs.redisai.Tensor;

public class AIOperationsImpl<K> implements AIOperations<K> {
  
  RedisModulesClient client;

  public AIOperationsImpl(RedisModulesClient client) {
    this.client = client;
  }

  @Override
  public boolean setTensor(K key, Object values, int[] shape) {
    return client.clientForAI().setTensor(key.toString(), values, shape);
  }

  @Override
  public boolean setTensor(K key, Tensor tensor) {
    return client.clientForAI().setTensor(key.toString(), tensor);
  }

  @Override
  public Tensor getTensor(K key) {
    return client.clientForAI().getTensor(key.toString());
  }

  @Override
  public boolean setModel(K key, Backend backend, Device device, String[] inputs, String[] outputs, String modelPath) {
    return client.clientForAI().setModel(key.toString(), backend, device, inputs, outputs, modelPath);
  }

  @Override
  public boolean setModel(K key, Model model) {
    return client.clientForAI().setModel(key.toString(), model);
  }

  @Override
  public boolean storeModel(K key, Model model) {
    return client.clientForAI().storeModel(key.toString(), model);
  }

  @Override
  public Model getModel(K key) {
    return client.clientForAI().getModel(key.toString());
  }

  @Override
  public boolean delModel(K key) {
    return client.clientForAI().delModel(key.toString());
  }

  @Override
  public boolean setScriptFile(K key, Device device, String scriptFile) {
    return client.clientForAI().setScript(key.toString(), device, scriptFile);
  }

  @Override
  public boolean setScript(K key, Device device, String source) {
    return client.clientForAI().setScript(key.toString(), device, source);
  }

  @Override
  public boolean setScript(K key, Script script) {
    return client.clientForAI().setScript(key.toString(), script);
  }

  @Override
  public Script getScript(K key) {
    return client.clientForAI().getScript(key.toString());
  }

  @Override
  public boolean delScript(K key) {
    return client.clientForAI().delScript(key.toString());
  }

  @Override
  public boolean runModel(K key, String[] inputs, String[] outputs) {
    return client.clientForAI().runModel(key.toString(), inputs, outputs);
  }

  @Override
  public boolean executeModel(K key, String[] inputs, String[] outputs) {
    return client.clientForAI().executeModel(key.toString(), inputs, outputs);
  }

  @Override
  public boolean executeModel(K key, String[] inputs, String[] outputs, long timeout) {
    return client.clientForAI().executeModel(key.toString(), inputs, outputs, timeout);
  }

  @Override
  public boolean runScript(K key, String function, String[] inputs, String[] outputs) {
    return client.clientForAI().runScript(key.toString(), function, inputs, outputs);
  }

  @Override
  public List<?> dagRun(String[] loadKeys, String[] persistKeys, Dag dag) {
    return client.clientForAI().dagRun(loadKeys, persistKeys, dag);
  }

  @Override
  public List<?> dagRunReadOnly(String[] loadKeys, Dag dag) {
    return client.clientForAI().dagRunReadOnly(loadKeys, dag);
  }

  @Override
  public List<?> dagExecute(String[] loadTensors, String[] persistTensors, String routingHint, Dag dag) {
    return client.clientForAI().dagExecute(loadTensors, persistTensors, routingHint, dag);
  }

  @Override
  public List<?> dagExecuteReadOnly(String[] loadKeys, String routingHint, Dag dag) {
    return client.clientForAI().dagExecuteReadOnly(loadKeys, routingHint, dag);
  }

  @Override
  public Map<String, Object> getInfo(K key) {
    return client.clientForAI().getInfo(key.toString());
  }

  @Override
  public boolean resetStat(K key) {
    return client.clientForAI().resetStat(key.toString());
  }

  @Override
  public boolean setBackendsPath(String path) {
    return client.clientForAI().setBackendsPath(path);
  }

  @Override
  public boolean loadBackend(Backend backEnd, String path) {
    return client.clientForAI().loadBackend(backEnd, path);
  }

}
