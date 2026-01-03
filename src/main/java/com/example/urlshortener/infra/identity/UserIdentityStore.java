package com.example.urlshortener.infra.identity;

import com.example.urlshortener.infra.AppPaths;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

/**
 * Хранит UUID пользователя локально между запусками приложения.
 * Файл: {dataDir}/user.properties, ключ: user.uuid
 */
public final class UserIdentityStore {

    private static Path file() {
        return AppPaths.dataDir().resolve("user.properties");
    }

    public static Optional<UUID> load() {
        Path f = file();
        if (!Files.exists(f)) return Optional.empty();

        Properties p = new Properties();
        try (InputStream is = Files.newInputStream(f)) {
            p.load(is);
            String raw = p.getProperty("user.uuid");
            if (raw == null || raw.isBlank()) return Optional.empty();
            return Optional.of(UUID.fromString(raw.trim()));
        } catch (Exception e) {
            System.err.println("Не удалось прочитать UUID из " + f + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    public static void save(UUID uuid) {
        Path f = file();
        Properties p = new Properties();
        p.setProperty("user.uuid", uuid.toString());
        try {
            Files.createDirectories(f.getParent());
            try (OutputStream os = Files.newOutputStream(f)) {
                p.store(os, "url-shortener-cli user identity");
            }
        } catch (Exception e) {
            System.err.println("Не удалось сохранить UUID в " + f + ": " + e.getMessage());
        }
    }

    private UserIdentityStore() {}
}
