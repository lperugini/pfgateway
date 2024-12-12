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
public class CustomerAggregator implements GatewayFilter {

    private final RestTemplate restTemplate;

    public CustomerAggregator(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest rq = exchange.getRequest();
        String path = rq.getPath().toString();

        CompletableFuture<String> customerfuture = getCustomer(path);

        Mono<Void> result = Mono
                .fromFuture(customerfuture)
                .flatMap(customer -> {
                    JSONObject jsonresponse = new JSONObject();
                    JSONObject jsoncustomer = new JSONObject(customer);
                    jsonresponse.put("customer", jsoncustomer);

                    CompletableFuture<String> orders = getOrders(jsoncustomer.get("id").toString());
                    String orders_ = orders.join();

                    JSONObject jsonorders = new JSONObject(orders_);
                    jsonresponse.put("orders", jsonorders);

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

    private CompletableFuture<String> getCustomer(String id) {
        Optional<String> uri = ServiceContainer.getService("customerservice");
        if (uri.isPresent())
            return CompletableFuture
                    .supplyAsync(() -> restTemplate.getForObject(uri.get() + id, String.class));

        return CompletableFuture
                .supplyAsync(() -> "error: customerservice not found");
    }

    private CompletableFuture<String> getOrders(String customerid) {
        Optional<String> uri = ServiceContainer.getService("orderservice");
        if (uri.isPresent())
            return CompletableFuture
                    .supplyAsync(() -> restTemplate.getForObject(uri.get() + "/orders/forcustomer/%s".formatted(customerid),
                            String.class));

        return CompletableFuture
                .supplyAsync(() -> "error: orderservice not found");
    }

}
