package com.paw.notifications.rabbit;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.paw.notifications.dto.NotificationEvent;
import com.paw.notifications.service.NotificationService;

@Component
@ConditionalOnProperty(prefix = "paw.notifications.rabbit", name = "enabled", havingValue = "true")
public class NotificationEventConsumer {
    private final NotificationService notificationService;

    public NotificationEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "${paw.notifications.rabbit.queue}")
    public void consume(NotificationEvent event) {
        notificationService.createFromEvent(event);
    }
}