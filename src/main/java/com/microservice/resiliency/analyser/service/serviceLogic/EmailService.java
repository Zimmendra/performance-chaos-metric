package com.microservice.resiliency.analyser.service.serviceLogic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.resiliency.analyser.service.model.ChaosDataPoint;
import com.microservice.resiliency.analyser.service.model.ResiliencyScore;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    public void sendResiliencyReportEmail(List<ResiliencyScore> resiliencyScores, String toEmail) {
        if (resiliencyScores == null || resiliencyScores.isEmpty()) {
            log.warn("No resiliency scores to report for {}", toEmail);
            return;
        }

        try {
            log.info("Preparing to send email to {} with {} data points", toEmail, resiliencyScores.size());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            ResiliencyScore latest = resiliencyScores.get(resiliencyScores.size() - 1);
            String subject = "Resiliency Report - " + latest.getServiceName();

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setFrom("no-reply@yourdomain.com");

            // Set individual variables instead of JSON
            Context context = new Context();
            context.setVariable("serviceName", latest.getServiceName());
            context.setVariable("timestamp", latest.getTimestamp());
            context.setVariable("labels", resiliencyScores.stream()
                    .map(score -> "ID: " + score.getDeploymentId())
                    .toList());
            context.setVariable("resiliencyScores", resiliencyScores.stream()
                    .map(ResiliencyScore::getResiliencyScore)
                    .toList());
            context.setVariable("failureRates", resiliencyScores.stream()
                    .map(ResiliencyScore::getFailureRate)
                    .toList());
            context.setVariable("avgLatencies", resiliencyScores.stream()
                    .map(ResiliencyScore::getAvgLatency)
                    .toList());
            log.info("{}",context);
            String htmlContent = templateEngine.process("emailTemplate", context);
            helper.setText(htmlContent, true);
            log.debug("Rendered email content: {}", htmlContent);
            mailSender.send(message);
            log.info("✅ Resiliency report email sent to {}", toEmail);

        } catch (MessagingException e) {
            log.error("❌ Failed to send resiliency report email to {}", toEmail, e);
        }
    }
}