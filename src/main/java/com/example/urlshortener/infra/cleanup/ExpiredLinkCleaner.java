package com.example.urlshortener.infra.cleanup;

import com.example.urlshortener.config.ApplicationConfig;
import com.example.urlshortener.core.model.Link;
import com.example.urlshortener.core.service.notification.NotificationService;
import com.example.urlshortener.infra.repository.InMemoryLinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Фоновая очистка истёкших ссылок по TTL.
 * Важно: уведомляет владельца перед удалением.
 */
public final class ExpiredLinkCleaner {

    private static final Logger log = LoggerFactory.getLogger(ExpiredLinkCleaner.class);

    private static final ScheduledExecutorService EXEC =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "expired-link-cleaner");
                t.setDaemon(true);
                return t;
            });

    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    /**
     * Запуск фоновой очистки. Повторный вызов безопасен (ничего не сделает).
     */
    public static void start(NotificationService notificationService) {
        if (!STARTED.compareAndSet(false, true)) {
            return;
        }
        long interval = ApplicationConfig.cleanupIntervalSeconds();
        EXEC.scheduleAtFixedRate(
                () -> clean(notificationService),
                interval, interval, TimeUnit.SECONDS
        );
        log.info("ExpiredLinkCleaner started, interval={}s", interval);
    }

    public static void start() {
        start(null);
    }

    /**
     * Однократная очистка (удобно для тестов).
     */
    public static void runOnce(NotificationService notificationService) {
        clean(notificationService);
    }

    private static void clean(NotificationService notificationService) {
        List<Link> removed = InMemoryLinkRepository.getInstance().removeExpired();
        if (removed.isEmpty()) return;

        for (Link link : removed) {
            try {
                if (notificationService != null) {
                    notificationService.notifyLinkExpired(link, link.getOwnerUuid());
                }
            } catch (Exception e) {
                // не ломаем очистку из-за уведомлений
                log.warn("Failed to notify about expired link {}", link.getCode(), e);
            }
        }
        log.info("Removed {} expired links", removed.size());
    }

    private ExpiredLinkCleaner() {}
}
