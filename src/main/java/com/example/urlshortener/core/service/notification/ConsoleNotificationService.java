package com.example.urlshortener.core.service.notification;

import com.example.urlshortener.core.model.Link;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Реализация NotificationService с выводом в консоль.
 * Использует ANSI-коды для цветного форматирования.
 */
public class ConsoleNotificationService implements NotificationService {

    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    @Override
    public void notifyLimitExceeded(Link link, UUID userUuid) {
        String message = String.format(
                "%s[ЛИМИТ ИСЧЕРПАН]%s Ссылка '%s' достигла максимума переходов (%d/%d). Пользователь: %s",
                RED, RESET,
                link.getCode(),
                link.getClicks(),
                link.getMaxClicks(),
                userUuid
        );
        printNotification(message);
    }

    @Override
    public void notifyLinkExpired(Link link, UUID userUuid) {
        Instant expiresAt = link.getCreatedAt().plusSeconds(link.getTtlSeconds());

        String message = String.format(
                "%s[ССЫЛКА ИСТЕКЛА]%s Ссылка '%s' истекла %s. Пользователь: %s",
                RED, RESET,
                link.getCode(),
                FORMATTER.format(expiresAt),
                userUuid
        );
        printNotification(message);
    }

    @Override
    public void notifyApproachingLimit(Link link, UUID userUuid, int remainingClicks) {
        String message = String.format(
                "%s[ПРЕДУПРЕЖДЕНИЕ]%s Ссылка '%s' приближается к лимиту. Осталось переходов: %d. Пользователь: %s",
                YELLOW, RESET,
                link.getCode(),
                remainingClicks,
                userUuid
        );
        printNotification(message);
    }

    @Override
    public void notifyLinkCreated(Link link, UUID userUuid) {
        Instant expiresAt = link.getCreatedAt().plusSeconds(link.getTtlSeconds());

        String message = String.format(
                "%s[СОЗДАНА]%s Новая ссылка '%s' -> %s. Лимит: %d, истекает: %s. Пользователь: %s",
                GREEN, RESET,
                link.getCode(),
                link.getOriginalUrl(),
                link.getMaxClicks(),
                FORMATTER.format(expiresAt),
                userUuid
        );
        printNotification(message);
    }

    @Override
    public void notifyLinkDeleted(String shortCode, UUID userUuid, String reason) {
        String message = String.format(
                "%s[УДАЛЕНА]%s Ссылка '%s' удалена. Причина: %s. Пользователь: %s",
                CYAN, RESET,
                shortCode,
                reason,
                userUuid
        );
        printNotification(message);
    }

    private void printNotification(String message) {
        System.out.println();
        System.out.println("═".repeat(70));
        System.out.println(message);
        System.out.println("═".repeat(70));
    }
}
