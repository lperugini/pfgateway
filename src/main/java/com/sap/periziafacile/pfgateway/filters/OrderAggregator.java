package com.sap.periziafacile.pfgateway.filters;

import org.json.JSONObject;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;

import com.sap.periziafacile.pfgateway.helpers.ServiceContainer;

import reactor.core.publisher.Mono;

@Component
public class OrderAggregator implements GatewayFilter {

    private final RestTemplate restTemplate;

    public OrderAggregator(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest rq = exchange.getRequest();
        String path = rq.getPath().toString();

        CompletableFuture<String> orderfuture = getOrder(path);

        Mono<Void> result = Mono
                .fromFuture(orderfuture)
                .flatMap(order -> {
                    JSONObject jsonresponse = new JSONObject();
                    JSONObject jsonorder = new JSONObject(order);
                    jsonresponse.put("order", jsonorder);

                    CompletableFuture<String> customer = getCustomer(jsonorder.get("customer").toString());
                    String customer_ = customer.join();

                    JSONObject jsoncustomer = new JSONObject(customer_);
                    jsonresponse.put("customer", jsoncustomer);

                    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    byte[] bytes = jsonresponse.toString().getBytes();
                    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                    return exchange.getResponse().writeWith(Mono.just(buffer));
                })
                .onErrorResume(e -> {
                    System.out.println(e);
                    exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                    return exchange.getResponse().setComplete();
                });

        return result;

    }

    private CompletableFuture<String> getOrder(String id) {
        Optional<String> uri = ServiceContainer.getService("orderservice");
        if (uri.isPresent())
            return CompletableFuture
                    .supplyAsync(() -> restTemplate.getForObject(uri.get() + id, String.class));

        return CompletableFuture
                .supplyAsync(() -> "error: orderservice not found");
    }

    private CompletableFuture<String> getCustomer(String id) {
        Optional<String> uri = ServiceContainer.getService("customerservice");
        if (uri.isPresent())
            return CompletableFuture
                    .supplyAsync(() -> restTemplate.getForObject(uri.get() + "/customers/%s".formatted(id),
                            String.class));

        return CompletableFuture
                .supplyAsync(() -> "error: customerservice not found");
    }

}
