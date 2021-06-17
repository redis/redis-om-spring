package com.redislabs.spring.ops.graph;

import java.util.List;

import javax.annotation.PreDestroy;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.redislabs.redisgraph.Header;
import com.redislabs.redisgraph.ResultSet;
import com.redislabs.redisgraph.Statistics.Label;
import com.redislabs.spring.ops.RedisModulesOperations;

@SpringBootTest(classes = OpsForGraphTest.Config.class)
public class OpsForGraphTest {
  @Autowired
  RedisModulesOperations<String, String> modulesOperations;


  @Test
  public void testSimpleGraph() {
    GraphOperations<String> ops = modulesOperations.opsForGraph();
    // Create both source and destination nodes
    Assert.assertNotNull(ops.query("social", "CREATE (:person{name:'roi',age:32})"));
    Assert.assertNotNull(ops.query("social", "CREATE (:person{name:'amit',age:30})"));

    // Connect source and destination nodes.
    ResultSet resultSet = ops.query("social",
        "MATCH (a:person), (b:person) WHERE (a.name = 'roi' AND b.name='amit')  CREATE (a)-[:knows]->(b)");

    Assert.assertFalse(resultSet.hasNext());
    Assert.assertNull(resultSet.getStatistics().getStringValue(Label.NODES_CREATED));
    Assert.assertNull(resultSet.getStatistics().getStringValue(Label.PROPERTIES_SET));
    Assert.assertEquals(1, resultSet.getStatistics().relationshipsCreated());
    Assert.assertEquals(0, resultSet.getStatistics().relationshipsDeleted());
    Assert.assertNotNull(resultSet.getStatistics().getStringValue(Label.QUERY_INTERNAL_EXECUTION_TIME));

    ResultSet createIndexResult = ops.query("social", "CREATE INDEX ON :person(age)");
    Assert.assertFalse(createIndexResult.hasNext());
    Assert.assertEquals(1, createIndexResult.getStatistics().indicesAdded());

    ResultSet queryResult = ops.query("social", "MATCH (a:person)-[r:knows]->(b:person) RETURN a,r, a.age");

    Header header = queryResult.getHeader();
    Assert.assertNotNull(header);
    Assert.assertEquals(
        "HeaderImpl{" + "schemaTypes=[COLUMN_SCALAR, COLUMN_SCALAR, COLUMN_SCALAR], " + "schemaNames=[a, r, a.age]}",
        header.toString());

    List<String> schemaNames = header.getSchemaNames();

    Assert.assertNotNull(schemaNames);
    Assert.assertEquals(3, schemaNames.size());
    Assert.assertEquals("a", schemaNames.get(0));
    Assert.assertEquals("r", schemaNames.get(1));
    Assert.assertEquals("a.age", schemaNames.get(2));

    ops.deleteGraph("social");
  }

  @SpringBootApplication
  @Configuration
  static class Config {
    @Autowired
    RedisConnectionFactory connectionFactory;
    
    @PreDestroy
    void cleanUp() {
      connectionFactory.getConnection().flushAll();
    }
  }
}
