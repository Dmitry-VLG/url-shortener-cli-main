package com.example.urlshortener.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Читает конфигурацию из application.properties.
 * Важно: системные свойства (System.getProperty) имеют приоритет над файлом,
 * что удобно для тестов и CI.
 */
public final class ApplicationConfig {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream is = ApplicationConfig.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (is != null) {
                PROPS.load(is);
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot load application.properties", e);
        }
    }

    private static String get(String key, String defaultValue) {
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) return sys;
        return PROPS.getProperty(key, defaultValue);
    }

    private static long getLong(String key, long defaultValue) {
        return Long.parseLong(get(key, Long.toString(defaultValue)));
    }

    /** TTL задаётся системой (через конфиг), не пользователем. */
    public static long defaultTtlSeconds() {
        return getLong("link.defaultTtlSeconds", 86400L);
    }

    public static long defaultMaxClicks() {
        return getLong("link.defaultMaxClicks", 100L);
    }

    public static long cleanupIntervalSeconds() {
        return getLong("cleanup.intervalSeconds", 60L);
    }

    private ApplicationConfig() {}
}
