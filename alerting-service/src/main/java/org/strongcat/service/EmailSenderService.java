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

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final JavaMailSender mailSender;

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

    private String buildAlertHtml(EmailAlertContext context) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <h2 style="color: #d9534f;">🚨 Критический порог логов превышен!</h2>
                <p>Система мониторинга зафиксировала всплеск событий, удовлетворяющих правилу.</p>
                <hr style="border: 0; border-top: 1px solid #eee;" />
                <table style="border-collapse: collapse; width: 100%%;">
                    <tr>
                        <td style="padding: 8px; font-weight: bold; width: 180px;">Сервис:</td>
                        <td style="padding: 8px; color: #5bc0de;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 8px; font-weight: bold;">Уровень логов:</td>
                        <td style="padding: 8px;"><span style="background-color: #d9534f; color: white; padding: 2px 6px; border-radius: 4px;">%s</span></td>
                    </tr>
                    <tr>
                        <td style="padding: 8px; font-weight: bold;">Поисковый запрос:</td>
                        <td style="padding: 8px; font-style: italic;">"%s"</td>
                    </tr>
                    <tr>
                        <td style="padding: 8px; font-weight: bold;">Зафиксировано:</td>
                        <td style="padding: 8px; color: #d9534f; font-weight: bold;">%d событий</td>
                    </tr>
                    <tr>
                        <td style="padding: 8px; font-weight: bold;">Лимит (Порог):</td>
                        <td style="padding: 8px;">%d событий</td>
                    </tr>
                </table>
                <hr style="border: 0; border-top: 1px solid #eee;" />
                <p style="font-size: 12px; color: #777;">Это автоматическое уведомление от alerting-service. Пожалуйста, не отвечайте на него.</p>
            </body>
            </html>
            """, 
            context.getServiceName(),
            context.getLogLevel(),
            context.getMessageQuery(),
            context.getActualCount(),
            context.getThresholdCount()
        );
    }
}