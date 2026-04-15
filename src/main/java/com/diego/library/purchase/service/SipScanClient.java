package com.diego.library.purchase.service;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class SipScanClient {

    private final WebClient erpWebClient;
    private final WebClient receiptsWebClient;

    private static final String RECEIPTS_BASE_URL = "http://34.60.178.4";
    private static final String WS_URL = "ws://34.60.178.4/receipts/ws";
    private static final String UPLOADER_NIT = "901000123";

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
                        "uploader_nit", UPLOADER_NIT,
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

    public String waitForReceiptViaWebSocket(String token, String receiptId) {
        CountDownLatch latch = new CountDownLatch(1);

        String wsUrl = WS_URL + "?nit=" + UPLOADER_NIT + "&token=" + token;
        StandardWebSocketClient wsClient = new StandardWebSocketClient();

        try {
            wsClient.execute(new AbstractWebSocketHandler() {
                @Override
                protected void handleTextMessage(WebSocketSession session,
                                                 TextMessage message) throws Exception {
                    String payload = message.getPayload();
                    if (payload.contains("suggestion_completed")
                            && payload.contains(receiptId)) {
                        latch.countDown();
                        session.close();
                    }
                }

                @Override
                public void afterConnectionEstablished(WebSocketSession session) {
                }
            }, wsUrl).get(10, TimeUnit.SECONDS);

            latch.await(120, TimeUnit.SECONDS);

        } catch (Exception e) {
            // Si falla devuelve la URL igual
        }

        return RECEIPTS_BASE_URL + "/v2/receipts/" + receiptId + "/pdf";
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