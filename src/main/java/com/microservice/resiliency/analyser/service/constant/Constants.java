package com.microservice.resiliency.analyser.service.constant;



public class Constants {

    public static final String NO_RESILIENCY_SCORES_TO_REPORT_FOR = "No resiliency scores to report for {}";
    public static final String PREPARING_TO_SEND_EMAIL_TO_WITH_DATA_POINTS = "Preparing to send email to {} with {} data points";
    public static final String UTF_8 = "UTF-8";
    public static final String RESILIENCY_REPORT = "Resiliency Report - ";
    public static final String SERVICE_NAME = "serviceName";
    public static final String TIMESTAMP = "timestamp";
    public static final String LABELS = "labels";
    public static final String ID = "ID: ";
    public static final String RESILIENCY_SCORES = "resiliencyScores";
    public static final String FAILURE_RATES = "failureRates";
    public static final String AVG_LATENCIES = "avgLatencies";
    public static final String EMAIL_TEMPLATE = "emailTemplate";
    public static final String RENDERED_EMAIL_CONTENT = "Rendered email content: {}";
    public static final String RESILIENCY_REPORT_EMAIL_SENT_TO = "✅ Resiliency report email sent to {}";
    public static final String FAILED_TO_SEND_RESILIENCY_REPORT_EMAIL_TO = "❌ Failed to send resiliency report email to {}";
    public static final String NO_REPLY_YOURDOMAIN_COM = "no-reply@yourdomain.com";



    public static final String ACTUATOR_PROMETHEUS = "/actuator/prometheus";
    public static final String CONNECTION_REFUSED = "Connection refused";
    public static final String SERVICE_AT_IS_NOT_STARTED_OR_UNREACHABLE = "Service at {} is not started or unreachable";
    public static final String ERROR_CALCULATING_RESILIENCY_FOR_SERVICE_WITH_DEPLOYMENT = "Error calculating resiliency for service: {} with deployment: {}";
    public static final String HTTP_SERVER_REQUESTS_SECONDS_SUM = "http_server_requests_seconds_sum";
    public static final String HTTP_SERVER_REQUESTS_SECONDS_COUNT_ERROR = "http_server_requests_seconds_count{error";
    public static final String HTTP_SERVER_REQUESTS_SECONDS_SUM_ERROR = "http_server_requests_seconds_sum{error";



    public static final String HTTPS = "https";
    public static final String ITEM = "item";
    public static final String PATH = "/request/url";
    public static final String RAW = "raw";
    public static final String MISMATCH_FOUND_HOST_VS_PORT_VS = "Mismatch found - Host: {} vs {}, Port: {} vs {}";
    public static final String INVALID_RAW_URL_FORMAT = "Invalid raw URL format: {}";
    public static final String ERROR_VALIDATING_HOST_AND_PORT = "Error validating host and port: {}";


    public static final String ERROR = "error";
    public static final String DETAILS = "details";
    public static final String VALIDATION_FAILED = "Validation failed";
    public static final String MISSING_REQUIRED_PARAMETER = "Missing required parameter";
    public static final String PARAMETER = "parameter";
    public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    public static final String MESSAGE = "message";
}
