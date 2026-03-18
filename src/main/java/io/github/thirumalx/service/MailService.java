package io.github.thirumalx.service;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

/**
 * Service for sending emails using FreeMarker templates.
 * 
 * @author Thirumal M
 */
@Service
public class MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final Configuration freemarkerConfig;

    @Value("${mail.cc}")
    private String cc;

    public MailService(JavaMailSender mailSender, Configuration freemarkerConfig) {
        this.mailSender = mailSender;
        this.freemarkerConfig = freemarkerConfig;
    }

    @Async
    public void sendEmail(String to, String subject, String templateName, Map<String, Object> model) {
        logger.info("Sending email to: {} with subject: {}", to, subject);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    "UTF-8");

            Template template = freemarkerConfig.getTemplate(templateName);
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

            helper.setTo(to);
            helper.setCc(cc);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);
            logger.info("Email sent successfully to: {}", to);
        } catch (MessagingException | IOException | TemplateException e) {
            logger.error("Failed to send email to: {}. Error: {}", to, e.getMessage());
            throw new RuntimeException("Could not send email", e);
        }
    }
}
