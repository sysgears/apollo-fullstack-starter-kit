package com.sysgears.counter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import com.sysgears.counter.model.Counter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
public class CounterMutationTest {
    @Autowired
    private GraphQLTestTemplate graphQLTestTemplate;

    @Test
    void addServerCounter() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("amount", 2);

        GraphQLResponse response = graphQLTestTemplate.perform("mutation/add-server-counter.graphql", node);

        assertTrue(response.isOk());
        assertEquals(3, response.get("$.data.addServerCounter", Counter.class).getAmount());
    }
}
