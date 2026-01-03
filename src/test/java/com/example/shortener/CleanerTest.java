package com.example.shortener;

import com.example.urlshortener.core.model.Link;
import com.example.urlshortener.core.service.notification.NotificationService;
import com.example.urlshortener.infra.cleanup.ExpiredLinkCleaner;
import com.example.urlshortener.infra.repository.InMemoryLinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CleanerTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        System.setProperty("urlshortener.dataDir", tempDir.toString());
        InMemoryLinkRepository.resetForTests();
    }

    @Test
    void cleanerRemovesExpiredLinks() throws Exception {
        Link link = new Link("AAAAAA", "https://example.com", UUID.randomUUID(), 1, 10);
        InMemoryLinkRepository.getInstance().save(link);

        Thread.sleep(1100);

        ExpiredLinkCleaner.runOnce(null);

        assertTrue(InMemoryLinkRepository.getInstance().find("AAAAAA").isEmpty());
    }

    @Test
    void cleanerNotifiesBeforeRemoval() throws Exception {
        TestNotifications notif = new TestNotifications();
        Link link = new Link("BBBBBB", "https://example.com", UUID.randomUUID(), 1, 10);
        InMemoryLinkRepository.getInstance().save(link);

        Thread.sleep(1100);

        ExpiredLinkCleaner.runOnce(notif);

        assertTrue(InMemoryLinkRepository.getInstance().find("BBBBBB").isEmpty());
        assertEquals(1, notif.expired);
    }

    private static final class TestNotifications implements NotificationService {
        int expired;

        @Override public void notifyLimitExceeded(Link link, UUID userUuid) {}
        @Override public void notifyLinkExpired(Link link, UUID userUuid) { expired++; }
        @Override public void notifyApproachingLimit(Link link, UUID userUuid, int remainingClicks) {}
        @Override public void notifyLinkCreated(Link link, UUID userUuid) {}
        @Override public void notifyLinkDeleted(String shortCode, UUID userUuid, String reason) {}
    }
}
