package com.sap.periziafacile.pfgateway.config;

import java.util.Optional;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import com.sap.periziafacile.pfgateway.filters.AggregationFilter;
import com.sap.periziafacile.pfgateway.filters.CustomerAggregator;
import com.sap.periziafacile.pfgateway.filters.OrderAggregator;
import com.sap.periziafacile.pfgateway.filters.OrderPublishFilter;
import com.sap.periziafacile.pfgateway.helpers.ServiceContainer;

@Configuration
public class Config {

        @Bean
        RouteLocator customRouteLocator(RouteLocatorBuilder builder,
                        UriConfiguration uriConfiguration,
                        AggregationFilter aggregationFilter,
                        OrderAggregator orderAggregator,
                        OrderPublishFilter orderPublishFilter,
                        CustomerAggregator customerAggregator) {

                Optional<String> customerservice = ServiceContainer.getService("customerservice");
                Optional<String> orderservice = ServiceContainer.getService("orderservice");
                Optional<String> authservice = ServiceContainer.getService("authservice");

                return builder
                                .routes()
                                /* .route("composite", p -> p.path("/composite/**")
                                                .filters(f -> f.filter(aggregationFilter))
                                                .uri("http://localhost:8080")) */
                                .route(p -> p
                                                .path("/orders")
                                                .and()
                                                .method(HttpMethod.GET)
                                                .uri(orderservice.get()))
                                .route(p -> p
                                                .path("/orders/**")
                                                .and()
                                                .method(HttpMethod.GET)
                                                .filters(f -> f.filter(orderAggregator))
                                                .uri("http://localhost:8080"))
                                .route(p -> p
                                                .method(HttpMethod.POST)
                                                .or()
                                                .path("/orders")
                                                .filters(f -> f.filter(orderPublishFilter))
                                                .uri("http://localhost:8080"))
                                .route(p -> p
                                                .path("/orders/**")
                                                .and()
                                                .method(HttpMethod.PUT)
                                                .uri("http://localhost:8080"))
                                .route(p -> p
                                                .path("/customers/**")
                                                .and()
                                                .method(HttpMethod.GET)
                                                .filters(f -> f.filter(customerAggregator))
                                                .uri("http://localhost:8080"))
                                .route(p -> p
                                                .path("/customers")
                                                .and()
                                                .method(HttpMethod.GET)
                                                .uri(customerservice.get()))
                                .route(p -> p
                                                .path("/users")
                                                .uri(authservice.get()))
                                .route(p -> p
                                                .path("/users/**")
                                                .uri(authservice.get()))
                                .route(p -> p
                                                .path("/auth/login")
                                                .uri(authservice.get()))
                                .route(p -> p
                                                .path("/auth/register")
                                                .uri(authservice.get()))
                                .route(p -> p
                                                .path("/auth/test")
                                                .uri(authservice.get()))
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
