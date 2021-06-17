package com.redislabs.spring.ops.graph;

import java.util.List;
import java.util.Map;

import com.redislabs.redisgraph.ResultSet;

public interface GraphOperations<K>  {
  ResultSet query(K graphId, String query);
  ResultSet readOnlyQuery(K graphId, String query);
  ResultSet query(K graphId, String query, long timeout);
  ResultSet readOnlyQuery(K graphId, String query, long timeout);
  ResultSet query(K graphId, String query, Map<String, Object> params);
  ResultSet readOnlyQuery(K graphId, String query, Map<String, Object> params);
  ResultSet query(K graphId, String query, Map<String, Object> params, long timeout);
  ResultSet readOnlyQuery(K graphId, String query, Map<String, Object> params, long timeout);
  ResultSet callProcedure(K graphId, String procedure);
  ResultSet callProcedure(K graphId, String procedure, List<String> args);
  ResultSet callProcedure(K graphId, String procedure, List<String> args  , Map<String, List<String>> kwargs);
  String deleteGraph(K graphId);
}
