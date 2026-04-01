package io.github.thirumalx.service;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
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
        sendEmail(to, subject, templateName, model, null, null);
    }

    @Async
    public void sendEmail(String to, String subject, String templateName, Map<String, Object> model, String attachmentName, byte[] attachment) {
        sendEmail(to, cc, subject, templateName, model, attachmentName, attachment);
    }

    @Async
    public void sendEmail(String to, String cc, String subject, String templateName, Map<String, Object> model, String attachmentName, byte[] attachment) {
        logger.info("Sending email to: {} (CC: {}) with subject: {} with attachment: {}", to, cc, subject, attachmentName);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    "UTF-8");

            Template template = freemarkerConfig.getTemplate(templateName);
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

            if (to.contains(",")) {
                helper.setTo(to.split(","));
            } else {
                helper.setTo(to);
            }
            if (cc != null && !cc.isBlank()) {
                if (cc.contains(",")) {
                    helper.setCc(cc.split(","));
                } else {
                    helper.setCc(cc);
                }
            }
            helper.setSubject(subject);
            helper.setText(html, true);

            if (attachment != null && attachmentName != null) {
                InputStreamSource source = new ByteArrayResource(attachment);
                helper.addAttachment(attachmentName, source);
            }

            mailSender.send(message);
            logger.info("Email sent successfully to: {}", to);
        } catch (MessagingException | IOException | TemplateException e) {
            logger.error("Failed to send email to: {}. Error: {}", to, e.getMessage());
            throw new RuntimeException("Could not send email", e);
        }
    }
}
