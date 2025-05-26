package com.microservice.resiliency.analyser.service.serviceLogic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;

@Service
public class PortValidationService {

    private static final Logger log = LoggerFactory.getLogger(PortValidationService.class);

    public boolean validateHostAndPortMatch(String collectionJson, String serviceUrl) {
        try {
            URI serviceUri = new URI(serviceUrl);

            String expectedHost = serviceUri.getHost();
            int expectedPort = serviceUri.getPort() != -1
                    ? serviceUri.getPort()
                    : (serviceUri.getScheme().equalsIgnoreCase("https") ? 443 : 80);

            JsonNode rootNode = new ObjectMapper().readTree(collectionJson);

            if (rootNode.has("item")) {
                JsonNode items = rootNode.get("item");

                for (JsonNode item : items) {
                    JsonNode requestNode = item.at("/request/url");

                    if (requestNode.has("raw")) {
                        String rawUrl = requestNode.get("raw").asText();
                        try {
                            URI requestUri = new URI(rawUrl);
                            String itemHost = requestUri.getHost();
                            int itemPort = requestUri.getPort() != -1
                                    ? requestUri.getPort()
                                    : (requestUri.getScheme().equalsIgnoreCase("https") ? 443 : 80);

                            if (!itemHost.equalsIgnoreCase(expectedHost) || itemPort != expectedPort) {
                                log.warn("Mismatch found - Host: {} vs {}, Port: {} vs {}",
                                        itemHost, expectedHost, itemPort, expectedPort);
                                return false;
                            }
                        } catch (URISyntaxException e) {
                            log.warn("Invalid raw URL format: {}", rawUrl, e);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error validating host and port: {}", e.getMessage(), e);
            return false;
        }

        return true; // All requests matched host and port
    }
}
