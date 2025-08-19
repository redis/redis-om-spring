package com.redis.om.spring.repository.query.clause;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class NumericInQueryClauseTest {

    @Test
    void testNumericInClauseExists() {
        // Verify that NUMERIC_IN clause is defined
        QueryClause numericIn = QueryClause.NUMERIC_IN;
        assertNotNull(numericIn);
        assertThat(numericIn.name()).isEqualTo("NUMERIC_IN");
    }

    @Test
    void testNumericNotInClauseExists() {
        // Verify that NUMERIC_NOT_IN clause is defined
        QueryClause numericNotIn = QueryClause.NUMERIC_NOT_IN;
        assertNotNull(numericNotIn);
        assertThat(numericNotIn.name()).isEqualTo("NUMERIC_NOT_IN");
    }

    @Test
    void testQueryClauseEnumContainsNumericInClauses() {
        // Verify that NUMERIC_IN and NUMERIC_NOT_IN are properly included in the enum
        QueryClause[] allClauses = QueryClause.values();
        
        boolean foundNumericIn = false;
        boolean foundNumericNotIn = false;
        
        for (QueryClause clause : allClauses) {
            if (clause == QueryClause.NUMERIC_IN) {
                foundNumericIn = true;
            }
            if (clause == QueryClause.NUMERIC_NOT_IN) {
                foundNumericNotIn = true;
            }
        }
        
        assertTrue(foundNumericIn, "NUMERIC_IN clause should be present in QueryClause enum");
        assertTrue(foundNumericNotIn, "NUMERIC_NOT_IN clause should be present in QueryClause enum");
    }
    
    @Test
    void testNumericInClauseValueOf() {
        // Test that valueOf works for the new clauses
        QueryClause numericIn = QueryClause.valueOf("NUMERIC_IN");
        assertNotNull(numericIn);
        
        QueryClause numericNotIn = QueryClause.valueOf("NUMERIC_NOT_IN");
        assertNotNull(numericNotIn);
    }
    
    @Test
    void testNumericClausesInEnumOrder() {
        // Verify the clauses are in the enum
        Arrays.stream(QueryClause.values())
            .map(QueryClause::name)
            .filter(name -> name.contains("NUMERIC_IN") || name.contains("NUMERIC_NOT_IN"))
            .forEach(name -> {
                assertThat(name).isIn("NUMERIC_IN", "NUMERIC_NOT_IN");
            });
    }
}