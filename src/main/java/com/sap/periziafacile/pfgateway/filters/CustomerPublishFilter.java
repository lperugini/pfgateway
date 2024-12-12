package com.sap.periziafacile.pfgateway.filters;

import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.sap.periziafacile.pfgateway.messaging.CustomerMessageConfig;
import reactor.core.publisher.Mono;

@Component
public class CustomerPublishFilter implements GatewayFilter {

    private final RabbitTemplate rabbitTemplate;

    public CustomerPublishFilter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        return exchange
                .getRequest()
                .getBody()
                .collectList()
                .map(body -> {
                    StringBuilder stringBuilder = new StringBuilder();
                    body.forEach(dataBuffer -> stringBuilder.append(dataBuffer.toString(StandardCharsets.UTF_8)));
                    return stringBuilder.toString();
                })
                .flatMap(body -> {
                    JSONObject customerJson = new JSONObject(body);
                    customerJson.put("timestamp", System.currentTimeMillis()); // Aggiunge un timestamp

                    System.out.println(customerJson);

                    // Pubblica il cliente su RabbitMQ
                    rabbitTemplate.convertAndSend(
                            CustomerMessageConfig.EXCHANGE_NAME,
                            CustomerMessageConfig.ROUTING_KEY,
                            customerJson.toString());

                    System.out.println("Published message: " + customerJson.toString());
                    return Mono.empty();
                });
    }

}
