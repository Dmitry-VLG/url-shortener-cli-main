package com.example.urlshortener.infra;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Общие пути хранения данных приложения.
 *
 * По умолчанию: ~/.urlshortener
 * Для тестов/CI можно переопределить системным свойством:
 *   -Durlshortener.dataDir=/tmp/some-dir
 */
public final class AppPaths {

    public static Path dataDir() {
        String configured = System.getProperty("urlshortener.dataDir");
        if (configured != null && !configured.isBlank()) {
            return Paths.get(configured);
        }
        return Paths.get(System.getProperty("user.home"), ".urlshortener");
    }

    private AppPaths() {}
}
