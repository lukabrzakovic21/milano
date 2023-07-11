package com.master.milano.service;

import com.master.milano.common.event.ItemAvailableAgain;
import com.master.milano.common.event.ItemNoLongerAvailable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static com.master.milano.configuration.RabbitMqConfiguration.ITEM_AVAILABLE_AGAIN;
import static com.master.milano.configuration.RabbitMqConfiguration.ITEM_NO_LONGER_AVAILABLE;


@Service
public class RabbitMqService {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMqService.class);
    private final RabbitTemplate rabbitTemplate;

    public RabbitMqService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void itemAvailableAgain(ItemAvailableAgain item) {
        logger.info("Sending event to queue: {}  with body {}.", ITEM_AVAILABLE_AGAIN, item);
        rabbitTemplate.convertAndSend(ITEM_AVAILABLE_AGAIN, item);
        logger.info("Send event to queue: {}  with body {}.", ITEM_AVAILABLE_AGAIN, item);
    }

    public void itemNoMoreAvailable(ItemNoLongerAvailable item) {
        logger.info("Sending event to queue: {}  with body {}.",ITEM_NO_LONGER_AVAILABLE , item);
        rabbitTemplate.convertAndSend(ITEM_NO_LONGER_AVAILABLE, item);
        logger.info("Send event to queue: {}  with body {}.", ITEM_NO_LONGER_AVAILABLE, item);
    }
}
