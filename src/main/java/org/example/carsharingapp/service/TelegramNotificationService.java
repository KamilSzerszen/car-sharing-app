package org.example.carsharingapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

@Service
public class TelegramNotificationService implements NotificationService {

    private final WebClient webClient;

    @Value("${telegram.api.key}")
    private String botToken;

    @Value("${telegram.chat.id}")
    private String chatId;

    public TelegramNotificationService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.telegram.org").build();
    }

    @Override
    public void sendNotification(String message) {
        String url = "/bot" + botToken + "/sendMessage";

        Map<String, String> requestBody = Map.of(
                "chat_id", chatId,
                "text", message
        );

        webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(e -> System.err.println(
                        "Error send message " + e.getMessage()
                ))
                .block();
    }

}
