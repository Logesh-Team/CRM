package com.vyg.eis.Notification.controller.Notification;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vyg.eis.Notification.domain.Notification.Notification;
import com.vyg.eis.Notification.service.Notification.NotificationService;

import jakarta.mail.MessagingException;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    
    @PostMapping("/save")
    public ResponseEntity<Notification> createNotification(@RequestBody Map<String, Object> payload,
            @RequestParam("templatePath") String templatePath) throws MessagingException, IOException {
                System.out.println("payload+"+payload);
         Notification savedNotification = notificationService.saveNotification(payload, templatePath);
        
        

        return new ResponseEntity<>(new Notification(), HttpStatus.CREATED);
    }

}
