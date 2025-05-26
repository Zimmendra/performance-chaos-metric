package com.microservice.resiliency.analyser.service.serviceLogic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;

import static com.microservice.resiliency.analyser.service.constant.Constants.*;

@Slf4j
@Service
public class PortValidationService {


    public boolean validateHostAndPortMatch(String collectionJson, String serviceUrl) {
        try {
            URI serviceUri = new URI(serviceUrl);

            String expectedHost = serviceUri.getHost();
            int expectedPort = serviceUri.getPort() != -1
                    ? serviceUri.getPort()
                    : (serviceUri.getScheme().equalsIgnoreCase(HTTPS) ? 443 : 80);

            JsonNode rootNode = new ObjectMapper().readTree(collectionJson);

            if (rootNode.has(ITEM)) {
                JsonNode items = rootNode.get(ITEM);

                for (JsonNode item : items) {
                    JsonNode requestNode = item.at(PATH);

                    if (readTheURL(requestNode, expectedHost, expectedPort)) return false;
                }
            }

        } catch (Exception e) {
            log.error(ERROR_VALIDATING_HOST_AND_PORT, e.getMessage(), e);
            return false;
        }

        return true; // All requests matched host and port
    }

    private static boolean readTheURL(JsonNode requestNode, String expectedHost, int expectedPort) {
        if (requestNode.has(RAW)) {
            String rawUrl = requestNode.get(RAW).asText();
            try {
                URI requestUri = new URI(rawUrl);
                String itemHost = requestUri.getHost();
                int itemPort = requestUri.getPort() != -1
                        ? requestUri.getPort()
                        : (requestUri.getScheme().equalsIgnoreCase(HTTPS) ? 443 : 80);

                if (!itemHost.equalsIgnoreCase(expectedHost) || itemPort != expectedPort) {
                    log.warn(MISMATCH_FOUND_HOST_VS_PORT_VS,
                            itemHost, expectedHost, itemPort, expectedPort);
                    return true;
                }
            } catch (URISyntaxException e) {
                log.warn(INVALID_RAW_URL_FORMAT, rawUrl, e);
            }
        }
        return false;
    }
}
