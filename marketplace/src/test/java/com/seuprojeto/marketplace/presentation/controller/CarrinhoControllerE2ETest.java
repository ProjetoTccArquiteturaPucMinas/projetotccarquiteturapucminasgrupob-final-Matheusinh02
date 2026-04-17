package com.seuprojeto.marketplace.presentation.controller;

import com.seuprojeto.marketplace.application.dto.SelecaoCarrinho;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CarrinhoControllerE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCalculateSingleItemDiscountEndToEnd() throws Exception {
        JsonNode result = calcular(List.of(new SelecaoCarrinho(1L, 1)));

        assertBigDecimalEquals("50.00", result.get("subtotal").asText());
        assertBigDecimalEquals("1.50", result.get("desconto").asText());
        assertBigDecimalEquals("48.50", result.get("total").asText());
    }

    @Test
    void shouldApplyCumulativeDiscountAndMaxLimitEndToEnd() throws Exception {
        JsonNode result = calcular(List.of(new SelecaoCarrinho(2L, 4)));

        assertBigDecimalEquals("400.00", result.get("subtotal").asText());
        assertBigDecimalEquals("100.00", result.get("desconto").asText());
        assertBigDecimalEquals("300.00", result.get("total").asText());
    }

    @Test
    void shouldApplyQuantityAndCategoryDiscountPerItemEndToEnd() throws Exception {
        JsonNode result = calcular(List.of(
                new SelecaoCarrinho(4L, 2),
                new SelecaoCarrinho(5L, 1)
        ));

        assertBigDecimalEquals("140.00", result.get("subtotal").asText());
        assertBigDecimalEquals("18.20", result.get("desconto").asText());
        assertBigDecimalEquals("121.80", result.get("total").asText());
    }

    private JsonNode calcular(List<SelecaoCarrinho> selecoes) throws Exception {
        ResponseEntity<String> response = RestClient.create()
                .post()
                .uri("http://localhost:" + port + "/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .body(selecoes)
                .retrieve()
                .toEntity(String.class);

        assertEquals(200, response.getStatusCode().value());

        return objectMapper.readTree(response.getBody());
    }

    private void assertBigDecimalEquals(String expected, String actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(new BigDecimal(actual)));
    }
}
