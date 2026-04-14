package com.diego.library.purchase.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SipScanClientTest {

    private WebClient erpWebClient;
    private WebClient receiptsWebClient;
    private SipScanClient sipScanClient;

    @BeforeEach
    void setUp() {
        erpWebClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
        receiptsWebClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
        sipScanClient = new SipScanClient(erpWebClient, receiptsWebClient);
    }

    @Test
    @SuppressWarnings("unchecked")
    void login_success() {
        when(erpWebClient.post()
                .uri(anyString())
                .bodyValue(any())
                .retrieve()
                .bodyToMono(Map.class))
                .thenReturn(Mono.just(Map.of("token", "fake-jwt-token")));

        String token = sipScanClient.login();

        assertNotNull(token);
        assertEquals("fake-jwt-token", token);
    }

    @Test
    @SuppressWarnings("unchecked")
    void login_noToken_throwsException() {
        when(erpWebClient.post()
                .uri(anyString())
                .bodyValue(any())
                .retrieve()
                .bodyToMono(Map.class))
                .thenReturn(Mono.just(Map.of()));

        assertThrows(RuntimeException.class, () -> sipScanClient.login());
    }

    @Test
    void isPdfReady_exception_returnsFalse() {
        when(receiptsWebClient.get())
                .thenThrow(new RuntimeException("connection error"));

        boolean ready = sipScanClient.isPdfReady("fake-token", "some-id");

        assertFalse(ready);
    }

    @Test
    void waitForReceiptViaWebSocket_returnsUrl() {
        String pdfUrl = sipScanClient.waitForReceiptViaWebSocket(
                "fake-token",
                "550e8400-e29b-41d4-a716-446655440000"
        );

        assertNotNull(pdfUrl);
        assertTrue(pdfUrl.contains("550e8400-e29b-41d4-a716-446655440000"));
        assertTrue(pdfUrl.contains("/pdf"));
    }
}