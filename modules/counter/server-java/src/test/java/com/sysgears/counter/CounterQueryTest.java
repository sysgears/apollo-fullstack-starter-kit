package com.sysgears.counter;

import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import com.sysgears.counter.model.Counter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class CounterQueryTest {
    @Autowired
    private GraphQLTestTemplate graphQLTestTemplate;

    @Test
    void serverCounter() throws IOException {
        GraphQLResponse response = graphQLTestTemplate.postForResource("query/server-counter.graphql");

        assertTrue(response.isOk());
        assertEquals(1, response.get("$.data.serverCounter", Counter.class).getAmount());
    }
}
