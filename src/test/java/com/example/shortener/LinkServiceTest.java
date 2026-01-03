package com.example.shortener;

import com.example.urlshortener.core.model.Link;
import com.example.urlshortener.core.service.LinkService;
import com.example.urlshortener.core.service.notification.NotificationService;
import com.example.urlshortener.infra.repository.InMemoryLinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LinkServiceTest {

    @TempDir
    Path tempDir;

    private TestNotifications notifications;
    private LinkService svc;
    private UUID user;

    @BeforeEach
    void setUp() {
        System.setProperty("urlshortener.dataDir", tempDir.toString());
        // TTL по умолчанию можно переопределять прямо системным свойством:
        System.clearProperty("link.defaultTtlSeconds");
        System.clearProperty("cleanup.intervalSeconds");
        InMemoryLinkRepository.resetForTests();

        notifications = new TestNotifications();
        svc = new LinkService(notifications);
        user = UUID.randomUUID();
    }

    @Test
    void createReturnsCode() {
        Link l = svc.create("https://example.com", user, 10L);
        assertNotNull(l.getCode());
        assertEquals("https://example.com", l.getOriginalUrl());
        assertEquals(user, l.getOwnerUuid());
        assertEquals(1, notifications.created);
    }

    @Test
    void sameUrlDifferentUsersHaveDifferentCodes() {
        UUID u1 = UUID.randomUUID();
        UUID u2 = UUID.randomUUID();

        Link l1 = svc.create("https://example.com", u1, 10L);
        Link l2 = svc.create("https://example.com", u2, 10L);

        assertNotEquals(l1.getCode(), l2.getCode());
    }

    @Test
    void openIncrementsClicksAndReturnsUrl() {
        Link l = svc.create("https://example.com", user, 2L);

        String url = svc.open(l.getCode());
        assertEquals("https://example.com", url);
        assertEquals(1, l.getClicks());
    }

    @Test
    void limitIsEnforced() {
        Link l = svc.create("https://example.com", user, 1L);

        svc.open(l.getCode()); // 1-й переход
        assertThrows(IllegalStateException.class, () -> svc.open(l.getCode())); // лимит
        assertEquals(1, notifications.limitExceeded);
    }

    @Test
    void clicksPersistBetweenRepositoryReloads() {
        Link l = svc.create("https://example.com", user, 2L);

        svc.open(l.getCode());
        assertEquals(1, l.getClicks());

        // имитируем перезапуск приложения: репозиторий перечитывает JSON
        InMemoryLinkRepository.resetForTests();
        LinkService svc2 = new LinkService(notifications);

        svc2.open(l.getCode()); // 2-й переход
        assertThrows(IllegalStateException.class, () -> svc2.open(l.getCode())); // лимит
    }

    @Test
    void invalidUrlRejected() {
        assertThrows(IllegalArgumentException.class, () -> svc.create("notaurl", user, 10L));
        assertThrows(IllegalArgumentException.class, () -> svc.create("ftp://example.com", user, 10L));
        assertThrows(IllegalArgumentException.class, () -> svc.create("https://", user, 10L));
    }

    @Test
    void ttlExpiresAndLinkIsDeletedOnOpen() throws Exception {
        System.setProperty("link.defaultTtlSeconds", "1");
        InMemoryLinkRepository.resetForTests();
        svc = new LinkService(notifications);

        Link l = svc.create("https://example.com", user, 10L);
        Thread.sleep(1100);

        assertThrows(IllegalStateException.class, () -> svc.open(l.getCode()));
        // после попытки открытия ссылка должна быть удалена
        assertTrue(InMemoryLinkRepository.getInstance().find(l.getCode()).isEmpty());
        assertEquals(1, notifications.expired);
    }

    @Test
    void deleteAllowedOnlyForOwner() {
        Link l = svc.create("https://example.com", user, 10L);

        UUID other = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> svc.delete(l.getCode(), other));

        svc.delete(l.getCode(), user);
        assertEquals(1, notifications.deleted);
    }

    @Test
    void updateMaxClicksOwnerOnly() {
        Link l = svc.create("https://example.com", user, 10L);

        UUID other = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> svc.updateMaxClicks(l.getCode(), other, 100));

        svc.updateMaxClicks(l.getCode(), user, 100);
        assertEquals(100, l.getMaxClicks());
    }

    @Test
    void updateMaxClicksCannotGoBelowCurrentClicks() {
        Link l = svc.create("https://example.com", user, 10L);
        svc.open(l.getCode());
        svc.open(l.getCode()); // clicks = 2

        assertThrows(IllegalArgumentException.class, () -> svc.updateMaxClicks(l.getCode(), user, 1)); // < clicks
        assertThrows(IllegalArgumentException.class, () -> svc.updateMaxClicks(l.getCode(), user, 0)); // <=0
    }

    @Test
    void approachingLimitNotificationFires() {
        Link l = svc.create("https://example.com", user, 5L);

        // после 4-го клика remaining=1, threshold=ceil(5*0.2)=1 => должно быть предупреждение
        svc.open(l.getCode());
        svc.open(l.getCode());
        svc.open(l.getCode());
        svc.open(l.getCode());

        assertTrue(notifications.approachingLimit >= 1);
    }

    @Test
    void collisionIsHandledByRegeneration() {
        Deque<String> codes = new ArrayDeque<>();
        codes.add("AAAAAA");
        codes.add("AAAAAA"); // коллизия
        codes.add("BBBBBB"); // следующий норм

        LinkService svcWithSeq = new LinkService(notifications, codes::removeFirst);

        // заранее сохраняем ссылку с AAAAAA
        Link existing = new Link("AAAAAA", "https://x.com", UUID.randomUUID(), 60, 10);
        InMemoryLinkRepository.getInstance().save(existing);

        Link created = svcWithSeq.create("https://example.com", user, 10L);
        assertEquals("BBBBBB", created.getCode());
    }

    private static final class TestNotifications implements NotificationService {
        int created;
        int expired;
        int limitExceeded;
        int approachingLimit;
        int deleted;

        @Override public void notifyLimitExceeded(Link link, UUID userUuid) { limitExceeded++; }
        @Override public void notifyLinkExpired(Link link, UUID userUuid) { expired++; }
        @Override public void notifyApproachingLimit(Link link, UUID userUuid, int remainingClicks) { approachingLimit++; }
        @Override public void notifyLinkCreated(Link link, UUID userUuid) { created++; }
        @Override public void notifyLinkDeleted(String shortCode, UUID userUuid, String reason) { deleted++; }
    }
}
