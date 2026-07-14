package com.paw.notifications.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "paw.notifications.rabbit", name = "enabled", havingValue = "true")
public class RabbitConfig {
    @Bean
    TopicExchange pawEventsExchange(@Value("${paw.notifications.rabbit.exchange}") String exchange) {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    Queue notificationsQueue(@Value("${paw.notifications.rabbit.queue}") String queue) {
        return new Queue(queue, true);
    }

    @Bean
    Binding usersNgoBinding(Queue notificationsQueue, TopicExchange pawEventsExchange) {
        return BindingBuilder.bind(notificationsQueue).to(pawEventsExchange).with("users.ngo.*");
    }

    @Bean
    Binding usersUserBinding(Queue notificationsQueue, TopicExchange pawEventsExchange) {
        return BindingBuilder.bind(notificationsQueue).to(pawEventsExchange).with("users.user.*");
    }

    @Bean
    Binding adoptionsApplicationBinding(Queue notificationsQueue, TopicExchange pawEventsExchange) {
        return BindingBuilder.bind(notificationsQueue).to(pawEventsExchange).with("adoptions.application.*");
    }

    @Bean
    Binding adoptionsAnimalBinding(Queue notificationsQueue, TopicExchange pawEventsExchange) {
        return BindingBuilder.bind(notificationsQueue).to(pawEventsExchange).with("adoptions.animal.*");
    }

    @Bean
    Binding donationsBinding(Queue notificationsQueue, TopicExchange pawEventsExchange) {
        return BindingBuilder.bind(notificationsQueue).to(pawEventsExchange).with("donations.*");
    }

    @Bean
    Binding campaignsBinding(Queue notificationsQueue, TopicExchange pawEventsExchange) {
        return BindingBuilder.bind(notificationsQueue).to(pawEventsExchange).with("campaigns.*");
    }

    @Bean
    Binding feedPostBinding(Queue notificationsQueue, TopicExchange pawEventsExchange) {
        return BindingBuilder.bind(notificationsQueue).to(pawEventsExchange).with("feed.post.*");
    }
}