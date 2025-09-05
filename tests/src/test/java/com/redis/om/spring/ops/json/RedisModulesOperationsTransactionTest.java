package com.redis.om.spring.ops.json;

import com.redis.om.spring.AbstractBaseDocumentTest;
import com.redis.om.spring.ops.RedisModulesOperations;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test to reproduce issue #377: JSON operations not participating in Redis transactions
 * 
 * The issue is that when using JSON.SET within a Redis transaction (WATCH/MULTI/EXEC),
 * the JSON operation is executed outside the transaction on a different connection,
 * defeating the purpose of using transactions for atomicity.
 */
public class RedisModulesOperationsTransactionTest extends AbstractBaseDocumentTest {

    @Autowired
    private RedisModulesOperations<String> redisModulesOperations;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, String> template;
    
    private static final String TEST_KEY = "issue377:testkey";
    private static final String COUNTER_KEY = "issue377:counter";
    
    @BeforeEach
    void setup() {
        // Clean up test keys
        template.delete(TEST_KEY);
        template.delete(COUNTER_KEY);
    }
    
    @Test
    void testTransactionWithWatchProtectsAgainstConcurrentModification() {
        // This test verifies that WATCH properly protects against concurrent modifications
        // when JSON operations are correctly participating in transactions
        
        // Set initial value
        TestObject initialObject = new TestObject("initial", 1);
        redisModulesOperations.opsForJSON().set(TEST_KEY, initialObject);
        
        // First transaction: Read value, then modify it
        List<Object> result1 = stringRedisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                // Watch the key
                operations.watch(TEST_KEY);
                
                // Read current value (simulating decision based on current state)
                TestObject current = redisModulesOperations.opsForJSON().get(TEST_KEY, TestObject.class);
                assertThat(current.getName()).isEqualTo("initial");
                
                // Simulate another client modifying the key between WATCH and EXEC
                // This would happen in a real concurrent scenario
                redisModulesOperations.opsForJSON().set(TEST_KEY, new TestObject("modified-by-other", 99));
                
                // Now try to execute our transaction
                operations.multi();
                redisModulesOperations.opsForJSON().set(TEST_KEY, new TestObject("our-change", 2));
                return operations.exec();
            }
        });
        
        // Transaction should fail because the watched key was modified
        // Failed transactions return an empty list
        assertThat(result1).isEmpty();
        
        // The value should be from the concurrent modification, not our transaction
        TestObject finalObject = redisModulesOperations.opsForJSON().get(TEST_KEY, TestObject.class);
        assertThat(finalObject.getName()).isEqualTo("modified-by-other");
        assertThat(finalObject.getVersion()).isEqualTo(99);
    }
    
    @Test
    void testTransactionAtomicityForMultipleOperations() {
        // This test verifies that JSON and regular Redis operations
        // are atomic when executed in a transaction
        
        // Set initial values
        TestObject initialObject = new TestObject("initial", 1);
        redisModulesOperations.opsForJSON().set(TEST_KEY, initialObject);
        stringRedisTemplate.opsForValue().set(COUNTER_KEY, "0");
        
        // Execute a transaction with multiple operations
        List<Object> result = stringRedisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                
                // Queue multiple operations
                redisModulesOperations.opsForJSON().set(TEST_KEY, new TestObject("updated", 2));
                operations.opsForValue().increment(COUNTER_KEY);
                operations.opsForValue().increment(COUNTER_KEY);
                operations.opsForValue().increment(COUNTER_KEY);
                
                return operations.exec();
            }
        });
        
        // All operations should have succeeded atomically
        assertThat(result).isNotNull();
        assertThat(result).hasSize(4); // 1 JSON.SET + 3 INCREMENTs
        
        // Verify final state - all operations executed
        String counterValue = stringRedisTemplate.opsForValue().get(COUNTER_KEY);
        assertThat(counterValue).isEqualTo("3");
        
        TestObject currentObject = redisModulesOperations.opsForJSON().get(TEST_KEY, TestObject.class);
        assertThat(currentObject.getName()).isEqualTo("updated");
        assertThat(currentObject.getVersion()).isEqualTo(2);
    }
    
    @Test
    void testTransactionRollbackOnWatchedKeyChange() {
        // This test verifies proper rollback behavior when a watched key changes
        
        // Set initial values
        redisModulesOperations.opsForJSON().set(TEST_KEY, new TestObject("initial", 1));
        stringRedisTemplate.opsForValue().set(COUNTER_KEY, "10");
        
        // Execute transaction that will be aborted due to watched key change
        List<Object> result = stringRedisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                // Watch the counter key
                operations.watch(COUNTER_KEY);
                
                // Read initial value
                String initialCounter = (String) operations.opsForValue().get(COUNTER_KEY);
                assertThat(initialCounter).isEqualTo("10");
                
                // Modify watched key outside transaction (simulating concurrent modification)
                stringRedisTemplate.opsForValue().set(COUNTER_KEY, "99");
                
                // Try to execute transaction - should fail
                operations.multi();
                redisModulesOperations.opsForJSON().set(TEST_KEY, new TestObject("should-not-be-set", 999));
                operations.opsForValue().set(COUNTER_KEY, "100");
                return operations.exec();
            }
        });
        
        // Transaction should have been aborted
        // Aborted transactions return an empty list
        assertThat(result).isEmpty();
        
        // Neither operation should have been applied
        TestObject obj = redisModulesOperations.opsForJSON().get(TEST_KEY, TestObject.class);
        assertThat(obj.getName()).isEqualTo("initial"); // JSON unchanged
        assertThat(obj.getVersion()).isEqualTo(1);
        
        String counter = stringRedisTemplate.opsForValue().get(COUNTER_KEY);
        assertThat(counter).isEqualTo("99"); // Counter has concurrent modification value
    }
    
    @Test 
    void testJsonOperationExecutesOutsideTransaction_ShowsIssue() {
        // This test clearly demonstrates the issue: JSON operations execute immediately,
        // not within the transaction boundary
        
        TestObject initialObject = new TestObject("initial", 1);
        redisModulesOperations.opsForJSON().set(TEST_KEY, initialObject);
        
        // Create a flag to track if JSON operation executed before transaction completes
        final boolean[] jsonExecutedBeforeExec = {false};
        
        List<Object> result = template.execute(new SessionCallback<List<Object>>() {
            @Override
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                
                // This JSON.SET should be queued but executes immediately (bug)
                redisModulesOperations.opsForJSON().set(TEST_KEY, new TestObject("insideTransaction", 2));
                
                // Read the value - if transaction is working, should still be "initial"
                // But due to bug, it will already be "insideTransaction"
                TestObject valueBeforeExec = redisModulesOperations.opsForJSON().get(TEST_KEY, TestObject.class);
                
                if ("insideTransaction".equals(valueBeforeExec.getName())) {
                    jsonExecutedBeforeExec[0] = true;
                }
                
                // Regular Redis operations are properly queued
                operations.opsForValue().set(COUNTER_KEY, "1");
                
                return operations.exec();
            }
        });
        
        // This assertion now passes with our fix - JSON operations are properly queued in transactions
        assertThat(jsonExecutedBeforeExec[0])
            .as("JSON.SET should be queued in transaction, not executed immediately")
            .isFalse();
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestObject {
        private String name;
        private int version;
    }
}