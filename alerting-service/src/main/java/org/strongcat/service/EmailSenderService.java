package org.strongcat.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.strongcat.data.EmailAlertContext;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendAlert(EmailAlertContext context) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(fromEmail);
            helper.setTo(context.getTo());
            helper.setSubject(String.format("🚨 ALERT: [%s] High Error Rate Detected", context.getServiceName()));

            String htmlContent = buildAlertHtml(context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Alert email successfully sent to {}", context.getTo());

        } catch (MessagingException e) {
            log.error("Failed to send alert email to {}. Error: {}", context.getTo(), e.getMessage());
        }
    }

    private String buildAlertHtml(EmailAlertContext alertContext) {
        Context context = new Context();
        context.setVariable("serviceName", alertContext.getServiceName());
        context.setVariable("logLevel", alertContext.getLogLevel());
        context.setVariable("messageQuery", alertContext.getMessageQuery());
        context.setVariable("actualCount", alertContext.getActualCount());
        context.setVariable("thresholdCount", alertContext.getThresholdCount());

        return templateEngine.process("alert-template", context);
    }
}