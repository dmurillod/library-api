package com.diego.library.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class HealthController {

    @Value("${app.version:1.0.0}")
    private String version;

    @Value("${app.environment:stable}")
    private String environment;

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", environment,
                "version", version,
                "deployedAt", LocalDateTime.now().toString(),
                "service", "library-api",
                "features", environment.equals("canary")
                        ? "new-book-recommendations"
                        : "standard"
        );
    }
}