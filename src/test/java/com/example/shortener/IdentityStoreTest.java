package com.example.shortener;

import com.example.urlshortener.infra.identity.UserIdentityStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IdentityStoreTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        System.setProperty("urlshortener.dataDir", tempDir.toString());
    }

    @Test
    void loadEmptyWhenNoFile() {
        assertTrue(UserIdentityStore.load().isEmpty());
    }

    @Test
    void saveAndLoadUuid() {
        UUID u = UUID.randomUUID();
        UserIdentityStore.save(u);

        assertEquals(u, UserIdentityStore.load().orElseThrow());
    }

    @Test
    void overwriteUuid() {
        UUID u1 = UUID.randomUUID();
        UUID u2 = UUID.randomUUID();

        UserIdentityStore.save(u1);
        UserIdentityStore.save(u2);

        assertEquals(u2, UserIdentityStore.load().orElseThrow());
    }
}
