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

@Slf4j
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private ObjectMapper objectMapper;

    public void sendResiliencyReportEmail(List<ResiliencyScore> resiliencyScores, String toEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);

            String subject = "Resiliency Report - " +
                    (resiliencyScores.isEmpty() ? "No Data" : resiliencyScores.get(resiliencyScores.size() - 1).getServiceName());

            helper.setSubject(subject);
            helper.setFrom("no-reply@yourdomain.com");

            String jsonData = convertToJson(resiliencyScores);

            Context context = new Context();
            context.setVariable("jsonData", jsonData);

            if (!resiliencyScores.isEmpty()) {
                ResiliencyScore latest = resiliencyScores.get(resiliencyScores.size() - 1);
                context.setVariable("serviceName", latest.getServiceName());
                context.setVariable("timestamp", latest.getTimestamp());
            }

            String htmlContent = templateEngine.process("emailTemplate", context);

            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("Resiliency report email sent to {}", toEmail);

        } catch (MessagingException | JsonProcessingException e) {
            log.error("Failed to send resiliency report email", e);
        }
    }

    private String convertToJson(List<ResiliencyScore> resiliencyScores) throws JsonProcessingException {
        List<ChaosDataPoint> dataPoints = resiliencyScores.stream()
                .map(rs -> new ChaosDataPoint(
                        rs.getDeploymentId(),
                        rs.getResiliencyScore(),
                        rs.getFailureRate(),
                        rs.getAvgLatency(),
                        rs.getTimestamp()
                ))
                .toList();

        return objectMapper.writeValueAsString(
                Map.of("chaosData", dataPoints)
        );
    }

}
