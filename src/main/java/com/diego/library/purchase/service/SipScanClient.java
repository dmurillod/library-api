package com.diego.library.purchase.service;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class SipScanClient {

    private final WebClient erpWebClient;
    private final WebClient receiptsWebClient;

    public SipScanClient(WebClient erpWebClient, WebClient receiptsWebClient) {
        this.erpWebClient = erpWebClient;
        this.receiptsWebClient = receiptsWebClient;
    }

    public String login() {
        Map<?, ?> response = erpWebClient.post()
                .uri("/login")
                .bodyValue(Map.of(
                        "nombre", "demo",
                        "contrasenna", "84ba92cf18135aec58ed1334183761a2"
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || !response.containsKey("token")) {
            throw new RuntimeException("No se pudo obtener token del ERP");
        }
        return (String) response.get("token");
    }

    public String sendReceiptText(String token, String text) {
        Map<?, ?> response = receiptsWebClient.post()
                .uri("/v2/receipts/from-text")
                .header("Authorization", "Bearer " + token)
                .bodyValue(Map.of(
                        "uploader_nit", "901000123",
                        "text", text
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || !response.containsKey("id")) {
            throw new RuntimeException("No se pudo crear el recibo");
        }
        return (String) response.get("id");
    }

    public boolean isPdfReady(String token, String receiptId) {
        try {
            receiptsWebClient.get()
                    .uri("/v2/receipts/" + receiptId + "/pdf")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}