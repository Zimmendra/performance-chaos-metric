package com.microservice.resiliency.analyser.service.serviceLogic;

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

import static com.microservice.resiliency.analyser.service.constant.Constants.*;

@Slf4j
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    public void sendResiliencyReportEmail(List<ResiliencyScore> resiliencyScores, String toEmail) {
        if (resiliencyScores == null || resiliencyScores.isEmpty()) {
            log.warn(NO_RESILIENCY_SCORES_TO_REPORT_FOR, toEmail);
            return;
        }

        try {
            log.info(PREPARING_TO_SEND_EMAIL_TO_WITH_DATA_POINTS, toEmail, resiliencyScores.size());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, UTF_8);

            ResiliencyScore latest = resiliencyScores.get(resiliencyScores.size() - 1);
            String subject = RESILIENCY_REPORT + latest.getServiceName();

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setFrom(NO_REPLY_YOURDOMAIN_COM);

            // Set individual variables instead of JSON
            Context context = new Context();
            context.setVariable(SERVICE_NAME, latest.getServiceName());
            context.setVariable(TIMESTAMP, latest.getTimestamp());
            context.setVariable(LABELS, resiliencyScores.stream()
                    .map(score -> ID + score.getDeploymentId())
                    .toList());
            context.setVariable(RESILIENCY_SCORES, resiliencyScores.stream()
                    .map(ResiliencyScore::getResiliencyScore)
                    .toList());
            context.setVariable(FAILURE_RATES, resiliencyScores.stream()
                    .map(ResiliencyScore::getFailureRate)
                    .toList());
            context.setVariable(AVG_LATENCIES, resiliencyScores.stream()
                    .map(ResiliencyScore::getAvgLatency)
                    .toList());
            log.info("{}",context);
            String htmlContent = templateEngine.process(EMAIL_TEMPLATE, context);
            helper.setText(htmlContent, true);
            log.debug(RENDERED_EMAIL_CONTENT, htmlContent);
            mailSender.send(message);
            log.info(RESILIENCY_REPORT_EMAIL_SENT_TO, toEmail);

        } catch (MessagingException e) {
            log.error(FAILED_TO_SEND_RESILIENCY_REPORT_EMAIL_TO, toEmail, e);
        }
    }
}