package com.redis.spring.ops.graph;

import java.util.List;
import java.util.Map;

import com.redis.spring.client.RedisModulesClient;
import com.redislabs.redisgraph.ResultSet;

public class GraphOperationsImpl<K> implements GraphOperations<K> {
  
  RedisModulesClient client;

  public GraphOperationsImpl(RedisModulesClient client) {
    this.client = client;
  }

  @Override
  public ResultSet query(K graphId, String query) {
    return client.clientForGraph().query(graphId.toString(), query);
  }

  @Override
  public ResultSet readOnlyQuery(K graphId, String query) {
    return client.clientForGraph().readOnlyQuery(graphId.toString(), query);
  }

  @Override
  public ResultSet query(K graphId, String query, long timeout) {
    return client.clientForGraph().query(graphId.toString(), query, timeout);
  }

  @Override
  public ResultSet readOnlyQuery(K graphId, String query, long timeout) {
    return client.clientForGraph().readOnlyQuery(graphId.toString(), query, timeout);
  }

  @Override
  public ResultSet query(K graphId, String query, Map<String, Object> params) {
    return client.clientForGraph().query(graphId.toString(), query, params);
  }

  @Override
  public ResultSet readOnlyQuery(K graphId, String query, Map<String, Object> params) {
    return client.clientForGraph().readOnlyQuery(graphId.toString(), query, params);
  }

  @Override
  public ResultSet query(K graphId, String query, Map<String, Object> params, long timeout) {
    return client.clientForGraph().query(graphId.toString(), query, params, timeout);
  }

  @Override
  public ResultSet readOnlyQuery(K graphId, String query, Map<String, Object> params, long timeout) {
    return client.clientForGraph().readOnlyQuery(graphId.toString(), query, params, timeout);
  }

  @Override
  public ResultSet callProcedure(K graphId, String procedure) {
    return client.clientForGraph().callProcedure(graphId.toString(), procedure);
  }

  @Override
  public ResultSet callProcedure(K graphId, String procedure, List<String> args) {
    return client.clientForGraph().callProcedure(graphId.toString(), procedure, args);
  }

  @Override
  public ResultSet callProcedure(K graphId, String procedure, List<String> args, Map<String, List<String>> kwargs) {
    return client.clientForGraph().callProcedure(graphId.toString(), procedure, args, kwargs);
  }

  @Override
  public String deleteGraph(K graphId) {
    return client.clientForGraph().deleteGraph(graphId.toString());
  }

}
