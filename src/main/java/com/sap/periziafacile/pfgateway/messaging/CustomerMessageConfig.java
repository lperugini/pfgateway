package com.sap.periziafacile.pfgateway.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class CustomerMessageConfig {

    public static String EXCHANGE_NAME = "customerservice.exchange";

    public static String ROUTING_KEY = "customerservice.routingKey";

    public static String QUEUE_NAME = "customerservice.queue";

    @Bean
    Queue queue() {
        // Dichiarazione della coda
        return new Queue(QUEUE_NAME);
    }

    @Bean
    public Binding binding(Queue customerQueue, DirectExchange customerExchange) {
        // Binding tra la coda e l'exchange
        return BindingBuilder.bind(customerQueue).to(customerExchange).with(ROUTING_KEY);

    }

    @Bean
    public DirectExchange customerExchange() {
        // Dichiarazione dell'exchange
        return new DirectExchange(EXCHANGE_NAME);
    }

}
