package com.notification_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String NOTIFICATION_EXCHANGE     = "notification.exchange";
    public static final String LOGIN_NOTIFICATION_QUEUE  = "login.notification.queue";
    public static final String REGISTER_NOTIFICATION_QUEUE = "register.notification.queue";
    public static final String LOGIN_ROUTING_KEY         = "notification.login";
    public static final String REGISTER_ROUTING_KEY      = "notification.register";

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Queue loginNotificationQueue() {
        return QueueBuilder.durable(LOGIN_NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Queue registerNotificationQueue() {
        return QueueBuilder.durable(REGISTER_NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Binding loginBinding() {
        return BindingBuilder.bind(loginNotificationQueue())
                .to(notificationExchange())
                .with(LOGIN_ROUTING_KEY);
    }

    @Bean
    public Binding registerBinding() {
        return BindingBuilder.bind(registerNotificationQueue())
                .to(notificationExchange())
                .with(REGISTER_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
