package com.example.urlshortener.core.service.notification;

import com.example.urlshortener.core.model.Link;

import java.util.UUID;

/**
 * Интерфейс сервиса уведомлений.
 * Определяет контракт для отправки уведомлений пользователям
 * о событиях, связанных с короткими ссылками.
 */
public interface NotificationService {

    /**
     * Уведомление о превышении лимита переходов.
     *
     * @param link     ссылка, по которой превышен лимит
     * @param userUuid UUID пользователя-владельца
     */
    void notifyLimitExceeded(Link link, UUID userUuid);

    /**
     * Уведомление об истечении срока действия ссылки.
     *
     * @param link     ссылка, срок которой истёк
     * @param userUuid UUID пользователя-владельца
     */
    void notifyLinkExpired(Link link, UUID userUuid);

    /**
     * Уведомление о приближении к лимиту переходов.
     *
     * @param link            ссылка
     * @param userUuid        UUID пользователя-владельца
     * @param remainingClicks оставшееся количество переходов
     */
    void notifyApproachingLimit(Link link, UUID userUuid, int remainingClicks);

    /**
     * Уведомление об успешном создании ссылки.
     *
     * @param link     созданная ссылка
     * @param userUuid UUID пользователя-владельца
     */
    void notifyLinkCreated(Link link, UUID userUuid);

    /**
     * Уведомление об удалении ссылки.
     *
     * @param shortCode короткий код удалённой ссылки
     * @param userUuid  UUID пользователя-владельца
     * @param reason    причина удаления (для логов/аудита)
     */
    void notifyLinkDeleted(String shortCode, UUID userUuid, String reason);
}
