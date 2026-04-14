package com.diego.library.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.receipts.url}")
    private String receiptsServiceUrl;

    @Value("${services.erp.url}")
    private String erpServiceUrl;

    @Bean
    public WebClient receiptsWebClient() {
        return WebClient.builder()
                .baseUrl(receiptsServiceUrl)
                .build();
    }

    @Bean
    public WebClient erpWebClient() {
        return WebClient.builder()
                .baseUrl(erpServiceUrl)
                .build();
    }
}