package com.example.urlshortener.infra.repository;

import com.example.urlshortener.core.model.Link;
import com.example.urlshortener.infra.JsonFileStorage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Простое in-memory хранилище с персистентностью в JSON.
 */
public final class InMemoryLinkRepository {

    // не final: чтобы тесты могли сбрасывать singleton
    private static volatile InMemoryLinkRepository INSTANCE = new InMemoryLinkRepository();

    private final Map<String, Link> storage;

    private InMemoryLinkRepository() {
        Map<String, Link> loaded = JsonFileStorage.load();
        this.storage = new ConcurrentHashMap<>(loaded);
    }

    public static InMemoryLinkRepository getInstance() {
        return INSTANCE;
    }

    /**
     * Сброс singleton для тестов (чтобы подхватить другой dataDir или перечитать JSON).
     */
    public static void resetForTests() {
        INSTANCE = new InMemoryLinkRepository();
    }

    public void save(Link link) {
        storage.put(link.getCode(), link);
        JsonFileStorage.save(storage);
    }

    public Optional<Link> find(String code) {
        return Optional.ofNullable(storage.get(code));
    }

    public boolean exists(String code) {
        return storage.containsKey(code);
    }

    public void delete(String code) {
        storage.remove(code);
        JsonFileStorage.save(storage);
    }

    public List<Link> findByOwner(UUID owner) {
        return storage.values().stream()
                .filter(l -> l.getOwnerUuid().equals(owner))
                .collect(Collectors.toList());
    }

    public Collection<Link> findAll() {
        return Collections.unmodifiableCollection(storage.values());
    }

    /**
     * Удаляет протухшие ссылки и сохраняет состояние.
     * @return список удалённых ссылок (для логов/уведомлений)
     */
    public List<Link> removeExpired() {
        List<Link> removed = new ArrayList<>();
        storage.entrySet().removeIf(e -> {
            Link link = e.getValue();
            if (link.isExpired()) {
                removed.add(link);
                return true;
            }
            return false;
        });
        if (!removed.isEmpty()) {
            JsonFileStorage.save(storage);
        }
        return removed;
    }

    /**
     * Принудительно сохранить текущее состояние (например, после изменения clicks/maxClicks).
     */
    public void persist() {
        JsonFileStorage.save(storage);
    }
}
