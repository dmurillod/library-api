package com.diego.library.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.receipts.url}")
    private String receiptsServiceUrl;

    @Bean
    public WebClient receiptsWebClient() {
        return WebClient.builder()
                .baseUrl(receiptsServiceUrl)
                .build();
    }
}
