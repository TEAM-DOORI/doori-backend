package com.doori.doori_backend.global.discord;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class DiscordNotificationService {

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String webhookUrl;
    private final RestTemplate restTemplate;

    public DiscordNotificationService(@Value("${discord.webhook.url:}") String webhookUrl) {
        this.webhookUrl = webhookUrl;
        // connectTimeout 3초, readTimeout 5초 설정
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(3).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(5).toMillis());
        this.restTemplate = new RestTemplate(factory);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBlockCreated(BlockCreatedEvent event) {
        if (webhookUrl.isBlank()) {
            return;
        }

        String message = String.format(
            "🚫 차단 발생\n차단한 유저: %s (ID: %d)\n차단당한 유저: %s\n발생 시각: %s",
            event.blockerNickname(),
            event.blockerId(),
            event.targetNickname(),
            LocalDateTime.now().format(FORMATTER)
        );

        try {
            restTemplate.postForObject(webhookUrl, Map.of("content", message), String.class);
        } catch (Exception e) {
            log.warn("Discord 알림 전송 실패: {}", e.getMessage());
        }
    }
}
