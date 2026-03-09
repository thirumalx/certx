package io.github.thirumalx.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.thirumalx.dto.Notification;
import io.github.thirumalx.service.NotificationService;

/**
 * @author Thirumal M
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/certificate/{certificateId}")
    public List<Notification> getNotificationsByCertificate(@PathVariable Long certificateId) {
        logger.debug("Getting notifications for certificate with ID: {}", certificateId);
        return notificationService.getNotificationsByCertificate(certificateId);
    }

    @org.springframework.web.bind.annotation.PostMapping("/notify/client/{clientId}/certificate/{certificateId}")
    public Long notifyClient(@PathVariable Long clientId, @PathVariable Long certificateId) {
        logger.info("Manually triggering notification for client {} and certificate {}", clientId, certificateId);
        return notificationService.createNotification(certificateId, clientId, 0);
    }

}
