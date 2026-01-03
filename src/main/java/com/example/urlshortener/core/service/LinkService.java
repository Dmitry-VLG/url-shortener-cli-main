package com.example.urlshortener.core.service;

import com.example.urlshortener.config.ApplicationConfig;
import com.example.urlshortener.core.model.Link;
import com.example.urlshortener.core.service.notification.NotificationService;
import com.example.urlshortener.infra.repository.InMemoryLinkRepository;
import com.example.urlshortener.util.HashGenerator;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class LinkService {

    private final InMemoryLinkRepository repo = InMemoryLinkRepository.getInstance();
    private final NotificationService notificationService;
    private final Supplier<String> codeSupplier;

    // Порог предупреждения: уведомляем, когда осталось 20% от лимита
    private static final double WARNING_THRESHOLD = 0.2;

    public LinkService(NotificationService notificationService) {
        this(notificationService, HashGenerator::randomCode);
    }

    // Удобно для тестов (можно подставить детерминированный генератор и проверить коллизии)
    public LinkService(NotificationService notificationService, Supplier<String> codeSupplier) {
        this.notificationService = notificationService;
        this.codeSupplier = codeSupplier;
    }

    /**
     * Создаёт новую короткую ссылку.
     * TTL задаётся системой (конфиг), не пользователем.
     */
    public Link create(String url, UUID owner, Long maxClicks) {
        validateUrl(url);

        long ttl = ApplicationConfig.defaultTtlSeconds();
        long max = (maxClicks != null) ? maxClicks : ApplicationConfig.defaultMaxClicks();
        if (max <= 0) throw new IllegalArgumentException("maxClicks must be > 0");

        String code = generateUniqueCode();
        Link link = new Link(code, url, owner, ttl, max);

        repo.save(link);
        notificationService.notifyLinkCreated(link, owner);

        return link;
    }

    /**
     * "Открывает" ссылку: проверяет TTL/лимит, регистрирует клик и возвращает исходный URL.
     * Реальное открытие браузера — задача CLI (UI-слоя), чтобы core не зависел от AWT/GUI.
     */
    public String open(String code) {
        Link link = repo.find(code)
                .orElseThrow(() -> new IllegalArgumentException("Link not found"));

        UUID owner = link.getOwnerUuid();

        if (link.isExpired()) {
            notificationService.notifyLinkExpired(link, owner);
            // удаляем сразу, чтобы поведение было предсказуемым
            repo.delete(code);
            repo.persist();
            throw new IllegalStateException("Link expired");
        }

        if (link.isLimitReached()) {
            notificationService.notifyLimitExceeded(link, owner);
            throw new IllegalStateException("Click limit reached");
        }

        link.registerClick();
        repo.persist(); // важно: сохраняем clicks, иначе лимит можно обойти перезапуском

        checkApproachingLimit(link, owner);

        return link.getOriginalUrl();
    }

    public List<Link> listByOwner(UUID owner) {
        return repo.findByOwner(owner);
    }

    /**
     * Удаляет ссылку по коду (только владелец).
     */
    public void delete(String code, UUID owner) {
        repo.find(code)
                .filter(l -> l.getOwnerUuid().equals(owner))
                .orElseThrow(() -> new IllegalArgumentException("Not found or not owner"));

        repo.delete(code);
        notificationService.notifyLinkDeleted(code, owner, "удалено пользователем");
    }

    /**
     * Расширение: владелец может изменить лимит кликов.
     */
    public void updateMaxClicks(String code, UUID owner, long newMaxClicks) {
        Link link = repo.find(code)
                .filter(l -> l.getOwnerUuid().equals(owner))
                .orElseThrow(() -> new IllegalArgumentException("Not found or not owner"));

        link.updateMaxClicks(newMaxClicks);
        repo.persist();
    }

    private void checkApproachingLimit(Link link, UUID owner) {
        long maxClicks = link.getMaxClicks();
        long currentClicks = link.getClicks();
        long remaining = maxClicks - currentClicks;

        long threshold = (long) Math.ceil(maxClicks * WARNING_THRESHOLD);

        if (remaining > 0 && remaining <= threshold) {
            notificationService.notifyApproachingLimit(link, owner, (int) remaining);
        }
    }

    private String generateUniqueCode() {
        // гарантируем уникальность в рамках репозитория (нет перезаписи при коллизии)
        for (int attempt = 0; attempt < 200; attempt++) {
            String code = codeSupplier.get();
            if (!repo.exists(code)) return code;
        }
        throw new IllegalStateException("Cannot generate unique code after many attempts");
    }

    private static void validateUrl(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("URL must start with http:// or https://");
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException("URL host is missing");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }
}
