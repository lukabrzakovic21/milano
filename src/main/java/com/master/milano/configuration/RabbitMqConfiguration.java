package com.master.milano.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfiguration {


    public static final String ITEM_AVAILABLE_AGAIN = "item.available.again";
    public static final String ITEM_NO_LONGER_AVAILABLE = "item.no.longer.available.again";

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue itemNoLongerAvailable() {
        return new Queue(ITEM_NO_LONGER_AVAILABLE, false);
    }

    @Bean
    public Queue itemAvailableAgain() {
        return new Queue(ITEM_AVAILABLE_AGAIN, false);
    }
}
