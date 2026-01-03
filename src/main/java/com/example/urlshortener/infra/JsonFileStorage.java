package com.example.urlshortener.infra;

import com.example.urlshortener.core.model.Link;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Простое JSON-хранилище для ссылок.
 * Файл: {dataDir}/links.json, где dataDir по умолчанию ~/.urlshortener
 */
public final class JsonFileStorage {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .setPrettyPrinting()
            .create();

    private static Path storageFile() {
        return AppPaths.dataDir().resolve("links.json");
    }

    public static Map<String, Link> load() {
        Path file = storageFile();
        if (!Files.exists(file)) {
            return new ConcurrentHashMap<>();
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            Type type = new TypeToken<ConcurrentHashMap<String, Link>>() {}.getType();
            Map<String, Link> data = GSON.fromJson(reader, type);
            return data != null ? data : new ConcurrentHashMap<>();
        } catch (Exception e) {
            System.err.println("Ошибка загрузки данных: " + e.getMessage());
            return new ConcurrentHashMap<>();
        }
    }

    public static void save(Map<String, Link> links) {
        Path file = storageFile();
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(links, writer);
            }
        } catch (Exception e) {
            System.err.println("Ошибка сохранения данных: " + e.getMessage());
        }
    }

    private JsonFileStorage() {}
}
