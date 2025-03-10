package com.vyg.eis.Notification.service.Notification;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyg.eis.Notification.domain.Notification.Notification;
import com.vyg.eis.Notification.domain.Notification.ReportSchedule;
import com.vyg.eis.Notification.repository.Notification.NotificationRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.mail.SimpleMailMessage;
import java.util.List;
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CommonService commonService;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, CommonService commonService) {
        this.notificationRepository = notificationRepository;
        this.commonService = commonService;
    }

    public Notification saveNotification(Map<String, Object> payload, String mailTemplateFilePath)
            throws IOException, MessagingException {

        Notification notification = new Notification();

        notification.setRecipient((String) payload.get("recipient"));
        notification.setSubject((String) payload.get("subject"));
        notification.setMessage((String) payload.get("message"));
        notification.setType((String) payload.get("type"));
        notification.setCreatedAt(LocalDateTime.now());
        notification.setStatus("PENDING");
        sendEmailStandMode(notification, mailTemplateFilePath, (Map<String, String>) payload.get("placeholders"));

        return notificationRepository.save(notification);
    }

    public void sendNotification(Notification notification, String mailTemplateFilePath, String mode)
            throws IOException, MessagingException {
        // Based on the type, route to specific handlers
        switch (notification.getType()) {
            case "EMAIL":
                CompletableFuture.runAsync(() -> {
                    try {
                        // sendEmail(notification, mailTemplateFilePath, mode);
                    } catch (Exception e) {
                        // Log the exception
                        e.printStackTrace();
                    }
                });

                break;
            case "SMS":
                sendSms(notification);
                break;
            case "PUSH":
                sendPush(notification);
                break;
        }
        notification.setStatus("SENT");
        notification.setSentAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    private void sendEmailStandMode(Notification notification, String mailTemplateFilePath,
            Map<String, String> placeholders)
            throws IOException, MessagingException {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        // Load mail configuration
        // Path filePath = Paths.get("/opt/eVyoog/systemconfig/mailconfig.json");
        // ObjectMapper objectMapper = new ObjectMapper();
        // JsonNode rootNode = objectMapper.readTree(Files.readString(filePath));

        // JsonNode mailConfigJsonNode = rootNode.path("default");
        // if (!rootNode.path(commonService.getTenantId()).isMissingNode())
        //     mailConfigJsonNode = rootNode.path(commonService.getTenantId());

        // mailSender.setUsername(mailConfigJsonNode.path("username").asText());
        // mailSender.setPassword(mailConfigJsonNode.path("password").asText());

        mailSender.setUsername("evyoog@vyoog.com");
        mailSender.setPassword("mYvyg@2020");

        java.util.Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        // Create MimeMessage
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        // Read the template
        Path templatePath = Paths.get(mailTemplateFilePath);
        String templateContent = Files.readString(templatePath);

        // Replace placeholders with dynamic data
        if (placeholders != null) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            templateContent = templateContent.replace("{{" + entry.getKey().toString() + "}}",
                    entry.getValue().toString());
        }
    }

        // Set email details
        helper.setTo(notification.getRecipient().split(","));
        helper.setSubject(notification.getSubject());
        helper.setText(templateContent, true); // Enable HTML content
        // helper.setFrom(mailConfigJsonNode.path("username").asText());
        helper.setFrom("evyoog@vyoog.com");

        // Send email
        mailSender.send(mimeMessage);
    }

    private void sendSms(Notification notification) {
        // SMS logic here
    }

    private void sendPush(Notification notification) {
        // Push notification logic here
    }

}
