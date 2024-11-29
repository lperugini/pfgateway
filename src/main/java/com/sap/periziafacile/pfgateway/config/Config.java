package com.sap.periziafacile.pfgateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.sap.periziafacile.pfgateway.filters.AggregationFilter;
import com.sap.periziafacile.pfgateway.filters.CustomerAggregator;
import com.sap.periziafacile.pfgateway.filters.OrderAggregator;

@Configuration
public class Config {

        @Bean
        RouteLocator customRouteLocator(RouteLocatorBuilder builder,
                        UriConfiguration uriConfiguration,
                        AggregationFilter aggregationFilter,
                        OrderAggregator orderAggregator,
                        CustomerAggregator customerAggregator) {

                UriConfiguration customerservice = new UriConfiguration("customerservice", "http://localhost:8082");
                UriConfiguration orderservice = new UriConfiguration("orderservice", "http://localhost:8083");

                return builder
                                .routes()
                                .route("composite", r -> r.path("/composite/**")
                                                .filters(f -> f.filter(aggregationFilter))
                                                .uri("http://localhost:8080"))
                                .route(p -> p
                                                .path("/customers")
                                                .uri(customerservice.getUrl()))
                                .route(p -> p
                                                .path("/orders")
                                                .uri(orderservice.getUrl()))
                                .route(p -> p
                                                .path("/customers/**")
                                                .filters(f -> f.filter(customerAggregator))
                                                .uri(customerservice.getUrl()))
                                .route(p -> p
                                                .path("/orders/**")
                                                .filters(f -> f.filter(orderAggregator))
                                                .uri(orderservice.getUrl()))

                                /*
                                 * .route(p -> p
                                 * .host("*.circuitbreaker.com")
                                 * .filters(f -> f
                                 * .circuitBreaker(config -> config
                                 * .setName("mycmd")
                                 * .setFallbackUri("forward:/fallback")))
                                 * .uri(httpUri))
                                 */
                                .build();
        }

        @Bean
        RestTemplate restTemplate() {
                return new RestTemplate();
        }
}
